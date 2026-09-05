package io.github.jsavellano.expensetracker;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Expense Model with JavaFX properties for binding.
 */
public class Expense {
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final DoubleProperty amount = new SimpleDoubleProperty();

    public Expense() {
    }

    public Expense(String description, String category, LocalDate date, double amount) {
        this.description.set(description);
        this.category.set(category);
        this.date.set(date);
        this.amount.set(amount);
    }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }
    public StringProperty categoryProperty() { return category; }

    public LocalDate getDate() { return date.get(); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public ObjectProperty<LocalDate> dateProperty() { return date; }

    public double getAmount() { return amount.get(); }
    public void setAmount(double amount) { this.amount.set(amount); }
    public DoubleProperty amountProperty() { return amount; }
}