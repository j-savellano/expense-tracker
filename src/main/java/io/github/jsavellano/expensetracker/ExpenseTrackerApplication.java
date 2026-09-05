package io.github.jsavellano.expensetracker;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Modern macOS Two-Pane Personal Expense Tracker in JavaFX.
 * Left Panel: Vertical Add Expense Form & Quick Snapshot
 * Right Panel: KPI Summary Cards, Search Toolbar, and TableView
 */
public class ExpenseTrackerApplication extends Application {

    private final ObservableList<Expense> masterData = FXCollections.observableArrayList();
    private FilteredList<Expense> filteredData;
    private final TableView<Expense> table = new TableView<>();

    // Snapshot Labels in Left Panel
    private final Label sideTotalLabel = new Label("$0.00");
    private final Label sideCountLabel = new Label("0 entries");
    private final Label sideTopCatLabel = new Label("None");

    // KPI Card Labels in Right Panel
    private final Label totalSpentVal = new Label("$0.00");
    private final Label entriesVal = new Label("0 expenses");
    private final Label avgEntryVal = new Label("$0.00");
    private final Label topCategoryVal = new Label("None");

    // Filter controls
    private final TextField searchField = new TextField();
    private final ComboBox<String> filterCatCombo = new ComboBox<>();

    public ExpenseTrackerApplication() {}

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

        // 1. Left Vertical Add Expense Panel
        VBox leftPanel = createLeftAddPanel();

        // 2. Right Table & Controls Panel
        VBox rightPanel = createRightTablePanel();

        // Main Two-Pane Split Layout
        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setCenter(rightPanel);
        root.getStyleClass().add("root-pane");

        Scene scene = new Scene(root, 980, 640);

        // Load styles.css
        try {
            URL css = getClass().getResource("styles.css");
            if (css == null) css = getClass().getResource("/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {}

        primaryStage.setScene(scene);
        updateAnalytics();
        primaryStage.show();
    }

    /**
     * Left Panel: Vertical Add Expense Form & Snapshot
     */
    private VBox createLeftAddPanel() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(18, 16, 18, 16));
        panel.setPrefWidth(280);
        panel.setMinWidth(260);
        panel.setMaxWidth(300);
        panel.getStyleClass().add("side-panel");

        Label headerTitle = new Label("Add Expense");
        headerTitle.getStyleClass().add("side-header-title");
        Label headerSubtitle = new Label("Record a new transaction");
        headerSubtitle.getStyleClass().add("side-header-sub");
        VBox headerBox = new VBox(2, headerTitle, headerSubtitle);

        // Form Fields (Vertical Stack)
        Label descLabel = new Label("Description *");
        descLabel.getStyleClass().add("field-label");
        TextField descField = new TextField();
        descField.setPromptText("e.g. Grocery shopping, Metro");

        Label catLabel = new Label("Category *");
        catLabel.getStyleClass().add("field-label");
        ComboBox<String> catCombo = new ComboBox<>();
        catCombo.getItems().addAll(
                "Food & Dining", "Groceries", "Transportation",
                "Housing & Bills", "Shopping", "Entertainment", "Healthcare", "Subscriptions", "Personal", "Other"
        );
        catCombo.setValue("Food & Dining");
        catCombo.setMaxWidth(Double.MAX_VALUE);

        Label dateLabel = new Label("Date * (without time)");
        dateLabel.getStyleClass().add("field-label");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        Label amtLabel = new Label("Amount ($) *");
        amtLabel.getStyleClass().add("field-label");
        TextField amtField = new TextField();
        amtField.setPromptText("0.00");

        Button addBtn = new Button("+ Add Expense");
        addBtn.getStyleClass().add("primary-add-button");
        addBtn.setMaxWidth(Double.MAX_VALUE);

        addBtn.setOnAction(e -> {
            String desc = descField.getText().trim();
            String cat = catCombo.getValue();
            LocalDate dt = datePicker.getValue();
            String amtStr = amtField.getText().trim();

            if (desc.isEmpty() || dt == null || amtStr.isEmpty()) {
                showAlert("Please fill in description, date, and amount.");
                return;
            }

            try {
                double amt = Double.parseDouble(amtStr);
                if (amt <= 0) {
                    showAlert("Amount must be greater than zero.");
                    return;
                }

                masterData.add(0, new Expense(desc, cat, dt, amt));
                updateAnalytics();

                descField.clear();
                amtField.clear();
                descField.requestFocus();
            } catch (NumberFormatException ex) {
                showAlert("Please enter a valid numeric amount.");
            }
        });

        VBox formBox = new VBox(6, descLabel, descField, catLabel, catCombo, dateLabel, datePicker, amtLabel, amtField, addBtn);
        formBox.setPadding(new Insets(4, 0, 8, 0));

        // Snapshot Card
        VBox snapshot = new VBox(8);
        snapshot.getStyleClass().add("snapshot-box");
        snapshot.setPadding(new Insets(12));

