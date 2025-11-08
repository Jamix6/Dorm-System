package com.dtdt.DormManager.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

public class StatsViewController {
    @FXML private Label totalResidentsLabel;
    @FXML private Label occupancyRateLabel;
    @FXML private Label pendingMaintenanceLabel;
    @FXML private Label revenueLabel;
    @FXML private LineChart<String, Number> occupancyTrendChart;
    @FXML private BarChart<String, Number> revenueChart;

    @FXML
    public void initialize() {
        // Initialize with dummy data for now
        // In a real app, this would come from a database

        // Setup Occupancy Trend Chart
        XYChart.Series<String, Number> occupancySeries = new XYChart.Series<>();
        occupancySeries.getData().add(new XYChart.Data<>("Jan", 75));
        occupancySeries.getData().add(new XYChart.Data<>("Feb", 78));
        occupancySeries.getData().add(new XYChart.Data<>("Mar", 82));
        occupancySeries.getData().add(new XYChart.Data<>("Apr", 85));
        occupancySeries.getData().add(new XYChart.Data<>("May", 85));
        occupancySeries.getData().add(new XYChart.Data<>("Jun", 87));
        occupancyTrendChart.getData().add(occupancySeries);
        
        // Setup Revenue Chart
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.getData().add(new XYChart.Data<>("Jan", 950000));
        revenueSeries.getData().add(new XYChart.Data<>("Feb", 980000));
        revenueSeries.getData().add(new XYChart.Data<>("Mar", 1100000));
        revenueSeries.getData().add(new XYChart.Data<>("Apr", 1150000));
        revenueSeries.getData().add(new XYChart.Data<>("May", 1180000));
        revenueSeries.getData().add(new XYChart.Data<>("Jun", 1200000));
        revenueChart.getData().add(revenueSeries);
        
        // Update summary labels
        updateStats();
    }

    private void updateStats() {
        // In a real app, these would be calculated from actual data
        totalResidentsLabel.setText("247");
        occupancyRateLabel.setText("85%");
        pendingMaintenanceLabel.setText("12");
        revenueLabel.setText("₱1.2M");
    }
}