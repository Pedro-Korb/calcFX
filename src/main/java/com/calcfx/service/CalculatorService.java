package com.calcfx.service;

import com.calcfx.model.CalculatorModel;
import com.calcfx.util.ExpressionEvaluator;
import com.calcfx.util.MathUtil;

/**
 * Toda a lógica de negócio da calculadora.
 * Constrói uma expressão string e a avalia com ExpressionEvaluator.
 */
public class CalculatorService {

    private final CalculatorModel model;

    public CalculatorService(CalculatorModel model) {
        this.model = model;
    }

    // ── Entrada de dígitos ───────────────────────────────────────────────────

    public void appendDigit(String digit) {
        if (digit.equals("(")) { appendOpenParen();  return; }
        if (digit.equals(")")) { appendCloseParen(); return; }

        if (model.isJustEvaluated()) {
            // Após "=": novo número começa do zero
            model.setExpression("");
            model.setPendingInput(digit);
            model.setJustEvaluated(false);
            return;
        }

        String current = model.getPendingInput();
        if (current.equals("0")) {
            model.setPendingInput(digit);
        } else if (current.equals("-0")) {
            model.setPendingInput("-" + digit);
        } else {
            model.setPendingInput(current + digit);
        }
    }

    public void appendDecimalPoint() {
        if (model.isJustEvaluated()) {
            model.setExpression("");
            model.setPendingInput("0.");
            model.setJustEvaluated(false);
            return;
        }
        if (!model.getPendingInput().contains(".")) {
            model.setPendingInput(model.getPendingInput() + ".");
        }
    }

    // ── Parênteses ───────────────────────────────────────────────────────────

    private void appendOpenParen() {
        String pending = model.getPendingInput();
        String expr    = model.getExpression();

        if (!pending.isEmpty() && !pending.equals("0")) {
            // "5(" → multiplicação implícita "5*("
            model.setExpression(expr + pending + "*(");
        } else if (!expr.isEmpty() && expr.charAt(expr.length() - 1) == ')') {
            // ")(" → ")*("
            model.setExpression(expr + "*(");
        } else {
            model.setExpression(expr + "(");
        }
        model.setPendingInput("");
        model.setJustEvaluated(false);
    }

    private void appendCloseParen() {
        String pending = model.getPendingInput();
        String expr    = model.getExpression();

        long opens  = expr.chars().filter(c -> c == '(').count();
        long closes = expr.chars().filter(c -> c == ')').count();
        // O ")" que vamos adicionar incrementaria closes; checar antes
        if (opens <= closes) return; // nenhum "(" sem par correspondente

        model.setExpression(expr + (pending.isEmpty() ? "" : pending) + ")");
        model.setPendingInput("");
        model.setJustEvaluated(false);
    }

    // ── Operadores básicos ───────────────────────────────────────────────────

    public void setOperator(String op) {
        String pending = model.getPendingInput();
        String expr    = model.getExpression();

        if (model.isJustEvaluated()) {
            // Encadear a partir do resultado
            model.setExpression(pending + op);
        } else {
            model.setExpression(expr + pending + op);
        }
        model.setPendingInput("");
        model.setJustEvaluated(false);
    }

    // ── Calcular ─────────────────────────────────────────────────────────────

    public void calculate() {
        String full = model.getExpression() + model.getPendingInput();
        if (full.isBlank() || full.equals("0")) return;

        try {
            double result = new ExpressionEvaluator(model.isRadianMode()).evaluate(full);
            model.setPendingInput(MathUtil.formatResult(result));
        } catch (Exception e) {
            model.setPendingInput("Erro");
        }
        model.setExpression("");
        model.setJustEvaluated(true);
    }

    // ── Funções científicas ──────────────────────────────────────────────────

    public void applyScientificFunction(String function) {
        // Constantes: substituem o pendingInput
        if ("pi".equals(function)) {
            model.setPendingInput(MathUtil.formatResult(Math.PI));
            model.setJustEvaluated(false);
            return;
        }
        if ("e".equals(function)) {
            model.setPendingInput(MathUtil.formatResult(Math.E));
            model.setJustEvaluated(false);
            return;
        }

        // xʸ age como operador binário
        if ("pow".equals(function)) {
            setOperator("^");
            return;
        }

        // Funções unárias: aplicadas imediatamente ao pendingInput
        String pending = model.getPendingInput();
        double value;
        try {
            value = Double.parseDouble(pending.isEmpty() ? "0" : pending);
        } catch (NumberFormatException e) {
            model.setPendingInput("Erro");
            return;
        }

        try {
            double result = new ExpressionEvaluator(model.isRadianMode()).applyFunction(function, value);
            model.setPendingInput(MathUtil.formatResult(result));
        } catch (ArithmeticException e) {
            model.setPendingInput("Erro");
        }
        model.setJustEvaluated(false);
    }

    // ── Controle ─────────────────────────────────────────────────────────────

    public void toggleSign() {
        String current = model.getPendingInput();
        if (current.equals("0") || current.equals("Erro")) return;
        model.setPendingInput(current.startsWith("-") ? current.substring(1) : "-" + current);
    }

    public void percentage() {
        try {
            double value = Double.parseDouble(model.getPendingInput());
            model.setPendingInput(MathUtil.formatResult(value / 100.0));
        } catch (NumberFormatException e) {
            model.setPendingInput("Erro");
        }
        model.setJustEvaluated(false);
    }

    public void clear() {
        model.reset();
    }

    public void backspace() {
        String current = model.getPendingInput();
        if (current.equals("Erro") || current.equals("∞") || current.equals("-∞")) {
            model.setPendingInput("0");
            return;
        }
        if (model.isJustEvaluated()) return;
        if (current.length() <= 1 || (current.startsWith("-") && current.length() <= 2)) {
            model.setPendingInput("0");
        } else {
            model.setPendingInput(current.substring(0, current.length() - 1));
        }
    }

    public void toggleAngleMode() {
        model.setRadianMode(!model.isRadianMode());
    }

    public CalculatorModel getModel() {
        return model;
    }
}
