package whatsapp.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class MultipleChatController {   
    
    @FXML private Label label;
    @FXML private Circle circle;
    @FXML private Label fileName;
    @FXML private TextField groupName;
    @FXML private ListView listViewFriends;
    
    private File file;
    private Stage stage;
    private String email;
    private Database database;
    private ImagePattern photo;
    private HomePageController forNotify;
    private final ObservableList<String> itemsFriend = FXCollections.observableArrayList();
    
    protected void setInfo(String email, Stage stage, Database database, HomePageController forNotify) {
        photo = new ImagePattern(new Image(getClass().getResourceAsStream("images/group.png")));
        circle.setFill(photo);
        this.database = database;
        this.stage = stage;
        this.email = email;
        this.forNotify = forNotify;
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF0000;");
        file = new File("src/whatsapp/web/images/group.png");
        
        ArrayList<String> friends = database.loadFriends(email);
        for (int i = 0; i < friends.size(); i++) {
            if (friends.get(i).charAt(0) != '@')
                itemsFriend.add(friends.get(i));          
        }
        listViewFriends.setItems(itemsFriend);
        listViewFriends.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    
    
    @FXML
    private void chooseFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
        file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            BufferedImage bufferedImage = ImageIO.read(file);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            photo = new ImagePattern(image);
            circle.setFill(photo);
            fileName.setText(file.getName());
        }
    }
    
    @FXML
    private void cancel() {
        stage.close();
    }
    
    @FXML
    private void create() {
        ObservableList<String> interlocutors = listViewFriends.getSelectionModel().getSelectedItems();
        ArrayList<String> arr = new ArrayList<>();
        arr.add(database.emailToName(email));
        for (int i = 0; i < interlocutors.size(); i++) {
            arr.add(interlocutors.get(i));
        }
        if (!interlocutors.isEmpty()) {
            if (!groupName.getText().isEmpty()) {
                String name = groupName.getText();
                boolean access = true;
                for (int i = 0; i < name.length(); i++) {
                    if (name.charAt(i) == ':' ||name.charAt(i) == '%'|| name.charAt(i) == '@') {
                        access = false;
                        break;
                    }
                }
                if (access) {
                    database.createGroup(name, email, arr, file);
                    forNotify.update();
                    cancel();
                } else {
                    label.setText("Name contains '@', ':', '%'");
                }
            } else {
                label.setText("Enter group name");
            }
        } else {
            label.setText("Select at least one participant");
        }
    }
}