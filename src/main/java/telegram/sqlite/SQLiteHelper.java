package telegram.sqlite;

import org.sqlite.SQLiteException;
import telegram.dbx.DbxHelper;
import telegram.objects.Task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SQLiteHelper {
    public static Task getTask(String userId, String id){
        Task task = null;

        try {
            Statement statement = getConnection(userId);
            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM assoc WHERE id = '%s'", id));
            if (rs.next()){
                String taskName = rs.getString("taskName");
                String groupName = rs.getString("groupName");
                String expDate = rs.getString("expDate");
                task = new Task(id, taskName, groupName, expDate);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return task;
    }

    public static Task checkQrCode(String userId, String code){
        Task task = null;
        String[] parts = code.split("\\....");
        if (parts.length > 1){
            String id = parts[0];
            String token = parts[1];

            if (DbxHelper.downloadDb(userId, token)){
                return getTask(userId, id);
            }
        }
        return task;
    }

    private static Statement getConnection(String userId) throws Exception{
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection(String.format("jdbc:sqlite:sqlite/sqlite_%s.db", userId));
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        return statement;
    }
}
