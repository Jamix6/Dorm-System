package com.dtdt.DormManager.service;

import com.dtdt.DormManager.model.Room;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RoomStore {
    private static RoomStore instance;
    private final ObservableList<Room> rooms = FXCollections.observableArrayList();

    private RoomStore() {}

    public static synchronized RoomStore getInstance() {
        if (instance == null) instance = new RoomStore();
        return instance;
    }

    public ObservableList<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> newRooms) {
        Platform.runLater(() -> {
            rooms.setAll(newRooms == null ? List.of() : newRooms);
        });
    }

    public void addRoom(Room room) {
        if (room == null) return;
        Platform.runLater(() -> rooms.add(room));
    }

    public void removeById(String id) {
        if (id == null) return;
        Platform.runLater(() -> rooms.removeIf(r -> Objects.equals(r.getId(), id)));
    }

    public List<Room> getRoomsByBuilding(String buildingId) {
        if (buildingId == null) return new ArrayList<>();
        List<Room> result = new ArrayList<>();
        for (Room r : rooms) {
            if (Objects.equals(buildingId, r.getBuildingId())) result.add(r);
        }
        return result;
    }
}

