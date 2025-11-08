package com.dtdt.DormManager.controller;

import com.dtdt.DormManager.Main;
import com.dtdt.DormManager.model.Tenant;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class TenantDashboardController {

    private Tenant currentTenant;

    // === FXML Components ===
    @FXML private ImageView profileImageView;
    @FXML private Label tenantNameLabel;
    @FXML private Label tenantIdLabel;
    @FXML private Label tenantEmailLabel;
    @FXML private Label buildingLabel;
    @FXML private Label roomLabel;
    @FXML private Label contractTypeLabel;
    @FXML private Label contractDatesLabel;
    @FXML private Button requestMaintenanceButton;
    @FXML private VBox announcementsVBox;
    @FXML private VBox maintenanceVBox;

    /**
     * This method is called by the LoginController to pass in the
     * currently logged-in tenant.
     */
    public void initData(Tenant tenant) {
        currentTenant = tenant;

        // 1. Populate Header
        tenantNameLabel.setText(currentTenant.getFullName());
        tenantIdLabel.setText(currentTenant.getUserId());
        tenantEmailLabel.setText(currentTenant.getEmail());

        // Example: Load a profile image (you would store this path in the model)
        // Image profilePic = new Image(getClass().getResourceAsStream("/com/dtdt/DormManager/img/default-profile.png"));
        // profileImageView.setImage(profilePic);

        // 2. Populate Building/Room Info (Dummy Data)
        // TODO: Get this info from the tenant's record (e.g., from a Room object)
        buildingLabel.setText("Building A");
        roomLabel.setText("Room 5210");
        contractTypeLabel.setText("6-Month Term");
        contractDatesLabel.setText("July 2025 - December 2025");

        // 3. Load dynamic content
        loadAnnouncements();
        loadMaintenanceRequests();
    }

    /**
     * FXML Action: Called when "Request Maintenance" button is clicked.
     */
    @FXML
    private void onRequestMaintenanceClick() {
        System.out.println("Request Maintenance button clicked.");
        // TODO: Open a pop-up window (new Stage) for the maintenance form
    }

    /**
     * FXML Action: Called when "Logout" hyperlink is clicked.
     */
    @FXML
    private void onLogoutClick() throws IOException {
        System.out.println("Logout clicked.");
        // Reload the login screen
        Main main = new Main();
        main.changeScene("login-view.fxml");
    }

    @FXML
    private void onPaymentClick(ActionEvent event) throws IOException {
        System.out.println("Payment link clicked.");

        // 1. Load the payment-view.fxml file
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/dtdt/DormManager/view/payment-view.fxml"));
        Parent root = loader.load();

        // 2. Get the controller of the new scene
        PaymentController controller = loader.getController();

        // 3. Pass the *current* tenant data to the new controller
        controller.initData(this.currentTenant);

        // 4. Get the current stage (window) and change the scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.setTitle("Payment Registration");


    }

    // === Private Helper Methods ===

    private void loadAnnouncements() {
        // TODO: Fetch announcements from the database
        // For now, we are just using the dummy data in the FXML.
        // You would clear the dummy data first:
        // announcementsVBox.getChildren().clear();

        // Then add new ones:
        // VBox announcementCard = createAnnouncementCard("Oct 20, 2025", "Water shutoff...");
        // announcementsVBox.getChildren().add(announcementCard);
    }

    private void loadMaintenanceRequests() {
        // TODO: Fetch this tenant's requests from the database
        // For now, we are just using the dummy data in the FXML.
        // maintenanceVBox.getChildren().clear();
        // ...
    }
}