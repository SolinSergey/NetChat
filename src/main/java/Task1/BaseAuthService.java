package Task1;
import Task1.AuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {
    private static final Logger logger = LogManager.getLogger(ClientHandler.class.getName());

    private class Entry {
        private String login;
        private String pass;
        private String nick;



        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }

    private List<Entry> entries;

    @Override
    public void start() {
        //System.out.println("Сервис аутентификации запущен");
        logger.info("Сервис аутентификации запущен");
    }

    @Override
    public void stop() {
        //System.out.println("Сервис аутентификации остановлен");
        logger.info("Сервис аутентификации остановлен");
    }


    public BaseAuthService() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
        Statement stmt = connection.createStatement();
        entries = new ArrayList<>();
        ResultSet rs = stmt.executeQuery("SELECT login,password,nick FROM users;");
        while (rs.next()){
            entries.add(new Entry(rs.getString("login"), rs.getString("password"), rs.getString("nick")));
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error(e.getStackTrace().toString());
        }

        }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry o : entries) {
            if (o.login.equals(login) && o.pass.equals(pass)) return o.nick;
        }
        return null;
    }
}
