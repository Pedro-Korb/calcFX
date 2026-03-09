package com.calcfx.util;

import java.util.Locale;

/**
 * Utilitários matemáticos reutilizáveis pela camada de serviço.
 */
public final class MathUtil {

    private MathUtil() {}

    public static double toRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    public static double toDegrees(double radians) {
        return Math.toDegrees(radians);
    }

    public static boolean isValidNumber(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Formata um double para exibição:
     * - Inteiros exatos → sem casas decimais ("5" em vez de "5.0")
     * - Demais → até 10 dígitos significativos sem zeros finais
     * - NaN / Infinito → mensagens amigáveis
     */
    public static String formatResult(double value) {
        if (Double.isNaN(value))      return "Erro";
        if (Double.isInfinite(value)) return value > 0 ? "∞" : "-∞";

        if (value == Math.floor(value) && Math.abs(value) < 1e15) {
            return String.valueOf((long) value);
        }

        String formatted = String.format(Locale.US, "%.10g", value);
        if (formatted.contains(".") && !formatted.contains("e")) {
            formatted = formatted.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return formatted;
    }

    /**
     * Fatorial de n (suporta 0..20; acima disso estoura long).
     */
    public static long factorial(int n) {
        if (n < 0)  throw new ArithmeticException("Fatorial indefinido para negativos");
        if (n > 20) throw new ArithmeticException("Valor muito grande para fatorial");
        long result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }
}
