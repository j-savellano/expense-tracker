package io.github.jsavellano.expensetracker.view;

import io.github.jsavellano.expensetracker.model.Expense;
import io.github.jsavellano.expensetracker.service.ExpenseService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.time.LocalDate;
import java.util.Locale;

public class DialogUtils {

    public static void applyDialogStylesheet(Dialog<?> dialog) {
        try {
            URL css = DialogUtils.class.getResource("styles.css");
            if (css == null) css = DialogUtils.class.getResource("/styles.css");
            if (css != null) {
                dialog.getDialogPane().getStylesheets().add(css.toExternalForm());
            }
        } catch (Exception ignored) {}
    }

    public static void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Expense Tracker");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        applyDialogStylesheet(alert);
        alert.showAndWait();
    }

    public static void showEditExpenseDialog(Expense expense, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Expense");
        dialog.setHeaderText("Update expense details");

        DialogPane dialogPane = dialog.getDialogPane();
        applyDialogStylesheet(dialog);
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

        Label l1 = new Label("Description:"); l1.getStyleClass().add("field-label");
        Label l2 = new Label("Category:");    l2.getStyleClass().add("field-label");
        Label l3 = new Label("Date:");        l3.getStyleClass().add("field-label");
        Label l4 = new Label("Amount ($):");  l4.getStyleClass().add("field-label");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        grid.add(l1, 0, 0); grid.add(descInput, 1, 0);
        grid.add(l2, 0, 1); grid.add(catInput, 1, 1);
        grid.add(l3, 0, 2); grid.add(dateInput, 1, 2);
        grid.add(l4, 0, 3); grid.add(amtInput, 1, 3);

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
                    ExpenseService.getInstance().updateExpense(
                            expense,
                            newDesc,
                            newCat,
                            newDate,
                            amtStr
                    );

                    onUpdate.run();
                } catch (NumberFormatException ex) {
                    showAlert("Please enter a valid numeric amount.");
                }
            }
        });
    }
}
