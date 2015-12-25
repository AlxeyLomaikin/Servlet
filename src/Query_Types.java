/**
 * Created by AlexL on 13.12.2015.
 */
public class Query_Types {
    public static final int CREATE_DATABASE = 1;
    public static final int DROP_DATABASE = 2;
    public static final int USE_DATABASE = 3;
    public static final int SELECT_DATA = 4;
    public static final int UPDATE_QUERY = 5;

    public static int parseQuery(String query){
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
}
