package com.calcfx.model;

/**
 * Estado da calculadora no modelo baseado em expressão.
 * - expression:   string acumulada com a expressão em construção (ex: "5*(3+")
 * - pendingInput: número que o usuário está digitando no momento
 * - justEvaluated: true logo após pressionar "="
 */
public class CalculatorModel {

    private String  expression    = "";
    private String  pendingInput  = "0";
    private boolean justEvaluated = false;
    private boolean isRadianMode  = true;

    // --- Getters e Setters ---

    public String getExpression() { return expression; }
    public void   setExpression(String expression) { this.expression = expression; }

    public String getPendingInput() { return pendingInput; }
    public void   setPendingInput(String pendingInput) { this.pendingInput = pendingInput; }

    public boolean isJustEvaluated() { return justEvaluated; }
    public void    setJustEvaluated(boolean justEvaluated) { this.justEvaluated = justEvaluated; }

    public boolean isRadianMode() { return isRadianMode; }
    public void    setRadianMode(boolean radianMode) { isRadianMode = radianMode; }

    /** Restaura ao estado inicial. */
    public void reset() {
        expression    = "";
        pendingInput  = "0";
        justEvaluated = false;
    }
}
