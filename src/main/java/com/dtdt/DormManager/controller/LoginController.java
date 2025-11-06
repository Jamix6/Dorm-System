package com.dtdt.DormManager.controller;

import com.dtdt.DormManager.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class LoginController {

    // === Login View Components ===
    @FXML private TextField studentIdField;
    @FXML private TextField emailFieldLogin;
    @FXML private PasswordField passwordFieldLogin;
    @FXML private Button signInButton;

    // === Sign Up View Components (Add these later) ===
    // @FXML private TextField fullNameField;
    // @FXML private TextField studentIdFieldSignUp;
    // ... etc.

    /**
     * Handles the "Sign In" button click.
     * (We will add the logic later)
     */
    @FXML
    protected void onSignInClick() {
        System.out.println("Sign In button clicked.");
        System.out.println("Student ID: " + studentIdField.getText());
        System.out.println("Email: " + emailFieldLogin.getText());

        // TODO: Add authentication logic here
        // If (auth_successful) {
        //    if (user_is_admin) {
        //        main.changeScene("admin-dashboard.fxml");
        //    } else {
        //        main.changeScene("tenant-dashboard.fxml");
        //    }
        // }
    }

    /**
     * Handles the "Create Account" button click.
     * (We will add the logic later)
     */
    @FXML
    protected void onCreateAccountClick() {
        System.out.println("Create Account button clicked.");
        // TODO: Add user registration logic here
    }


    /**
     * Handles the "Sign up" text link click.
     * It switches the scene to the sign-up view.
     */
    @FXML
    protected void goToSignUp(MouseEvent event) throws IOException {
        Main main = new Main();
        main.changeScene("signup-view.fxml");
    }

    /**
     * Handles the "Sign in" text link click.
     * It switches the scene to the login view.
     */
    @FXML
    protected void goToSignIn(MouseEvent event) throws IOException {
        Main main = new Main();
        main.changeScene("login-view.fxml");
    }
}