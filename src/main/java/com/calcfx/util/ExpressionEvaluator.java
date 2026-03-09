package com.calcfx.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * Avaliador de expressões infix usando o algoritmo Shunting-Yard de Dijkstra.
 * Suporta: números, +, -, *, /, ^, parênteses, funções científicas e unário.
 */
public final class ExpressionEvaluator {

    private static final Set<String> FUNCTIONS = Set.of(
        "sin", "cos", "tan", "asin", "acos", "atan",
        "log", "ln", "sqrt", "exp", "sq", "inv", "fact"
    );

    private final boolean radianMode;

    public ExpressionEvaluator(boolean radianMode) {
        this.radianMode = radianMode;
    }

    // ── API pública ──────────────────────────────────────────────────────────

    public double evaluate(String expression) {
        List<String> tokens  = tokenize(expression.trim());
        List<String> postfix = toPostfix(tokens);
        return evalPostfix(postfix);
    }

    /** Aplica uma função científica unária sobre um valor. */
    public double applyFunction(String fn, double v) {
        return switch (fn) {
            case "sin"  -> Math.sin(toRad(v));
            case "cos"  -> Math.cos(toRad(v));
            case "tan"  -> Math.tan(toRad(v));
            case "asin" -> fromRad(Math.asin(v));
            case "acos" -> fromRad(Math.acos(v));
            case "atan" -> fromRad(Math.atan(v));
            case "log"  -> Math.log10(v);
            case "ln"   -> Math.log(v);
            case "sqrt" -> {
                if (v < 0) throw new ArithmeticException("√ de número negativo");
                yield Math.sqrt(v);
            }
            case "exp"  -> Math.exp(v);
            case "sq"   -> v * v;
            case "inv"  -> {
                if (v == 0) throw new ArithmeticException("Divisão por zero");
                yield 1.0 / v;
            }
            case "fact" -> {
                int n = (int) v;
                if (n != v || n < 0) throw new ArithmeticException("Fatorial requer inteiro não-negativo");
                yield MathUtil.factorial(n);
            }
            default -> v;
        };
    }

    // ── Tokenizador ──────────────────────────────────────────────────────────

    private List<String> tokenize(String expr) {
        List<String> raw = new ArrayList<>();
        int i = 0;

        while (i < expr.length()) {
            char c = expr.charAt(i);

            if (Character.isWhitespace(c)) { i++; continue; }

            // Número (incluindo notação científica como 1.5e10)
            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i++));
                }
                if (i < expr.length() && (expr.charAt(i) == 'e' || expr.charAt(i) == 'E')) {
                    int saved = i;
                    sb.append(expr.charAt(i++));
                    if (i < expr.length() && (expr.charAt(i) == '+' || expr.charAt(i) == '-')) {
                        sb.append(expr.charAt(i++));
                    }
                    if (i < expr.length() && Character.isDigit(expr.charAt(i))) {
                        while (i < expr.length() && Character.isDigit(expr.charAt(i))) {
                            sb.append(expr.charAt(i++));
                        }
                    } else {
                        i = saved; // não era notação científica, volta
                        sb.deleteCharAt(sb.length() - 1);
                    }
                }
                raw.add(sb.toString());
                continue;
            }

            // Nome de função ou constante (sin, cos, ln, pi, ...)
            if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && Character.isLetter(expr.charAt(i))) {
                    sb.append(expr.charAt(i++));
                }
                raw.add(sb.toString());
                continue;
            }

            raw.add(String.valueOf(c));
            i++;
        }

        // Injeta multiplicação implícita: "2(" → "2", "*", "("
        List<String> tokens = new ArrayList<>();
        for (int j = 0; j < raw.size(); j++) {
            tokens.add(raw.get(j));
            if (j + 1 < raw.size()) {
                boolean currEndsValue  = isNumber(raw.get(j)) || raw.get(j).equals(")");
                boolean nextStartsExpr = raw.get(j + 1).equals("(") || FUNCTIONS.contains(raw.get(j + 1));
                if (currEndsValue && nextStartsExpr) tokens.add("*");
            }
        }
        return tokens;
    }

    // ── Shunting-Yard → Notação pós-fixa ────────────────────────────────────

    private List<String> toPostfix(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> ops   = new ArrayDeque<>();

        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);

            if (isNumber(t)) {
                output.add(t);

            } else if (t.equals("pi") || t.equals("π")) {
                output.add(String.valueOf(Math.PI));

            } else if (FUNCTIONS.contains(t)) {
                ops.push(t);

            } else if (t.equals("(")) {
                ops.push(t);

            } else if (t.equals(")")) {
                while (!ops.isEmpty() && !ops.peek().equals("(")) {
                    output.add(ops.pop());
                }
                if (!ops.isEmpty()) ops.pop(); // descarta "("
                if (!ops.isEmpty() && FUNCTIONS.contains(ops.peek())) {
                    output.add(ops.pop());
                }

            } else if (isBinaryOrUnary(t)) {
                // Detecta unário: "-" ou "+" no início ou após operador/"("
                boolean isUnary = t.equals("-") && isUnaryPos(tokens, i);
                String op = isUnary ? "neg" : t;

                if (!op.equals("neg")) {
                    while (!ops.isEmpty()
                           && isOp(ops.peek())
                           && !ops.peek().equals("neg")
                           && ((isLeft(op) && prec(op) <= prec(ops.peek()))
                               || (!isLeft(op) && prec(op) < prec(ops.peek())))) {
                        output.add(ops.pop());
                    }
                }
                ops.push(op);
            }
        }

        while (!ops.isEmpty()) output.add(ops.pop());
        return output;
    }

    private boolean isUnaryPos(List<String> tokens, int i) {
        if (i == 0) return true;
        String prev = tokens.get(i - 1);
        return isOp(prev) || prev.equals("(");
    }

    // ── Avaliador pós-fixo ───────────────────────────────────────────────────

    private double evalPostfix(List<String> postfix) {
        Deque<Double> stack = new ArrayDeque<>();

        for (String t : postfix) {
            if (isNumber(t)) {
                stack.push(Double.parseDouble(t));
            } else if (t.equals("neg")) {
                stack.push(-stack.pop());
            } else if (FUNCTIONS.contains(t)) {
                stack.push(applyFunction(t, stack.pop()));
            } else {
                double b = stack.pop();
                double a = stack.pop();
                double r = switch (t) {
                    case "+" -> a + b;
                    case "-" -> a - b;
                    case "*" -> a * b;
                    case "/" -> {
                        if (b == 0) throw new ArithmeticException("Divisão por zero");
                        yield a / b;
                    }
                    case "^" -> Math.pow(a, b);
                    default  -> throw new ArithmeticException("Operador desconhecido: " + t);
                };
                stack.push(r);
            }
        }

        if (stack.isEmpty()) throw new ArithmeticException("Expressão vazia");
        return stack.pop();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean isNumber(String t) {
        try { Double.parseDouble(t); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private boolean isBinaryOrUnary(String t) {
        return switch (t) { case "+", "-", "*", "/", "^" -> true; default -> false; };
    }

    private boolean isOp(String t) {
        return switch (t) { case "+", "-", "*", "/", "^", "neg" -> true; default -> false; };
    }

    private boolean isLeft(String op) { return !op.equals("^"); }

    private int prec(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "^"      -> 3;
            case "neg"    -> 4;
            default       -> 0;
        };
    }

    private double toRad(double v)   { return radianMode ? v : MathUtil.toRadians(v); }
    private double fromRad(double v) { return radianMode ? v : MathUtil.toDegrees(v); }
}
