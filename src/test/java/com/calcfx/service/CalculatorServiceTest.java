package com.calcfx.service;

import com.calcfx.model.CalculatorModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CalculatorService")
class CalculatorServiceTest {

    private CalculatorService service;
    private CalculatorModel   model;

    @BeforeEach
    void setUp() {
        model   = new CalculatorModel();
        service = new CalculatorService(model);
    }

    // ── Operações básicas ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Operações básicas")
    class BasicOperations {

        @Test @DisplayName("5 + 3 = 8")
        void addition() {
            input("5"); service.setOperator("+"); input("3"); service.calculate();
            assertEquals("8", model.getPendingInput());
        }

        @Test @DisplayName("9 − 4 = 5")
        void subtraction() {
            input("9"); service.setOperator("-"); input("4"); service.calculate();
            assertEquals("5", model.getPendingInput());
        }

        @Test @DisplayName("6 × 7 = 42")
        void multiplication() {
            input("6"); service.setOperator("*"); input("7"); service.calculate();
            assertEquals("42", model.getPendingInput());
        }

        @Test @DisplayName("10 ÷ 4 = 2.5")
        void division() {
            input("10"); service.setOperator("/"); input("4"); service.calculate();
            assertEquals("2.5", model.getPendingInput());
        }

        @Test @DisplayName("Divisão por zero → Erro")
        void divisionByZero() {
            input("5"); service.setOperator("/"); input("0"); service.calculate();
            assertEquals("Erro", model.getPendingInput());
        }
    }

    // ── Parênteses ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Parênteses")
    class Parentheses {

        @Test @DisplayName("(3+2)*4 = 20")
        void groupedMultiplication() {
            service.appendDigit("(");
            input("3"); service.setOperator("+"); input("2");
            service.appendDigit(")");
            service.setOperator("*");
            input("4"); service.calculate();
            assertEquals("20", model.getPendingInput());
        }

        @Test @DisplayName("2*(3+1) = 8  (multiplicação implícita)")
        void implicitMultiply() {
            input("2");
            service.appendDigit("(");
            input("3"); service.setOperator("+"); input("1");
            service.appendDigit(")");
            service.calculate();
            assertEquals("8", model.getPendingInput());
        }

        @Test @DisplayName("(2+3)*(1+4) = 25")
        void nestedGroups() {
            service.appendDigit("(");
            input("2"); service.setOperator("+"); input("3");
            service.appendDigit(")");
            service.setOperator("*");
            service.appendDigit("(");
            input("1"); service.setOperator("+"); input("4");
            service.appendDigit(")");
            service.calculate();
            assertEquals("25", model.getPendingInput());
        }

        @Test @DisplayName("Fecha parêntese sem abrir é ignorado")
        void closeWithoutOpen() {
            input("5");
            service.appendDigit(")");
            service.calculate();
            assertEquals("5", model.getPendingInput());
        }
    }

    // ── Funções científicas ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Funções científicas")
    class ScientificFunctions {

        @Test @DisplayName("√9 = 3")
        void squareRoot() {
            input("9"); service.applyScientificFunction("sqrt");
            assertEquals("3", model.getPendingInput());
        }

        @Test @DisplayName("5² = 25")
        void square() {
            input("5"); service.applyScientificFunction("sq");
            assertEquals("25", model.getPendingInput());
        }

        @Test @DisplayName("2 ^ 10 = 1024")
        void power() {
            input("2"); service.applyScientificFunction("pow");
            input("10"); service.calculate();
            assertEquals("1024", model.getPendingInput());
        }

        @Test @DisplayName("log(100) = 2")
        void log10() {
            input("100"); service.applyScientificFunction("log");
            assertEquals("2", model.getPendingInput());
        }

        @Test @DisplayName("ln(e) ≈ 1  (tolerância de ponto flutuante)")
        void naturalLog() {
            service.applyScientificFunction("e");
            service.applyScientificFunction("ln");
            assertEquals(1.0, Double.parseDouble(model.getPendingInput()), 1e-9);
        }

        @Test @DisplayName("1/4 = 0.25")
        void inverse() {
            input("4"); service.applyScientificFunction("inv");
            assertEquals("0.25", model.getPendingInput());
        }

        @Test @DisplayName("1/0 → Erro")
        void inverseZero() {
            input("0"); service.applyScientificFunction("inv");
            assertEquals("Erro", model.getPendingInput());
        }

        @Test @DisplayName("5! = 120")
        void factorial() {
            input("5"); service.applyScientificFunction("fact");
            assertEquals("120", model.getPendingInput());
        }

        @Test @DisplayName("sin(0) = 0 em RAD")
        void sinZeroRad() {
            assertTrue(model.isRadianMode());
            input("0"); service.applyScientificFunction("sin");
            assertEquals("0", model.getPendingInput());
        }

        @Test @DisplayName("sin(90°) = 1 em DEG")
        void sin90Deg() {
            service.toggleAngleMode();
            assertFalse(model.isRadianMode());
            input("90"); service.applyScientificFunction("sin");
            assertEquals("1", model.getPendingInput());
        }

        @Test @DisplayName("constante π está correta")
        void piConstant() {
            service.applyScientificFunction("pi");
            assertTrue(model.getPendingInput().startsWith("3.14159"));
        }
    }

    // ── Controle ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Controle")
    class Control {

        @Test @DisplayName("clear() restaura estado inicial")
        void clearResetsModel() {
            input("42"); service.setOperator("+");
            service.clear();
            assertEquals("0", model.getPendingInput());
            assertEquals("", model.getExpression());
            assertFalse(model.isJustEvaluated());
        }

        @Test @DisplayName("backspace remove último dígito")
        void backspace() {
            input("123"); service.backspace();
            assertEquals("12", model.getPendingInput());
        }

        @Test @DisplayName("backspace em dígito único retorna 0")
        void backspaceSingleDigit() {
            input("5"); service.backspace();
            assertEquals("0", model.getPendingInput());
        }

        @Test @DisplayName("+/- inverte sinal")
        void toggleSign() {
            input("7");
            service.toggleSign(); assertEquals("-7", model.getPendingInput());
            service.toggleSign(); assertEquals("7",  model.getPendingInput());
        }

        @Test @DisplayName("% divide por 100")
        void percentage() {
            input("50"); service.percentage();
            assertEquals("0.5", model.getPendingInput());
        }

        @Test @DisplayName("toggleAngleMode alterna RAD ↔ DEG")
        void toggleAngleMode() {
            assertTrue(model.isRadianMode());
            service.toggleAngleMode(); assertFalse(model.isRadianMode());
            service.toggleAngleMode(); assertTrue(model.isRadianMode());
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private void input(String value) {
        for (char c : value.toCharArray()) {
            if (c == '.') service.appendDecimalPoint();
            else          service.appendDigit(String.valueOf(c));
        }
    }
}
