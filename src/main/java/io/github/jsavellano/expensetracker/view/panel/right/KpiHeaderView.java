package io.github.jsavellano.expensetracker.view.panel.right;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class KpiHeaderView extends GridPane {

    private final Label totalSpentVal = new Label("$0.00");
    private final Label entriesVal = new Label("0 expenses");
    private final Label avgEntryVal = new Label("$0.00");
    private final Label topCategoryVal = new Label("None");

    public KpiHeaderView() {
        setHgap(12);
        setPadding(new Insets(14, 16, 12, 16));
        getStyleClass().add("kpi-bar");

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            getColumnConstraints().add(col);
        }

        add(createKpiCard("TOTAL SPENT", totalSpentVal), 0, 0);
        add(createKpiCard("ENTRIES COUNT", entriesVal), 1, 0);
        add(createKpiCard("AVERAGE ENTRY", avgEntryVal), 2, 0);
        add(createKpiCard("TOP CATEGORY", topCategoryVal), 3, 0);
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

    public void updateMetrics(double total, int count, double avg, String topCat) {
        totalSpentVal.setText(String.format("$%.2f", total));
        entriesVal.setText(count + (count == 1 ? " entry" : " entries"));
        avgEntryVal.setText(String.format("$%.2f", avg));
        topCategoryVal.setText(topCat);
    }
}