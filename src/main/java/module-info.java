module io.github.jsavellano.expensetracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;

    // Grant JavaFX reflection access to your packages (Fixes IllegalAccessException)
    opens io.github.jsavellano.expensetracker to javafx.graphics, javafx.fxml, javafx.base;
    exports io.github.jsavellano.expensetracker;
    exports io.github.jsavellano.expensetracker.model;
    opens io.github.jsavellano.expensetracker.model to javafx.base, javafx.fxml, javafx.graphics;
}