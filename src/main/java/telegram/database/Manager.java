package telegram.database;

import telegram.objects.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Manager {
    public static User getUser(String id){
        Driver mysqlConnect = new Driver();
        User user = null;

        String sql = String.format("SELECT * FROM users WHERE id = '%s'", id);
        try {
            Statement statement = mysqlConnect.connect().createStatement();
            ResultSet rs = statement.executeQuery(sql);

            if (rs.next())
                user = new User(rs.getString("id"), rs.getString("alias"), rs.getInt("state"));
            else
                user = createNewUser(id);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysqlConnect.disconnect();
        }

        return user;
    }

    public static void changeUserAlias(String id, String alias){
        Driver mysqlConnect = new Driver();

        String sql = String.format("UPDATE users SET alias = '%s' WHERE id = '%s'", alias, id);
        try {
            Statement statement = mysqlConnect.connect().createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysqlConnect.disconnect();
        }
    }

    public static void changeUserState(String id, int state){
        Driver mysqlConnect = new Driver();

        String sql = String.format("UPDATE users SET state = '%d' WHERE id = '%s'", state, id);
        try {
            Statement statement = mysqlConnect.connect().createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysqlConnect.disconnect();
        }
    }

    private static User createNewUser(String id){
        Driver mysqlConnect = new Driver();
        User user = null;

        String sql = String.format("INSERT INTO users (id, alias, state) VALUES ('%s', '%s', '%d')", id, "", 0);
        try {
            Statement statement = mysqlConnect.connect().createStatement();
            statement.execute(sql);
            user = new User(id, "", 0);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysqlConnect.disconnect();
        }

        return user;
    }
}
