/**
 * Created by AlexL on 10.12.2015.
 */
import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;

public class FirstServlet extends GenericServlet {
    private Connection con;

    //list of all Databases
    private ArrayList<String> dbNames = new ArrayList<>();
    private String curDatabase = "";

    private ArrayList<String>updDatabasesResult = new ArrayList<>();
    private final String tab = "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp";

    @Override
    public void init() {
        try{
            Class.forName("com.mysql.jdbc.Driver");

            //get DBases names
            con = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "smosh4071");
            DatabaseMetaData dbMeta = con.getMetaData();
            ResultSet resSc = dbMeta.getCatalogs();
            while (resSc.next()) {
                dbNames.add(resSc.getString("TABLE_CAT"));
            }
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        try{
            if (con!=null)con.close();
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public int parseQuery(String query){
        int query_type = -1;
        if (query!=null){
            String sql_query = query.trim().toLowerCase();
            String[] sql = sql_query.split(" ");
            if (sql.length>0){
                switch (sql[0]){
                    case "use":
                        if (sql.length==2)
                            query_type=Query_Types.USE_DATABASE;
                        break;
                    case "create":
                        if (sql.length==3)
                            if (sql[1].equals("database"))
                                query_type=Query_Types.CREATE_DATABASE;
                        break;
                    case "drop":
                        if (sql.length==3)
                            if (sql[1].equals("database"))
                                query_type=Query_Types.DROP_DATABASE;
                        break;
                    case "select":
                    case "select*":
                        query_type = Query_Types.SELECT_DATA;
                        break;
                    case "":
                        query_type = -1;
                        break;
                    default:
                        query_type = Query_Types.UPDATE_QUERY;
                        break;
                }
            }
        }
        return query_type;
    }

    @Override
    public void service(ServletRequest servletRequest,
                        ServletResponse servletResponse) throws ServletException, IOException {
        sendSqlForm(servletRequest, servletResponse);
    }


    private void sendSqlForm(ServletRequest servletRequest,
                             ServletResponse servletResponse) throws ServletException, IOException{
        PrintWriter w = servletResponse.getWriter();
        w.println("<HTML>");
        w.println("<HEAD>");
        w.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"first.css\">");
        w.println("<TITLE>SQL Servlet</TITLE>");
        w.println("</HEAD>");
        w.println("<BODY>");

        String sql = servletRequest.getParameter("sql");
        String use = servletRequest.getParameter("use");
        if (use!=null)
            useDB("use "+ use + ";", w);
        if (sql!=null)
            switch (parseQuery(sql)){
                case Query_Types.CREATE_DATABASE:
                    createDB(sql.trim(), w);
                    break;
                case Query_Types.DROP_DATABASE:
                    dropDB(sql.trim(), w);
                    break;
                case Query_Types.USE_DATABASE:
                    useDB(sql.trim(), w);
                    break;
                default:
                    break;
            }

        w.println("<pre>databases:</pre>");

        //links to change parameter "use" of servlet - database will be changed
        for (int i=0;i<dbNames.size();i++){
            if (dbNames.get(i).equals(curDatabase))
                w.print("<a class=\"active\" href=\"/first?use="
                        + dbNames.get(i) + "\">" + dbNames.get(i) + "</a>" + tab);
            else
                w.print("<a href=\"/first?use=" + dbNames.get(i) + "\">" + dbNames.get(i) + "</a>" + tab);
        }

        w.println("<p>please, type your request");
        w.println("<br><FORM action=/first method=service>");
        w.println("<TEXTAREA name=sql cols=90 rows=8>");

        if(sql != null) {
            w.print(sql);
        }

        w.println("</TEXTAREA>");

        w.println("<INPUT TYPE=submit value=execute>");
        w.println("</FORM>");

        w.println("<BR>");

        for (int i =0; i<updDatabasesResult.size(); i++){
            w.println("<p>" + updDatabasesResult.get(i));
        }

        if (sql != null) {
            w.println("<BR>");
            if (!curDatabase.equals(""))
                executeSql(sql.trim(), servletResponse);
            else
                w.println("Database not selected!");
        }

        w.println("</BODY>");
        w.println("</HTML>");
        w.close();
        updDatabasesResult.clear();
    }


    private void executeSql(String sql, ServletResponse response) throws ServletException, IOException{
        PrintWriter w = response.getWriter();
        int query_type = parseQuery(sql);
        switch (query_type){
            case Query_Types.CREATE_DATABASE:
                break;
            case Query_Types.DROP_DATABASE:
                break;
            case Query_Types.USE_DATABASE:
                break;
            case Query_Types.SELECT_DATA:
                selectData(sql, w);
                break;
            case Query_Types.UPDATE_QUERY:
                executeUpdQuery(sql,w);
                break;
            default:
                w.println("Incorrect request!");
                break;
        }
    }

    private void useDB(String sql, PrintWriter w){
        String dbName = sql.split(" ")[1];
        if (dbName.charAt(dbName.length()-1)==';')
            dbName = dbName.replace(";","");
        if ( (!dbName.equals(curDatabase)) && (dbNames.contains(dbName)) )
            try{
                if (con!=null)
                    con.close();
                con = DriverManager.getConnection("jdbc:mysql://localhost/" + dbName, "root", "smosh4071");
                this.curDatabase = dbName;
                updDatabasesResult.add("Database was changed");
            }catch (SQLException ex){
                updDatabasesResult.add(ex.getMessage());
            }
        else if (!dbNames.contains(dbName)) {
            try{
                con = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "smosh4071");
            }catch (SQLException ex){
                w.println(ex.getMessage());
            }
            this.curDatabase="";
            updDatabasesResult.add("Database with name `" + dbName + "` is not exist!");
        }
    }

