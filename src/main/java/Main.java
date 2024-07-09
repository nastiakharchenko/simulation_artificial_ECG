import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;

public class Main extends Application {

    @Getter
    private static Stage primaryStage;
    @Getter
    private static GraphicController graphicController;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("КП3");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("Graphic.fxml"));
        AnchorPane grp = (AnchorPane) loader.load();

        graphicController = loader.getController();

        Scene scene = new Scene(grp);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}