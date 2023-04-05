package Task1;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MyServer {
    private static final Logger logger = LogManager.getLogger(MyServer.class.getName());

    private final int PORT = 8189;
    private final String SERVER_ADDR = "localhost";

    private List<ClientHandler> clients;
    private AuthService authService;


    public AuthService getAuthService() {
        return authService;
    }

    public MyServer() {

        try (ServerSocket server = new ServerSocket(PORT)) {
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
                //System.out.println("Сервер ожидает подключения");
                logger.info("Сервер ожидает подключение");
                Socket socket = server.accept();
                //System.out.println("Клиент подключился");
                logger.info("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException | SQLException e) {
            //System.out.println("Ошибка в работе сервера");
            logger.error(e);
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    public synchronized boolean personalMsg (String msg, String nick){
        for(ClientHandler o:clients){
            if (nick.equals(o.getName())){
                System.out.println(o.getName());
                o.sendMsg(msg);
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastClientsList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler o : clients) {
            sb.append(o.getName() + " ");
        }
        broadcastMsg(sb.toString());
    }

    public synchronized void unsubscribe(ClientHandler o) {
        clients.remove(o);
        broadcastClientsList();
    }

    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
        broadcastClientsList();
    }
}