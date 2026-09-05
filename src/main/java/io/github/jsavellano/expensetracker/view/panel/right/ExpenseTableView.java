package io.github.jsavellano.expensetracker.view.panel.right;

import io.github.jsavellano.expensetracker.model.Expense;
import io.github.jsavellano.expensetracker.service.ExpenseService;
import io.github.jsavellano.expensetracker.view.DialogUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ExpenseTableView extends TableView<Expense> {
    private final Runnable onDataChanged;

    public ExpenseTableView(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        buildColumns();

        SortedList<Expense> sorted = new SortedList<>(ExpenseService.getInstance().getFilteredData());
        sorted.comparatorProperty().bind(comparatorProperty());
        setItems(sorted);
    }

    private void buildColumns() {
        // 1. Date Column
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
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // 4. Amount Column
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

        // 5. Actions Column
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
                        DialogUtils.showEditExpenseDialog(expense, () -> {
                            refresh();
                            onDataChanged.run();
                        });
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
                        DialogUtils.applyDialogStylesheet(confirm);

                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.YES) {
                                ExpenseService.getInstance().deleteExpense(expense);
                                onDataChanged.run();
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonPane);
            }
        });

        getColumns().addAll(dateCol, descCol, catCol, amtCol, actionCol);
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
}