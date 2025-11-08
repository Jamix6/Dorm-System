package com.dtdt.DormManager.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;

public class ResidentsViewController {
    @FXML private TableView residentsTable;

    @FXML
    public void initialize() {
        // For now we leave the table empty; a real app would populate this from a database
    }
}