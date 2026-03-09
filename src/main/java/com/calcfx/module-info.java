module com.calcfx {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.calcfx to javafx.fxml;
    opens com.calcfx.controller to javafx.fxml;

    exports com.calcfx;
    exports com.calcfx.controller;
    exports com.calcfx.model;
    exports com.calcfx.service;
    exports com.calcfx.util;
}
