package com.dtdt.DormManager.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import com.dtdt.DormManager.model.Admin;
import com.dtdt.DormManager.controller.admin.AdminDashboardController;
import com.dtdt.DormManager.model.Owner;
import com.dtdt.DormManager.model.Tenant;
import com.dtdt.DormManager.model.User;
import com.dtdt.DormManager.Main;

public class LoginController {

    // === Login View Components ===
    @FXML private TextField studentIdField;
    @FXML private TextField emailFieldLogin;
    @FXML private PasswordField passwordFieldLogin;
    @FXML private Button signInButton;

    // === Sign Up View Components ===
    @FXML private TextField fullNameField;
    @FXML private TextField studentIdFieldSignUp;
    @FXML private TextField emailFieldSignUp;
    @FXML private PasswordField passwordFieldSignUp;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> currentYearBox;
    @FXML private Button createAccountButton;

    // Gender/Sex selection for signup (only exists in signup-view.fxml)
    @FXML private ComboBox<String> sexBox;

    /**
     * This method is automatically called after the FXML is loaded.
     * We use it to populate the ComboBox.
     */
    @FXML
    public void initialize() {
        // This check is important because the ComboBox only exists in signup-view.fxml
        // It prevents errors when loading login-view.fxml
        if (currentYearBox != null) {
            currentYearBox.setItems(FXCollections.observableArrayList(
                    "1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year"
            ));
        }

        // Populate sexBox when present (signup view)
        if (sexBox != null) {
            sexBox.setItems(FXCollections.observableArrayList("Male", "Female"));
        }
    }

    /**
     * Handles the "Sign In" button click.
     */
    @FXML
    protected void onSignInClick(ActionEvent event) throws IOException {
        System.out.println("Sign In button clicked.");

        User user;
        String idInput = studentIdField.getText() == null ? "" : studentIdField.getText().trim();
        String emailInput = emailFieldLogin.getText() == null ? "" : emailFieldLogin.getText().trim();
        String pwInput = passwordFieldLogin.getText() == null ? "" : passwordFieldLogin.getText();

        if ((idInput.equalsIgnoreCase("admin") || emailInput.equalsIgnoreCase("admin@dorm.local"))
                && pwInput.equals("adminpass")) {
            user = new Admin("admin", "admin@dorm.local", "adminpass", "System Admin", "Manager");
        } else {
            user = new Tenant(
                    idInput,
                    emailInput,
                    pwInput,
                    "Tenant Name",
                    2
            );
        }

        if (user instanceof Tenant) {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/dtdt/DormManager/view/tenant-dashboard.fxml"));
            Parent root = loader.load();

            TenantDashboardController controller = loader.getController();

            controller.initData((Tenant) user);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Tenant Dashboard");

        } else if (user instanceof Admin) {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/dtdt/DormManager/view/admin/admin-dashboard.fxml"));
                Parent root = loader.load();

                AdminDashboardController controller = loader.getController();
                controller.initData((Admin) user);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.setTitle("Admin Dashboard");
        } else if (user instanceof Owner) {
        }
    }

    /**
     * Handles the "Create Account" button click.
     */
    @FXML
    protected void onCreateAccountClick() throws IOException {
        System.out.println("Create Account button clicked.");

        // 1. Check if passwords match
        if (!passwordFieldSignUp.getText().equals(confirmPasswordField.getText())) {
            System.err.println("Passwords do not match!");
            return;
        }

        // 2. Get the selected year (as an integer)
        int year = 0;
        String selectedYear = currentYearBox.getValue();
        if (selectedYear != null && !selectedYear.isEmpty()) {
            year = Integer.parseInt(selectedYear.substring(0, 1));
        }

        // NEW: read selected sex (can be null if not chosen)
        String selectedSex = null;
        if (sexBox != null) {
            selectedSex = sexBox.getValue();
        }

        // 3. Create a new Tenant object from the model
        Tenant newTenant = new Tenant(
                studentIdFieldSignUp.getText(),
                emailFieldSignUp.getText(),
                passwordFieldSignUp.getText(),
                fullNameField.getText(),
                year
        );

        // Log or store the selected sex for later integration
        System.out.println("Selected sex for new tenant: " + selectedSex);
        // TODO: Add a 'sex' field to Tenant model (and constructor or setter) and persist it to DB.
        // e.g. newTenant.setSex(selectedSex); // uncomment once model supports it

        // 4. TODO: Save this 'newTenant' object to your database
        System.out.println("New Tenant Created: " + newTenant.getFullName());

        // 5. After successful creation, automatically switch to the login screen
        goToSignIn(null);
    }

    @FXML
    protected void goToReservation(MouseEvent event) throws IOException {
        Main main = new Main();
        main.changeScene("reservation-view.fxml");
    }

    @FXML
    protected void goToSignIn(MouseEvent event) throws IOException {
        Main main = new Main();
        main.changeScene("login-view.fxml");
    }
}
