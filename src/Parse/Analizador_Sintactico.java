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
    private int[] lineasTokens;
    
    public Analizador_Sintactico(Analizador_Lexico lexer) {
        this.lexer = lexer;
        this.pila = new Stack<>();
        inicializarTablaSintactica();
        prepararAnalisis();
    }

    private void inicializarTablaSintactica() {
        tablaSintactica = new HashMap<>();

        // Regla Q (300) -> SELECT A FROM F J
        Map<Integer, int[]> reglasQ = new HashMap<>();
        reglasQ.put(10, new int[]{10, 301, 11, 306, 310}); // SELECT
        tablaSintactica.put(300, reglasQ);

        // Regla A (301) -> * | B
        Map<Integer, int[]> reglasA = new HashMap<>();
        reglasA.put(72, new int[]{72}); // *
        reglasA.put(4, new int[]{302}); // IDENTIFICADOR (via B)
        tablaSintactica.put(301, reglasA);

        // Regla B (302) -> C D
        Map<Integer, int[]> reglasB = new HashMap<>();
        reglasB.put(4, new int[]{304, 303}); // IDENTIFICADOR (via C D)
        tablaSintactica.put(302, reglasB);

     // Regla C (304) -> IDENTIFICADOR E
        Map<Integer, int[]> produccionesC = new HashMap<>();
        produccionesC.put(4, new int[]{4, 305}); // IDENTIFICADOR
        tablaSintactica.put(304, produccionesC);
        
        // Regla D (303) -> , B 
        Map<Integer, int[]> reglasD = new HashMap<>();
        reglasD.put(50, new int[]{50, 302}); // , 
        reglasD.put(11, new int[]{99}); //  (FROM)
        reglasD.put(12, new int[]{99}); //  (WHERE) //REVISAR
        reglasD.put(199, new int[]{99}); //  ($)
        tablaSintactica.put(303, reglasD);

        // Regla E (305) -> . IDENTIFICADOR 
        Map<Integer, int[]> reglasE = new HashMap<>();
        reglasE.put(51, new int[]{51, 4}); // .
        reglasE.put(50, new int[]{99}); //  (,)
        reglasE.put(11, new int[]{99}); //  (FROM)
        reglasE.put(12, new int[]{99}); //  (WHERE) //REVISAR
        reglasE.put(199, new int[]{99}); //  ($)
        reglasE.put(83, new int[]{99}); //  (=)
        reglasE.put(81, new int[]{99}); //  (>)
        reglasE.put(82, new int[]{99}); //  (<)
        reglasE.put(84, new int[]{99}); //  (>=)
        reglasE.put(85, new int[]{99}); //  (<=)
        reglasE.put(86, new int[]{99}); //  (!=)
        reglasE.put(13, new int[]{99}); 
        reglasE.put(14, new int[]{99}); 
        reglasE.put(53, new int[]{99}); 
        tablaSintactica.put(305, reglasE);

        // Regla F (306) -> G H
        Map<Integer, int[]> reglasF = new HashMap<>();
        reglasF.put(4, new int[]{308, 307}); // IDENTIFICADOR (via G H)
        tablaSintactica.put(306, reglasF);

        // Regla H (307) -> , F
        Map<Integer, int[]> reglasH = new HashMap<>();
        reglasH.put(50, new int[]{50, 306}); // , 
        reglasH.put(12, new int[]{99}); //  (WHERE)
        reglasH.put(199, new int[]{99}); //  ($)
        reglasH.put(53, new int[]{99}); 
        tablaSintactica.put(307, reglasH);

        // Regla G (308) -> IDENTIFICADOR I
        Map<Integer, int[]> reglasG = new HashMap<>();
        reglasG.put(4, new int[]{4, 309}); // IDENTIFICADOR
        tablaSintactica.put(308, reglasG);

        // Regla I (309) -> IDENTIFICADOR 
        Map<Integer, int[]> reglasI = new HashMap<>();
        reglasI.put(4, new int[]{4}); // IDENTIFICADOR (alias)
        reglasI.put(50, new int[]{99}); //  (,)
        reglasI.put(12, new int[]{99}); //  (WHERE)
        reglasI.put(199, new int[]{99}); //  ($)
        reglasI.put(53, new int[]{99}); 
        tablaSintactica.put(309, reglasI);

        // Regla J (310) -> WHERE K 
        Map<Integer, int[]> reglasJ = new HashMap<>();
        reglasJ.put(12, new int[]{12, 311}); // WHERE
        reglasJ.put(199, new int[]{99}); // ($)
        reglasJ.put(53, new int[]{99}); 
        tablaSintactica.put(310, reglasJ);

        // Regla K (311) -> L M V
        Map<Integer, int[]> reglasK = new HashMap<>();
        reglasK.put(4, new int[]{313, 312}); // IDENTIFICADOR (via L M V)
        tablaSintactica.put(311, reglasK);

        // Regla V (312) -> P K 
        Map<Integer, int[]> reglasV = new HashMap<>();
        reglasV.put(14, new int[]{317, 311}); // AND
        reglasV.put(15, new int[]{317, 311}); // OR
        reglasV.put(199, new int[]{99}); // ($)
      	reglasV.put(53, new int[]{99}); 
        tablaSintactica.put(312, reglasV);

        // Regla L (313) -> C N
        Map<Integer, int[]> reglasL = new HashMap<>();
        reglasL.put(4, new int[]{304, 314}); // IDENTIFICADOR (via C N)
        tablaSintactica.put(313, reglasL);

        // Regla M (314) -> OPERADOR O | IN ( Q )
        Map<Integer, int[]> reglasM = new HashMap<>();
        reglasM.put(83, new int[]{315, 316}); // =
        reglasM.put(81, new int[]{315, 316}); // >
        reglasM.put(82, new int[]{315, 316}); // <
        reglasM.put(84, new int[]{315, 316}); // >=
        reglasM.put(85, new int[]{315, 316}); // <=
        reglasM.put(86, new int[]{315, 316}); // !=
        reglasM.put(13, new int[]{13, 52, 300, 53}); // IN ( Q )
        tablaSintactica.put(314, reglasM);

        // Regla N (315) -> OPERADOR
        Map<Integer, int[]> reglasN = new HashMap<>();
        reglasN.put(83, new int[]{83}); // =
        reglasN.put(81, new int[]{81}); // >
        reglasN.put(82, new int[]{82}); // <
        reglasN.put(84, new int[]{84}); // >=
        reglasN.put(85, new int[]{85}); // <=
        reglasN.put(86, new int[]{86}); // !=
        tablaSintactica.put(315, reglasN);

        // Regla O (316) -> C | ' R ' | CONSTANTE
        Map<Integer, int[]> reglasO = new HashMap<>();
        reglasO.put(4, new int[]{304}); // IDENTIFICADOR (via C)
        reglasO.put(54, new int[]{54, 318, 54}); // ' R '
        reglasO.put(61, new int[]{319}); // CONSTANTE numérica
        reglasO.put(62, new int[]{318}); // CONSTANTE alfanumerica
        tablaSintactica.put(316, reglasO);

        // Regla P (317) -> AND | OR
        Map<Integer, int[]> reglasP = new HashMap<>();
        reglasP.put(14, new int[]{14}); // AND
        reglasP.put(15, new int[]{15}); // OR
        tablaSintactica.put(317, reglasP);

        // Regla R (318) -> CONSTANTE alfanumérica
        Map<Integer, int[]> reglasR = new HashMap<>();
        reglasR.put(62, new int[]{62}); // CONSTANTE alfanumérica
        tablaSintactica.put(318, reglasR);

        // Regla T (319) -> CONSTANTE numérica
        Map<Integer, int[]> reglasT = new HashMap<>();
        reglasT.put(61, new int[]{61}); // CONSTANTE numérica
        tablaSintactica.put(319, reglasT);
    }

    private void prepararAnalisis() {
        List<Analizador_Lexico.Token> tokens = lexer.getTokens();
        tokensLexicos = new int[tokens.size() + 1];
        lineasTokens = new int[tokens.size() + 1]; 
        
        for (int i = 0; i < tokens.size(); i++) {
            Analizador_Lexico.Token token = tokens.get(i);
            tokensLexicos[i] = token.getCodigo();
            lineasTokens[i] = token.getLinea();
        }
        tokensLexicos[tokens.size()] = 199; // '$' fin de cadena
        lineasTokens[tokens.size()] = tokens.isEmpty() ? 1 : tokens.get(tokens.size()-1).getLinea();
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
        String mensajeError = "";
        
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
                    mensajeError = "ERROR [ 1: 101]: Símbolo desconocido (no hay producciones para " + X + ")";
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
    	 // Obtener la línea actual
        int lineaActual = apuntador < lineasTokens.length ? lineasTokens[apuntador] : lineasTokens[lineasTokens.length-1];
        String lineaStr = String.format("%02d", lineaActual);
        
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
        } else if (X == 70 || X == 71 || X == 72 || X == 73){
        	tipoEsperado = "Operador"; codigoError = 207;
        }
        else if (X == 83 || X == 81 || X == 82 || X == 84 || X == 85 || X == 86) {
            tipoEsperado = "Operador Relacional"; codigoError = 208;
        } else {
            tipoEsperado = "Símbolo desconocido"; codigoError = 101;
        }
        return "ERROR [" + (codigoError == 101 ? 1 : 2) + ": " + codigoError + "]: Linea " + lineaStr + 
                ". Se esperaba " + tipoEsperado + " (" + X + ") pero se encontró " + K;
        //return "ERROR [" + (codigoError == 101 ? 1 : 2) + ": " + codigoError + "]: Se esperaba " + tipoEsperado + " (" + X + ") pero se encontró " + K;
    }
    
    private String clasificarErrorNoTerminal(int X, int K) {
        // Clasifica errores cuando X es un no terminal
    	int lineaActual = apuntador < lineasTokens.length ? lineasTokens[apuntador] : lineasTokens[lineasTokens.length-1];
    	String lineaStr = String.format("%02d", lineaActual);
    	    
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
        return "ERROR [" + (codigoError == 101 ? 1 : 2) + ": " + codigoError + "]: Linea " + lineaStr + 
                ". Se esperaba " + tipoEsperado + " para " + X + " pero se encontró " + K;
        //return "ERROR [" + (codigoError == 101 ? 1 : 2) + ": " + codigoError + "]: Se esperaba " + tipoEsperado + " para " + X + " pero se encontró " + K;
    }

    private boolean esTerminal(int simbolo) {
        return simbolo < 300 || simbolo == 199;
    }

    private String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (array[i] != 99) { 
                sb.append(array[i]);
                if (i < array.length - 1) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }
}