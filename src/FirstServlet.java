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

    private ArrayList<String>Errors = new ArrayList<>();
    private ArrayList<String> lasSelectionHTML = new ArrayList<>();
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
            useDB("use "+ use + ";");
        if (sql!=null)
            executeSql(sql);

        w.println("<pre>databases:</pre>");

        //links to change parameter "use" of servlet - database will be changed
        for (int i=0;i<dbNames.size();i++){
            if (dbNames.get(i).equals(curDatabase))
                w.print("<a class=\"active\">" + dbNames.get(i) + "</a>" + tab);
            else
                w.print("<a href=\"/first?use=" + dbNames.get(i) + "\">" + dbNames.get(i) + "</a>" + tab);
        }

        w.println("<p>please, type your request");

        w.println("<br><FORM action=/first method=service>");

        if (Errors.size()==0)
            w.println("<TEXTAREA name=sql cols=90 rows=8>");
        else
            w.println("<TEXTAREA style=\"border: 2px solid red\" name=sql cols=90 rows=8>");
        if(sql != null) {
            w.print(sql);
        }
        w.println("</TEXTAREA>");

        w.println("<INPUT TYPE=submit value=execute>");
        w.println("</FORM>");

        w.println("<BR>");

        if (lasSelectionHTML.size()!=0)
            for (String tag: lasSelectionHTML)
                w.append(tag);

        for (int i =0; i<Errors.size(); i++){
            w.println("<p>" + Errors.get(i));
        }
        Errors.clear();

        w.println("</BODY>");
        w.println("</HTML>");
        w.flush();
        w.close();
    }


    private void executeSql(String sql) throws ServletException, IOException{
        int query_type = Query_Types.parseQuery(sql);
        switch (query_type){
            case Query_Types.CREATE_DATABASE:
                createDB(sql.trim());
                break;
            case Query_Types.DROP_DATABASE:
                dropDB(sql.trim());
                break;
            case Query_Types.USE_DATABASE:
                useDB(sql.trim());
                break;
            case Query_Types.UPDATE_QUERY:
                if (!curDatabase.equals(""))
                    executeUpdQuery(sql.trim());
                else
                    Errors.add("\nDatabase not selected");
                break;
            case Query_Types.SELECT_DATA:
                if (!curDatabase.equals(""))
                    selectData(sql.trim());
                else
                    Errors.add("\nDatabase not selected");
                break;
            default:
                Errors.add("\nIncorrect request!");
                break;
        }
    }

    private void useDB(String sql){
        String dbName = sql.split(" ")[1];
        if (dbName.charAt(dbName.length()-1)==';')
            dbName = dbName.replace(";","");
        if ( (!dbName.equals(curDatabase)) && (dbNames.contains(dbName)) )
            try{
                if (con!=null)
                    con.close();
                con = DriverManager.getConnection("jdbc:mysql://localhost/" + dbName, "root", "smosh4071");
                this.curDatabase = dbName;
            }catch (SQLException ex){
                Errors.add(ex.getMessage());
            }
        else if (!dbNames.contains(dbName)) {
            try{
                con = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "smosh4071");
            }catch (SQLException ex){
                Errors.add(ex.getMessage());
            }
            this.curDatabase="";
            Errors.add("Database with name `" + dbName + "` is not exist!");
        }
    }

    private void selectData (String sql){
        lasSelectionHTML.clear();
        if (con!=null) {
            Statement st = null;
            ResultSet rs = null;
            try {
                st = con.createStatement();
                rs = st.executeQuery(sql);
                lasSelectionHTML.add("<table>");
                lasSelectionHTML.add("<caption><b><p>Result of selection :</b></caption>");

                lasSelectionHTML.add("<tr class=\"header\">");
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    lasSelectionHTML.add("<th>");
                    lasSelectionHTML.add(rs.getMetaData().getColumnName(i));
                    lasSelectionHTML.add("</th>");
                }
                lasSelectionHTML.add("</tr>");

                while (rs.next()) {
                    lasSelectionHTML.add("<tr>");
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        String td = "<td>" + rs.getString(i) + "</td>";
                        lasSelectionHTML.add(td);
                    }
                    lasSelectionHTML.add("</tr>");
                }
                lasSelectionHTML.add("</table>");
            } catch (SQLException ex) {
                Errors.add(ex.getMessage());
            }finally{
                try {
                    if (st != null) st.close();
                    if (rs != null) rs.close();
                }catch(SQLException ex){
                    ex.getMessage();
                }
            }
        }
        else Errors.add("Connection error");
    }

    private void executeUpdQuery(String sql) {
        if (con != null) {
            Statement st = null;
            try {
                st = con.createStatement();
                st.executeUpdate(sql);
            } catch (SQLException ex) {
                Errors.add(ex.getMessage());
            }finally{
                try{
                    if (st!=null) st.close();
                }catch (SQLException ex){
                    ex.getMessage();
                }
            }
        } else Errors.add("Connection error");
    }

    private void dropDB(String sql){
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
            } catch (SQLException ex) {
                Errors.add(ex.getMessage());
            } finally {
                try {
                    if (statement != null) statement.close();
                } catch (SQLException ex) {
                    ex.getMessage();
                }
            }
        }
        else Errors.add("connection error");
    }

    private void createDB(String sql) {
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
            } catch (SQLException ex) {
                Errors.add(ex.getMessage());
            } finally {
                try {
                    if (statement != null) statement.close();
                } catch (SQLException ex) {
                    ex.getMessage();
                }
            }
        }
        else Errors.add("connection error");
    }

}
