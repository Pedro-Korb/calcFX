package com.calcfx.controller;

import com.calcfx.model.CalculatorModel;
import com.calcfx.service.CalculatorService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller MVC: liga a View (FXML) à camada de serviço.
 * Não contém lógica de negócio — apenas delega ao CalculatorService.
 */
public class CalculatorController {

    @FXML private Label  labelExpression;
    @FXML private Label  labelResult;
    @FXML private Label  labelAngleMode;
    @FXML private Button btnAngleMode;
    @FXML private Button btnPercent;   // "%" não pode ser declarado direto no FXML

    private CalculatorService service;

    @FXML
    public void initialize() {
        service = new CalculatorService(new CalculatorModel());
        btnPercent.setText("%");
        updateDisplay();
    }

    // ── Dígitos e ponto ──────────────────────────────────────────────────────

    @FXML
    private void onDigit(javafx.event.ActionEvent event) {
        service.appendDigit(((Button) event.getSource()).getText());
        updateDisplay();
    }

    @FXML
    private void onDecimalPoint() {
        service.appendDecimalPoint();
        updateDisplay();
    }

    // ── Operadores ───────────────────────────────────────────────────────────

    @FXML
    private void onOperator(javafx.event.ActionEvent event) {
        service.setOperator(((Button) event.getSource()).getUserData().toString());
        updateDisplay();
    }

    @FXML
    private void onEquals() {
        service.calculate();
        updateDisplay();
    }

    // ── Funções científicas ──────────────────────────────────────────────────

    @FXML
    private void onScientificFunction(javafx.event.ActionEvent event) {
        service.applyScientificFunction(((Button) event.getSource()).getUserData().toString());
        updateDisplay();
    }

    // ── Controle ─────────────────────────────────────────────────────────────

    @FXML private void onClear()      { service.clear();       updateDisplay(); }
    @FXML private void onBackspace()  { service.backspace();   updateDisplay(); }
    @FXML private void onToggleSign() { service.toggleSign();  updateDisplay(); }
    @FXML private void onPercentage() { service.percentage();  updateDisplay(); }

    @FXML
    private void onToggleAngleMode() {
        service.toggleAngleMode();
        boolean rad = service.getModel().isRadianMode();
        labelAngleMode.setText(rad ? "RAD" : "DEG");
        btnAngleMode.setText(rad ? "RAD" : "DEG");
    }

    // ── Display ──────────────────────────────────────────────────────────────

    private void updateDisplay() {
        CalculatorModel m = service.getModel();
        String pending = m.getPendingInput();
        String expr    = m.getExpression();

        labelResult.setText(pending.isEmpty() ? "0" : pending);
        labelExpression.setText(toDisplayExpr(expr));
    }

    /** Converte símbolos internos (+, -, *, /) para exibição (×, ÷, −). */
    private String toDisplayExpr(String expr) {
        return expr.replace("*", "×").replace("/", "÷").replace("-", "−");
    }
}
