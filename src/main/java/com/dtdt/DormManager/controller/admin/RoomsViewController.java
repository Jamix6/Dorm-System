package com.dtdt.DormManager.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Pos;

public class RoomsViewController {
    @FXML private FlowPane roomsContainer;

    @FXML
    public void initialize() {
        // Add demo rooms
        for (int i = 1; i <= 6; i++) {
            VBox card = createRoomCard(i);
            roomsContainer.getChildren().add(card);
        }
    }

    private VBox createRoomCard(int roomNum) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0); " +
                     "-fx-background-radius: 10; -fx-padding: 15; -fx-pref-width: 280;");

        // Header with room number and status
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label roomName = new Label("Room " + (100 + roomNum));
        roomName.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        Label status = new Label(roomNum % 2 == 0 ? "Occupied" : "Available");
        status.setStyle(roomNum % 2 == 0 ? 
            "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-padding: 4 8; -fx-background-radius: 4;" :
            "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-padding: 4 8; -fx-background-radius: 4;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        header.getChildren().addAll(roomName, spacer, status);

        // Separator
        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 10 0;");

        // Room details in grid
        GridPane details = new GridPane();
        details.setVgap(8);
        details.setHgap(10);

        addDetailRow(details, 0, "Building:", "Building A");
        addDetailRow(details, 1, "Floor:", (roomNum % 4 + 1) + "st Floor");
        addDetailRow(details, 2, "Type:", roomNum % 2 == 0 ? "Double" : "Single");
        addDetailRow(details, 3, "Rate:", "₱" + (roomNum % 2 == 0 ? "7,500" : "5,000") + "/mo");

        // Action buttons
        HBox actions = new HBox();
        actions.setSpacing(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setStyle("-fx-padding: 10 0 0 0;");

        Button viewDetails = new Button("View Details");
        viewDetails.setStyle("-fx-background-color: transparent; -fx-border-color: #1A1A1A; -fx-border-radius: 4;");

        MenuButton more = new MenuButton("More");
        more.setStyle("-fx-background-color: transparent;");
        more.getItems().addAll(
            new MenuItem("Edit"),
            new MenuItem("Delete"),
            new MenuItem(roomNum % 2 == 0 ? "Mark as Available" : "Mark as Occupied")
        );

        actions.getChildren().addAll(viewDetails, more);

        // Add all sections to card
        card.getChildren().addAll(header, separator, details, actions);
        return card;
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-text-fill: #666;");
        Label valueNode = new Label(value);
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }
}