package io.github.jsavellano.expensetracker.view.panel.right;

import io.github.jsavellano.expensetracker.model.Expense;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class RightTablePanel extends VBox {

    private final KpiHeaderView kpiHeaderView;
    private final FilterToolbar filterToolbar;
    private final ExpenseTableView expenseTableView;

    public RightTablePanel(ObservableList<Expense> masterData, FilteredList<Expense> filteredData, Runnable onDataChanged) {
        super(0);
        HBox.setHgrow(this, Priority.ALWAYS);

        this.kpiHeaderView = new KpiHeaderView();
        this.filterToolbar = new FilterToolbar(filteredData, onDataChanged);
        this.expenseTableView = new ExpenseTableView(masterData, filteredData, onDataChanged);

        VBox.setVgrow(expenseTableView, Priority.ALWAYS);

        getChildren().addAll(kpiHeaderView, filterToolbar, expenseTableView);
    }

    public void updateKpiCards(double total, int count, double avg, String topCat) {
        kpiHeaderView.updateMetrics(total, count, avg, topCat);
    }
}