    private void selectData (String sql, PrintWriter w){
        if (con!=null) {
            Statement st = null;
            ResultSet rs = null;
            try {
                st = con.createStatement();
                rs = st.executeQuery(sql);
                w.append("<table>");
                w.append("<caption><b><p>Result of selection :</b></caption>");

                w.append("<tr class=\"header\">");
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    w.append("<th>");
                    w.append(rs.getMetaData().getColumnName(i));
                    w.append("</th>");
                }
                w.append("</tr>");


                while (rs.next()) {
                    w.append("<tr>");
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        String td = "<td>" + rs.getString(i) + "</td>";
                        w.append(td);
                    }
                    w.append("</tr>");
                }
                w.append("</table>");
                w.flush();
            } catch (SQLException ex) {
                w.println(ex.getMessage());
            }finally{
                try {
                    if (st != null) st.close();
                    if (rs != null) rs.close();
                }catch(SQLException ex){
                    w.println(ex.getMessage());
                }
            }
        }
        else w.println("Connection error");
    }

    private void executeUpdQuery(String sql, PrintWriter w) {
        if (con != null) {
            Statement st = null;
            try {
                st = con.createStatement();
                st.executeUpdate(sql);
                w.append("<p>Request was executed successfully.</p>");
            } catch (SQLException ex) {
                w.println(ex.getMessage());
            }finally{
                try{
                    if (st!=null) st.close();
                }catch (SQLException ex){
                    ex.getMessage();
                }
            }
        } else w.println("Connection error");
    }

    private void dropDB(String sql, PrintWriter w){
        if (con != null) {
            Statement statement = null;
            try {
                statement = con.createStatement();
                String dbName = sql.split(" ")[2];
                if (dbName.charAt(dbName.length() - 1) == ';') {
                    statement.executeUpdate(sql);
                    dbName = dbName.replace(";", "");
                } else
                    statement.executeUpdate(sql + ";");
                dbNames.remove(dbName);
                updDatabasesResult.add("database `"+dbName+"` was successfully droped!");
            } catch (SQLException ex) {
                updDatabasesResult.add(ex.getMessage());
            } finally {
                try {
                    if (statement != null) statement.close();
                } catch (SQLException ex) {
                    updDatabasesResult.add(ex.getMessage());
                }
            }
        }
        else updDatabasesResult.add("connection error");
    }

    private void createDB(String sql, PrintWriter w) {
        if (con != null) {
            Statement statement = null;
            try {
                statement = con.createStatement();
                String dbName = sql.split(" ")[2];
                if (dbName.charAt(dbName.length() - 1) == ';') {
                    statement.executeUpdate(sql);
                    dbName = dbName.replace(";", "");
                } else
                    statement.executeUpdate(sql + ";");
                dbNames.add(dbName);
                updDatabasesResult.add("database `"+dbName+"` was successfully created");
            } catch (SQLException ex) {
                updDatabasesResult.add(ex.getMessage());
            } finally {
                try {
                    if (statement != null) statement.close();
                } catch (SQLException ex) {
                    updDatabasesResult.add(ex.getMessage());
                }
            }
        }
        else updDatabasesResult.add("connection error");
    }

}
