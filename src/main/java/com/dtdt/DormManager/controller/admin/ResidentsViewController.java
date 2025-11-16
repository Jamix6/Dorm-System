package com.dtdt.DormManager.controller.admin;

import com.dtdt.DormManager.controller.config.FirebaseInit;
import com.dtdt.DormManager.model.Contract;
import com.dtdt.DormManager.model.Room;
import com.dtdt.DormManager.model.Tenant;
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
    private final ObservableList<Tenant> tenantList = FXCollections.observableArrayList();
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

        // --- THIS IS THE FIX for your old error ---
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

        // 4. Load all data from Firebase
        loadAllRooms();
        loadTenants();
    }

    private void loadAllRooms() {
        allRoomsList.clear();
        ApiFuture<QuerySnapshot> future = db.collection("rooms").get();
        future.addListener(() -> {
            try {
                List<QueryDocumentSnapshot> documents = future.get().getDocuments();
                for (QueryDocumentSnapshot document : documents) {
                    allRoomsList.add(document.toObject(Room.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Runnable::run);
    }

    private void loadTenants() {
        tenantList.clear();
        ApiFuture<QuerySnapshot> future = db.collection("users")
                .whereEqualTo("userType", "Tenant")
                .get();
        future.addListener(() -> {
            try {
                List<QueryDocumentSnapshot> documents = future.get().getDocuments();
                for (QueryDocumentSnapshot document : documents) {
                    tenantList.add(document.toObject(Tenant.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Platform::runLater);
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
     * and create a contract.
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

        // Tenant Name
        grid.add(new Label("Tenant:"), 0, 0);
        grid.add(new Label(tenant.getFullName()), 1, 0);

        // Room Assignment ComboBox
        grid.add(new Label("Assign Room:"), 0, 1);
        ComboBox<Room> roomComboBox = new ComboBox<>(allRoomsList);

        // This makes the ComboBox show the room number, not the object hash
        roomComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Room room) {
                return room == null ? "Select a room..." : room.getBuildingName() + " - Room " + room.getRoomNumber();
            }
            @Override
            public Room fromString(String string) { return null; }
        });

        // Find and select the tenant's current room, if they have one
        if (tenant.getRoomID() != null) {
            for (Room room : allRoomsList) {
                if (room.getId().equals(tenant.getRoomID())) {
                    roomComboBox.setValue(room);
                    break;
                }
            }
        }
        grid.add(roomComboBox, 1, 1);

        // Contract Status
        grid.add(new Label("Contract:"), 0, 2);
        Label contractStatusLabel = new Label(tenant.getContractID() != null ? "Active (ID: " + tenant.getContractID() + ")" : "Not Created");
        grid.add(contractStatusLabel, 1, 2);

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
            db.collection("users").document(tenant.getDocumentId()).update("roomID", selectedRoom.getId());
            tenant.setRoomID(selectedRoom.getId()); // Update local object

            // --- 5. Create Contract if one doesn't exist ---
            if (tenant.getContractID() == null) {
                System.out.println("Creating new contract...");
                String contractId = UUID.randomUUID().toString();
                Date dateSigned = new Date(); // Today

                // Set end date (e.g., end of November 2025)
                Calendar cal = Calendar.getInstance();
                cal.set(2025, Calendar.NOVEMBER, 30);
                Date endDate = cal.getTime();

                Contract newContract = new Contract();
                newContract.setId(contractId);
                newContract.setTenantId(tenant.getUserId());
                newContract.setRoomId(selectedRoom.getId());
                newContract.setContractType("Semesterly");
                newContract.setRentAmount(selectedRoom.getRate()); // Get rate from room
                newContract.setDateSigned(dateSigned);
                newContract.setStartDate(dateSigned);
                newContract.setEndDate(endDate);

                // Save new contract
                db.collection("contracts").document(contractId).set(newContract);

                // Update user with new contract ID
                db.collection("users").document(tenant.getDocumentId()).update("contractID", contractId);
                tenant.setContractID(contractId); // Update local object
            }

            // Refresh the table to show the new Room ID and "Active" status
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

    // This method is now handled by handleManageTenant
    private void handleViewDetails(Tenant tenant) {
        System.out.println("Viewing details for: " + tenant.getFullName());
        handleManageTenant(tenant); // Just call the main management dialog
    }
}