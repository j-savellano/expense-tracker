package io.github.jsavellano.expensetracker.view.panel.right;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class RightTablePanel extends VBox {

    private final KpiHeaderView kpiHeaderView;
    private final ExpenseTableView expenseTableView;

    public RightTablePanel(Runnable onDataChanged) {
        super(0);
        HBox.setHgrow(this, Priority.ALWAYS);

        this.kpiHeaderView = new KpiHeaderView();
        this.expenseTableView = new ExpenseTableView(onDataChanged);

        VBox.setVgrow(expenseTableView, Priority.ALWAYS);

        getChildren().addAll(kpiHeaderView, new FilterToolbar(onDataChanged), expenseTableView);
    }

    public void updateKpiCards(double total, int count, double avg, String topCat) {
        kpiHeaderView.updateMetrics(total, count, avg, topCat);
    }
}