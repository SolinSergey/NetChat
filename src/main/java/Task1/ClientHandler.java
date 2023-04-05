package Task1;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    public static ExecutorService clientPool = Executors.newCachedThreadPool();
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;
    private String nick;
    private String login;

    private static final Logger logger = LogManager.getLogger(ClientHandler.class.getName());

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            clientPool.execute(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException | SQLException e) {
                    logger.error(e);
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
        } catch (IOException e) {
            logger.error("Проблемы при создании обработчика клиента");
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    public void authentication() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/auth")) {
                logger.info("Клиент прислал комманду >> "+str);
                String[] parts = str.split("\\s");
                login = parts[1];
                nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {
                        sendMsg("/authok " + nick + " " + login);
                        name = nick;
                        myServer.broadcastMsg(name + " зашел в чат");
                        logger.info(name + " зашел в чат");
                        myServer.subscribe(this);
                        return;
                    } else {
                        sendMsg("Учетная запись уже используется");
                    }
                } else {
                    sendMsg("Неверные логин/пароль");
                }
            }

        }
    }

    public void readMessages() throws IOException, SQLException {
        while (true) {
            String strFromClient = in.readUTF();
            strFromClient = strFromClient.trim();
            logger.info("Клиент прислал >> "+strFromClient);
            String msg;
            //System.out.println("от " + name + ": " + strFromClient);
            if (strFromClient.equals("/end")) {
                return;
            }
            if (strFromClient.startsWith("/w")) {
                String[] parts = strFromClient.split("\\s");
                msg=strFromClient.substring(parts[0].length()+parts[1].length()+2);
                if (myServer.personalMsg("Личное сообщение от " + getName() + ": "+msg,parts[1])){
                    out.writeUTF("Личное сообщение для "+parts[1]+":"+msg);
                }
                else{
                    out.writeUTF("Участника с ником: "+parts[1]+" нет в чате");
                }
            }else if (strFromClient.startsWith("/cn")) {
                String[] parts = strFromClient.split("\\s");
                Connection connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
                Statement stmt = connection.createStatement();
                connection.setAutoCommit(false);
                try{
                    PreparedStatement ps;
                    ps=connection.prepareStatement( "UPDATE users SET nick=? WHERE nick=?");
                    ps.setString(1,parts[1]);
                    ps.setString(2,nick);
                    ps.executeUpdate();
                    ps.close();
                    connection.commit();
                    name = parts[1];
                    nick = parts[1];
                    myServer.broadcastClientsList();
                    myServer.broadcastMsg("Участник чата " + name + " заменил ник на новый: " + parts[1]);
                }
                catch (SQLException e){
                    logger.error(e);
                    //e.printStackTrace();
                    connection.rollback();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }else{
                myServer.broadcastMsg(name + ": " + strFromClient);
            }


        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + " вышел из чата");
        try {
            in.close();
        } catch (IOException e) {
            logger.error(e);
        }
        try {
            out.close();
        } catch (IOException e) {
            logger.error(e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            logger.error(e);
        }
    }
}