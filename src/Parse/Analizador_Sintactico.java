package Parse;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.List;

public class Analizador_Sintactico {
    private Analizador_Lexico lexer;
    private Stack<Integer> pila;
    private Map<Integer, Map<Integer, int[]>> tablaSintactica;
    private int[] tokensLexicos;
    private int apuntador;

    public Analizador_Sintactico(Analizador_Lexico lexer) {
        this.lexer = lexer;
        this.pila = new Stack<>();
        inicializarTablaSintactica();
        prepararAnalisis();
    }

    private void inicializarTablaSintactica() {
        tablaSintactica = new HashMap<>();

        // Regla Q (300) -> SELECT A FROM F J
        // Start symbol for a SELECT query
        Map<Integer, int[]> produccionesQ = new HashMap<>();
        produccionesQ.put(10, new int[]{10, 301, 11, 306, 310}); // SELECT
        tablaSintactica.put(300, produccionesQ);

        // Regla A (301) -> * | B
        // Columns: either all (*) or a list of columns
        Map<Integer, int[]> produccionesA = new HashMap<>();
        produccionesA.put(72, new int[]{72}); // *
        produccionesA.put(4, new int[]{302}); // IDENTIFICADOR (via B)
        tablaSintactica.put(301, produccionesA);

        // Regla B (302) -> C D
        // Column list starts with a column and optionally more
        Map<Integer, int[]> produccionesB = new HashMap<>();
        produccionesB.put(4, new int[]{304, 303}); // IDENTIFICADOR (via C D)
        tablaSintactica.put(302, produccionesB);

        // Regla D (303) -> , B | ε
        // Optional additional columns after a comma
        Map<Integer, int[]> produccionesD = new HashMap<>();
        produccionesD.put(50, new int[]{50, 302}); // , (corrected from 21)
        produccionesD.put(11, new int[]{99}); // ε (FROM)
        produccionesD.put(12, new int[]{99}); // ε (WHERE)
        produccionesD.put(199, new int[]{99}); // ε ($)
        tablaSintactica.put(303, produccionesD);

        // Regla C (304) -> IDENTIFICADOR E
        // Single column, possibly qualified (e.g., table.column)
        Map<Integer, int[]> produccionesC = new HashMap<>();
        produccionesC.put(4, new int[]{4, 305}); // IDENTIFICADOR
        tablaSintactica.put(304, produccionesC);

        // Regla E (305) -> . IDENTIFICADOR | ε
        // Optional qualification (e.g., .column)
        Map<Integer, int[]> produccionesE = new HashMap<>();
        produccionesE.put(51, new int[]{51, 4}); // .
        produccionesE.put(50, new int[]{99}); // ε (,) (corrected from 21)
        produccionesE.put(11, new int[]{99}); // ε (FROM)
        produccionesE.put(12, new int[]{99}); // ε (WHERE)
        produccionesE.put(199, new int[]{99}); // ε ($)
        produccionesE.put(83, new int[]{99}); // ε (=)
        produccionesE.put(81, new int[]{99}); // ε (>)
        produccionesE.put(82, new int[]{99}); // ε (<)
        produccionesE.put(84, new int[]{99}); // ε (>=)
        produccionesE.put(85, new int[]{99}); // ε (<=)
        produccionesE.put(86, new int[]{99}); // ε (!=)
        tablaSintactica.put(305, produccionesE);

        // Regla F (306) -> G H
        // Table list starts with a table and optionally more
        Map<Integer, int[]> produccionesF = new HashMap<>();
        produccionesF.put(4, new int[]{308, 307}); // IDENTIFICADOR (via G H)
        tablaSintactica.put(306, produccionesF);

        // Regla H (307) -> , F | ε
        // Optional additional tables after a comma
        Map<Integer, int[]> produccionesH = new HashMap<>();
        produccionesH.put(50, new int[]{50, 306}); // , (corrected from 21)
        produccionesH.put(12, new int[]{99}); // ε (WHERE)
        produccionesH.put(199, new int[]{99}); // ε ($)
        tablaSintactica.put(307, produccionesH);

        // Regla G (308) -> IDENTIFICADOR I
        // Single table, possibly with an alias
        Map<Integer, int[]> produccionesG = new HashMap<>();
        produccionesG.put(4, new int[]{4, 309}); // IDENTIFICADOR
        tablaSintactica.put(308, produccionesG);

        // Regla I (309) -> IDENTIFICADOR | ε
        // Optional alias after a table
        Map<Integer, int[]> produccionesI = new HashMap<>();
        produccionesI.put(4, new int[]{4}); // IDENTIFICADOR (alias)
        produccionesI.put(50, new int[]{99}); // ε (,) (corrected from 21)
        produccionesI.put(12, new int[]{99}); // ε (WHERE)
        produccionesI.put(199, new int[]{99}); // ε ($)
        tablaSintactica.put(309, produccionesI);

        // Regla J (310) -> WHERE K | ε
        // Optional WHERE clause
        Map<Integer, int[]> produccionesJ = new HashMap<>();
        produccionesJ.put(12, new int[]{12, 311}); // WHERE
        produccionesJ.put(199, new int[]{99}); // ε ($)
        tablaSintactica.put(310, produccionesJ);

        // Regla K (311) -> L M V
        // Condition list starts with a condition and optionally more
        Map<Integer, int[]> produccionesK = new HashMap<>();
        produccionesK.put(4, new int[]{313, 312}); // IDENTIFICADOR (via L M V)
        tablaSintactica.put(311, produccionesK);

        // Regla V (312) -> P K | ε
        // Optional additional conditions with AND/OR
        Map<Integer, int[]> produccionesV = new HashMap<>();
        produccionesV.put(14, new int[]{317, 311}); // AND
        produccionesV.put(15, new int[]{317, 311}); // OR
        produccionesV.put(199, new int[]{99}); // ε ($)
        tablaSintactica.put(312, produccionesV);

        // Regla L (313) -> C N
        // Left side of a condition (e.g., column)
        Map<Integer, int[]> produccionesL = new HashMap<>();
        produccionesL.put(4, new int[]{304, 314}); // IDENTIFICADOR (via C N)
        tablaSintactica.put(313, produccionesL);

        // Regla M (314) -> OPERADOR O | IN ( Q )
        // Operator and right side of a condition, or IN subquery
        Map<Integer, int[]> produccionesM = new HashMap<>();
        produccionesM.put(83, new int[]{315, 316}); // =
        produccionesM.put(81, new int[]{315, 316}); // >
        produccionesM.put(82, new int[]{315, 316}); // <
        produccionesM.put(84, new int[]{315, 316}); // >=
        produccionesM.put(85, new int[]{315, 316}); // <=
        produccionesM.put(86, new int[]{315, 316}); // !=
        produccionesM.put(13, new int[]{13, 52, 300, 53}); // IN ( Q )
        tablaSintactica.put(314, produccionesM);

        // Regla N (315) -> OPERADOR
        // Operator in a condition
        Map<Integer, int[]> produccionesN = new HashMap<>();
        produccionesN.put(83, new int[]{83}); // =
        produccionesN.put(81, new int[]{81}); // >
        produccionesN.put(82, new int[]{82}); // <
        produccionesN.put(84, new int[]{84}); // >=
        produccionesN.put(85, new int[]{85}); // <=
        produccionesN.put(86, new int[]{86}); // !=
        tablaSintactica.put(315, produccionesN);

        // Regla O (316) -> C | ' R ' | CONSTANTE
        // Right side of a condition: column, string, or numeric constant
        Map<Integer, int[]> produccionesO = new HashMap<>();
        produccionesO.put(4, new int[]{304}); // IDENTIFICADOR (via C)
        produccionesO.put(54, new int[]{54, 318, 54}); // ' R '
        produccionesO.put(61, new int[]{319}); // CONSTANTE numérica
        produccionesO.put(62, new int[]{318}); // CONSTANTE numérica
        tablaSintactica.put(316, produccionesO);

        // Regla P (317) -> AND | OR
        // Logical operator between conditions
        Map<Integer, int[]> produccionesP = new HashMap<>();
        produccionesP.put(14, new int[]{14}); // AND
        produccionesP.put(15, new int[]{15}); // OR
        tablaSintactica.put(317, produccionesP);

        // Regla R (318) -> CONSTANTE alfanumérica
        // Alphanumeric constant inside quotes
        Map<Integer, int[]> produccionesR = new HashMap<>();
        produccionesR.put(62, new int[]{62}); // CONSTANTE alfanumérica
        tablaSintactica.put(318, produccionesR);

        // Regla T (319) -> CONSTANTE numérica
        // Numeric constant
        Map<Integer, int[]> produccionesT = new HashMap<>();
        produccionesT.put(61, new int[]{61}); // CONSTANTE numérica
        tablaSintactica.put(319, produccionesT);
    }

