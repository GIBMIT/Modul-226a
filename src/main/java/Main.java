import controllers.StartController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    /**
     * Entry point for the application
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.loadDependencies();
        StartController controller = new StartController();
        controller.startApplication(primaryStage);
    }

    private void loadDependencies() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
