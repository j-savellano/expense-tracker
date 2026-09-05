package io.github.jsavellano.expensetracker.service;

import io.github.jsavellano.expensetracker.model.Expense;

import java.util.function.Predicate;

public class FilterService {

    public static Predicate<Expense> createFilterPredicate(String query, String category, String month, String year) {
        String q = (query == null) ? "" : query.trim().toLowerCase();

        return exp -> {
            boolean matchQ = q.isEmpty()
                    || exp.getDescription().toLowerCase().contains(q)
                    || exp.getCategory().toLowerCase().contains(q);

            boolean matchCat = category == null
                    || "All Categories".equals(category)
                    || exp.getCategory().equalsIgnoreCase(category);

            boolean matchMonth = month == null || "All Months".equals(month);
            if (!matchMonth && exp.getDate() != null) {
                matchMonth = exp.getDate().getMonth().name().equalsIgnoreCase(month);
            }

            boolean matchYear = year == null || "All Years".equals(year);
            if (!matchYear && exp.getDate() != null) {
                matchYear = String.valueOf(exp.getDate().getYear()).equals(year);
            }

            return matchQ && matchCat && matchMonth && matchYear;
        };
    }
}