    private void prepararAnalisis() {
        List<Analizador_Lexico.Token> tokens = lexer.getTokens();
        tokensLexicos = new int[tokens.size() + 1];
        
        for (int i = 0; i < tokens.size(); i++) {
            Analizador_Lexico.Token token = tokens.get(i);
            tokensLexicos[i] = token.getCodigo();
        }
        tokensLexicos[tokens.size()] = 199; // '$' fin de cadena
        
        apuntador = 0;
    }

    
    
    public String analizar() {
        // Paso 1: Inicializar pila
        pila.push(199); // '$'
        pila.push(300); // Símbolo inicial 'Q'
        
        System.out.println("Iniciando análisis sintáctico LL(1)...");
        System.out.println("Tokens a analizar:");
        for (int i = 0; i < tokensLexicos.length; i++) {
            if (i == apuntador) {
                System.out.print("[" + tokensLexicos[i] + "] ");
            } else {
                System.out.print(tokensLexicos[i] + " ");
            }
        }
        System.out.println("\nPila inicial: " + pila);
    
        int X, K;
        boolean error = false;
        String mensajeError = ""; // Variable para almacenar el mensaje de error
        
        do {
            X = pila.pop();
            K = tokensLexicos[apuntador];
            
            System.out.println("\nPaso actual:");
            System.out.println("X (tope pila): " + X);
            System.out.println("K (token actual): " + K);
            System.out.println("Pila actual: " + pila);
    
            if (esTerminal(X) || X == 199) { // Si X es terminal o '$'
                if (X == K) {
                    System.out.println("Emparejado: " + X);
                    if (X != 199) { // Si no es '$', avanzar
                        apuntador++;
                    }
                } else {
                    error = true;
                    mensajeError = clasificarErrorTerminal(X, K);
                    System.out.println(mensajeError);
                    break;
                }
            } else { // X es no terminal
                Map<Integer, int[]> producciones = tablaSintactica.get(X);
                if (producciones != null) {
                    int[] produccion = producciones.get(K);
                    if (produccion != null) {
                        System.out.println("Aplicando producción: " + X + " -> " + arrayToString(produccion));
                        
                        if (produccion.length > 0 && produccion[0] != 99) { // Si no es producción vacía
                            // Insertar en orden inverso
                            for (int i = produccion.length - 1; i >= 0; i--) {
                                if (produccion[i] != 99) { // Ignorar ε
                                    pila.push(produccion[i]);
                                }
                            }
                        }
                    } else {
                        error = true;
                        mensajeError = clasificarErrorNoTerminal(X, K);
                        System.out.println(mensajeError);
                        break;
                    }
                } else {
                    error = true;
                    mensajeError = "ERROR [Tipo 1, Código 101]: Símbolo desconocido (no hay producciones para " + X + ")";
                    System.out.println(mensajeError);
                    break;
                }
            }
            
        } while (X != 199 && !error); // Hasta que X sea '$' o haya error
    
        if (!error) {
            System.out.println("\nAnálisis completado con éxito!");
            return ""; // Sin errores, devolvemos cadena vacía
        } else {
            System.out.println("\nAnálisis terminado con errores");
            return mensajeError; // Devolvemos el mensaje de error
        }
    }
    
