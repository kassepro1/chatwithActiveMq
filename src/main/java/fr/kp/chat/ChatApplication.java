package fr.kp.chat;

import fr.kp.chat.config.Consumer;
import fr.kp.chat.config.Producer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.jms.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

@SpringBootApplication
@Slf4j
public class ChatApplication extends Application {
    private MessageProducer messageProducer;
    private Session session;
    String code;
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
        Application.launch(ChatApplication.class);
        Producer.produce();
        Consumer.consume();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("JMS CHAT");
        //Creation Layou
        BorderPane borderPane = new BorderPane();
        //Creation HBOX
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10));
        hBox.setBackground(new Background(new BackgroundFill(Color.BLUEVIOLET, CornerRadii.EMPTY, Insets.EMPTY)));
        Label codelabel = new Label("Code");
        TextField textFieldCode = new TextField("C1");
        textFieldCode.setPromptText("Entrer Code");
        Label hostodelabel = new Label("Code");
        TextField textFielHost = new TextField("localhost");
        textFielHost.setPromptText("Entrer le host");
        Label porttodelabel = new Label("Code");
        TextField textFielPort = new TextField("6616");
        textFielPort.setPromptText("Entrer le port");
        Button buttonCon = new Button("Connecter");
        hBox.getChildren().add(codelabel);
        hBox.getChildren().add(textFieldCode);
        hBox.getChildren().add(hostodelabel);
        hBox.getChildren().add(textFielHost);
        hBox.getChildren().add(porttodelabel);
        hBox.getChildren().add(textFielPort);
        hBox.getChildren().add(buttonCon);
        borderPane.setTop(hBox);
        //creation vbox
        VBox vBox = new VBox();
        GridPane gridPane = new GridPane();
        HBox hBoxchat = new HBox();
        vBox.getChildren().add(gridPane);
        vBox.getChildren().add(hBoxchat);
        borderPane.setCenter(vBox);

        //corps
        Label labelTo = new Label("To");
        TextField textFieldTo = new TextField("C1");
        Label labelMessage = new Label("Message :");
        TextArea textAreaMessage = new TextArea();
        Button buttonSendText = new Button("Envoyer Text");
        Label labelImage = new Label("Image");
        File images = new File("D:\\DEV\\Java\\chat\\src\\main\\resources\\images");
        ObservableList<String> stringObservableList = FXCollections.observableArrayList(images.list());
        ComboBox<String> comboBoxImg = new ComboBox<String>(stringObservableList);
        comboBoxImg.getSelectionModel().select(0);
        Button buttonSendImg = new Button("Envoyer Image");
        gridPane.setPadding(new Insets(10));
        textAreaMessage.setPrefRowCount(3);
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.add(labelTo,0,0);
        gridPane.add(textFieldTo,1,0);
        gridPane.add(labelMessage,0,1);
        gridPane.add(textAreaMessage,1,1);
        gridPane.add(buttonSendText,2,1);
        gridPane.add(labelImage,0,2);
        gridPane.add(comboBoxImg,1,2);
        gridPane.add(buttonSendImg,2,2);
        //Liste des message
        ObservableList<String> messages = FXCollections.observableArrayList();
        TableView<String> tableView = new TableView<>(messages);
        TableColumn<String,String> messageCol = new TableColumn<String,String>("Message");
        messageCol.setCellValueFactory(new PropertyValueFactory("Message"));

        tableView.getColumns().setAll(messageCol);
        File selectedFile = new File("D:\\DEV\\Java\\chat\\src\\main\\resources\\images/"+comboBoxImg.getSelectionModel().getSelectedItem());
        Image image = new Image(selectedFile.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(320);
        imageView.setFitHeight(240);
        hBoxchat.setPadding(new Insets(10));
        hBoxchat.setSpacing(10);
        hBoxchat.getChildren().add(tableView);
        hBoxchat.getChildren().add(imageView);
        //Creation de la scene
        Scene scene = new Scene(borderPane, 600, 400);
        //MIs en place
        primaryStage.setScene(scene);
        primaryStage.show();
        buttonSendImg.setOnAction(e->{
            try {
                StreamMessage streamMessage = session.createStreamMessage();
         String nomImage = comboBoxImg.getSelectionModel().getSelectedItem();
            File imgToStream = new File("D:\\DEV\\Java\\chat\\src\\main\\resources\\images/"+nomImage);
                FileInputStream fis = new FileInputStream(imgToStream);
                byte [] data = new byte[(int)imgToStream.length()];
                streamMessage.setStringProperty("code",textFieldTo.getText());
                streamMessage.writeString(nomImage);
                streamMessage.writeInt(data.length);
                fis.read(data);
                streamMessage.writeBytes(data);
                messageProducer.send(streamMessage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
        buttonSendText.setOnAction(e->{
            try {
                TextMessage textMessage = session.createTextMessage();
                textMessage.setText(textAreaMessage.getText());
                textMessage.setStringProperty("code",textFieldTo.getText());
                messageProducer.send(textMessage);
                log.info("Message envoye ");
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        });
        //recuperer l'image selectionner dans le combo
        comboBoxImg.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                File imgSelect = new File("D:\\DEV\\Java\\chat\\src\\main\\resources\\images/"+newValue);
                Image image1 = new Image(imgSelect.toURI().toString());
                imageView.setImage(image1);
                    }
                });
        buttonCon.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                code = textFieldCode.getText();
                String host = textFielHost.getText();
                int port = Integer.parseInt(textFielPort.getText());

                ConnectionFactory connectionFactory =
                        new ActiveMQConnectionFactory("tcp://" + host + ":" + port);
                try {
                    Connection connection = connectionFactory.createConnection();
                    connection.start();
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    Destination destination = session.createTopic("kp.topic");
                    messageProducer = session.createProducer(destination);
                    MessageConsumer consumer = session.createConsumer(destination, "code='" + code + "'");
                    consumer.setMessageListener(message -> {
                        if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            try {
                                log.info("Message envoye == " + textMessage.getText());
                                messages.add(textMessage.getText());
                            } catch (JMSException e) {
                                e.printStackTrace();
                            }
                        } else if (message instanceof StreamMessage) {
StreamMessage streamMessage = (StreamMessage) message;
                            try {
                                String nomPhoto = streamMessage.readString();
                                int size = streamMessage.readInt();
                                byte [] data = new byte[size] ;
                                ByteArrayInputStream imgByte = new ByteArrayInputStream(data);
                                Image image1 = new Image(imgByte);
                                imageView.setImage(image1);
                            } catch (JMSException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JMSException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
