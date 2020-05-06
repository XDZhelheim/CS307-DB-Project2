import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.sql.*;


public class GUI_test extends Application {
    static final String driver = "org.postgresql.Driver";
    static final String host = "192.168.137.1";
    static final String dbname = "project2";
    static final String user = "checker";
    static final String password = "222222";
    static final String port = "5432";
    static final String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
    static Connection conn = null;

    static {
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("12306");
        f1(stage);
    }

    public void f1(Stage stage) {
        GridPane gp = new GridPane();
        gp.setAlignment(Pos.CENTER);
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(25, 25, 25, 25));

        Text text = new Text("Welcome to 12306");
        text.setFont(Font.font("Times New Roman", FontWeight.NORMAL, 20));
        gp.add(text, 0, 0, 2, 1);

        Label name = new Label("User Name:");
        gp.add(name, 0, 1);
        TextField nametf = new TextField();
        gp.add(nametf, 1, 1);

        Label pw = new Label("Password:");
        gp.add(pw, 0, 2);
        PasswordField pwf = new PasswordField();
        gp.add(pwf, 1, 2);

        Button b = new Button("Sign in");
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.BOTTOM_RIGHT);
        hb.getChildren().add(b);
        gp.add(hb, 1, 4);

        Text actiontext = new Text();
        gp.add(actiontext, 1, 6);

        b.setOnAction((ActionEvent event) -> {
            try {
                PreparedStatement stmt = conn.prepareStatement("select 1 from users where user_name=? and password=?;");
                stmt.setString(1, nametf.getText());
                stmt.setString(2, pwf.getText());

                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    actiontext.setFill(Color.FIREBRICK);
                    actiontext.setText("Wrong password!");
                } else {
                    stage.hide();
                    C1 open = new C1(nametf.getText());
                    open.start(new Stage());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        Scene scene = new Scene(gp, 400, 375);
        stage.setScene(scene);
        stage.show();
    }
}
