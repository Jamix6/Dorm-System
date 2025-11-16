package com.dtdt.DormManager.controller;

import com.dtdt.DormManager.model.Tenant;
import com.dtdt.DormManager.model.User; // To use User.hashPassword
import com.dtdt.DormManager.controller.config.FirebaseInit;
import com.google.cloud.firestore.Firestore;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordDialogController {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorMessageLabel;

    private Tenant currentTenant;
    private Firestore db;

    public void initialize() {
        db = FirebaseInit.db;
        errorMessageLabel.managedProperty().bind(errorMessageLabel.visibleProperty());
    }

    public void initData(Tenant tenant) {
        this.currentTenant = tenant;
    }

    @FXML
    private void handlePasswordChange() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        errorMessageLabel.setVisible(false); // Hide previous errors

        // 1. Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            errorMessageLabel.setText("All fields are required.");
            errorMessageLabel.setVisible(true);
            return;
        }

        if (newPassword.length() < 8) {
            errorMessageLabel.setText("New password must be at least 8 characters long.");
            errorMessageLabel.setVisible(true);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            errorMessageLabel.setText("New password and confirmation do not match.");
            errorMessageLabel.setVisible(true);
            return;
        }

        // 2. Verify current password
        String hashedCurrentPasswordInput = User.hashPassword(currentPassword);
        if (!currentTenant.getPasswordHash().equals(hashedCurrentPasswordInput)) {
            errorMessageLabel.setText("Incorrect current password.");
            errorMessageLabel.setVisible(true);
            return;
        }

        // 3. Hash the new password
        String hashedNewPassword = User.hashPassword(newPassword);

        // 4. Update password in Firebase
        if (db != null && currentTenant != null) {
            db.collection("users").document(currentTenant.getUserId())
                    .update("passwordHash", hashedNewPassword)
                    .addListener(() -> {
                        // This runs after the update is attempted
                        try {
                            // Wait for the update to complete
                            db.collection("users").document(currentTenant.getUserId())
                                .update("passwordHash", hashedNewPassword).get(); // .get() waits for completion

                            // Update the in-memory tenant object
                            currentTenant.setPasswordHash(hashedNewPassword); 

                            // Show success message and close dialog
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText("Your password has been changed successfully.");
                            alert.showAndWait();
                            closeDialog();

                        } catch (Exception e) {
                            e.printStackTrace();
                            errorMessageLabel.setText("Failed to change password: " + e.getMessage());
                            errorMessageLabel.setVisible(true);
                        }
                    }, javafx.application.Platform::runLater); // Run UI updates on JavaFX thread
        } else {
            errorMessageLabel.setText("Database not initialized or tenant not set.");
            errorMessageLabel.setVisible(true);
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) currentPasswordField.getScene().getWindow();
        stage.close();
    }
}