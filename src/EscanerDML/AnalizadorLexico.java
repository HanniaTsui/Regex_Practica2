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
            // Eliminar el punto y coma al final de la sentencia para evitar errores en el análisis léxico
            sentenciaSQL = sentenciaSQL.trim().substring(0, sentenciaSQL.trim().length() - 1);
        }

        // Expresiones regulares para identificar tokens, identificadores y constantes
        String patronToken = "\\b(SELECT|FROM|WHERE|AND|OR|CREATE|TABLE|CHAR|NUMERIC|NOT|NULL|"
                + "CONSTRAINT|KEY|PRIMARY|FOREIGN|REFERENCES|INSERT|INTO|VALUES)\\b|"
                + ">=|<=|<>|=|>|<|\\*|,|\\(|\\)|'[^']*'|\\d+\\w*|\\w+";

        Pattern pattern = Pattern.compile(patronToken);
        Matcher matcher = pattern.matcher(sentenciaSQL);

        int linea = 1; 
        int posicionInicio = 0;

        // Recorrer la sentencia SQL para contar los saltos de línea
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
            } else if (lexema.matches("\\d+")) {
                // Es una constante numérica
                constantes.add(new Constante(lexema, linea));
            } else if (lexema.matches("'[^']*'")) {
                // Es una constante alfanumérica (cadena entre comillas simples)
                constantes.add(new Constante(lexema, linea));
            } else  if (lexema.matches("\\w+")) {
                // Es un identificador
                if (lexema.matches("^\\d.*")) {
                    // Identificador que comienza con un número (inválido)
                    errores.add("Línea " + linea + ": '" + lexema + "' : Identificador inválido (no puede comenzar con un número)");
                } else {
                    identificadores.add(new Identificador(lexema, linea));
                }
            

            }
        }

        // Verificar errores léxicos
        verificarSimbolosDesconocidos(sentenciaSQL, linea);
        verificarCadenasMalFormateadas(sentenciaSQL, linea);
        verificarPalabrasReservadasMalEscritas();
        verificarOperadoresNoValidos(sentenciaSQL, linea);

        // Si no hay tokens válidos, agregar un error
        if (tokens.isEmpty()) {
            errores.add("Error léxico: La sentencia SQL no contiene tokens válidos.");
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
                errores.add("Línea " + linea + ": '" + cadenaMalFormateada + "' : Cadena mal formateada");
                dentroDeCadena = false; // Reiniciamos el estado
            }
        }
    }

    private void verificarOperadoresNoValidos(String sentenciaSQL, int linea) {
        // Solo marcar como error operadores no válidos, como "=>", "=<", "><"
        Pattern operadoresNoValidos = Pattern.compile("=>|=<|><");
        Matcher matcherOperadores = operadoresNoValidos.matcher(sentenciaSQL);
        while (matcherOperadores.find()) {
            errores.add("Línea " + linea + ": '" + matcherOperadores.group() + "' : Operador no válido");
            System.out.println("Línea " + linea + ": '" + matcherOperadores.group() + "' : Operador no válido");
        }
    }

    private void verificarSimbolosDesconocidos(String sentenciaSQL, int linea) {
        Pattern simbolosDesconocidos = Pattern.compile("[#\\$@]");
        Matcher matcherSimbolos = simbolosDesconocidos.matcher(sentenciaSQL);
        while (matcherSimbolos.find()) {
            errores.add("Línea " + linea + ": '" + matcherSimbolos.group() + "' : Símbolo desconocido");
            System.out.println("Línea " + linea + ": '" + matcherSimbolos.group() + "' : Símbolo desconocido");
        }
    }

    private void verificarPalabrasReservadasMalEscritas() {
        for (Token token : tokens) {
            String lexema = token.getLexema().toUpperCase();
            if (!PALABRAS_RESERVADAS.contains(lexema)) {
                for (String palabraReservada : PALABRAS_RESERVADAS) {
                    if (calcularDistanciaLevenshtein(lexema, palabraReservada) == 1) {
                        errores.add("Línea " + token.getLinea() + ": '" + lexema + ": Error palabra reservada mal escrita " + palabraReservada);
                        System.out.println("Línea " + token.getLinea() + ": '" + lexema + ": Error palabra reservada mal escrita " + palabraReservada);
                        break;
                    }
                }
            }
        }
    }


    private boolean esPosiblePalabraReservadaMalEscrita(String lexema) {
        // Convertir el lexema a mayúsculas para comparación insensible a mayúsculas/minúsculas
        String lexemaMayusculas = lexema.toUpperCase();

        // Excluir operadores y constantes numéricas de la validación
        if (lexema.matches("=|>|<|>=|<=|<>") || lexema.matches("\\d+")) {
            return false; // No es una palabra reservada mal escrita
        }

        // Verificar si el lexema es similar a una palabra reservada pero no coincide exactamente
        for (String palabraReservada : PALABRAS_RESERVADAS) {
            if (lexemaMayusculas.equals(palabraReservada)) {
                return false; // Coincide exactamente, no es un error
            }
            if (calcularDistanciaLevenshtein(lexemaMayusculas, palabraReservada) <= 1) {
                return true; // Es similar pero no coincide, probablemente mal escrita
            }

        }
        return false;
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
                case "AND": return 14;
                case "OR": return 15;
                case "=": return 83;
                case ">": return 81;
                case "<": return 82;
                case ">=": return 84;
                case "<=": return 85;
                case ",": return 50;
                case "(": return 52;
                case ")": return 53;
                case "'": return 54;
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