package whatsapp.web;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class ConversationView extends AnchorPane{
    private final ObservableList<Node> speechBubbles = FXCollections.observableArrayList();

    private ScrollPane messageScroller;
    private VBox messageContainer;
    private final Color DEFAULT_SENDER_COLOR = Color.valueOf("#DDF9C7");
    private final Color DEFAULT_RECEIVER_COLOR = Color.valueOf("#FFFFFF");

    public ConversationView(){
        super();
        setupElements();
    }

    private void setupElements(){
        setupMessageDisplay();
        getChildren().setAll(messageScroller,messageContainer);
    }

    private void setupMessageDisplay(){
        messageContainer = new VBox(5);
        messageContainer.setPadding(new Insets(15, 15, 30, 15));
        Bindings.bindContentBidirectional(speechBubbles, messageContainer.getChildren());

        messageScroller = new ScrollPane(messageContainer);
        messageScroller.setVbarPolicy(ScrollBarPolicy.NEVER);
        messageScroller.setHbarPolicy(ScrollBarPolicy.NEVER);
        messageScroller.prefWidthProperty().bind(messageContainer.prefWidthProperty().subtract(5));
        messageScroller.setFitToWidth(true);
        
        speechBubbles.addListener((ListChangeListener<Node>) change -> {
             while (change.next()) {
                 if(change.wasAdded()){
                     messageScroller.setVvalue(messageScroller.getVmax());
                 }
             }
        });
        
        AnchorPane.setTopAnchor(messageScroller, 0.0);
        AnchorPane.setRightAnchor(messageScroller, 0.0);
        AnchorPane.setBottomAnchor(messageScroller, 0.0);
        AnchorPane.setLeftAnchor(messageScroller, 0.0);
    }

    protected void displaySendMessage (String message){
        Label label = new Label(message);
        label.setPadding(new Insets(5));
        Background DEFAULT_SENDER_BACKGROUND = new Background(
                new BackgroundFill(DEFAULT_SENDER_COLOR, new CornerRadii(5,5,5,5,false), Insets.EMPTY));
        label.setBackground(DEFAULT_SENDER_BACKGROUND);
        SVGPath  directionIndicator = new SVGPath();
        directionIndicator.setContent("M10 0 L0 10 L0 0 Z");
        directionIndicator.setFill(DEFAULT_SENDER_COLOR);
        HBox container = new HBox(label, directionIndicator);
        container.setAlignment(Pos.CENTER_RIGHT);
        speechBubbles.add(container);
    }

    protected void displayReceiveMessage (String message){
        Label label = new Label(message);
        label.setPadding(new Insets(5));
        Background DEFAULT_RECEIVER_BACKGROUND = new Background(
                new BackgroundFill(DEFAULT_RECEIVER_COLOR, new CornerRadii(5,5,5,5,false), Insets.EMPTY));
        label.setBackground(DEFAULT_RECEIVER_BACKGROUND);
        SVGPath  directionIndicator = new SVGPath();
        directionIndicator.setContent("M0 0 L10 0 L10 10 Z");
        directionIndicator.setFill(DEFAULT_RECEIVER_COLOR);
        HBox container = new HBox(directionIndicator, label);
        container.setAlignment(Pos.CENTER_LEFT);
        speechBubbles.add(container);
    }
}