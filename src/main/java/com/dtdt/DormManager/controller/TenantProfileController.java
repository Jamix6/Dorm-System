package com.dtdt.DormManager.controller;

import com.dtdt.DormManager.Main;
import com.dtdt.DormManager.model.Tenant;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class TenantProfileController {

    @FXML private Label tenantNameLabel;
    @FXML private Label tenantIdLabel;
    @FXML private Label tenantEmailLabel;
    
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField studentIdField;
    @FXML private TextField buildingField;
    @FXML private TextField roomField;

    private Tenant currentTenant;

    public void initData(Tenant tenant) {
        this.currentTenant = tenant;

        // Populate header
        tenantNameLabel.setText(tenant.getFullName());
        tenantIdLabel.setText(tenant.getUserId());
        tenantEmailLabel.setText(tenant.getEmail());

        // Populate fields
        nameField.setText(tenant.getFullName());
        emailField.setText(tenant.getEmail());
        studentIdField.setText(tenant.getUserId());
        
        // TODO: Load and display building/room data
        buildingField.setText(tenant.getAssignedRoomID() != null ? tenant.getAssignedRoomID() : "Not Assigned");
        roomField.setText("N/A"); // You'll need to fetch this from the Room object
    }

    // --- Navigation ---
    @FXML
    private void goToDashboard(ActionEvent event) throws IOException {
        loadScene(event, "/com/dtdt/DormManager/view/tenant-dashboard.fxml", "Tenant Dashboard");
    }

    @FXML
    private void goToPayment(ActionEvent event) throws IOException {
        loadScene(event, "/com/dtdt/DormManager/view/payment-view.fxml", "Payment Registration");
    }

    @FXML
    private void onLogoutClick() throws IOException {
        Main main = new Main();
        main.changeScene("login-view.fxml");
    }

    // --- Main Feature ---
    
    @FXML
    private void onChangePasswordClick(ActionEvent event) {
        try {
            // 1. Load the dialog FXML
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/dtdt/DormManager/view/change-password-dialog.fxml"));
            Parent root = loader.load();

            // 2. Get its controller and pass the tenant data
            ChangePasswordDialogController dialogController = loader.getController();
            dialogController.initData(currentTenant);

            // 3. Show the dialog as a new "pop-up" window
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Change Password");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait(); // Wait for the user to close it

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Helper method to load new scenes
    private void loadScene(ActionEvent event, String fxmlFile, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlFile));
        Parent root = loader.load();

        // Get the controller and pass data
        if (title.equals("Tenant Dashboard")) {
            com.dtdt.DormManager.controller.TenantDashboardController controller = loader.getController();
            controller.initData(currentTenant);
        } else if (title.equals("Payment Registration")) {
            com.dtdt.DormManager.controller.PaymentController controller = loader.getController();
            controller.initData(currentTenant);
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.setTitle(title);
    }
}