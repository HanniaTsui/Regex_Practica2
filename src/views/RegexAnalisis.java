package views;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegexAnalisis {
    public static List<String[]> analizarTexto(String texto) {
        String[] lineas = texto.split("\n");
        List<String[]> resultados = new ArrayList<>();
        List<String[]> invalidos = new ArrayList<>();
        int contador = 1;

        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];
            String[] palabras = linea.split(" ");

            for (String palabra : palabras) {
                // Eliminar comas o puntos al final de la palabra (antes de un espacio)
                palabra = palabra.replaceAll("[.,]$", "");

                if (contieneDigito(palabra)) {
                    if (esNumeroValido(palabra)) {
                        String tipo = clasificarNumero(palabra);
                        resultados.add(new String[]{String.valueOf(contador++), String.valueOf(i + 1), palabra, tipo});
                    } else {
                        invalidos.add(new String[]{String.valueOf(contador++), String.valueOf(i + 1), palabra, "Inválido"});
                    }
                }
            }
        }

        return resultados;
    }

    private static boolean contieneDigito(String palabra) {
        for (char caracter : palabra.toCharArray()) {
            if (Character.isDigit(caracter)) {
                return true;
            }
        }
        return false;
    }

    private static boolean esNumeroValido(String palabra) {
        String regex = "^(\\$?\\d{1,3}(,\\d{3})*(\\.\\d+)?%?|\\d+\\.\\d+|\\d{1,3}(,\\d{3})*(\\.\\d+)?|\\d+)$";
        return Pattern.matches(regex, palabra);
    }

    private static String clasificarNumero(String palabra) {
        if (palabra.contains("%")) {
            return "Porcentaje";
        } else if (palabra.startsWith("$")) {
            return "Valor monetario";
        } else if (palabra.contains(".")) {
            return "Real";
        } else {
            return "Natural";
        }
    }

    public static List<String[]> obtenerInvalidos(String texto) {
        String[] lineas = texto.split("\n");
        List<String[]> invalidos = new ArrayList<>();
        int contador = 1;

        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];
            String[] palabras = linea.split(" ");

            for (String palabra : palabras) {
                // Eliminar comas o puntos al final de la palabra (antes de un espacio)
                palabra = palabra.replaceAll("[.,]$", "");

                if (contieneDigito(palabra) && !esNumeroValido(palabra)) {
                    invalidos.add(new String[]{String.valueOf(contador++), String.valueOf(i + 1), palabra, "Inválido"});
                }
            }
        }

        return invalidos;
    }
}