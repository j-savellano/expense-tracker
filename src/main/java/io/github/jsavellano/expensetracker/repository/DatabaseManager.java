package io.github.jsavellano.expensetracker.repository;

import io.github.jsavellano.expensetracker.model.Expense;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = getDatabaseUrl();

    private static String getDatabaseUrl() {
        // Find user home directory
        String userHome = System.getProperty("user.home");

        // Define Application Support folder for macOS
        File appDir = new File(userHome, "Library/Application Support/ExpenseTracker");

        // Ensure directory exists
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        File dbFile = new File(appDir, "expenses.db");
        return "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    public static void initializeDatabase() {
        String sql = """
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                date TEXT NOT NULL,
                amount REAL NOT NULL
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Expense> loadAllExpenses() {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT description, category, date, amount FROM expenses ORDER BY date DESC, id DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String desc = rs.getString("description");
                String cat = rs.getString("category");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                double amt = rs.getDouble("amount");

                list.add(new Expense(desc, cat, date, amt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void insertExpense(Expense expense) {
        String sql = "INSERT INTO expenses(description, category, date, amount) VALUES(?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expense.getDescription());
            pstmt.setString(2, expense.getCategory());
            pstmt.setString(3, expense.getDate().toString());
            pstmt.setDouble(4, expense.getAmount());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateExpense(Expense expense, String oldDesc, LocalDate oldDate) {
        // Updating based on description and date (or primary key if added to Expense model)
        String sql = "UPDATE expenses SET description = ?, category = ?, date = ?, amount = ? " +
                "WHERE description = ? AND date = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expense.getDescription());
            pstmt.setString(2, expense.getCategory());
            pstmt.setString(3, expense.getDate().toString());
            pstmt.setDouble(4, expense.getAmount());
            pstmt.setString(5, oldDesc);
            pstmt.setString(6, oldDate.toString());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteExpense(Expense expense) {
        String sql = "DELETE FROM expenses WHERE description = ? AND date = ? AND amount = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expense.getDescription());
            pstmt.setString(2, expense.getDate().toString());
            pstmt.setDouble(3, expense.getAmount());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}