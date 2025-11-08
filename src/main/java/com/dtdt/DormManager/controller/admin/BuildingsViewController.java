package com.dtdt.DormManager.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import java.util.Optional;

public class BuildingsViewController {
    @FXML private VBox buildingsContainer;
    // Dialog fields removed from FXML; create dialog programmatically

    @FXML
    public void initialize() {
        // Clear any example cards and load actual data
        buildingsContainer.getChildren().clear();
        loadBuildings();
    }

    @FXML
    private void onAddBuildingClick() {
        // Create a dialog dynamically to add a building
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Building");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Building Name");
        TextField floorsField = new TextField();
        floorsField.setPromptText("Number of Floors");
        TextField roomsField = new TextField();
        roomsField.setPromptText("Total Rooms");

        VBox content = new VBox(10, nameField, floorsField, roomsField);
        content.setStyle("-fx-padding: 10;");
        pane.setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = nameField.getText();
            String floors = floorsField.getText();
            String rooms = roomsField.getText();
            addBuildingCard(name, floors, rooms);
        }
    }

    private void loadBuildings() {
        // TODO: Load from database
        // For now, add some example buildings
        addBuildingCard("Building A", "4", "40");
        addBuildingCard("Building B", "4", "40");
        addBuildingCard("Building C", "3", "30");
    }

    private void addBuildingCard(String name, String floors, String totalRooms) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-padding: 20;");
        
        HBox content = new HBox();
        content.setSpacing(20);
        content.setAlignment(Pos.CENTER_LEFT);

        // Building Image (placeholder)
        ImageView buildingImage = new ImageView();
        buildingImage.setFitWidth(120);
        buildingImage.setFitHeight(120);
        buildingImage.setPreserveRatio(true);

        VBox details = new VBox();
        details.setSpacing(10);

        // Building Info
        Label buildingName = new Label(name);
        buildingName.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        Label buildingDesc = new Label(floors + "-Story Building");
        buildingDesc.setStyle("-fx-text-fill: #666;");

        // Stats in a row
        HBox stats = new HBox();
        stats.setSpacing(40);
        stats.getChildren().addAll(
            createInfoBox("Occupancy Rate", "85%"),
            createInfoBox("Total Rooms", totalRooms),
            createInfoBox("Available Rooms", "6")
        );

        details.getChildren().addAll(buildingName, buildingDesc, stats);

        // Spacer
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Actions column
        VBox actions = new VBox();
        actions.setAlignment(Pos.TOP_RIGHT);
        actions.setSpacing(10);

        Button viewButton = new Button("View Details");
        viewButton.setStyle("-fx-background-color: transparent; -fx-border-color: #1A1A1A; -fx-border-radius: 4;");
        viewButton.setOnAction(e -> onViewDetailsClick(name));

        MenuButton more = new MenuButton("More");
        more.setStyle("-fx-background-color: transparent;");
        more.getItems().addAll(
            new MenuItem("Edit"),
            new MenuItem("Delete"),
            new MenuItem("View Floor Plan")
        );

        actions.getChildren().addAll(viewButton, more);

        content.getChildren().addAll(buildingImage, details, spacer, actions);
        card.getChildren().add(content);
        
        buildingsContainer.getChildren().add(card);
    }

    private VBox createInfoBox(String title, String value) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER_RIGHT);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #666;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        
        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    private void onViewDetailsClick(String buildingName) {
        // TODO: Implement building details view
        System.out.println("View details for " + buildingName);
    }
}