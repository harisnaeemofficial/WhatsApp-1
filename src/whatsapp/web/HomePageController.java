package whatsapp.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HomePageController {
    
    private final String IP = "localhost";     
    private final int PORT = 5000;       
    
    @FXML private Button btnAdd;
    @FXML private ComboBox comboBoxSearch;
    @FXML private TextField homeMessageInput;
    @FXML private AnchorPane centerAnchorPane;
    @FXML private Circle myAva, circleSmallAva;
    @FXML private ListView listView, contactInfo;
    @FXML private Label labelUserName, nameOfFriend, notification;
    
    private Parent parent;
    private Database database;
    private PrintWriter writer;
    private ImagePattern myPhoto;
    private String buffer = null;
    private FXMLLoader fxmlLoader;
    private ConversationView conV;
    private String email, name, emailOfFriend;
    private Label labelInListViewName, labelInListViewEmail;
    private final Circle circleBigAva = new Circle(130);
    private final ObservableList<String> items = FXCollections.observableArrayList();
    private final ObservableList<Node> itemsInfo = FXCollections.observableArrayList(); 
    private final ObservableList<String> allUsersItems = FXCollections.observableArrayList();
    private final FilteredList<String> filteredItems  = new FilteredList<>(allUsersItems, event -> true);
    
    private final Color DEFAULT_SENDER_COLOR = Color.valueOf("#75D77F");
    Background DEFAULT_SENDER_BACKGROUND = new Background(
                new BackgroundFill(DEFAULT_SENDER_COLOR, new CornerRadii(5,5,5,5,false), Insets.EMPTY));
    
    protected void start() {
        setNet();
        createScene();
        update();
        updateContactInfo(name, email, "ME");
    }
    
    protected void receiveMessage (String message) {conV.displayReceiveMessage(message);}
    protected void notify (String message) {notification.setText(message);}
    protected void setEmail (String email) {this.email  = email;}
    protected void setDatabase (Database database) {this.database = database;}

    private void setNet() {
        try {
            Socket sock = new Socket(IP, PORT);
            InputStreamReader is = new InputStreamReader(sock.getInputStream());
            BufferedReader reader = new BufferedReader(is);
            writer = new PrintWriter(sock.getOutputStream());
            writer.println(email);
            writer.flush();          
            Thread listenerThread = new Thread(new Listener(this, reader));
            listenerThread.start();
        } catch (IOException ex){notify(ex.getMessage());}		
    }
    
    private void createScene() {
        AnchorPane circleAvaAP = new AnchorPane();
        AnchorPane.setTopAnchor(circleBigAva, 10.0);
        AnchorPane.setRightAnchor(circleBigAva, 0.0);
        AnchorPane.setBottomAnchor(circleBigAva, 10.0);
        AnchorPane.setLeftAnchor(circleBigAva, 0.0);
        circleAvaAP.getChildren().add(circleBigAva);
        labelInListViewName = new Label();
        labelInListViewEmail = new Label();
        itemsInfo.addAll(circleAvaAP, labelInListViewName, labelInListViewEmail);
        contactInfo.setItems(itemsInfo);
        circleBigAva.setId("btn");
        
        
        comboBoxSearch.setVisibleRowCount(3);
        comboBoxSearch.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            final TextField editor = comboBoxSearch.getEditor();
            final String selected = (String) comboBoxSearch.getSelectionModel().getSelectedItem();
            Platform.runLater(() -> {
                if (selected == null || !selected.equals(editor.getText())) {
                    filteredItems.setPredicate(item -> {
                        return item.toUpperCase().startsWith(newValue.toUpperCase());
                    });
                    if (filteredItems.isEmpty())
                        comboBoxSearch.hide();
                    else
                        comboBoxSearch.show();
                }
            });
        });
    }
    
    @FXML
    protected void update() {

        if (allUsersItems != null) {allUsersItems.clear();} // clear allUsers items in combobox
        if (items != null) {items.clear();} // clear friends list view
        
        ArrayList<String> allUsers = database.loadAllUsers(email); // add allUsers items in combobox
        for (int i = 0; i < allUsers.size(); i++) {
            allUsersItems.add(allUsers.get(i));
        }  
        
        ArrayList<String> friends = database.loadFriends(email); // add friends items in combobox
        for (int i = 0; i < friends.size(); i++) {
            items.add(friends.get(i));
        }
        
        name = database.emailToName(email); // updateName
        labelUserName.setText(name);
        
        myPhoto = new ImagePattern(database.getImage(email)); // updateImage
        myAva.setFill(myPhoto);
        
        comboBoxSearch.setItems(filteredItems);
        listView.setItems(items);
    }
    
    protected void updateContactInfo(String name, String email, String WHO) {
        ImagePattern image;
        switch (WHO) {
            case "ME":
                if (itemsInfo != null) {
                    itemsInfo.clear();
                    AnchorPane circleAvaAP = new AnchorPane();
                    AnchorPane.setTopAnchor(circleBigAva, 10.0);
                    AnchorPane.setRightAnchor(circleBigAva, 0.0);
                    AnchorPane.setBottomAnchor(circleBigAva, 10.0);
                    AnchorPane.setLeftAnchor(circleBigAva, 0.0);
                    circleAvaAP.getChildren().add(circleBigAva);
                    labelInListViewName = new Label();
                    labelInListViewEmail = new Label();
                    labelInListViewName.setPrefWidth(260.0);
                    labelInListViewName.setStyle("-fx-alignment: CENTER;"
                            + "-fx-font-weight: bold;");
                    labelInListViewEmail.setPrefWidth(260.0);
                    labelInListViewEmail.setStyle("-fx-alignment: CENTER;"
                            + "-fx-font-weight: bold;");
                    labelInListViewName.setText(name);
                    labelInListViewEmail.setText(email);
                    itemsInfo.addAll(circleAvaAP, labelInListViewName, labelInListViewEmail);
                    contactInfo.setItems(itemsInfo);
                }              
                if (conV != null) {conV.setVisible(false);}
                btnAdd.setVisible(false);
                nameOfFriend.setVisible(false);
                circleSmallAva.setVisible(false);
                homeMessageInput.setVisible(false);
                image = new ImagePattern(database.getImage(email));
                circleBigAva.setFill(image);
                Button btnEdit = new Button("Edit");
                btnEdit.setStyle("-fx-font-weight: bold;");
                btnEdit.setPrefWidth(260.0);
                btnEdit.setId("btn");
                itemsInfo.add(btnEdit);
                btnEdit.setOnAction(event -> {
                    edit(event);
                });
                break;
            case "GROUP":
                if (itemsInfo != null) {
                    itemsInfo.clear();
                    AnchorPane circleAvaAP = new AnchorPane();
                    AnchorPane.setTopAnchor(circleBigAva, 10.0);
                    AnchorPane.setRightAnchor(circleBigAva, 0.0);
                    AnchorPane.setBottomAnchor(circleBigAva, 10.0);
                    AnchorPane.setLeftAnchor(circleBigAva, 0.0);
                    circleAvaAP.getChildren().add(circleBigAva);
                    itemsInfo.add(circleAvaAP);
                    
                    
                    Label par = new Label("Participants");
                    par.setPrefWidth(260.0);
                    par.setStyle("-fx-alignment: CENTER;"
                            + "-fx-font-weight: bold;");
                    par.setBackground(DEFAULT_SENDER_BACKGROUND);
                    par.setPadding(new Insets(2, 5, 2, 5));
                    par.setEffect(new DropShadow(10, Color.GREEN));
                    itemsInfo.add(par);
                    
                    ArrayList<String> n = database.getParticipants(name);
                    for (int i = 0; i < n.size(); i++) {
                        par = new Label(n.get(i));
                        par.setPrefWidth(260.0);
                        par.setStyle("-fx-alignment: CENTER;"
                                + "-fx-font-weight: bold;");
                        itemsInfo.add(par);
                    }
                    contactInfo.setItems(itemsInfo);
                }
                if (conV != null) {conV.setVisible(false);}
                image = new ImagePattern(database.getImageOfGroup(name));
                circleBigAva.setFill(image);
                circleSmallAva.setFill(image);
                
                Button btnLeft = new Button("Left");
                btnLeft.setStyle("-fx-font-weight: bold;");
                btnLeft.setPrefWidth(260.0);
                btnLeft.setId("btn");
                itemsInfo.add(btnLeft);
                btnAdd.setVisible(true);
                nameOfFriend.setVisible(true);
                circleSmallAva.setVisible(true);
                homeMessageInput.setVisible(true);
                btnLeft.setOnAction(event -> {
                    btnLeftAction();
                });
                break;
            default:
                if (itemsInfo != null) {
                    itemsInfo.clear();
                    AnchorPane circleAvaAP = new AnchorPane();
                    AnchorPane.setTopAnchor(circleBigAva, 10.0);
                    AnchorPane.setRightAnchor(circleBigAva, 0.0);
                    AnchorPane.setBottomAnchor(circleBigAva, 10.0);
                    AnchorPane.setLeftAnchor(circleBigAva, 0.0);
                    circleAvaAP.getChildren().add(circleBigAva);
                    labelInListViewName = new Label();
                    labelInListViewEmail = new Label();
                    labelInListViewName.setPrefWidth(260.0);
                    labelInListViewName.setStyle("-fx-alignment: CENTER;"
                            + "-fx-font-weight: bold;");
                    labelInListViewEmail.setPrefWidth(260.0);
                    labelInListViewEmail.setStyle("-fx-alignment: CENTER;"
                            + "-fx-font-weight: bold;");
                    labelInListViewName.setText(name);
                    labelInListViewEmail.setText(email);
                    itemsInfo.addAll(circleAvaAP, labelInListViewName, labelInListViewEmail);
                    contactInfo.setItems(itemsInfo);
                }
                image = new ImagePattern(database.getImage(email));
                circleBigAva.setFill(image);
                circleSmallAva.setFill(image);
                Button btnDelete = new Button("Delete");
                btnDelete.setStyle("-fx-font-weight: bold;");
                btnDelete.setPrefWidth(260.0);
                btnDelete.setId("btn");
                itemsInfo.add(btnDelete);
                btnAdd.setVisible(true);
                nameOfFriend.setVisible(true);
                circleSmallAva.setVisible(true);
                homeMessageInput.setVisible(true);
                btnDelete.setOnAction(event -> {
                    btnDeleteAction();
                });
                break;
        }
    }
    
    @FXML
    private void chooseInterlocutor() {
        try {
            String toWhom = listView.getSelectionModel().getSelectedItem().toString();
            buffer = toWhom;
            nameOfFriend.setText(toWhom);
            if (toWhom.charAt(0) != '@') {
                emailOfFriend = database.nameToEmail(toWhom); 
                updateContactInfo(toWhom, emailOfFriend, "FRIEND"); 

                conV = new ConversationView();          
                AnchorPane.setRightAnchor(conV, 0.0);
                AnchorPane.setTopAnchor(conV, 0.0);
                AnchorPane.setLeftAnchor(conV, 0.0);
                AnchorPane.setBottomAnchor(conV, 55.0);
                centerAnchorPane.getChildren().add(conV);    
                database.loadChatHistory(email, emailOfFriend, conV);
            } else { // group is choosed
                emailOfFriend = toWhom; // MAIN
                updateContactInfo(toWhom, null, "GROUP"); 
                conV = new ConversationView();
                AnchorPane.setRightAnchor(conV, 0.0);
                AnchorPane.setTopAnchor(conV, 0.0);
                AnchorPane.setLeftAnchor(conV, 0.0);
                AnchorPane.setBottomAnchor(conV, 55.0);
                centerAnchorPane.getChildren().add(conV);    
                database.loadChatHistory(email, emailOfFriend, conV);
            }
        } catch (Exception ex) {}
    }
    
    @FXML
    private void sendMessage() {
        try {
            String message = homeMessageInput.getText();
            if (!message.isEmpty()) {
                conV.displaySendMessage(message);   
                if (emailOfFriend.charAt(0) != '@')
                    database.safeChat(email, emailOfFriend, message);
                else
                    database.safeChat(email, emailOfFriend, name + ": " + message);
                homeMessageInput.clear();
                writer.println(emailOfFriend + ": " + email + "% " + message);
                writer.flush();
            }
        } catch (Exception ex) {notify("Can not send the message!!!");}
    }
    
    @FXML 
    private void edit(ActionEvent event) {
        try {
            fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("layouts/Edit.fxml"));
            parent = fxmlLoader.load();
            EditController editController = fxmlLoader.getController();
            Stage stage = new Stage();
            editController.setInfo(name, email, stage, database, this);
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.setTitle("Profile settings");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("images/logo.png")));
            stage.setResizable(false);     
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node)event.getSource()).getScene().getWindow() );
            stage.show();
        } catch (IOException ex) {notify(ex.getMessage());}
    }
    
    @FXML 
    private void createMultipleChat(ActionEvent event) {
        try {
            fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("layouts/MultipleChat.fxml"));
            parent = fxmlLoader.load();
            MultipleChatController multipleChatController = fxmlLoader.getController();
            Stage stage = new Stage();
            multipleChatController.setInfo(email, stage, database, this);
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.setTitle("Create chat");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("images/logo.png")));
            stage.setResizable(false);     
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node)event.getSource()).getScene().getWindow() );
            stage.show();
        } catch (IOException ex) {notify(ex.getMessage());}
    }
    
    @FXML
    private void logOut() {
            Stage parentStage = (Stage) labelUserName.getScene().getWindow();
            parentStage.close();
    }
    
    @FXML
    private void add() {
        String user = comboBoxSearch.getSelectionModel().getSelectedItem().toString();
        boolean accept = false;
        String check;
        for (int i = 0; i < allUsersItems.size(); i++) {
            check = allUsersItems.get(i);
            if (user.equals(check)) {
                accept = true;
                break;
            }
        }
        if (accept) {
            database.add(email, user);
            update();
        }
    }
    
    protected String getBuffer() {
        return buffer;
    }
	
    private void btnLeftAction() {
        database.leftGroup(email, emailOfFriend);
        update();
        updateContactInfo(name, email, "ME");
    }
    
    private void btnDeleteAction() {
        database.deleteFriend(email, emailOfFriend);
        update();
        updateContactInfo(name, email, "ME");
    }
}