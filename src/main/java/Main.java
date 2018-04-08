import controllers.StartController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main Class
 */
public class Main extends Application {
    /**
     * Entry point for the application
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Start method of JavaFX
     * @param primaryStage Stage
     * @throws Exception if anything failed
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.loadDependencies();
        StartController controller = new StartController();
        controller.startApplication(primaryStage);
    }

    /**
     * Check if all dependencies can be loaded
     */
    private void loadDependencies() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
