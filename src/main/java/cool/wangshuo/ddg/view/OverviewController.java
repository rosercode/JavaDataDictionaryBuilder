package cool.wangshuo.ddg.view;

import cool.wangshuo.ddg.MainApp;
import javafx.fxml.FXML;

public class OverviewController {

    // Reference to the main application.
    private MainApp mainApp;

    public OverviewController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

        // Clear person details.

    }

    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}