package io.github.jsavellano.expensetracker.service;

import io.github.jsavellano.expensetracker.model.Expense;
import io.github.jsavellano.expensetracker.repository.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseService {

    private static ExpenseService instance;

    private ExpenseService() {}

    public static synchronized ExpenseService getInstance() {
        if(instance == null) {
            instance = new ExpenseService();
        }
        return instance;
    }

    private final ObservableList<Expense> masterData = FXCollections.observableArrayList();
    private final ObservableList<Expense> readOnlyMasterData = FXCollections.unmodifiableObservableList(masterData);
    private final FilteredList<Expense> filteredData = new FilteredList<>(readOnlyMasterData, p -> true);

    public void initialize() {
        DatabaseManager.initializeDatabase();
        List<Expense> loaded = DatabaseManager.loadAllExpenses();

        if (loaded.isEmpty()) {
            loaded = createDefaultSamples();
            loaded.forEach(DatabaseManager::insertExpense);
        }

        masterData.setAll(loaded);
    }

    public ObservableList<Expense> getMasterData() {
        return readOnlyMasterData;
    }

    public FilteredList<Expense> getFilteredData() {
        return filteredData;
    }

    public void addExpense(String description, String category, LocalDate date, String amountStr) {
        double amount = parseAndValidateAmount(amountStr);
        if (description == null || description.isBlank() || category == null || date == null) {
            throw new IllegalArgumentException("Please fill in description, category, date, and amount.");
        }

        Expense expense = new Expense(description.trim(), category, date, amount);
        DatabaseManager.insertExpense(expense);
        masterData.addFirst(expense);
    }

    public void updateExpense(Expense expense, String newDesc, String newCat, LocalDate newDate, String newAmtStr) {
        double newAmount = parseAndValidateAmount(newAmtStr);
        if (newDesc == null || newDesc.isBlank() || newCat == null || newDate == null) {
            throw new IllegalArgumentException("All fields are required.");
        }

        String oldDesc = expense.getDescription();
        LocalDate oldDate = expense.getDate();

        expense.setDescription(newDesc.trim());
        expense.setCategory(newCat);
        expense.setDate(newDate);
        expense.setAmount(newAmount);

        DatabaseManager.updateExpense(expense, oldDesc, oldDate);
    }

    public void deleteExpense(Expense expense) {
        DatabaseManager.deleteExpense(expense);
        masterData.remove(expense);
    }

    public double calculateTotal(List<Expense> list) {
        return list.stream().mapToDouble(Expense::getAmount).sum();
    }

    public String findTopCategory(List<Expense> list) {
        if (list.isEmpty()) return "None";

        return list.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    private double parseAndValidateAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) {
            throw new IllegalArgumentException("Amount cannot be empty.");
        }
        try {
            double amt = Double.parseDouble(amountStr.trim());
            if (amt <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }
            return amt;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid numeric amount.");
        }
    }

    private List<Expense> createDefaultSamples() {
        return List.of(
                new Expense("Groceries & organic produce", "Groceries", LocalDate.now(), 68.45),
                new Expense("Espresso & pastry with colleague", "Food & Dining", LocalDate.now().minusDays(1), 11.50),
                new Expense("Monthly transit pass reload", "Transportation", LocalDate.now().minusDays(2), 85.00)
        );
    }
}
