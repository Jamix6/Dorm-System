package com.dtdt.DormManager.controller;

import com.dtdt.DormManager.Main;
import com.dtdt.DormManager.model.Tenant;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import com.dtdt.DormManager.controller.TenantDashboardController;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class PaymentController {

    private Tenant currentTenant;

    // === FXML Header Components ===
    @FXML private Label tenantNameLabel;
    @FXML private Label tenantIdLabel;
    @FXML private Label tenantEmailLabel;
    @FXML private Label buildingLabel;
    @FXML private Label roomLabel;
    @FXML private Label contractTypeLabel;
    @FXML private Label contractDatesLabel;

    // === FXML Page-Specific Components ===
    @FXML private ComboBox<String> receiptComboBox;
    @FXML private VBox billingHistoryVBox;

    /**
     * This method will be called by the previous controller (or Login)
     * to pass in the currently logged-in tenant.
     */
    public void initData(Tenant tenant) {
        currentTenant = tenant;

        // 1. Populate Header
        tenantNameLabel.setText(currentTenant.getFullName());
        tenantIdLabel.setText(currentTenant.getUserId());
        tenantEmailLabel.setText(currentTenant.getEmail());

        // 2. Populate Building/Room Info (Dummy Data)
        // TODO: Get this info from the tenant's record
        buildingLabel.setText("Building A");
        roomLabel.setText("Room 5210");
        contractTypeLabel.setText("6-Month Term");
        contractDatesLabel.setText("July 2025 - December 2025");

        // 3. Load dynamic content
        loadBillingHistory();
    }

    private void loadBillingHistory() {
        // TODO: Fetch this tenant's billing history from the database
        // You would clear the dummy data first:
        // billingHistoryVBox.getChildren().clear();

        // Then add new ones:
        // HBox billCard = createBillingCard("October 2025", "PENDING", 5000.00);
        // billingHistoryVBox.getChildren().add(billCard);
    }

    /**
     * FXML Action: Called when "Dashboard" hyperlink is clicked.
     */
    @FXML
    private void goToDashboard(ActionEvent event) throws IOException {
        // 1. Load the dashboard FXML
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/dtdt/DormManager/view/tenant-dashboard.fxml"));
        Parent root = loader.load();

        // 2. Get the dashboard controller
        TenantDashboardController controller = loader.getController();

        // 3. Pass the tenant data BACK to the dashboard
        controller.initData(this.currentTenant);

        // 4. Get the current stage (window) from the event
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.setTitle("Tenant Dashboard");


    }

    /**
     * FXML Action: Called when "Logout" hyperlink is clicked.
     */
    @FXML
    private void onLogoutClick() throws IOException {
        System.out.println("Logout clicked.");
        Main main = new Main();
        main.changeScene("login-view.fxml");
    }
}