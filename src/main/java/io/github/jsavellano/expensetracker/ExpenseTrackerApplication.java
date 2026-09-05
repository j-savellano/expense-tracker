package io.github.jsavellano.expensetracker;

import io.github.jsavellano.expensetracker.model.Expense;
import io.github.jsavellano.expensetracker.view.panel.left.LeftAddPanel;
import io.github.jsavellano.expensetracker.view.panel.right.RightTablePanel;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ExpenseTrackerApplication extends Application {

    private final ObservableList<Expense> masterData = FXCollections.observableArrayList();
    private FilteredList<Expense> filteredData;

    private LeftAddPanel leftPanel;
    private RightTablePanel rightPanel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Personal Expense Tracker");

        // Initial Sample Data
        masterData.addAll(
                new Expense("Groceries & organic produce", "Groceries", LocalDate.now(), 68.45),
                new Expense("Espresso & pastry with colleague", "Food & Dining", LocalDate.now().minusDays(1), 11.50),
                new Expense("Monthly transit pass reload", "Transportation", LocalDate.now().minusDays(2), 85.00),
                new Expense("Gigabit fiber internet bill", "Housing & Bills", LocalDate.now().minusDays(4), 70.00),
                new Expense("Wireless desk keyboard", "Shopping", LocalDate.now().minusDays(6), 99.00),
                new Expense("Cinema tickets & snacks", "Entertainment", LocalDate.now().minusDays(9), 36.50)
        );

        filteredData = new FilteredList<>(masterData, p -> true);

        // UI Panels
        leftPanel = new LeftAddPanel(masterData, this::updateAnalytics);
        rightPanel = new RightTablePanel(masterData, filteredData, this::updateAnalytics);

        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setCenter(rightPanel);
        root.getStyleClass().add("root-pane");

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

        try {
            URL css = getClass().getResource("styles.css");
            if (css == null) css = getClass().getResource("/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {}

        primaryStage.setScene(scene);
        updateAnalytics();

        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
        primaryStage.setResizable(false);

        primaryStage.show();
    }

    private void updateAnalytics() {
        double total = 0.0;
        int count = filteredData.size();
        Map<String, Double> catMap = new HashMap<>();

        for (Expense exp : filteredData) {
            total += exp.getAmount();
            catMap.put(exp.getCategory(), catMap.getOrDefault(exp.getCategory(), 0.0) + exp.getAmount());
        }

        double avg = count > 0 ? total / count : 0.0;

        String top = "None";
        double topAmt = 0.0;
        for (var e : catMap.entrySet()) {
            if (e.getValue() > topAmt) {
                topAmt = e.getValue();
                top = e.getKey();
            }
        }

        leftPanel.updateSnapshot(total, count, top);
        rightPanel.updateKpiCards(total, count, avg, top);
    }

    public static void main(String[] args) {
        launch(args);
    }
}