package io.github.jsavellano.expensetracker.view.panel.left;

import io.github.jsavellano.expensetracker.model.Expense;
import io.github.jsavellano.expensetracker.view.DialogUtils;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;

public class LeftAddPanel extends VBox {

    private final ObservableList<Expense> masterData;
    private final Runnable onAddCallback;

    private final Label sideTotalLabel = new Label("$0.00");
    private final Label sideCountLabel = new Label("0 entries");
    private final Label sideTopCatLabel = new Label("None");

    public LeftAddPanel(ObservableList<Expense> masterData, Runnable onAddCallback) {
        super(14);
        this.masterData = masterData;
        this.onAddCallback = onAddCallback;

        setPadding(new Insets(18, 16, 18, 16));
        setPrefWidth(280);
        setMinWidth(260);
        setMaxWidth(300);
        getStyleClass().add("side-panel");

        buildUI();
    }

    private void buildUI() {
        Label headerTitle = new Label("Add Expense");
        headerTitle.getStyleClass().add("side-header-title");
        Label headerSubtitle = new Label("Record a new transaction");
        headerSubtitle.getStyleClass().add("side-header-sub");
        VBox headerBox = new VBox(2, headerTitle, headerSubtitle);

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

        Label dateLabel = new Label("Date *");
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
                DialogUtils.showAlert("Please fill in description, date, and amount.");
                return;
            }

            try {
                double amt = Double.parseDouble(amtStr);
                if (amt <= 0) {
                    DialogUtils.showAlert("Amount must be greater than zero.");
                    return;
                }

                masterData.add(0, new Expense(desc, cat, dt, amt));
                onAddCallback.run();

                descField.clear();
                amtField.clear();
                descField.requestFocus();
            } catch (NumberFormatException ex) {
                DialogUtils.showAlert("Please enter a valid numeric amount.");
            }
        });

        VBox formBox = new VBox(6, descLabel, descField, catLabel, catCombo, dateLabel, datePicker, amtLabel, amtField, addBtn);
        formBox.setPadding(new Insets(4, 0, 8, 0));

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

        getChildren().addAll(headerBox, formBox, spacer, snapshot);
    }

    public void updateSnapshot(double total, int count, String topCat) {
        sideTotalLabel.setText(String.format("$%.2f", total));
        sideCountLabel.setText(String.format("%d recorded entries", count));
        sideTopCatLabel.setText("Top: " + topCat);
    }
}
