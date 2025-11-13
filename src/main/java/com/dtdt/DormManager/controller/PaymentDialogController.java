package com.dtdt.DormManager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PaymentDialogController {

    @FXML private TextField amountField;
    @FXML private Label selectedMethodLabel;
    @FXML private Label selectedMonthLabel;
    @FXML private TextField referenceField;
    @FXML private TextField payerNameField;

    private Stage dialogStage;
    private PaymentResult result;
    private String selectedMonth;

    @FXML
    public void initialize() {
        // Nothing to initialize here for method selection; method is provided by main view
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public PaymentResult getResult() {
        return result;
    }

    @FXML
    private void onCancel() {
        this.result = null;
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void onSubmit() {
        String amountText = amountField.getText();
        String method = (selectedMethodLabel != null) ? selectedMethodLabel.getText() : null;
        String reference = referenceField.getText();
        String payer = payerNameField.getText();
        String month = this.selectedMonth;

        // Basic validation
        if (amountText == null || amountText.trim().isEmpty()) {
            amountField.requestFocus();
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            amountField.requestFocus();
            return;
        }

        if (method == null || method.trim().isEmpty()) {
            // No method selected from main view; focus amount field instead
            amountField.requestFocus();
            return;
        }

        // Accept empty reference/payer as optional

        this.result = new PaymentResult(amount, method, reference, payer, month);
        if (dialogStage != null) dialogStage.close();
    }

    /**
     * Called by the parent controller to set which method was selected in the main view.
     */
    public void setSelectedMethod(String method) {
        if (selectedMethodLabel != null) selectedMethodLabel.setText(method);
    }
    
    /**
     * Called by the parent controller to set which month was selected.
     */
    public void setSelectedMonth(String month) {
        this.selectedMonth = month;
        if (selectedMonthLabel != null) selectedMonthLabel.setText(month);
    }

    public static class PaymentResult {
        public final double amount;
        public final String method;
        public final String reference;
        public final String payerName;
        public final String month;

        public PaymentResult(double amount, String method, String reference, String payerName, String month) {
            this.amount = amount;
            this.method = method;
            this.reference = reference;
            this.payerName = payerName;
            this.month = month;
        }

        @Override
        public String toString() {
            return "PaymentResult{" + "amount=" + amount + ", method='" + method + '\'' + ", reference='" + reference + '\'' + ", payerName='" + payerName + '\'' + ", month='" + month + '\'' + '}';
        }
    }
}
