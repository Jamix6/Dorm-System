package com.dtdt.DormManager.controller.admin;

import com.dtdt.DormManager.controller.config.FirebaseInit;
import com.dtdt.DormManager.model.Contract;
import com.dtdt.DormManager.model.Room;
import com.dtdt.DormManager.model.Tenant;
import com.dtdt.DormManager.service.RoomStore;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import java.time.ZoneId;

import java.text.SimpleDateFormat;
import java.util.*;

public class ResidentsViewController {

    // --- FXML Components ---
    @FXML private TextField searchField;
    @FXML private Button addResidentButton;
    @FXML private ComboBox<String> buildingFilterBox;
    @FXML private ComboBox<String> floorFilterBox;
    @FXML private ComboBox<String> roomTypeFilterBox;
    @FXML private ComboBox<String> sortBox;

    @FXML private TableView<Tenant> residentsTable;
    @FXML private TableColumn<Tenant, String> nameColumn;
    @FXML private TableColumn<Tenant, String> studentIdColumn;
    @FXML private TableColumn<Tenant, String> roomColumn;
    @FXML private TableColumn<Tenant, String> contactColumn;
    @FXML private TableColumn<Tenant, String> statusColumn;
    @FXML private TableColumn<Tenant, Void> actionsColumn;

    // --- Data ---
    private final ObservableList<Tenant> tenantList = FXCollections.observableArrayList(); // For the table
    private final ObservableList<Tenant> allTenantsList = FXCollections.observableArrayList(); // Master list
    private final ObservableList<Room> allRoomsList = FXCollections.observableArrayList(); // Cache for rooms
    private final Firestore db = FirebaseInit.db;

