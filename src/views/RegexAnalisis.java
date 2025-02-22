package views;

import java.util.ArrayList;
import java.util.List;

public class RegexAnalisis {
	    public static List<String[]> analizarTexto(String texto) {
	        String[] lineas = texto.split("\n");
	        List<String[]> resultados = new ArrayList<>();
	        int contador = 1;

	        for (int i = 0; i < lineas.length; i++) {
	            String linea = lineas[i];
	            String[] palabras = linea.split(" ");

	            for (String palabra : palabras) {
	                if (contieneDigito(palabra)) {
	                    String tipo = clasificarNumero(palabra);
	                    resultados.add(new String[]{String.valueOf(contador++), String.valueOf(i + 1), palabra, tipo});
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
	}