package com.dtdt.DormManager.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import com.dtdt.DormManager.model.Admin;
import com.dtdt.DormManager.controller.admin.AdminDashboardController;
import com.dtdt.DormManager.model.Tenant;
import com.dtdt.DormManager.model.User;
import com.dtdt.DormManager.Main;
import com.dtdt.DormManager.controller.config.FirebaseInit; // Import Firebase
import com.google.cloud.firestore.Firestore;
// Unused imports from sign-up have been removed

public class LoginController {

    // === Login View Components ===
    @FXML private TextField studentIdField;
    @FXML private TextField emailFieldLogin;
    @FXML private PasswordField passwordFieldLogin;
    @FXML private TextField passwordTextFieldLogin;
    @FXML private Button signInButton;
    @FXML private Button togglePasswordLoginBtn;
    @FXML private Label loginErrorLabel;

    // === All Sign-Up @FXML variables have been removed ===

    // Password visibility flag
    private boolean isPasswordVisibleLogin = false;

    /**
     * This method is automatically called after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        // Bind TextFields to PasswordFields for password visibility toggle
        if (passwordTextFieldLogin != null && passwordFieldLogin != null) {
            passwordTextFieldLogin.textProperty().bindBidirectional(passwordFieldLogin.textProperty());
        }

        // Bind managed property to visible property for error labels
        if (loginErrorLabel != null) {
            loginErrorLabel.managedProperty().bind(loginErrorLabel.visibleProperty());
            loginErrorLabel.setVisible(false); // Start hidden
        }
    }

    @FXML
    protected void onSignInClick(ActionEvent event) throws IOException {
        System.out.println("Sign In button clicked.");

        String idInput = studentIdField.getText() == null ? "" : studentIdField.getText().trim();
        String emailInput = emailFieldLogin.getText() == null ? "" : emailFieldLogin.getText().trim();
        String pwInput = passwordFieldLogin.getText() == null ? "" : passwordFieldLogin.getText().trim();

        Firestore db = FirebaseInit.db;
        if (db == null) {
            loginErrorLabel.setText("Database connection not established.");
            loginErrorLabel.setVisible(true);
            return;
        }

        try {
            com.google.cloud.firestore.QuerySnapshot querySnapshot;

            // --- THIS IS THE NEW, SMARTER LOGIC ---

            // 1. Try to find user by Student ID first
            if (!idInput.isEmpty()) {
                querySnapshot = db.collection("users").whereEqualTo("userId", idInput).get().get();
            } else {
                // If ID is empty, create an empty snapshot
                querySnapshot = null;
            }

            // 2. If no user was found with the ID, AND the email field is not empty, try email
            if ((querySnapshot == null || querySnapshot.getDocuments().isEmpty()) && !emailInput.isEmpty()) {
                System.out.println("No user found with ID. Trying email...");
                querySnapshot = db.collection("users").whereEqualTo("email", emailInput).get().get();
            }

            // --- END NEW LOGIC ---

            // 3. If we still have no user, then fail
            if (querySnapshot == null || querySnapshot.getDocuments().isEmpty()) {
                loginErrorLabel.setText("Invalid Credentials (user not found)");
                loginErrorLabel.setVisible(true);
                return;
            }

            // 4. Get the user document and verify password
            var userDoc = querySnapshot.getDocuments().get(0);
            String storedPasswordHash = userDoc.getString("passwordHash");

            // Make sure you deleted the local hashPassword() method
            String inputPasswordHash = User.hashPassword(pwInput);

            if (storedPasswordHash == null || !storedPasswordHash.equals(inputPasswordHash)) {
                loginErrorLabel.setText("Invalid Credentials (password mismatch)");
                loginErrorLabel.setVisible(true);
                return;
            }

            // 5. Authentication successful! Check user's role.
            String userType = userDoc.getString("userType");
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            loginErrorLabel.setVisible(false);

            // 6. Route user based on their role
            if ("Admin".equals(userType) || "Owner".equals(userType)) {
                // --- LOAD ADMIN DASHBOARD ---
                Admin admin = userDoc.toObject(Admin.class);

                FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/dtdt/DormManager/view/admin/admin-dashboard.fxml"));
                Parent root = loader.load();

                AdminDashboardController controller = loader.getController();
                controller.initData(admin);

                stage.getScene().setRoot(root);
                stage.setTitle("Admin Dashboard");
                stage.sizeToScene();
                stage.centerOnScreen();

            } else if ("Tenant".equals(userType)) {
                // --- LOAD TENANT DASHBOARD ---
                Tenant tenant = userDoc.toObject(Tenant.class);

                FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/dtdt/DormManager/view/tenant-dashboard.fxml"));
                Parent root = loader.load();

                TenantDashboardController controller = loader.getController();
                controller.initData(tenant);

                stage.getScene().setRoot(root);
                stage.setTitle("Tenant Dashboard");

            } else {
                loginErrorLabel.setText("User account is not configured correctly.");
                loginErrorLabel.setVisible(true);
            }

        } catch (Exception e) {
            System.err.println("--- LOGIN FAILED ---");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();

            loginErrorLabel.setText("Login failed: " + e.getMessage());
            loginErrorLabel.setVisible(true);
        }
    }

    /**
     * Handles the "Make a Reservation" text link click.
     */
    @FXML
    protected void goToReservation(MouseEvent event) throws IOException {
        Main main = new Main();
        main.changeScene("reservation-view.fxml");
    }

    /**
     * Handles the "Sign in" text link click (from reservation-view).
     */
    @FXML
    protected void goToSignIn(MouseEvent event) throws IOException {
        Main main = new Main();
        main.changeScene("login-view.fxml");
    }

    /**
     * Toggles password visibility for the login form.
     */
    @FXML
    protected void togglePasswordVisibilityLogin() {
        isPasswordVisibleLogin = !isPasswordVisibleLogin;
        if (isPasswordVisibleLogin) {
            passwordTextFieldLogin.setVisible(true);
            passwordFieldLogin.setVisible(false);
            togglePasswordLoginBtn.setText("\uD83D\uDE48"); // 🙈
        } else {
            passwordFieldLogin.setVisible(true);
            passwordTextFieldLogin.setVisible(false);
            togglePasswordLoginBtn.setText("\uD83D\uDC41"); // 👁
        }
    }

    // All old sign-up methods have been removed
}