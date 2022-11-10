package cool.wangshuo.ddg;

import cool.wangshuo.ddg.view.OverviewController;
import cool.wangshuo.ddg.view.RootLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;


/**
 * @author wangshuo
 * @description
 * @createDate 2022/11/10 10:00
 */

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;


    /**
     * Constructor
     */
    public MainApp() {

    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("数据字典生成器 （data dictionary generator）");

        // Set the application icon.
        this.primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("icon/logo.png").toString()));

        initRootLayout();

        showOverview();
    }

    /**
     * Initializes the root layout
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the overview inside the root layout.
     */
    public void showOverview() {
        try {
            // Load  overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/Overview.fxml"));
            AnchorPane overview = (AnchorPane) loader.load();
            rootLayout.setCenter(overview);
            OverviewController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

}