import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class C1 extends Application {

    private String username;

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 664, 600);
        primaryStage.setTitle(username);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    C1() {
    }

    C1(String name) {
        username = name;
    }
}
