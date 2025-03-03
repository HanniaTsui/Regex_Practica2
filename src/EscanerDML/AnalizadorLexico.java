package EscanerDML;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorLexico {

    private List<Token> tokens;
    private static List<Identificador> identificadores;
    private static List<Constante> constantes;
    private List<String> errores;

    // Lista de palabras reservadas de SQL
    private static final List<String> PALABRAS_RESERVADAS = Arrays.asList(
        "SELECT", "FROM", "WHERE", "AND", "OR", "CREATE", "TABLE", "CHAR", "NUMERIC", "NOT", "NULL",
        "CONSTRAINT", "KEY", "PRIMARY", "FOREIGN", "REFERENCES", "INSERT", "INTO", "VALUES"
    );

    public AnalizadorLexico() {
        tokens = new ArrayList<>();
        identificadores = new ArrayList<>();
        constantes = new ArrayList<>();
        errores = new ArrayList<>();
    }

    public void analizar(String sentenciaSQL) {
        // Limpiar listas antes de un nuevo análisis
        tokens.clear();
        identificadores.clear();
        constantes.clear();
        errores.clear();

        // Contar el número de líneas en la sentencia SQL
        int totalLineas = sentenciaSQL.split("\n").length;

        // Verificar si la sentencia SQL termina con ';'
        if (!sentenciaSQL.trim().endsWith(";")) {
            errores.add("Línea " + totalLineas + ": La sentencia no termina con ';' : Error de sintaxis");
        } else {
            sentenciaSQL = sentenciaSQL.trim().substring(0, sentenciaSQL.trim().length() - 1);
        }

        // Validar que la sentencia inicie con una palabra reservada
        String primeraPalabra = sentenciaSQL.trim().split("\\s+")[0].toUpperCase();
        if (!PALABRAS_RESERVADAS.contains(primeraPalabra)) {
            errores.add("Línea 1: La sentencia debe comenzar con una palabra clave válida : Error de sintaxis");
            
        }

        // Expresiones regulares para identificar tokens, identificadores y constantes
        String patronToken = "\\b(SELECT|FROM|WHERE|AND|OR|CREATE|TABLE|CHAR|NUMERIC|NOT|NULL|"
                + "CONSTRAINT|KEY|PRIMARY|FOREIGN|REFERENCES|INSERT|INTO|VALUES)\\b|"
                + ">=|<=|<>|=|>|<|\\*|,|\\(|\\)|'[^']*'|\\d+\\w*|\\w+";
        /*
         * >=|<=|<>|=|>|< Operadores de comparación
         * \\* Caracter *
         * , Coma
         * \\( y \\) Para paréntesis
         * '[^']*' Cadenas de texto entre comillas simples
         * \\d+\\w* Uno o mas dígito y cero o más caracteres de palabra
         * \\w+ Secuencia de uno o más caracteres de palabra
         */
         
        Pattern pattern = Pattern.compile(patronToken);
        Matcher matcher = pattern.matcher(sentenciaSQL);

        int linea = 1;
        int posicionInicio = 0;
        String tokenAnterior =  null;

        while (matcher.find()) {
            String lexema = matcher.group();
            Token token = new Token(lexema, linea);
            tokens.add(token);

            // Contar los saltos de línea antes del lexema actual
            for (int i = posicionInicio; i < matcher.start(); i++) {
                if (sentenciaSQL.charAt(i) == '\n') {
                    linea++;
                }
            }
            posicionInicio = matcher.end();

            // Clasificar el lexema
            if (PALABRAS_RESERVADAS.contains(lexema.toUpperCase())) {
                // Es una palabra reservada, no se agrega a identificadores
            } else if (lexema.matches("\\d+")) { // Uno o más digitos
                constantes.add(new Constante(lexema, linea));
            } else if (lexema.matches("'[^']*'")) { //Cadenas de texto entre comillas simples
                constantes.add(new Constante(lexema, linea));
            } else  if (lexema.matches("\\w+")) { //Uno o más caracteres de palabra
                // Es un identificador 
                if (lexema.matches("^\\d.*")) { //Comienza con numero al principio de la cadena
                    // Identificador que comienza con un número (inválido)
                    errores.add("Línea " + linea + ": '" + lexema + "' : Error de sintaxis. Identificador inválido (no puede comenzar con un número)");
                } else {
                    identificadores.add(new Identificador(lexema, linea));
                }
                
                if (tokenAnterior != null && esOperador(tokenAnterior)) {
                    errores.add("Línea " + linea + ": '" + lexema + "' : Error de sintaxis. Constante alfanumérica debe estar entre comillas simples");
                }                
            } 
            
            tokenAnterior = lexema;
        }
        verificarIdentificadoresEntreComillas(sentenciaSQL, linea);
        verificarSimbolosDesconocidos(sentenciaSQL, linea);
        verificarCadenasMalFormateadas(sentenciaSQL, linea);
        verificarPalabrasReservadasMalEscritas();
        verificarOperadoresNoValidos(sentenciaSQL, linea);
        verificarComasEntreIdentificadores();
        verificarAgrupacionCondiciones();
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
        Pattern simbolosDesconocidos = Pattern.compile("[\\$@]"); //Caracteres no validos
        Matcher matcherSimbolos = simbolosDesconocidos.matcher(sentenciaSQL);
        while (matcherSimbolos.find()) {
            errores.add("Línea " + linea + ": '" + matcherSimbolos.group() + "' : Error de sintaxis. Símbolo desconocido");
            System.out.println("Línea " + linea + ": '" + matcherSimbolos.group() + "' : Símbolo desconocido");
        }
    }

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
    }

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
            } else if (lexema.matches("\\w+")) {
                return 4; // Identificadores
            } else if (lexema.matches("'[^']*'")) {
                return 6; // Constantes alfanuméricas
            } else if (lexema.matches("\\d+")) {
                return 6; // Constantes numéricas
            } else if (lexema.matches(",|\\(|\\)|'")) {
                return 5; // Delimitadores
            } else if (lexema.matches("=|>|<|>=|<=|<>")) {
                return 8; // Operadores relacionales
            } else {
                return 0; // Desconocido
            }
        }

        public int getCodigo() {
            // Definir el código según el lexema
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
            // Asignar un valor único a cada identificador
            return 401 + identificadores.indexOf(this);
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
            return 600 + constantes.indexOf(this);
        }
    }
}