package whatsapp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class WhatsAppServer {
    
    private static HashMap<String, PrintWriter> map;
    private static final int PORT = 5000;
    private static final String URL = "jdbc:mysql://localhost:3306/whatsapp";
    private static final String USER = "root";                                
    private static final String PASSWORD = "";  
    
    private static void startServer() {
        try {
            map = new HashMap<>();
            ServerSocket ss = new ServerSocket(PORT);
            while (true) {
                Socket sock = ss.accept();
                PrintWriter writer = new PrintWriter(sock.getOutputStream());
                InputStreamReader is = new InputStreamReader(sock.getInputStream());
                BufferedReader reader = new BufferedReader(is);
                
                String email = reader.readLine();
                System.out.println(" + User: " + email);      
                map.put(email, writer);
                
                Thread t = new Thread(new Listener(sock));
                t.start();
            }
	} catch (IOException ex) {System.out.println(ex.getMessage());}
    }
	
    private static class Listener implements Runnable { 
	BufferedReader reader;
	Listener(Socket sock) {
            try {
		InputStreamReader is = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(is);
            } catch (IOException ex) {System.out.println(ex.getMessage());}
	}

	@Override
	public void run() {
            String data, from, to, message;
            try {
		while ((data = reader.readLine()) != null) {
                    to = data.substring(0, data.indexOf(":"));
                    from = data.substring(data.indexOf(":") + 2, data.indexOf("%"));
                    message = data.substring(data.indexOf("%") + 2, data.length());
                    System.out.println("from: " + from + " to: " + to + " message: " + message);
                    tellToInterlocutor(from, to, message);
		}
            } catch (IOException ex) {System.out.println(ex.getMessage());}
	}
    }
    
    private static void tellToInterlocutor(String from, String to, String message) {
        try {
            String nameOfMe;
            PrintWriter writer;
            if (to.charAt(0) != '@') {
                if (map.containsKey(to)) {
                    writer = map.get(to);
                    nameOfMe = emailToName(from);
                    writer.println(nameOfMe + ": " + message);
                    writer.flush();
                }
            } else {
                ArrayList<String> participants = getParticipants(to, from);
                System.out.println(participants);
                for (int i = 0; i < participants.size(); i++) {
                    System.out.println(participants.get(i));
                    if (map.containsKey(participants.get(i))) {
                        writer = map.get(participants.get(i));
                        nameOfMe = emailToName(from);
                        writer.println(to + "% " + nameOfMe + ": " + message);
                        writer.flush();
                    }
                }
            }
        } catch (Exception ex) {System.out.println("User is offline " + ex.getMessage());}
    }
    
    private static ArrayList<String> getParticipants (String name, String from) {
        try {
            ArrayList<String> list = new ArrayList<>();
            Class.forName("com.mysql.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM `groups` WHERE name LIKE '"+name+"'");
                while (resultSet.next()) {
                    list.add(resultSet.getString("participants"));
                }
                list.remove(from);
            }
            return list;
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println(ex.getMessage());
            return null;
        } 
    }
    
    private static String emailToName (String email) {
       try {
            Class.forName("com.mysql.jdbc.Driver");
            String name = null;
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM `users` WHERE email LIKE '"+email+"'");
                while (resultSet.next()) {
                    name = resultSet.getString("name");
                }
            }
            return name;
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println(ex.getMessage());
            return null;
        }  
    }
    
    public static void main(String[] args) {
	startServer();
    }

}