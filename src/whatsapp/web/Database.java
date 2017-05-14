package whatsapp.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javafx.scene.image.Image;

public class Database {
    
    private final String URL = "jdbc:mysql://localhost:3306/whatsapp";
    private final String USER = "root";                                
    private final String PASSWORD = "";                                
    
    private Connection connection;
    private Statement statement; 
    private ResultSet resultSet;
    private final LoginController notify;
    private final HomePageController homePageController;
    
    public Database(LoginController notify, HomePageController homePageController) {
        this.notify = notify;
        this.homePageController = homePageController;
        connectDB();
    }
    
    private void connectDB() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            statement = connection.createStatement();          
        } catch (ClassNotFoundException | SQLException ex) {System.out.println(ex.getMessage());}
    }
    
    protected boolean signUp(String name, String email, String password) {
        try {
            String Email = null;
            File file = new File("src/whatsapp/web/images/profile.png");
            InputStream image = new FileInputStream(file);
            String SQL = "INSERT INTO `whatsapp`.`users` (`id`, `name`, `email`, `password`, `image`) "
            + "VALUES (NULL, ?, ?, ?, ?)";
            resultSet = statement.executeQuery("SELECT * FROM `users`");
            PreparedStatement prepStatement = connection.prepareStatement(SQL);
            while (resultSet.next()) {
                if (resultSet.getString("email").equals(email)) {
                    Email = resultSet.getString("email");
                    break;
                }
            }
            if (!email.equals(Email)) {
                prepStatement.setString(1, name);
                prepStatement.setString(2, email);
                prepStatement.setString(3, password);
                prepStatement.setBinaryStream(4, image, (int)(file.length()));
                prepStatement.executeUpdate();
                return true;
            } else {
                notify.notify("This email is already registered");
                return false;
            }
        } catch (FileNotFoundException | SQLException ex) {
            notify.notify(ex.getMessage());
            return false;
        }
    }
    
    protected boolean logIn(String email, String password) {
        try {
            String Email = null;
            String Password = null;
            resultSet = statement.executeQuery("SELECT * FROM `users`");
            while (resultSet.next()) {
                Email = resultSet.getString("email");
                Password = resultSet.getString("password");
                if (Email.equals(email) && Password.equals(password))
                    break;
            }
            if (email.equals(Email) && password.equals(Password)) {
                return true;
            } else {
                notify.notify("Wrong login or password");
                return false;
            }
        } catch (SQLException ex) {
            notify.notify(ex.getMessage());
            return false;
        }
    }
    
    protected String emailToName(String email) {     
        try {
            String name = null;
            resultSet = statement.executeQuery("SELECT * FROM `users` WHERE email LIKE '"+email+"'");
            while (resultSet.next()) {
                name = resultSet.getString("name");
            }
            return name;
        } catch (SQLException ex) {
            homePageController.notify(ex.getMessage());
            return null;
        } 
    }
    
    protected String nameToEmail(String name) {
        String email = null;
        try {
            resultSet = statement.executeQuery("SELECT * FROM `users` WHERE name LIKE '"+name+"'");
            while (resultSet.next()) {
                email = resultSet.getString("email");
            }
            return email;
        } catch (SQLException ex) {
            homePageController.notify(ex.getMessage());
            return null;
        }
    }
    
    protected ArrayList<String> loadAllUsers(String email) {
        ArrayList<String> all = new ArrayList<>();
        try {
            resultSet = statement.executeQuery("SELECT * FROM `users`");
            while (resultSet.next()) {
                all.add(resultSet.getString("name"));
            }
            all.remove(emailToName(email));
            return all;
        } catch (SQLException ex) {
            homePageController.notify(ex.getMessage());
            return null;
        }
    }
    
    protected ArrayList<String> loadFriends(String email) {
        ArrayList<String> friends = new ArrayList<>();
        ArrayList<String> friendsName = new ArrayList<>();
        try {
            resultSet = statement.executeQuery("SELECT * FROM `friends` WHERE email LIKE '"+email+"'");
            while (resultSet.next()) {
                friends.add(resultSet.getString("friends"));
            }
            for (int i = 0; i < friends.size(); i++) {
                if (friends.get(i).charAt(0) != '@')
                    friendsName.add(emailToName(friends.get(i)));
                else
                    friendsName.add(friends.get(i));
            }
            return friendsName;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    protected void loadChatHistory(String email, String emailOfFriend, ConversationView conV) {
        try {
            String SQL = "SELECT * FROM `chat` WHERE `from` LIKE '"+email+"' "
                    + "AND `to` LIKE '"+emailOfFriend+"' "
                    + "OR `to` LIKE '"+email+"' AND `from` LIKE '"+emailOfFriend+"'";
            if (emailOfFriend.charAt(0) != '@') {
                resultSet = statement.executeQuery(SQL);
                while (resultSet.next()) {
                    if (resultSet.getString("from").equals(email)) {
                        conV.displaySendMessage(resultSet.getString("message"));
                    } else {
                        conV.displayReceiveMessage(resultSet.getString("message"));
                    }          
                }
            } else {
                SQL = "SELECT * FROM `chat` WHERE `to` LIKE '"+emailOfFriend+"'";
                resultSet = statement.executeQuery(SQL);
                String data;
                while (resultSet.next()) {
                    if (resultSet.getString("from").equals(email)) {
                        data = resultSet.getString("message");
                        conV.displaySendMessage(data.substring(data.indexOf(":") + 2, data.length()));
                    } else {
                        conV.displayReceiveMessage(resultSet.getString("message"));
                    }          
                }
            }
        } catch (SQLException ex) {homePageController.notify(ex.getMessage());}
    }
    
    protected void safeChat(String email, String emailOfFriend, String message) {
        try {
            String SQL = "INSERT INTO `whatsapp`.`chat` (`id`, `from`, `to`, `message`) "
                + "VALUES (NULL, '"+email+"', '"+emailOfFriend+"', '"+message+"')";
            statement.executeUpdate(SQL);
        } catch (SQLException ex) {homePageController.notify(ex.getMessage());}
    }
    
    protected Image getImage(String email) {
        InputStream is;
        OutputStream os;
        try {
            String SQL = "SELECT * FROM `users` WHERE `email` LIKE '"+email+"'";
            resultSet = statement.executeQuery(SQL);
            Blob blob = null;
            while (resultSet.next()) {
                blob = resultSet.getBlob("image");
            }            
            is = blob.getBinaryStream(); 
            os = new FileOutputStream(new File("photo.png"));
            byte[] content = new byte[1024];
            int size;
            while ((size = is.read(content)) != -1) {
                os.write(content, 0, size);
            }
            os.close();
            is.close();
            return new Image("file:photo.png", 1000, 1000, true, true);
        } catch (IOException | SQLException ex) {
            homePageController.notify(ex.getMessage());
            return new Image(getClass().getResourceAsStream("images/profile.png"));
        }
    }
    
    protected void updateImage(String email, File file) {
        try {
            InputStream image = new FileInputStream(file);
            String SQL = "UPDATE `whatsapp`.`users` SET `image` = ? WHERE `email` LIKE '"+email+"'";
            PreparedStatement prepStatement = connection.prepareStatement(SQL);
            prepStatement.setBinaryStream(1, image, (int)(file.length()));
            prepStatement.executeUpdate();
        } catch (FileNotFoundException | SQLException ex) {System.out.println(ex.getMessage());}
    }
    
    protected void updateName(String name, String email) {
        try {
            String SQL = "UPDATE `whatsapp`.`users` SET `name` = '"+name+"' WHERE `email` LIKE '"+email+"'";
            statement.executeUpdate(SQL);
        } catch (SQLException ex) {System.out.println(ex.getMessage());}
    }
    
    protected boolean updatePassword(String email, String curPass, String newPass) {
        try {
            String pass = null;
            String SQL = "UPDATE `whatsapp`.`users` SET `password` = '"+newPass+"' WHERE `email` LIKE '"+email+"'";
            String check = "SELECT * FROM `users` WHERE `email` LIKE '"+email+"'";
            resultSet = statement.executeQuery(check);
            while (resultSet.next()) {
                pass = resultSet.getString("password");
            }
            if (pass.equals(curPass)) {
                statement.executeUpdate(SQL);
                return true;
            } else {return false;}
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }
    
    protected void add(String email, String name) {
        try {
            String friend = name;
            if (name.charAt(0) != '@')
                friend = nameToEmail(name);
            String SQL = "INSERT INTO `whatsapp`.`friends` (`id`, `email`, `friends`) VALUES (NULL, '"+email+"', '"+friend+"')";
            String SQL2 = "INSERT INTO `whatsapp`.`friends` (`id`, `email`, `friends`) VALUES (NULL, '"+friend+"', '"+email+"')";
            ArrayList<String> check = loadFriends(email);
            boolean access = true;
            for (int i = 0; i < check.size(); i++) {
                if (name.equals(check.get(i))) { // wrong there is must be emails of check not names need convert it
                    access = false;
                    break;
                }
            }
            if (access) {
                statement.executeUpdate(SQL);
                if (friend.charAt(0) != '@')
                    statement.executeUpdate(SQL2);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    protected void createGroup(String name, String email, ArrayList<String> interlocutors, File file) {
        try {
            add(email, "@ " + name);
            String SQL = "INSERT INTO `whatsapp`.`groups` (`id`, `name`, `participants`, `image`) "
            + "VALUES (NULL, ?, ?, ?)";
            InputStream image;
            PreparedStatement prepStatement;
            for (int i = 0; i < interlocutors.size(); i++) {
                image = new FileInputStream(file);
                prepStatement = connection.prepareStatement(SQL);
                prepStatement.setString(1, "@ " + name);
                prepStatement.setString(2, nameToEmail(interlocutors.get(i)));
                prepStatement.setBinaryStream(3, image, (int)(file.length()));
                prepStatement.executeUpdate();
                add(nameToEmail(interlocutors.get(i)), "@ " + name);
            }
        } catch (FileNotFoundException | SQLException ex) {System.out.println(ex.getMessage());}
    }
    
    protected ArrayList<String> getParticipants(String name) {
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> listName = new ArrayList<>();
        try {
            resultSet = statement.executeQuery("SELECT * FROM `groups` WHERE name LIKE '"+name+"'");
            while (resultSet.next()) {
                list.add(resultSet.getString("participants"));
            }
            for (int i = 0; i < list.size(); i++) {
                listName.add(emailToName(list.get(i)));
            }
            return listName;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    protected Image getImageOfGroup(String name) {
        InputStream is;
        OutputStream os;
        try {
            String SQL = "SELECT * FROM `groups` WHERE `name` LIKE '"+name+"'";
            resultSet = statement.executeQuery(SQL);
            Blob blob = null;
            while (resultSet.next()) {
                blob = resultSet.getBlob("image");
            }            
            is = blob.getBinaryStream(); 
            os = new FileOutputStream(new File("group.png"));
            byte[] content = new byte[1024];
            int size;
            while ((size = is.read(content)) != -1) {
                os.write(content, 0, size);
            }
            os.close();
            is.close();
            return new Image("file:group.png", 1000, 1000, true, true);
        } catch (IOException | SQLException ex) {
            homePageController.notify(ex.getMessage());
            return new Image(getClass().getResourceAsStream("images/profile.png"));
        }
    }
    
    protected void leftGroup (String email, String groupName) {
        try {
            String SQL = "DELETE FROM `whatsapp`.`groups` "
                    + "WHERE name LIKE '"+groupName+"' AND participants LIKE '"+email+"'";
            statement.executeUpdate(SQL);
            SQL = "DELETE FROM `whatsapp`.`friends` "
                    + "WHERE email LIKE '"+email+"' AND friends LIKE '"+groupName+"'";
            statement.executeUpdate(SQL);
        } catch (SQLException ex) {System.out.println(ex.getMessage());}
    }
    
     protected void deleteFriend (String email, String emailOfFriend) {
        try {
            String SQL = "DELETE FROM `whatsapp`.`friends` "
                    + "WHERE email LIKE '"+email+"' AND friends LIKE '"+emailOfFriend+"'";
            statement.executeUpdate(SQL);
            SQL = "DELETE FROM `whatsapp`.`friends` "
                    + "WHERE email LIKE '"+emailOfFriend+"' AND friends LIKE '"+email+"'";
            statement.executeUpdate(SQL);
        } catch (SQLException ex) {System.out.println(ex.getMessage());}
    }
    
}