    @FXML
    public void initialize() {
        // 1. Set up the table columns
        nameColumn.setCellValueFactory(cellData -> {
            Tenant tenant = cellData.getValue();
            String name = tenant.getFullName();
            if (name == null || name.trim().isEmpty()) {
                name = (tenant.getFirstName() != null ? tenant.getFirstName() : "") + " " +
                        (tenant.getLastName() != null ? tenant.getLastName() : "");
            }
            return new SimpleStringProperty(name.trim());
        });

        studentIdColumn.setCellValueFactory(cellData -> {
            Tenant tenant = cellData.getValue();
            String id = tenant.getUserId();
            if (id == null || id.trim().isEmpty()) {
                id = tenant.getStudentID();
            }
            return new SimpleStringProperty(id);
        });

        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomID"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getContractID() != null ? "Active" : "No Contract";
            return new SimpleStringProperty(status);
        });

        // 2. Set up the custom "Actions" column
        setupActionsColumn();

        // 3. Set the table's data source
        residentsTable.setItems(tenantList);

        // 4. Set up listeners for filters
        setupFilterListeners();

        // 5. Load all data from Firebase
        loadAllData();
    }

    /**
     * Sets up listeners for all filter components.
     */
    private void setupFilterListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderTenants());
        buildingFilterBox.setOnAction(e -> renderTenants());
        floorFilterBox.setOnAction(e -> renderTenants());
        roomTypeFilterBox.setOnAction(e -> renderTenants());
        // sortBox.setOnAction(e -> renderTenants()); // Add sorting logic later
    }

    /**
     * Loads all rooms, then all tenants, then populates filters and renders the table.
     */
    private void loadAllData() {
        allRoomsList.clear();
        allTenantsList.clear();

        // 1. Load Rooms first
        ApiFuture<QuerySnapshot> roomsFuture = db.collection("rooms").get();
        roomsFuture.addListener(() -> {
            try {
                for (QueryDocumentSnapshot document : roomsFuture.get().getDocuments()) {
                    allRoomsList.add(document.toObject(Room.class));
                }

                // 2. Now load Tenants
                ApiFuture<QuerySnapshot> tenantsFuture = db.collection("users").whereEqualTo("userType", "Tenant").get();
                tenantsFuture.addListener(() -> {
                    try {
                        for (QueryDocumentSnapshot document : tenantsFuture.get().getDocuments()) {
                            allTenantsList.add(document.toObject(Tenant.class));
                        }

                        // 3. Now that all data is loaded, update the UI
                        Platform.runLater(() -> {
                            populateFilters();
                            renderTenants();
                        });

                    } catch (Exception e) { e.printStackTrace(); }
                }, Runnable::run);

            } catch (Exception e) { e.printStackTrace(); }
        }, Runnable::run);
    }

    /**
     * Populates the filter ComboBoxes with data from allRoomsList.
     */
    private void populateFilters() {
        ObservableList<String> buildings = FXCollections.observableArrayList("All Buildings");
        ObservableList<String> floors = FXCollections.observableArrayList("All Floors");
        ObservableList<String> types = FXCollections.observableArrayList("All Types");

        for (Room room : allRoomsList) {
            if (room.getBuildingName() != null && !buildings.contains(room.getBuildingName())) {
                buildings.add(room.getBuildingName());
            }
            String floorStr = String.valueOf(room.getFloor());
            if (!floors.contains(floorStr)) {
                floors.add(floorStr);
            }
            if (room.getRoomType() != null && !types.contains(room.getRoomType())) {
                types.add(room.getRoomType());
            }
        }

        buildingFilterBox.setItems(buildings);
        floorFilterBox.setItems(floors);
        roomTypeFilterBox.setItems(types);

        buildingFilterBox.setValue("All Buildings");
        floorFilterBox.setValue("All Floors");
        roomTypeFilterBox.setValue("All Types");
    }

    /**
     * Filters the 'allTenantsList' into the 'tenantList' based on filter selections.
     */
    private void renderTenants() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedBuilding = buildingFilterBox.getValue();
        String selectedFloor = floorFilterBox.getValue();
        String selectedType = roomTypeFilterBox.getValue();

        ObservableList<Tenant> filteredList = FXCollections.observableArrayList();

        for (Tenant tenant : allTenantsList) {
            // 1. Check Search Filter
            boolean matchesSearch = true;
            if (!searchText.isEmpty()) {
                String fullName = (tenant.getFirstName() + " " + tenant.getLastName()).toLowerCase();
                String userId = (tenant.getUserId() != null ? tenant.getUserId() : "").toLowerCase();
                String email = (tenant.getEmail() != null ? tenant.getEmail() : "").toLowerCase();

                matchesSearch = fullName.contains(searchText) ||
                        userId.contains(searchText) ||
                        email.contains(searchText);
            }
            if (!matchesSearch) continue; // Skip if no match

            // 2. Check Room-Based Filters
            Room tenantRoom = getRoomForTenant(tenant.getRoomID());

            // Building Filter
            if (selectedBuilding != null && !selectedBuilding.equals("All Buildings")) {
                if (tenantRoom == null || !selectedBuilding.equals(tenantRoom.getBuildingName())) {
                    continue; // Skip if no room or room doesn't match
                }
            }

            // Floor Filter
            if (selectedFloor != null && !selectedFloor.equals("All Floors")) {
                if (tenantRoom == null || !selectedFloor.equals(String.valueOf(tenantRoom.getFloor()))) {
                    continue;
                }
            }

            // Type Filter
            if (selectedType != null && !selectedType.equals("All Types")) {
                if (tenantRoom == null || !selectedType.equals(tenantRoom.getRoomType())) {
                    continue;
                }
            }

            // If all checks pass, add the tenant
            filteredList.add(tenant);
        }

        // (Sorting can be added here)

        tenantList.setAll(filteredList);
        residentsTable.refresh();
    }

    /**
     * Helper method to find a Room object from the local list by its ID.
     */
    private Room getRoomForTenant(String roomId) {
        if (roomId == null) return null;
        for (Room room : allRoomsList) {
            if (roomId.equals(room.getId())) {
                return room;
            }
        }
        return null; // No matching room found in cache
    }

    @FXML
    private void onAddResidentClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Resident");
        alert.setHeaderText("Please use the 'Reservations' tab");
        alert.setContentText("You can approve a reservation to add a new resident.");
        alert.showAndWait();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button manageButton = new Button("Manage");
            private final HBox pane = new HBox(manageButton);

            {
                pane.setSpacing(10);
                manageButton.setOnAction(event -> {
                    Tenant tenant = getTableView().getItems().get(getIndex());
                    handleManageTenant(tenant);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    /**
     * Replaces handleViewDetails. This opens a dialog to assign a room
     * and create/update a contract.
     */
    private void handleManageTenant(Tenant tenant) {
        System.out.println("Managing: " + tenant.getFullName());

        // --- 1. Create the Dialog ---
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Resident");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // --- 2. Create Form Content ---
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Tenant:"), 0, 0);
        grid.add(new Label(tenant.getFullName()), 1, 0);

        // --- FIX: Filter the list to only show AVAILABLE rooms ---
        ObservableList<Room> availableRooms = FXCollections.observableArrayList();
        for (Room room : allRoomsList) {
            // Check if room is "Available" AND not full
            int occupancy = getOccupancyForRoom(room.getId());
            if ("Available".equalsIgnoreCase(room.getStatus()) && occupancy < room.getCapacity()) {
                availableRooms.add(room);
            }
        }

        // Add the tenant's current room to the list if they already have one
        if (tenant.getRoomID() != null) {
            Room currentRoom = getRoomForTenant(tenant.getRoomID()); // Use your existing helper
            if (currentRoom != null && !availableRooms.contains(currentRoom)) {
                availableRooms.add(currentRoom); // Add their current room
            }
        }

        grid.add(new Label("Assign Room:"), 0, 1);
        ComboBox<Room> roomComboBox = new ComboBox<>(availableRooms); // Use the filtered list
        // --- END FIX ---

        roomComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Room room) {
                if (room == null) return "Select a room...";
                int occupancy = getOccupancyForRoom(room.getId());
                return String.format("%s - Room %s (%d/%d)",
                        room.getBuildingName(), room.getRoomNumber(), occupancy, room.getCapacity());
            }
            @Override
            public Room fromString(String string) { return null; }
        });

        if (tenant.getRoomID() != null) {
            for (Room room : allRoomsList) { // Check all rooms, not just available
                if (room.getId().equals(tenant.getRoomID())) {
                    roomComboBox.setValue(room);
                    break;
                }
            }
        }
        grid.add(roomComboBox, 1, 1);

        grid.add(new Label("Contract:"), 0, 2);
        Label contractStatusLabel = new Label(tenant.getContractID() != null ? "Active (ID: " + tenant.getContractID() + ")" : "Not Created");
        grid.add(contractStatusLabel, 1, 2);

        grid.add(new Label("Contract End Date:"), 0, 3);
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Select end date");

        if (tenant.getContractID() != null) {
            endDatePicker.setDisable(true);
            endDatePicker.setPromptText("Contract already active");
            // (You can add the logic here to fetch and show the date)
        }
        grid.add(endDatePicker, 1, 3);

        pane.setContent(grid);

        // --- 3. Handle Dialog Submission ---
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Room selectedRoom = roomComboBox.getValue();
            if (selectedRoom == null) {
                showError("No Room Selected", "You must select a room.");
                return;
            }

            // --- 4. Update Tenant's Room in DB ---
            if (!Objects.equals(tenant.getRoomID(), selectedRoom.getId())) {
                db.collection("users").document(tenant.getDocumentId()).update("roomID", selectedRoom.getId());
                tenant.setRoomID(selectedRoom.getId());
                System.out.println("Tenant room updated to: " + selectedRoom.getRoomNumber());
            }

            // --- 5. Create Contract if one doesn't exist ---
            if (tenant.getContractID() == null) {
                // (All your contract creation logic...)
                if (endDatePicker.getValue() == null) {
                    showError("No End Date", "You must select a contract end date.");
                    return;
                }
                String contractId = UUID.randomUUID().toString();
                Date dateSigned = new Date();
                Date endDate = Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Contract newContract = new Contract();
                newContract.setId(contractId);
                newContract.setTenantId(tenant.getUserId());
                newContract.setRoomId(selectedRoom.getId());
                newContract.setContractType("Semesterly");
                newContract.setRentAmount(selectedRoom.getRate());
                newContract.setDateSigned(dateSigned);
                newContract.setStartDate(dateSigned);
                newContract.setEndDate(endDate);
                db.collection("contracts").document(contractId).set(newContract);
                db.collection("users").document(tenant.getDocumentId()).update("contractID", contractId);
                tenant.setContractID(contractId);
            }

            // --- FIX: Check room status after assignment ---
            updateRoomStatusAfterAssignment(selectedRoom);
            // --- END FIX ---

            residentsTable.refresh();
        }
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void handleViewDetails(Tenant tenant) {
        System.out.println("Viewing details for: " + tenant.getFullName());
        handleManageTenant(tenant);
    }

    // Inside ResidentsViewController.java

    /**
     * Helper method to get the current occupancy of a room.
     * @param roomId The ID of the room to check.
     * @return The number of tenants currently assigned to that room.
     */
    private int getOccupancyForRoom(String roomId) {
        if (roomId == null) return 0;
        int count = 0;
        for (Tenant tenant : allTenantsList) {
            if (roomId.equals(tenant.getRoomID())) {
                count++;
            }
        }
        return count;
    }

    private void updateRoomStatusAfterAssignment(Room room) {
        // Get the new occupancy count
        int currentOccupancy = getOccupancyForRoom(room.getId());

        if (currentOccupancy >= room.getCapacity()) {
            System.out.println("Room " + room.getRoomNumber() + " is now full. Setting status to Occupied.");

            // Update in Firebase
            db.collection("rooms").document(room.getId()).update("status", "Occupied");

            // Update in our local lists
            room.setStatus("Occupied");
            RoomStore.getInstance().removeById(room.getId()); // Remove old version
            RoomStore.getInstance().addRoom(room); // Add new version
        }
    }
}