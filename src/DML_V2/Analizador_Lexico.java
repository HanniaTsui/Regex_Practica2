package DML_V2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analizador_Lexico {

    private List<Token> tokens;
    private static List<Identificador> identificadores;
    private static List<Constante> constantes;
    private List<String> errores;

    // Lista de palabras reservadas de SQL
    private static final List<String> PALABRAS_RESERVADAS = Arrays.asList(
        "SELECT", "FROM", "WHERE", "AND", "OR", "CREATE", "TABLE", "CHAR", "NUMERIC", "NOT", "NULL",
        "CONSTRAINT", "KEY", "PRIMARY", "FOREIGN", "REFERENCES", "INSERT", "INTO", "VALUES", "IN"
    );

    public Analizador_Lexico() {
        tokens = new ArrayList<>();
        identificadores = new ArrayList<>();
        constantes = new ArrayList<>();
        errores = new ArrayList<>();
    }

    public void analizar(String sentenciaSQL) {
        // Limpiar listas antes de un nuevo análisis
    	Token.identificadoresMap.clear();
        Token.contadorIdentificadores = 401;
    	Identificador.identificadoresMap.clear();
    	Identificador.contadorIdentificadores = 401;
    	Token.contadorConstantes = 601;
        tokens.clear();
        identificadores.clear();
        constantes.clear();
        errores.clear();
        
        // Dividir la sentencia SQL en líneas
        String[] lineas = sentenciaSQL.split("\n");
        
        // Validar que la sentencia inicie con una palabra reservada
        String primeraPalabra = sentenciaSQL.trim().split("\\s+")[0].toUpperCase();
        if (!PALABRAS_RESERVADAS.contains(primeraPalabra)) {
            errores.add("Línea 1: La sentencia debe comenzar con una palabra clave válida : Error de sintaxis");
        }

        // Expresiones regulares para identificar tokens, identificadores y constantes
      /*  String patronToken = "\\b(SELECT|FROM|WHERE|AND|OR|CREATE|TABLE|CHAR|NUMERIC|NOT|NULL|"
                + "CONSTRAINT|KEY|PRIMARY|FOREIGN|REFERENCES|INSERT|INTO|VALUES)\\b|"
                + ">=|<=|<>|=|>|<|\\*|,|\\(|\\)|'[^']*'|\\d+\\w*|\\w+";
        String patronToken = "\\b(SELECT|FROM|WHERE|AND|OR|CREATE|TABLE|CHAR|NUMERIC|NOT|NULL|"
                + "CONSTRAINT|KEY|PRIMARY|FOREIGN|REFERENCES|INSERT|INTO|VALUES)\\b|"
                + ">=|<=|<>|=|>|<|\\*|,|\\(|\\)|'[^']*'|\\d+\\w*|[\\w#]+(\\.[\\w#]+)?";
        /*
         * >=|<=|<>|=|>|< Operadores de comparación
         * \\* Caracter *
         * , Coma
         * \\( y \\) Para paréntesis
         * '[^']*' Cadenas de texto entre comillas simples
         * \\d+\\w* Uno o mas dígito y cero o más caracteres de palabra
         * \\w+ Secuencia de uno o más caracteres de palabra
         */
      /*  String patronToken = "\\b(SELECT|FROM|WHERE|AND|OR|CREATE|TABLE|CHAR|NUMERIC|NOT|NULL|"
                + "CONSTRAINT|KEY|PRIMARY|FOREIGN|REFERENCES|INSERT|INTO|VALUES)\\b|"
                + ">=|<=|<>|=|>|<|,\\(\\)|'[^']*'|\\d+\\w*|[A-Za-z][\\w#]*(\\.[A-Za-z][\\w#]*)?";
     */
        String patronToken = "\\b(SELECT|FROM|WHERE|AND|OR|CREATE|TABLE|CHAR|NUMERIC|NOT|NULL|"
                + "CONSTRAINT|KEY|PRIMARY|FOREIGN|REFERENCES|INSERT|INTO|VALUES)\\b|"
                + ">=|<=|<>|=|>|<|,|\\(|\\)|\\*|'[^']*'|\\d+\\w*|[A-Za-z][\\w#]*(\\.[A-Za-z][\\w#]*)?";
        Pattern pattern = Pattern.compile(patronToken);
        Matcher matcher = pattern.matcher(sentenciaSQL);

        int linea = 1;
        int posicionInicio = 0;
        String tokenAnterior = null;

        while (matcher.find()) {
            String lexema = matcher.group();
            
            // Determinar la línea actual basándonos en la posición del lexema en el arreglo de líneas
            int posicionLexema = matcher.start();
            for (int i = 0; i < lineas.length; i++) {
                if (posicionLexema < lineas[i].length()) {
                    linea = i + 1;
                    break;
                }
                posicionLexema -= lineas[i].length() + 1; // +1 para el salto de línea
            }
            
            
            if (lexema.matches("[A-Za-z][\\w#]*(\\.[A-Za-z][\\w#]*)+")) {
                String[] partes = lexema.split("\\.");
                for (String parte : partes) {
                    tokens.add(new Token(parte, linea));
                    if (parte.matches("[A-Za-z][\\w#]*")) { // Si es un identificador válido
                        identificadores.add(new Identificador(parte, linea));
                    }
                    tokens.add(new Token(".", linea)); // Agregar el delimitador "."
                }
                tokens.remove(tokens.size() - 1); // Eliminar el último "." agregado
            } else {
                tokens.add(new Token(lexema, linea));
            }

            // Clasificar el lexema
            if (PALABRAS_RESERVADAS.contains(lexema.toUpperCase())) {
                // Es una palabra reservada, no se agrega a identificadores
            } else if (lexema.matches("\\d+")) { // Uno o más dígitos
                constantes.add(new Constante(lexema, linea));
            } else if (lexema.matches("'[^']*'")) { // Cadenas de texto entre comillas simples
                constantes.add(new Constante(lexema, linea));
            } else if (lexema.matches("[\\w#]+(\\.[\\w#]+)?")) { // Uno o más caracteres de palabra
                // Es un identificador
                if (lexema.matches("^\\d.*")) { // Comienza con número al principio de la cadena
                    // Identificador que comienza con un número (inválido)
                    errores.add("Línea " + linea + ": '" + lexema + "' : Error de sintaxis. Identificador inválido (no puede comenzar con un número)");
                } else if (!lexema.contains(".")) { // Solo registrar identificadores simples (sin punto)
                    identificadores.add(new Identificador(lexema, linea));
                }
            }
            tokenAnterior = lexema;
        }
        //verificarIdentificadoresEntreComillas(sentenciaSQL, linea);
        verificarSimbolosDesconocidos(sentenciaSQL, linea);
        verificarCadenasMalFormateadas(sentenciaSQL, linea);
        verificarPalabrasReservadasMalEscritas();
        verificarOperadoresNoValidos(sentenciaSQL, linea);
        //verificarComasEntreIdentificadores();
    //    verificarAgrupacionCondiciones();
        if (tokens.isEmpty()) {
            errores.add("Error léxico: La sentencia SQL no contiene tokens válidos.");
        }
    }
    
    private boolean esOperador(String token) {
        return token.matches("=|>|<|>=|<=|<>"); //Operadores 
    }
    
    private void verificarIdentificadoresEntreComillas(String sentenciaSQL, int linea) {
        // Primero, verifica los identificadores entre comillas dobles
        verificarIdentificadoresEntreComillasDobles(sentenciaSQL, linea);
        
        Pattern patronComillasSimples = Pattern.compile("'([^']*)'"); //cadenas de texto en comillas simples
        Matcher matcherComillasSimples = patronComillasSimples.matcher(sentenciaSQL);
        
        while (matcherComillasSimples.find()) {
            String contenido = matcherComillasSimples.group(1); // Contenido entre comillas simples
            
            if (contenido.matches("^[a-zA-Z]\\w*$") && !contenido.contains(" ")) {
                // "^[a-zA-Z]\\w*$" Validar nombres de identificadores
                int posicionInicio = matcherComillasSimples.start();
                boolean esConstanteEsperada = false;
                
                // Busca si hay un operador antes de este potencial identificador
                for (int i = posicionInicio - 1; i >= 0; i--) {
                    char c = sentenciaSQL.charAt(i);
                    if (Character.isWhitespace(c)) {
                        continue; // Omite espacios en blanco
                    }
                    if (c == '=' || c == '>' || c == '<') {
                        // Si hay un operador, probablemente sea una constante de cadena válida
                        esConstanteEsperada = true;
                    }
                    break;
                }
                
                // Si no está en un contexto donde se esperaría una constante de cadena, márquelo como error
                if (!esConstanteEsperada) {
                    errores.add("Línea " + linea + ": '" + contenido + "' : Error de sintaxis. Identificador no debe ir entre comillas simples");
                }
            }
        }
    }
    
    private void verificarIdentificadoresEntreComillasDobles(String sentenciaSQL, int linea) {
        // Expresión regular para identificar identificadores entre comillas dobles
        Pattern patronComillasDobles = Pattern.compile("\"([^\"]*)\""); //cadenas de texto en comillas dobles
        Matcher matcherComillasDobles = patronComillasDobles.matcher(sentenciaSQL);

        while (matcherComillasDobles.find()) {
            String lexema = matcherComillasDobles.group(1); // Extraer el contenido entre comillas dobles
            if (lexema.matches("^[a-zA-Z]\\w*$")) { // Verificar si es un identificador válido
                errores.add("Línea " + linea + ": '\"" + lexema + "\"' : Error de sintaxis. Identificador no debe ir entre comillas dobles");
            }
        }
    }  

    private void verificarCadenasMalFormateadas(String sentenciaSQL, int linea) {
        boolean dentroDeCadena = false; // Indica si estamos dentro de una cadena
        int inicioCadena = -1; // Posición donde comienza la cadena

        for (int i = 0; i < sentenciaSQL.length(); i++) {
            char c = sentenciaSQL.charAt(i);

            // Si encontramos una comilla simple
            if (c =='\'') {
                if (!dentroDeCadena) {
                    // Comienza una nueva cadena
                    dentroDeCadena = true;
                    inicioCadena = i;
                } else {
                    // Termina una cadena
                    dentroDeCadena = false;
                }
            }

            // Si llegamos al final de la línea o la sentencia y estamos dentro de una cadena
            if ((c == '\n' || i == sentenciaSQL.length() - 1) && dentroDeCadena) {
                // La cadena no está cerrada
                String cadenaMalFormateada = sentenciaSQL.substring(inicioCadena, i + 1);
                errores.add("Línea " + linea + ": '" + cadenaMalFormateada + "' : Error. Cadena mal formateada");
                dentroDeCadena = false; // Reiniciamos el estado
            }
        }
    }

    private void verificarOperadoresNoValidos(String sentenciaSQL, int linea) {
        // Solo marcar como error operadores no válidos, como "=>", "=<", "><"
        Pattern operadoresNoValidos = Pattern.compile("=>|=<|><");
        Matcher matcherOperadores = operadoresNoValidos.matcher(sentenciaSQL);
        while (matcherOperadores.find()) {
            errores.add("Línea " + linea + ": '" + matcherOperadores.group() + "' : Error de sintaxis. Operador no válido");
            System.out.println("Línea " + linea + ": '" + matcherOperadores.group() + "' : Operador no válido");
        }
    }

    private void verificarSimbolosDesconocidos(String sentenciaSQL, int linea) {
        Pattern simbolosDesconocidos = Pattern.compile("[\\$@%&^`~|\\\\?]"); //Caracteres no validos
        Matcher matcherSimbolos = simbolosDesconocidos.matcher(sentenciaSQL);
        while (matcherSimbolos.find()) {
            errores.add("Línea " + linea + ": '" + matcherSimbolos.group() + "' : Error de sintaxis. Símbolo desconocido");
         //   System.out.println("Línea " + linea + ": '" + matcherSimbolos.group() + "' : Símbolo desconocido");
        }
    }
/*
    private void verificarPalabrasReservadasMalEscritas() {
        for (Token token : tokens) {
            String lexema = token.getLexema().toUpperCase();
            if (!PALABRAS_RESERVADAS.contains(lexema)) {
                for (String palabraReservada : PALABRAS_RESERVADAS) {
                    // Comprobar si el lexema empieza con una palabra reservada
                    if (lexema.startsWith(palabraReservada)) {
                        errores.add("Línea " + token.getLinea() + ": '" + lexema + "': Error de sintaxis. Palabra reservada mal escrita, debería ser '" + palabraReservada + "'");
                        System.out.println("Línea " + token.getLinea() + ": '" + lexema + "': Error de sintaxis. Palabra reservada mal escrita, debería ser '" + palabraReservada + "'");
                        break;
                    }
                    // Usar la distancia de Levenshtein como validación adicional
                    if (calcularDistanciaLevenshtein(lexema, palabraReservada) == 1) {
                        errores.add("Línea " + token.getLinea() + ": '" + lexema + "': Error de sintaxis. Palabra reservada mal escrita, debería ser '" + palabraReservada + "'");
                        System.out.println("Línea " + token.getLinea() + ": '" + lexema + "': Error sintaxis. Palabra reservada mal escrita, debería ser '" + palabraReservada + "'");
                        break;
                    }
                }
            }
        }
    }*/

    
    private void verificarPalabrasReservadasMalEscritas() {
        for (Token token : tokens) {
            String lexema = token.getLexema().toUpperCase();

            // Solo verificar tokens que sean identificadores
            if (token.getTipo() == 4) {
                // Excluir identificadores de una sola letra y aquellos con caracteres especiales (como #)
                if (lexema.length() > 1 && !lexema.matches(".*[^A-Za-z0-9].*")) {
                    // Si el lexema no es una palabra reservada exacta
                    if (!PALABRAS_RESERVADAS.contains(lexema)) {
                        String palabraMasCercana = null;
                        int distanciaMinima = Integer.MAX_VALUE;

                        // Buscar la palabra reservada más cercana
                        for (String palabraReservada : PALABRAS_RESERVADAS) {
                            // Calcular la distancia de Levenshtein
                            int distancia = calcularDistanciaLevenshtein(lexema, palabraReservada);

                            // Si la distancia es menor que la mínima encontrada, actualizar
                            if (distancia < distanciaMinima) {
                                distanciaMinima = distancia;
                                palabraMasCercana = palabraReservada;
                            }
                        }

                        // Si la distancia mínima es menor o igual a 2 y el lexema tiene una longitud similar, considerar como error
                        if (distanciaMinima <= 2 && Math.abs(lexema.length() - palabraMasCercana.length()) <= 2) {
                            errores.add("Línea " + token.getLinea() + ": '" + lexema + "': Error de sintaxis. ¿Quizás quisiste decir '" + palabraMasCercana + "'?");
                            System.out.println("Línea " + token.getLinea() + ": '" + lexema + "': Error de sintaxis. ¿Quizás quisiste decir '" + palabraMasCercana + "'?");
                        }
                    }
                }
            }
        }
    }

    /*
    private void verificarPalabrasReservadasMalEscritas() {
        for (Token token : tokens) {
            String lexema = token.getLexema().toUpperCase();

            // ✅ Verifica si el lexema es una palabra reservada y sáltalo si lo es
            if (PALABRAS_RESERVADAS.contains(lexema)) {
                continue; // No lo tratamos como error
            }

            // 🔍 Verifica que el token sea realmente un identificador antes de continuar
            if (token.getTipo() == 4) { 
                // ❌ Excluir identificadores de una sola letra y con caracteres especiales
                if (lexema.length() > 1 && lexema.matches("^[A-Za-z0-9]+$")) {
                    String palabraMasCercana = null;
                    int distanciaMinima = Integer.MAX_VALUE;

                    // 📌 Buscar la palabra reservada más cercana
                    for (String palabraReservada : PALABRAS_RESERVADAS) {
                        int distancia = calcularDistanciaLevenshtein(lexema, palabraReservada);
                        if (distancia < distanciaMinima) {
                            distanciaMinima = distancia;
                            palabraMasCercana = palabraReservada;
                        }
                    }

                    // 🚨 Solo sugerir si hay un error real (evita sugerencias incorrectas como `IN → AND`)
                    if (distanciaMinima <= 2 && palabraMasCercana != null 
                        && Math.abs(lexema.length() - palabraMasCercana.length()) <= 2) {
                        errores.add("Línea " + token.getLinea() + ": '" + lexema + "': Error de sintaxis. ¿Quizás quisiste decir '" + palabraMasCercana + "'?");
                        System.out.println("Línea " + token.getLinea() + ": '" + lexema + "': Error de sintaxis. ¿Quizás quisiste decir '" + palabraMasCercana + "'?");
                    }
                }
            }
        }
    }*/

    private int calcularDistanciaLevenshtein(String s1, String s2) {
        // Algoritmo de distancia de Levenshtein para medir la similitud entre dos cadenas
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }
    
    private void verificarComasEntreIdentificadores() {
        boolean enClausulaSelect = false; // Indica si estamos en la cláusula SELECT
        boolean enClausulaFrom = false;  // Indica si estamos en la cláusula FROM

        for (int i = 0; i < tokens.size(); i++) {
            Token tokenActual = tokens.get(i);

            // Verificar si estamos en la cláusula SELECT
            if (tokenActual.getLexema().equalsIgnoreCase("SELECT")) {
                enClausulaSelect = true;
                enClausulaFrom = false;
            }
            // Verificar si estamos en la cláusula FROM
            else if (tokenActual.getLexema().equalsIgnoreCase("FROM")) {
                enClausulaSelect = false;
                enClausulaFrom = true;
            }
            // Verificar si estamos en la cláusula WHERE u otra cláusula
            else if (tokenActual.getLexema().equalsIgnoreCase("WHERE") || 
                     tokenActual.getLexema().equalsIgnoreCase("AND") || 
                     tokenActual.getLexema().equalsIgnoreCase("OR")) {
                enClausulaSelect = false;
                enClausulaFrom = false;

                // Verificar si hay una coma antes de la palabra reservada
                if (i > 0) {
                    Token tokenAnterior = tokens.get(i - 1);
                    if (tokenAnterior.getLexema().equals(",")) {
                        errores.add("Línea " + tokenActual.getLinea() + ": " + tokenActual.getLexema() + "' : Error de sintaxis. Coma incorrecta antes de '" + tokenActual.getLexema() );
                    }
                }
            }

            // Verificar comas solo en las cláusulas SELECT y FROM
            if (enClausulaSelect || enClausulaFrom) {
                if (tokenActual.getTipo() == 4) { // Si es un identificador
                    // Verificar si el siguiente token no es una coma y no es el final de la cláusula
                    if (i < tokens.size() - 1) {
                        Token tokenSiguiente = tokens.get(i + 1);
                        // Si el siguiente token no es una coma y no es una palabra reservada (fin de la cláusula)
                        if (!tokenSiguiente.getLexema().equals(",") && 
                            !PALABRAS_RESERVADAS.contains(tokenSiguiente.getLexema().toUpperCase())) {
                            errores.add("Línea " + tokenActual.getLinea() + ":" + tokenActual.getLexema() + "' : Error de sintaxis. Falta una coma después de '" + tokenActual.getLexema());
                        }
                    }
                }
            }
        }
    }
    
    private void verificarAgrupacionCondiciones() {
        boolean enClausulaWhere = false; // Indica si estamos en la cláusula WHERE
        int nivelAgrupacion = 0; // Nivel de anidamiento de paréntesis
        boolean tieneAND = false; // Indica si hay un operador AND en la cláusula WHERE
        boolean tieneOR = false;  // Indica si hay un operador OR en la cláusula WHERE

        for (int i = 0; i < tokens.size(); i++) {
            Token tokenActual = tokens.get(i);

            // Verificar si estamos en la cláusula WHERE
            if (tokenActual.getLexema().equalsIgnoreCase("WHERE")) {
                enClausulaWhere = true;
            }
            // Verificar si salimos de la cláusula WHERE
            else if (enClausulaWhere && tokenActual.getLexema().equalsIgnoreCase(";")) {
                enClausulaWhere = false;
            }

            // Verificar paréntesis y operadores en la cláusula WHERE
            if (enClausulaWhere) {
                if (tokenActual.getLexema().equals("(")) {
                    nivelAgrupacion++; // Incrementar el nivel de anidamiento
                } else if (tokenActual.getLexema().equals(")")) {
                    nivelAgrupacion--; // Decrementar el nivel de anidamiento
                    if (nivelAgrupacion < 0) {
                        errores.add("Línea " + tokenActual.getLinea() + ": Paréntesis de cierre sin apertura : Error de sintaxis");
                    }
                }

                // Verificar si hay un operador AND u OR
                if (tokenActual.getLexema().equalsIgnoreCase("AND")) {
                    tieneAND = true;
                } else if (tokenActual.getLexema().equalsIgnoreCase("OR")) {
                    tieneOR = true;
                }

                // Verificar si hay una mezcla de AND y OR sin paréntesis
                if (tieneAND && tieneOR && nivelAgrupacion == 0) {
                    errores.add("Línea " + tokenActual.getLinea() + ": Falta agrupar condiciones con paréntesis debido a la mezcla de AND y OR : Error de sintaxis");
                    tieneAND = false; // Reiniciar para evitar mensajes duplicados
                    tieneOR = false;
                }
            }
        }

        // Verificar si todos los paréntesis están cerrados
        if (nivelAgrupacion != 0) {
            errores.add("Línea 1: Paréntesis sin cerrar : Error de sintaxis");
        }
    }
    
    public List<Token> getTokens() {
        return tokens;
    }

    public List<Identificador> getIdentificadores() {
        return identificadores;
    }

    public List<Constante> getConstantes() {
        return constantes;
    }

    public List<String> getErrores() {
        return errores;
    }

    public static class Token {
        private String lexema;
        private int linea;
        private static int contadorIdentificadores = 401;
        private static int contadorConstantes = 601;
        private static Map<String, Integer> identificadoresMap = new HashMap<>();

        public Token(String lexema, int linea) {
            this.lexema = lexema;
            this.linea = linea;
        }

        public String getLexema() {
            return lexema;
        }

        public int getLinea() {
            return linea;
        }

        public int getTipo() {
            // Definir el tipo de token según el lexema
            if (PALABRAS_RESERVADAS.contains(lexema.toUpperCase())) {
                return 1; // Palabras reservadas
            } if (lexema.matches("[A-Za-z][\\w#]*(\\.[A-Za-z][\\w#]*)*")) {
                return 4; // Identificadores    
            } else if (lexema.matches("\\d+")) {
                return 6; // Constantes numéricas
            }else if (lexema.matches("'[^']*'")) {
                return 6; // Constantes alfanuméricas
            } else if (lexema.matches(".|,|\\(|\\)|'|\\*")) {
                return 5; // Delimitadores
            } else if (lexema.matches("=|>|<|>=|<=|<>")) {
                return 8; // Operadores relacionales
            } else {
                return 0; // Desconocido
            }
            
            
        }

        
        public int getCodigo() {
            // Primero verificar si es una palabra reservada
            if (PALABRAS_RESERVADAS.contains(lexema.toUpperCase())) {
                // Si es una palabra reservada, retornamos su código correspondiente
                switch (lexema.toUpperCase()) {
                    case "SELECT": return 10;
                    case "FROM": return 11;
                    case "WHERE": return 12;
                    case "IN": return 13;
                    case "AND": return 14;
                    case "OR": return 15;
                    case "CREATE": return 16;
                    case "TABLE": return 17;
                    case "CHAR": return 18;
                    case "NUMERIC": return 19;
                    case "NOT": return 20;
                    case "NULL": return 21;
                    case "CONSTRAINT": return 22;
                    case "KEY": return 23;
                    case "PRIMARY": return 24;
                    case "FOREIGN": return 25;
                    case "REFERENCES": return 26;
                    case "INSERT": return 27;
                    case "INTO": return 28;
                    case "VALUES": return 29;
                    default: return 0;
                }
            }

            if (lexema.matches("'[^']*'") || lexema.matches("\\d+")) {
                return contadorConstantes++; // Asigna el código para constante (numérica o alfanumérica)
            }
            // Si no es una palabra reservada, asignamos un código único a los identificadores
            if (lexema.matches("[\\w#]+")) {
            	if (identificadoresMap.containsKey(lexema)) {
                    return identificadoresMap.get(lexema); // Si ya existe, devolver el código existente
                } else {
                    // Si no existe, asignar un nuevo código y almacenarlo en el mapa
                    int codigo = contadorIdentificadores++;
                    identificadoresMap.put(lexema, codigo);
                    return codigo;
                }
            }
            
            // Si no es ninguna de las anteriores, asignamos el código según el lexema
            switch (lexema) {
                case ",": return 50;
                case ".": return 51;
                case "(": return 52;
                case ")": return 53;
                case "'": return 54;
                case "+": return 70;
                case "-": return 71;
                case "*": return 72;
                case "/": return 73;
                case "=": return 83;
                case ">": return 81;
                case "<": return 82;
                case ">=": return 84;
                case "<=": return 85;
                default: return 0;
            }
        }
    }

    public static class Identificador {
        private String nombre;
        private int linea;
        private static Map<String, Integer> identificadoresMap = new HashMap<>(); // Mapa para almacenar identificadores
        private static int contadorIdentificadores = 401;

        public Identificador(String nombre, int linea) {
            this.nombre = nombre;
            this.linea = linea;
        }

        public String getNombre() {
            return nombre;
        }

        public int getLinea() {
            return linea;
        }

        public int getValor() {
        	if (identificadoresMap.containsKey(nombre)) {
                return identificadoresMap.get(nombre); // Si ya existe, devolver el código existente
            } else {
                // Si no existe, asignar un nuevo código
                int codigo = contadorIdentificadores++;
                identificadoresMap.put(nombre, codigo); // Guardar el identificador con su código
                return codigo;
            }
        }
    }

    public static class Constante {
        private String valor;
        private int linea;

        public Constante(String valor, int linea) {
            this.valor = valor;
            this.linea = linea;
        }

        public String getValor() {
            return valor;
        }

        public int getLinea() {
            return linea;
        }

        public int getTipo() {
            return valor.matches("\\d+") ? 61 : 62; // 61 para numéricos, 62 para alfanuméricos
        }

        public int getCodigo() {
            return 601 + constantes.indexOf(this);
        }
    }
}