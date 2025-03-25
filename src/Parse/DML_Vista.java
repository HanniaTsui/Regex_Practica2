package Parse;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;


public class DML_Vista extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel panelContenido;
    private JTable tablaLexica;
    private JTable tablaIdentificadores;
    private JTable tablaConstantes;
    private JLabel etiquetaTitulo;
    private JTextArea areaTexto;
    private JButton botonAceptar;
    private JButton botonLimpiar;
    private JTable tablaErrores;
    private DefaultTableModel modeloTablaErrores;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    DML_Vista ventana = new DML_Vista();
                    ventana.setVisible(true);
                    
                    Analizador_Lexico lexer = new Analizador_Lexico();
                    
                    lexer.analizar("SELECT ANOMBRE FROM ALUMNOS WHERE A# = 'valor' + 5");
                    System.out.println("SELECT ANOMBRE\r\n"
                    		+ "FROM ALUMNOS,INSCRITOS,\r\n"
                    		+ "WHERE ALUMNOS.A#=INSCRITOS.A# AND\r\n"
                    		+ "INSCRITOS.SEMESTRE='2010I'");

                    // Create and run syntactic analyzer
                    Analizador_Sintactico parser = new Analizador_Sintactico(lexer);
                    parser.analizar();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public DML_Vista() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setTitle("AV01_Escáner DML - Programación de sistemas");
        setLocationRelativeTo(null);

        String sentenciaSQL = "SELECT ANOMBRE, CALIFICACION, TURNO\n"
                + "FROM ALUMNOS, INSCRITOS, MATERIAS, CARRERAS\n"
                + "WHERE MNOMBRE='LENAUT2' AND TURNO = 'TM' AND CNOMBRE='ISC'\n"
                + "AND SEMESTRE='2023I' AND CALIFICACION >= 70";

        panelContenido = new JPanel();
        panelContenido.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(panelContenido);
        panelContenido.setBackground(new Color(233, 252, 240));
        panelContenido.setLayout(null);

        etiquetaTitulo = new JLabel("Escáner DML");
        etiquetaTitulo.setOpaque(true);
        etiquetaTitulo.setBackground(new Color(137, 240, 108));
        etiquetaTitulo.setFont(new Font("Tahoma", Font.BOLD, 18));
        etiquetaTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        etiquetaTitulo.setBounds(30, 25, 1130, 65);
        panelContenido.add(etiquetaTitulo);

        areaTexto = new JTextArea(sentenciaSQL);
        areaTexto.setFont(new Font("Tahoma", Font.PLAIN, 13));
        areaTexto.setBackground(Color.WHITE);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollAreaTexto = new JScrollPane(areaTexto);
        scrollAreaTexto.setBounds(30, 120, 580, 217);
        panelContenido.add(scrollAreaTexto);

        JTabbedPane pestañasTablas = new JTabbedPane();
        pestañasTablas.setBounds(640, 120, 520, 510);
        panelContenido.add(pestañasTablas);

        tablaLexica = new JTable();
        JScrollPane scrollTablaLexica = new JScrollPane(tablaLexica);
        pestañasTablas.addTab("Tabla Léxica", scrollTablaLexica);

        tablaIdentificadores = new JTable();
        JScrollPane scrollTablaIdentificador = new JScrollPane(tablaIdentificadores);
        pestañasTablas.addTab("Tabla de identificadores", scrollTablaIdentificador);

        tablaConstantes = new JTable();
        JScrollPane scrollTablaConstantes = new JScrollPane(tablaConstantes);
        pestañasTablas.addTab("Tabla de constantes", scrollTablaConstantes);

        setResizable(false);

        botonAceptar = new JButton("Aceptar");
        botonAceptar.setFont(new Font("Tahoma", Font.BOLD, 16));
        botonAceptar.setForeground(new Color(255, 255, 255));
        botonAceptar.setFocusable(false);
        botonAceptar.setBackground(new Color(31, 203, 23));
        botonAceptar.setBounds(157, 358, 130, 30);
        panelContenido.add(botonAceptar);

        botonLimpiar = new JButton("Limpiar");
        botonLimpiar.setFont(new Font("Tahoma", Font.BOLD, 16));
        botonLimpiar.setForeground(new Color(255, 255, 255));
        botonLimpiar.setFocusable(false);
        botonLimpiar.setBounds(317, 358, 130, 30);
        botonLimpiar.setBackground(new Color(49, 59, 255));
        panelContenido.add(botonLimpiar);

        tablaErrores = new JTable();
        modeloTablaErrores = new DefaultTableModel();
        modeloTablaErrores.addColumn("No. de línea");
        modeloTablaErrores.addColumn("Lexema inválido");
        modeloTablaErrores.addColumn("Tipo");
        tablaErrores.setModel(modeloTablaErrores);

        JScrollPane scrollTablaErrores = new JScrollPane(tablaErrores);
        scrollTablaErrores.setBounds(30, 432, 578, 198);
        panelContenido.add(scrollTablaErrores);

        JLabel lblErrores = new JLabel("Errores");
        lblErrores.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblErrores.setHorizontalAlignment(SwingConstants.CENTER);
        lblErrores.setOpaque(true);
        lblErrores.setBackground(new Color(137, 240, 108));
        lblErrores.setBounds(30, 409, 97, 25);
        panelContenido.add(lblErrores);

        // Configurar el formato de las columnas de la tabla de errores
        configurarFormatoColumnasErrores();

        botonAceptar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String sentenciaSQL = areaTexto.getText();
                Analizador_Lexico analizador = new Analizador_Lexico();
                analizador.analizar(sentenciaSQL);

                // Limpiar las tablas antes de agregar nuevos datos
                modeloTablaErrores.setRowCount(0); // Limpiar la tabla de errores
                DefaultTableModel modeloTablaLexica = new DefaultTableModel();
                modeloTablaLexica.addColumn("No.");
                modeloTablaLexica.addColumn("Línea");
                modeloTablaLexica.addColumn("TOKEN");
                modeloTablaLexica.addColumn("Tipo");
                modeloTablaLexica.addColumn("Código");
                tablaLexica.setModel(modeloTablaLexica);

                DefaultTableModel modeloTablaIdentificadores = new DefaultTableModel();
                modeloTablaIdentificadores.addColumn("Identificador");
                modeloTablaIdentificadores.addColumn("Valor");
                modeloTablaIdentificadores.addColumn("Línea");
                tablaIdentificadores.setModel(modeloTablaIdentificadores);

                DefaultTableModel modeloTablaConstantes = new DefaultTableModel();
                modeloTablaConstantes.addColumn("No.");
                modeloTablaConstantes.addColumn("Constante");
                modeloTablaConstantes.addColumn("Tipo");
                modeloTablaConstantes.addColumn("Valor");
                tablaConstantes.setModel(modeloTablaConstantes);

                boolean hayErrores = false;
                for (String error : analizador.getErrores()) {
                    String[] partesError = error.split(":");
                    if (partesError.length >= 3) { // Verificar que hay al menos 3 partes
                        modeloTablaErrores.addRow(new Object[]{
                                partesError[0], // Línea
                                partesError[1], // Lexema inválido
                                partesError[2]  // Tipo de error
                        });
                        hayErrores = true;
                    } else {
                        // Si el error no tiene el formato esperado, mostrarlo completo en una sola columna
                        modeloTablaErrores.addRow(new Object[]{
                                "N/A", // Línea no disponible
                                error, // Mostrar el error completo
                                "N/A"  // Tipo no disponible
                        });
                        hayErrores = true;
                    }
                }

                if (hayErrores) {
                    // Mostrar solo la tabla de errores
                    tablaErrores.setModel(modeloTablaErrores);
                } else {
                    // Si no hay errores, mostrar las otras tablas
                    // Mostrar tokens en la tabla léxica
                    int contador = 1;
                    for (Parse.Analizador_Lexico.Token token : analizador.getTokens()) {
                        modeloTablaLexica.addRow(new Object[]{
                                contador++,
                                token.getLinea(),
                                token.getLexema(),
                                token.getTipo(),
                                token.getCodigo(),
                        });
                    }

                    // Mostrar identificadores en la tabla de identificadores
                    TreeMap<Integer, String[]> mapaIdentificadores = new TreeMap<>();
                    for (Parse.Analizador_Lexico.Identificador identificador : analizador.getIdentificadores()) {
                        String nombre = identificador.getNombre();
                        int valor = identificador.getValor();
                        String linea = Integer.toString(identificador.getLinea());

                        if (mapaIdentificadores.containsKey(valor)) {
                            String[] datos = mapaIdentificadores.get(valor);
                            if (!datos[1].contains(linea)) {
                                datos[1] += "," + linea;
                            }
                        } else {
                            mapaIdentificadores.put(valor, new String[]{nombre, linea});
                        }
                    }

                    for (Map.Entry<Integer, String[]> entry : mapaIdentificadores.entrySet()) {
                        modeloTablaIdentificadores.addRow(new Object[]{
                                entry.getValue()[0], // Nombre del identificador
                                entry.getKey(),       // Valor del identificador
                                entry.getValue()[1]   // Líneas unificadas
                        });
                    }

                    // Mostrar constantes en la tabla de constantes
                    contador = 1;
                    for (Parse.Analizador_Lexico.Constante constante : analizador.getConstantes()) {
                        modeloTablaConstantes.addRow(new Object[]{
                                contador++,
                                constante.getValor(),
                                constante.getTipo(),
                                constante.getCodigo()
                        });
                    }
                }
            }
        });

        botonLimpiar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                areaTexto.setText("");
                tablaLexica.setModel(new DefaultTableModel());
                tablaIdentificadores.setModel(new DefaultTableModel());
                tablaConstantes.setModel(new DefaultTableModel());

                // Limpiar la tabla de errores y restaurar el formato de las columnas
                configurarFormatoColumnasErrores();
            }
        });
    }

    // Método para configurar el formato de las columnas de la tabla de errores
    private void configurarFormatoColumnasErrores() {
        // Restaurar el modelo de la tabla de errores con las columnas definidas
        modeloTablaErrores = new DefaultTableModel();
        modeloTablaErrores.addColumn("No. de línea");
        modeloTablaErrores.addColumn("Lexema inválido");
        modeloTablaErrores.addColumn("Tipo");
        tablaErrores.setModel(modeloTablaErrores);

        // Configurar el ancho preferido de las columnas
        TableColumn columna;
        columna = tablaErrores.getColumnModel().getColumn(0); // Columna "No. de línea"
        columna.setPreferredWidth(100); // Ancho de 100 píxeles

        columna = tablaErrores.getColumnModel().getColumn(1); // Columna "Lexema inválido"
        columna.setPreferredWidth(100); // Ancho de 300 píxeles

        columna = tablaErrores.getColumnModel().getColumn(2); // Columna "Tipo"
        columna.setPreferredWidth(350); // Ancho de 150 píxeles
    }
}