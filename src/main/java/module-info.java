module com.dtdt.DormManager {
    requires javafx.controls;
    requires javafx.fxml;

    // This is the important part!
    // It allows JavaFX to access your controllers and models
    opens com.dtdt.DormManager to javafx.fxml;
    opens com.dtdt.DormManager.controller to javafx.fxml;
    opens com.dtdt.DormManager.controller.admin to javafx.fxml;
    opens com.dtdt.DormManager.model to javafx.fxml;

    exports com.dtdt.DormManager;
}