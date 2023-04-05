package Task1;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class NetworkChat extends JFrame {
    private JTextField message;
    private JTextArea chatArea;
    private JTextArea clientsList;
    private JButton sendButton;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick = "";
    private String login = "";
    private boolean timeOutStatus=false;
    ArrayList <String> chatHistory = new ArrayList<>();
    private boolean saveStatus=false;

    public NetworkChat() throws IOException {
        setTitle("Сетевой чат");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(300,300,400,400);
        JLabel header = new JLabel("Сетевой чат");
        header.setFont(new Font("Arial",Font.BOLD,16));
        add(header,BorderLayout.PAGE_START);
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(chatArea,BorderLayout.CENTER);
        clientsList = new JTextArea();
        clientsList.setEditable(false);
        add(clientsList,BorderLayout.LINE_END);
        JPanel downPanel = new JPanel();
        downPanel.setLayout(new BoxLayout(downPanel,BoxLayout.LINE_AXIS));
        message = new JTextField();
        message.addActionListener(e -> {
            sendMessage();
        });

        message.setText("/auth login1 pass1");
        downPanel.add(message);
        sendButton = new JButton("Отправить");
        sendButton.addActionListener(e -> {
            sendMessage();
        });
        downPanel.add(sendButton);
        add(downPanel,BorderLayout.PAGE_END);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!saveStatus){
                    try {
                        saveHistoryChat();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                System.out.println("Окно закрылось");
                System.out.println(chatHistory);
                super.windowClosing(e);
            }
        });
        setVisible(true);
        openConnection();
        startTread();

    }


    private void saveHistoryChat() throws IOException {
        String fileName = "history_"+login+".txt";
        //File file = new File(fileName);
        //if (file.exists()){
        //    file.delete();
        //}
        FileWriter fileWriter = new FileWriter(fileName,false);
        try {
            for (int i=0; i<chatHistory.size();i++) {
                fileWriter.write(chatHistory.get(i)+"\n");
            }
            fileWriter.close();
            chatHistory.clear();
            saveStatus=true;
        }

        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void readHistoryChat() throws IOException {
        String fileName = "history_"+login+".txt";
        File test = new File(fileName);
        if (!test.exists()){
            test.createNewFile();
        }
        FileReader file = new FileReader(fileName);
        try (BufferedReader reader = new BufferedReader(file)){
           String str;
           chatArea.setText("");
           while ((str = reader.readLine())!=null){
               chatHistory.add(str);
           }
           reader.close();
           file.close();
           int i=0;
           if (chatHistory.size()>20){
               i=chatHistory.size()-20;
           }
           for (int j=i;j<chatHistory.size();j++){
               chatArea.append(chatHistory.get(j)+"\n");
           }
        } catch (FileNotFoundException ex){
            System.out.println("История не существует");
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void closeConnection() throws IOException {
        if (!timeOutStatus) {
            chatArea.append("# Вы вышли из чата\n");
            chatHistory.add("# Вы вышли из чата");
            if (!saveStatus){
                saveHistoryChat();
            }
            message.setText("/auth login1 pass1");
            clientsList.setText("");
        }
        else{
            chatArea.append("Истекло время отведенное на авторизацию.\n");
            chatArea.append("Соединение разорвано\n");
            chatArea.append("Перезапустите приложение");
            message.setText("");
            message.setEnabled(false);
            sendButton.setEnabled(false);
        }
        try {
            in.close();
            out.close();
            socket.close();
            if (!timeOutStatus){
                openConnection();
                startTread();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void openConnection() throws IOException {
        socket = new Socket("localhost",8189);
        in = new DataInputStream((socket.getInputStream()));
        out = new DataOutputStream((socket.getOutputStream()));
        socket.setSoTimeout(120000);
        timeOutStatus = false;
    }

    public void startTread(){
        new Thread(()->{
            try{
                while (true && !timeOutStatus){
                    String strFromServer = in.readUTF();
                    if (strFromServer.startsWith("/authok")){
                        nick = strFromServer.split(" ")[1];
                        login = strFromServer.split(" ")[2];
                        //System.out.println(strFromServer);
                        System.out.println(login);
                        readHistoryChat();
                        String s="# Вы авторизованы как: " +nick;
                        saveStatus=false;
                        chatArea.append(s+"\n");
                        chatHistory.add(s);
                        socket.setSoTimeout(0);
                        break;
                    }
                    chatArea.append(strFromServer + "\n");
                    chatHistory.add(strFromServer);
                }
                while (!timeOutStatus){
                    String strFromServer = in.readUTF();
                    if (strFromServer.equalsIgnoreCase("/end")){
                        break;
                    }
                    if (strFromServer.startsWith("/clients")){
                        String clients = strFromServer.substring(9);
                        clientsList.setText(clients.replace(' ','\n'));
                    }
                    else {
                        chatArea.append(strFromServer + "\n");
                        chatHistory.add(strFromServer);
                    }
                }
            } catch (SocketTimeoutException e){
                System.out.println("Time out connection");
                timeOutStatus = true;
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(){
        String trimmedMessage = this.message.getText().trim();
        if (!trimmedMessage.isEmpty()){
            message.setText("");
            try{
                if (socket == null || socket.isClosed() && !timeOutStatus){
                    openConnection();
                    startTread();
                }
                out.writeUTF(trimmedMessage);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new NetworkChat();
    }
}