    private String clasificarErrorTerminal(int X, int K) {
        // Clasifica errores cuando X es un terminal
        String tipoEsperado = "";
        int codigoError = 0;

        if (X == 199) {
            tipoEsperado = "Fin de entrada ($)"; codigoError = 205; // Delimitador
        } else if (X == 10 || X == 11 || X == 12 || X == 13 || X == 14 || X == 15) {
            tipoEsperado = "Palabra Reservada"; codigoError = 201;
        } else if (X == 4) {
            tipoEsperado = "Identificador"; codigoError = 204;
        } else if (X == 50 || X == 51 || X == 52 || X == 53 || X == 72) {
            tipoEsperado = "Delimitador"; codigoError = 205;
        } else if (X == 61 || X == 62) {
            tipoEsperado = "Constante"; codigoError = 206;
        } else if (X == 83 || X == 81 || X == 82 || X == 84 || X == 85 || X == 86) {
            tipoEsperado = "Operador Relacional"; codigoError = 208;
        } else {
            tipoEsperado = "Símbolo desconocido"; codigoError = 101;
        }

        return "ERROR [Tipo " + (codigoError == 101 ? 1 : 2) + ", Código " + codigoError + "]: Se esperaba " + tipoEsperado + " (" + X + ") pero se encontró " + K;
    }
    
