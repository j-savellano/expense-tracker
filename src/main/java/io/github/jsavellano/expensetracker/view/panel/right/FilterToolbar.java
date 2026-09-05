package io.github.jsavellano.expensetracker.view.panel.right;

import io.github.jsavellano.expensetracker.service.ExpenseService;
import io.github.jsavellano.expensetracker.service.FilterService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.time.LocalDate;

public class FilterToolbar extends HBox {
    private final Runnable onFilterApplied;

    private final TextField searchField = new TextField();
    private final ComboBox<String> filterCatCombo = new ComboBox<>();
    private final ComboBox<String> filterMonthCombo = new ComboBox<>();
    private final ComboBox<String> filterYearCombo = new ComboBox<>();

    public FilterToolbar(Runnable onFilterApplied) {
        super(10);
        this.onFilterApplied = onFilterApplied;

        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(8, 16, 8, 16));
        getStyleClass().add("toolbar-box");

        setupControls();
    }

    private void setupControls() {
        searchField.setPromptText("🔍 Search descriptions...");
        searchField.setPrefWidth(240);

        filterCatCombo.getItems().add("All Categories");
        filterCatCombo.getItems().addAll(
                "Food & Dining", "Groceries", "Transportation",
                "Housing & Bills", "Shopping", "Entertainment", "Healthcare", "Subscriptions", "Personal", "Other"
        );
        filterCatCombo.setValue("All Categories");

        filterMonthCombo.getItems().add("All Months");
        filterMonthCombo.getItems().addAll(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );
        filterMonthCombo.setValue("All Months");

        filterYearCombo.getItems().add("All Years");
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear; y >= currentYear - 5; y--) {
            filterYearCombo.getItems().add(String.valueOf(y));
        }
        filterYearCombo.setValue("All Years");

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterCatCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterMonthCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterYearCombo.valueProperty().addListener((obs, o, n) -> applyFilters());

        Button resetFiltersBtn = new Button("Reset");
        resetFiltersBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563EB; -fx-cursor: hand;");
        resetFiltersBtn.setOnAction(e -> {
            searchField.clear();
            filterCatCombo.setValue("All Categories");
            filterMonthCombo.setValue("All Months");
            filterYearCombo.setValue("All Years");
        });

        getChildren().addAll(searchField, filterCatCombo, filterMonthCombo, filterYearCombo, resetFiltersBtn);
    }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selCat = filterCatCombo.getValue();
        String selMonth = filterMonthCombo.getValue();
        String selYear = filterYearCombo.getValue();

        var predicate = FilterService.createFilterPredicate(q, selCat, selMonth, selYear);
        ExpenseService.getInstance().getFilteredData().setPredicate(predicate);

        onFilterApplied.run();
    }
}