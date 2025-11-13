package com.dtdt.DormManager.controller.admin;

import com.dtdt.DormManager.model.Building; // We need this to get a list of buildings
import com.dtdt.DormManager.model.Room;
import com.dtdt.DormManager.service.RoomStore;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import com.dtdt.DormManager.controller.config.FirebaseInit;

public class RoomsViewController {
    @FXML private FlowPane roomsContainer;
    @FXML private ComboBox<Building> buildingCombo;
    @FXML private ComboBox<String> floorCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button addRoomBtn;

    // Local cache of rooms so we can sort/filter in-memory without refetching
    private final ObservableList<Room> roomList = FXCollections.observableArrayList();

    // No programmatic toolbar anymore — we use the FXML-provided ComboBoxes

    @FXML
    public void initialize() {
        loadRooms();
        setupFilters();
    }

    private void setupFilters() {
        // When building changes, repopulate floor/type/status based on rooms in that building
        if (buildingCombo != null) {
            buildingCombo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Building item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            buildingCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Building item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });

            buildingCombo.setOnAction(e -> populateDependentFiltersAndRender());
        }

        if (floorCombo != null) floorCombo.setOnAction(e -> renderRooms());
        if (typeCombo != null) typeCombo.setOnAction(e -> renderRooms());
        if (statusCombo != null) statusCombo.setOnAction(e -> renderRooms());
    }

    private void populateDependentFiltersAndRender() {
        Building selected = buildingCombo == null ? null : buildingCombo.getValue();
        if (selected == null) {
            if (floorCombo != null) floorCombo.getItems().clear();
            if (typeCombo != null) typeCombo.getItems().clear();
            if (statusCombo != null) statusCombo.getItems().clear();
            renderRooms();
            return;
        }

        // Gather unique floors/types/status for the selected building
        var floors = FXCollections.observableArrayList(new ArrayList<String>());
        var types = FXCollections.observableArrayList(new ArrayList<String>());
        var statuses = FXCollections.observableArrayList(new ArrayList<String>());

        for (Room r : roomList) {
            if (selected.getId().equals(r.getBuildingId())) {
                String f = String.valueOf(r.getFloor());
                if (!floors.contains(f)) floors.add(f);
                if (r.getRoomType() != null && !types.contains(r.getRoomType())) types.add(r.getRoomType());
                if (r.getStatus() != null && !statuses.contains(r.getStatus())) statuses.add(r.getStatus());
            }
        }

        if (floorCombo != null) {
            floorCombo.getItems().setAll(floors);
            floorCombo.getSelectionModel().clearSelection();
            floorCombo.setPromptText("Floor");
        }
        if (typeCombo != null) {
            typeCombo.getItems().setAll(types);
            typeCombo.getSelectionModel().clearSelection();
            typeCombo.setPromptText("Type");
        }
        if (statusCombo != null) {
            statusCombo.getItems().setAll(statuses);
            statusCombo.getSelectionModel().clearSelection();
            statusCombo.setPromptText("Status");
        }

        renderRooms();
    }

    private void loadRooms() {
        // After loading rooms, we'll populate building dropdown and dependent filters
        ApiFuture<QuerySnapshot> future = FirebaseInit.db.collection("rooms").get();
        future.addListener(() -> {
            try {
                List<QueryDocumentSnapshot> documents = future.get().getDocuments();
                List<Room> fetched = new ArrayList<>();
                for (QueryDocumentSnapshot document : documents) {
                    Room room = document.toObject(Room.class);
                    // Ensure the room's id is set if model uses it (some models expect it)
                    try {
                        java.lang.reflect.Field idField = Room.class.getDeclaredField("id");
                        idField.setAccessible(true);
                        Object existing = idField.get(room);
                        if (existing == null) idField.set(room, document.getId());
                    } catch (NoSuchFieldException nsf) {
                        // ignore if no id field exists
                    } catch (Exception ex) {
                        // ignore reflection issues
                    }
                    fetched.add(room);
                }
                Platform.runLater(() -> {
                    roomList.setAll(fetched);
                    // Update central RoomStore for other views to consume
                    RoomStore.getInstance().setRooms(fetched);
                    // Populate building combo from fetched rooms (unique buildings)
                    populateBuildings();
                    renderRooms();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Runnable::run);
    }

    private void populateBuildings() {
        if (buildingCombo == null) return;
        // Unique buildings from roomList
        var buildings = FXCollections.observableArrayList(new ArrayList<Building>());
        for (Room r : roomList) {
            Building b = new Building();
            b.setId(r.getBuildingId());
            b.setName(r.getBuildingName());
            // Ensure we only add unique building ids
            boolean exists = false;
            for (Building ex : buildings) if (ex.getId().equals(b.getId())) { exists = true; break; }
            if (!exists) buildings.add(b);
        }
        buildingCombo.getItems().setAll(buildings);
        buildingCombo.getSelectionModel().clearSelection();
    }

    /**
     * Renders room cards from roomList into the roomsContainer, applying the selected sort.
     * Preserves the toolbar as the first child.
     */
    private void renderRooms() {
        // Filter rooms by building/floor/type/status selections
        Building selectedBuilding = buildingCombo == null ? null : buildingCombo.getValue();
        String selectedFloor = floorCombo == null ? null : floorCombo.getValue();
        String selectedType = typeCombo == null ? null : typeCombo.getValue();
        String selectedStatus = statusCombo == null ? null : statusCombo.getValue();

        List<Room> filtered = new ArrayList<>();
        for (Room r : roomList) {
            if (selectedBuilding != null && !Objects.equals(r.getBuildingId(), selectedBuilding.getId())) continue;
            if (selectedFloor != null && !selectedFloor.isEmpty() && !String.valueOf(r.getFloor()).equals(selectedFloor)) continue;
            if (selectedType != null && !selectedType.isEmpty() && !Objects.equals(r.getRoomType(), selectedType)) continue;
            if (selectedStatus != null && !selectedStatus.isEmpty() && !Objects.equals(r.getStatus(), selectedStatus)) continue;
            filtered.add(r);
        }

        // For now, order by room number within the filtered set
        filtered.sort(Comparator.comparing(r -> safeString(r.getRoomNumber())));

        Platform.runLater(() -> {
            roomsContainer.getChildren().clear();
            for (Room room : filtered) {
                VBox card = createRoomCard(room);
                roomsContainer.getChildren().add(card);
            }
        });
    }

    private String safeString(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    @FXML
    private void onAddRoomClick() {
        // --- 1. First, get a list of available buildings for the ComboBox ---
        ObservableList<Building> buildings = FXCollections.observableArrayList();
        ApiFuture<QuerySnapshot> buildingsFuture = com.dtdt.DormManager.controller.config.FirebaseInit.db.collection("buildings").get();
        buildingsFuture.addListener(() -> {
            try {
                for (QueryDocumentSnapshot doc : buildingsFuture.get().getDocuments()) {
                    buildings.add(doc.toObject(Building.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Runnable::run);

        // --- 2. Create the Dialog ---
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Room");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // --- 3. Create Form Fields ---
        ComboBox<Building> buildingComboBox = new ComboBox<>(buildings);
        buildingComboBox.setPromptText("Select Building");
        // Use a cell factory to show building names in the dropdown
        buildingComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Building item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
        buildingComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Building item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        TextField roomNumberField = new TextField();
        roomNumberField.setPromptText("Room Number (e.g., 101)");
        TextField floorField = new TextField();
        floorField.setPromptText("Floor (e.g., 1)");
        TextField typeField = new TextField();
        typeField.setPromptText("Room Type (e.g., Double)");
        TextField rateField = new TextField();
        rateField.setPromptText("Rate per bed (e.g., 5000)");
        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity (e.g., 2)");

        VBox content = new VBox(10,
                new Label("Building:"), buildingComboBox,
                new Label("Room Info:"), roomNumberField, floorField, typeField, rateField, capacityField
        );
        content.setStyle("-fx-padding: 10;");
        pane.setContent(content);

        // --- 4. Handle Dialog Submission ---
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Building selectedBuilding = buildingComboBox.getValue();

                Room newRoom = new Room();
                newRoom.setBuildingId(selectedBuilding.getId());
                newRoom.setBuildingName(selectedBuilding.getName()); // Denormalize name
                newRoom.setRoomNumber(roomNumberField.getText());
                newRoom.setFloor(Integer.parseInt(floorField.getText()));
                newRoom.setRoomType(typeField.getText());
                newRoom.setRate(Double.parseDouble(rateField.getText()));
                newRoom.setCapacity(Integer.parseInt(capacityField.getText()));
                newRoom.setStatus("Available"); // Default status

                // --- 5. Save to Firebase ---
                String newRoomId = UUID.randomUUID().toString();
                newRoom.setId(newRoomId); // ensure the model has its id set locally
                DocumentReference docRef = com.dtdt.DormManager.controller.config.FirebaseInit.db.collection("rooms").document(newRoomId);
                docRef.set(newRoom);

                // Add new card to UI immediately and local list
                roomList.add(newRoom);
                RoomStore.getInstance().addRoom(newRoom);
                 // After adding a room, refresh building list and dependent filters
                 populateBuildings();
                 renderRooms();

            } catch (Exception e) {
                e.printStackTrace();
                // TODO: Show an error alert
            }
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0); " +
                "-fx-background-radius: 10; -fx-padding: 15; -fx-pref-width: 280;");

        // Header with room number and status
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label roomName = new Label("Room " + room.getRoomNumber());
        roomName.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        boolean isOccupied = "Occupied".equalsIgnoreCase(room.getStatus());
        Label status = new Label(room.getStatus());
        status.setStyle(isOccupied ?
                "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-padding: 4 8; -fx-background-radius: 4;" :
                "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-padding: 4 8; -fx-background-radius: 4;");
        // TODO: Add style for "Maintenance"

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(roomName, spacer, status);

        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 10 0;");

        GridPane details = new GridPane();
        details.setVgap(8);
        details.setHgap(10);

        addDetailRow(details, 0, "Building:", room.getBuildingName());
        addDetailRow(details, 1, "Floor:", room.getFloor() + "");
        addDetailRow(details, 2, "Type:", room.getRoomType());
        addDetailRow(details, 3, "Rate:", "₱" + room.getRate() + "/mo");
        addDetailRow(details, 4, "Capacity:", "0/" + room.getCapacity()); // TODO: Track current occupancy

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setStyle("-fx-padding: 10 0 0 0;");

        Button viewDetails = new Button("View Details");
        viewDetails.setStyle("-fx-background-color: transparent; -fx-border-color: #1A1A1A; -fx-border-radius: 4;");

        MenuButton more = new MenuButton("More");
        more.setStyle("-fx-background-color: transparent;");
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> openEditRoomDialog(room));
        MenuItem deleteItem = createDeleteMenuItem(room.getId(), card);
        MenuItem statusToggle = new MenuItem(isOccupied ? "Mark as Available" : "Mark as Occupied");
        statusToggle.setOnAction(e -> {
            // Toggle status locally and in Firestore
            String newStatus = isOccupied ? "Available" : "Occupied";
            try {
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", newStatus);
                com.dtdt.DormManager.controller.config.FirebaseInit.db.collection("rooms").document(room.getId()).update(updates);
                // Update local model and RoomStore
                room.setStatus(newStatus);
                for (Room r : roomList) if (Objects.equals(r.getId(), room.getId())) r.setStatus(newStatus);
                RoomStore.getInstance().removeById(room.getId());
                RoomStore.getInstance().addRoom(room);
                renderRooms();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        more.getItems().addAll(editItem, deleteItem, statusToggle);

        actions.getChildren().addAll(viewDetails, more);
        card.getChildren().addAll(header, separator, details, actions);
        return card;
    }

    // Opens a small dialog to edit only the room type (and adjust capacity automatically)
    private void openEditRoomDialog(Room room) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Room Type");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> typeSelect = new ComboBox<>();
        typeSelect.getItems().addAll("Single", "Shared(D)", "Shared");
        typeSelect.setValue(room.getRoomType() == null ? "Single" : room.getRoomType());

        VBox content = new VBox(10, new Label("Room Type:"), typeSelect);
        content.setStyle("-fx-padding: 10;");
        pane.setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newType = typeSelect.getValue();
            int newCapacity = capacityForType(newType);

            // Prepare updates
            try {
                Map<String, Object> updates = new HashMap<>();
                updates.put("roomType", newType);
                updates.put("capacity", newCapacity);

                // Update Firestore
                com.dtdt.DormManager.controller.config.FirebaseInit.db.collection("rooms").document(room.getId()).update(updates).addListener(() -> {
                    Platform.runLater(() -> {
                        // Update local model
                        room.setRoomType(newType);
                        room.setCapacity(newCapacity);
                        // update roomList
                        for (Room r : roomList) if (Objects.equals(r.getId(), room.getId())) { r.setRoomType(newType); r.setCapacity(newCapacity); }
                        // update RoomStore
                        RoomStore.getInstance().removeById(room.getId());
                        RoomStore.getInstance().addRoom(room);
                        // Re-render UI
                        renderRooms();
                    });
                }, Runnable::run);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Map the room type to capacity according to rules: Single=1, Shared(D)=2, Shared=4
    private int capacityForType(String type) {
        if (type == null) return 1;
        switch (type) {
            case "Shared(D)": return 2;
            case "Shared": return 4;
            case "Single":
            default:
                return 1;
        }
    }

    private MenuItem createDeleteMenuItem(String documentId, Node cardToRemove) {
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            // TODO: Add "Are you sure?" confirmation

            ApiFuture<WriteResult> deleteFuture = com.dtdt.DormManager.controller.config.FirebaseInit.db.collection("rooms").document(documentId).delete();
            deleteFuture.addListener(() -> {
                Platform.runLater(() -> {
                    roomsContainer.getChildren().remove(cardToRemove);
                    // Also remove from local list
                    roomList.removeIf(r -> Objects.equals(r.getId(), documentId));
                    // Also update the central store
                    RoomStore.getInstance().removeById(documentId);
                });
            }, Runnable::run);
        });
        return deleteItem;
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-text-fill: #666;");
        Label valueNode = new Label(value);
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }
}