    private String clasificarErrorNoTerminal(int X, int K) {
        // Clasifica errores cuando X es un no terminal
        Map<Integer, int[]> producciones = tablaSintactica.get(X);
        String tipoEsperado = "";
        int codigoError = 0;

        // Determinar qué se esperaba según las producciones posibles
        if (producciones.containsKey(10) || producciones.containsKey(11) || producciones.containsKey(12) ||
            producciones.containsKey(13) || producciones.containsKey(14) || producciones.containsKey(15)) {
            tipoEsperado = "Palabra Reservada"; codigoError = 201;
        }
        if (producciones.containsKey(4)) {
            tipoEsperado += (tipoEsperado.isEmpty() ? "" : " o ") + "Identificador"; codigoError = 204;
        }
        if (producciones.containsKey(50) || producciones.containsKey(51) || producciones.containsKey(52) ||
            producciones.containsKey(53) || producciones.containsKey(72)) {
            tipoEsperado += (tipoEsperado.isEmpty() ? "" : " o ") + "Delimitador"; codigoError = 205;
        }
        if (producciones.containsKey(61) || producciones.containsKey(62)) {
            tipoEsperado += (tipoEsperado.isEmpty() ? "" : " o ") + "Constante"; codigoError = 206;
        }
        if (producciones.containsKey(83) || producciones.containsKey(81) || producciones.containsKey(82) ||
            producciones.containsKey(84) || producciones.containsKey(85) || producciones.containsKey(86)) {
            tipoEsperado += (tipoEsperado.isEmpty() ? "" : " o ") + "Operador Relacional"; codigoError = 208;
        }
        if (tipoEsperado.isEmpty()) {
            tipoEsperado = "Símbolo desconocido"; codigoError = 101;
        }

        return "ERROR [Tipo " + (codigoError == 101 ? 1 : 2) + ", Código " + codigoError + "]: Se esperaba " + tipoEsperado + " para " + X + " pero se encontró " + K;
    }

    private boolean esTerminal(int simbolo) {
        return simbolo < 300 || simbolo == 199;
    }

    private String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (array[i] != 99) { // No mostrar ε
                sb.append(array[i]);
                if (i < array.length - 1) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }
}