        Label snapTitle = new Label("SPENDING SNAPSHOT");
        snapTitle.getStyleClass().add("snapshot-title");

        sideTotalLabel.getStyleClass().add("snapshot-total");
        sideCountLabel.getStyleClass().add("snapshot-sub");
        sideTopCatLabel.getStyleClass().add("snapshot-sub");

        snapshot.getChildren().addAll(snapTitle, sideTotalLabel, sideCountLabel, sideTopCatLabel);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        panel.getChildren().addAll(headerBox, formBox, spacer, snapshot);
        return panel;
    }

    /**
     * Right Panel: Summary KPI Cards + Filter Toolbar + TableView
     */
    private VBox createRightTablePanel() {
        VBox rightPanel = new VBox(0);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        // 1. Top KPI Summary Grid
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(12);
        kpiGrid.setPadding(new Insets(14, 16, 12, 16));
        kpiGrid.getStyleClass().add("kpi-bar");

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            kpiGrid.getColumnConstraints().add(col);
        }

        kpiGrid.add(createKpiCard("TOTAL SPENT", totalSpentVal), 0, 0);
        kpiGrid.add(createKpiCard("ENTRIES COUNT", entriesVal), 1, 0);
        kpiGrid.add(createKpiCard("AVERAGE ENTRY", avgEntryVal), 2, 0);
        kpiGrid.add(createKpiCard("TOP CATEGORY", topCategoryVal), 3, 0);

        // 2. Filter Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(8, 16, 8, 16));
        toolbar.getStyleClass().add("toolbar-box");

        searchField.setPromptText("🔍 Search descriptions...");
        searchField.setPrefWidth(240);

        filterCatCombo.getItems().add("All Categories");
        filterCatCombo.getItems().addAll(
                "Food & Dining", "Groceries", "Transportation",
                "Housing & Bills", "Shopping", "Entertainment", "Healthcare", "Subscriptions", "Personal", "Other"
        );
        filterCatCombo.setValue("All Categories");

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterCatCombo.valueProperty().addListener((obs, o, n) -> applyFilters());

        Button resetFiltersBtn = new Button("Reset");
        resetFiltersBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563EB; -fx-cursor: hand;");
        resetFiltersBtn.setOnAction(e -> {
            searchField.clear();
            filterCatCombo.setValue("All Categories");
        });

        toolbar.getChildren().addAll(searchField, filterCatCombo, resetFiltersBtn);

        // 3. TableView Setup
        setupTableView();

        rightPanel.getChildren().addAll(kpiGrid, toolbar, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return rightPanel;
    }

    private HBox createKpiCard(String title, Label valLabel) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.getStyleClass().add("kpi-card");

        VBox text = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
        valLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        text.getChildren().addAll(t, valLabel);

        card.getChildren().add(text);
        return card;
    }

    private void setupTableView() {
        // 1. Date Column (Centered, without time)
        TableColumn<Expense, String> dateCol = new TableColumn<>("DATE");
        dateCol.setPrefWidth(115);
        dateCol.setStyle("-fx-alignment: CENTER;");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        dateCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getDate().format(dtf)));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String dateStr, boolean empty) {
                super.updateItem(dateStr, empty);
                if (empty || dateStr == null) {
                    setText(null);
                } else {
                    setText(dateStr);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-alignment: CENTER; -fx-font-family: monospace; -fx-text-fill: #4B5563;");
                }
            }
        });

        // 2. Description Column
        TableColumn<Expense, String> descCol = new TableColumn<>("DESCRIPTION");
        descCol.setPrefWidth(240);
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // 3. Category Column
        TableColumn<Expense, String> catCol = new TableColumn<>("CATEGORY");
        catCol.setPrefWidth(150);
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String cat, boolean empty) {
                super.updateItem(cat, empty);
                if (empty || cat == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(cat);
                    badge.setPadding(new Insets(2, 8, 2, 8));
                    badge.setStyle(getBadgeStyle(cat));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // 4. Amount Column (Centered)
        TableColumn<Expense, Double> amtCol = new TableColumn<>("AMOUNT");
        amtCol.setPrefWidth(110);
        amtCol.setStyle("-fx-alignment: CENTER;");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amtCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amt, boolean empty) {
                super.updateItem(amt, empty);
                if (empty || amt == null) {
                    setText(null);
                } else {
                    setText(String.format(Locale.US, "$%.2f", amt));
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-family: monospace;");
                }
            }
        });

        // 5. Actions Column (Beside Amount with Edit & Delete buttons)
        TableColumn<Expense, Void> actionCol = new TableColumn<>("ACTIONS");
        actionCol.setPrefWidth(140);
        actionCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttonPane = new HBox(6, editBtn, deleteBtn);

            {
                buttonPane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("table-edit-btn");
                deleteBtn.getStyleClass().add("table-delete-btn");

                editBtn.setOnAction(e -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    if (expense != null) {
                        showEditExpenseDialog(expense);
                    }
                });

                deleteBtn.setOnAction(e -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    if (expense != null) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                                "Are you sure you want to delete " + expense.getDescription() + "?",
                                ButtonType.YES, ButtonType.NO);
                        confirm.setTitle("Delete Expense");
                        confirm.setHeaderText(null);
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.YES) {
                                masterData.remove(expense);
                                updateAnalytics();
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonPane);
                }
            }
        });

        table.getColumns().addAll(dateCol, descCol, catCol, amtCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        SortedList<Expense> sorted = new SortedList<>(filteredData);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
    }

    /**
     * Dialog to edit an existing expense in JavaFX
     */
    private void showEditExpenseDialog(Expense expense) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Expense");
        dialog.setHeaderText("Update expense details");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField descInput = new TextField(expense.getDescription());
        ComboBox<String> catInput = new ComboBox<>();
        catInput.getItems().addAll(
                "Food & Dining", "Groceries", "Transportation",
                "Housing & Bills", "Shopping", "Entertainment", "Healthcare", "Subscriptions", "Personal", "Other"
        );
        catInput.setValue(expense.getCategory());
        catInput.setMaxWidth(Double.MAX_VALUE);

        DatePicker dateInput = new DatePicker(expense.getDate());
        dateInput.setMaxWidth(Double.MAX_VALUE);

        TextField amtInput = new TextField(String.format(Locale.US, "%.2f", expense.getAmount()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Description:"), 0, 0);
        grid.add(descInput, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(catInput, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(dateInput, 1, 2);
        grid.add(new Label("Amount ($):"), 0, 3);
        grid.add(amtInput, 1, 3);

        dialogPane.setContent(grid);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String newDesc = descInput.getText().trim();
                String newCat = catInput.getValue();
                LocalDate newDate = dateInput.getValue();
                String amtStr = amtInput.getText().trim();

                if (newDesc.isEmpty() || newDate == null || amtStr.isEmpty()) {
                    showAlert("All fields are required.");
                    return;
                }

                try {
                    double newAmt = Double.parseDouble(amtStr);
                    if (newAmt <= 0) {
                        showAlert("Amount must be greater than zero.");
                        return;
                    }

                    expense.setDescription(newDesc);
                    expense.setCategory(newCat);
                    expense.setDate(newDate);
                    expense.setAmount(newAmt);

                    table.refresh();
                    updateAnalytics();
                } catch (NumberFormatException ex) {
                    showAlert("Please enter a valid numeric amount.");
                }
            }
        });
    }

    private String getBadgeStyle(String category) {
        String bg = "#F3F4F6", fg = "#374151", border = "#E5E7EB";
        if ("Food & Dining".equals(category)) { bg = "#FFF7ED"; fg = "#C2410C"; border = "#FED7AA"; }
        else if ("Groceries".equals(category)) { bg = "#ECFDF5"; fg = "#047857"; border = "#A7F3D0"; }
        else if ("Transportation".equals(category)) { bg = "#F0F9FF"; fg = "#0369A1"; border = "#BAE6FD"; }
        else if ("Housing & Bills".equals(category)) { bg = "#EEF2FF"; fg = "#4338CA"; border = "#C7D2FE"; }
        else if ("Shopping".equals(category)) { bg = "#FDF2F8"; fg = "#BE185D"; border = "#FBCFE8"; }
        else if ("Entertainment".equals(category)) { bg = "#FAF5FF"; fg = "#6D28D9"; border = "#DDD6FE"; }
        return String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-border-color: %s; " +
                "-fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-size: 11px; -fx-font-weight: bold;", bg, fg, border);
    }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selCat = filterCatCombo.getValue();

        filteredData.setPredicate(exp -> {
            boolean matchQ = q.isEmpty() || exp.getDescription().toLowerCase().contains(q) || exp.getCategory().toLowerCase().contains(q);
            boolean matchCat = selCat == null || "All Categories".equals(selCat) || exp.getCategory().equalsIgnoreCase(selCat);
            return matchQ && matchCat;
        });
        updateAnalytics();
    }

    private void updateAnalytics() {
        double total = 0.0;
        int count = filteredData.size();
        Map<String, Double> catMap = new HashMap<>();

        for (Expense exp : filteredData) {
            total += exp.getAmount();
            catMap.put(exp.getCategory(), catMap.getOrDefault(exp.getCategory(), 0.0) + exp.getAmount());
        }

        totalSpentVal.setText(String.format("$%.2f", total));
        entriesVal.setText(count + (count == 1 ? " entry" : " entries"));
        avgEntryVal.setText(String.format("$%.2f", count > 0 ? total / count : 0.0));

        String top = "None";
        double topAmt = 0.0;
        for (var e : catMap.entrySet()) {
            if (e.getValue() > topAmt) {
                topAmt = e.getValue();
                top = e.getKey();
            }
        }
        topCategoryVal.setText(top);

        // Sidebar stats
        sideTotalLabel.setText(String.format("$%.2f", total));
        sideCountLabel.setText(String.format("%d recorded entries", count));
        sideTopCatLabel.setText("Top: " + top);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Expense Tracker");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}