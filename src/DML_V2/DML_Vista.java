package DML_V2;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

import DML_V2.Analizador_Lexico.Constante;
import DML_V2.Analizador_Lexico.Identificador;
import DML_V2.Analizador_Lexico.Token;
import EscanerDML.AnalizadorLexico;



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

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                	DML_Vista  ventana = new DML_Vista ();
                    ventana.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public DML_Vista () {
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

     // Actualizar el método actionPerformed del botonAceptar en la clase DML_Vista
        botonAceptar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String sentenciaSQL = areaTexto.getText();
                Analizador_Lexico analizador = new Analizador_Lexico();
                analizador.analizar(sentenciaSQL);

                // Limpiar las tablas antes de agregar nuevos datos
                DefaultTableModel modeloTablaErrores = new DefaultTableModel();
                modeloTablaErrores.addColumn("No. de línea");
                modeloTablaErrores.addColumn("Lexema inválido");
                modeloTablaErrores.addColumn("Tipo");
                tablaErrores.setModel(modeloTablaErrores);

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
                    tablaLexica.setModel(new DefaultTableModel());
                    tablaIdentificadores.setModel(new DefaultTableModel());
                    tablaConstantes.setModel(new DefaultTableModel());
                } else {
                    // Si no hay errores, mostrar las otras tablas
                    // Mostrar tokens en la tabla léxica
                    DefaultTableModel modeloTablaLexica = new DefaultTableModel();
                    modeloTablaLexica.addColumn("No.");
                    modeloTablaLexica.addColumn("Línea");
                    modeloTablaLexica.addColumn("TOKEN");
                    modeloTablaLexica.addColumn("Tipo");
                    modeloTablaLexica.addColumn("Código");   

                    int contador = 1;
                    for (Token token : analizador.getTokens()) {
                        modeloTablaLexica.addRow(new Object[]{
                            contador++,
                            token.getLinea(),
                            token.getLexema(),
                            token.getTipo(),
                            token.getCodigo(), 
                        });
                    }
                    tablaLexica.setModel(modeloTablaLexica);
                /* // Mostrar la tabla léxica en la consola
                    System.out.println("Tabla Léxica:");
                    System.out.println("+----+-------+----------------+----------------+--------+");
                    System.out.println("| No.| Línea | Lexema         | Tipo           | Código |");
                    System.out.println("+----+-------+----------------+----------------+--------+");

                    for (int i = 0; i < modeloTablaLexica.getRowCount(); i++) {
                        int numero = (int) modeloTablaLexica.getValueAt(i, 0); // No.
                        int linea = (int) modeloTablaLexica.getValueAt(i, 1);  // Línea
                        String lexema = (String) modeloTablaLexica.getValueAt(i, 2); // Lexema
                        int tipo = (int) modeloTablaLexica.getValueAt(i, 3); // Tipo
                        int codigo = (int) modeloTablaLexica.getValueAt(i, 4); // Código

                        // Formatear la salida
                        System.out.printf("| %-2d | %-5d | %-14s | %-14d | %-6d |\n", numero, linea, lexema, tipo, codigo);
                    }

                    System.out.println("+----+-------+----------------+----------------+--------+");
*/
                 // Mostrar identificadores en la tabla de identificadores
                    DefaultTableModel modeloTablaIdentificadores = new DefaultTableModel();
                    modeloTablaIdentificadores.addColumn("Identificador");
                    modeloTablaIdentificadores.addColumn("Valor");
                    modeloTablaIdentificadores.addColumn("Línea");

                    // Mapa temporal para evitar duplicados (clave: valor del identificador, valor: [nombre, líneas])
                    TreeMap<Integer, String[]> mapaIdentificadores = new TreeMap<>();

                    for (Identificador identificador : analizador.getIdentificadores()) {
                        String nombre = identificador.getNombre();
                        int valor = identificador.getValor(); // Se usa como clave para ordenar automáticamente
                        String linea = Integer.toString(identificador.getLinea()); 

                        if (mapaIdentificadores.containsKey(valor)) {
                            // Si ya existe, actualizamos la lista de líneas
                            String[] datos = mapaIdentificadores.get(valor);
                            if (!datos[1].contains(linea)) { // Evita líneas repetidas
                                datos[1] += "," + linea;
                            }
                        } else {
                            // Si no existe, lo agregamos normalmente
                            mapaIdentificadores.put(valor, new String[]{nombre, linea});
                        }
                    }

                    // Agregar datos ordenados a la tabla
                    for (Map.Entry<Integer, String[]> entry : mapaIdentificadores.entrySet()) {
                        modeloTablaIdentificadores.addRow(new Object[]{
                            entry.getValue()[0], // Nombre del identificador
                            entry.getKey(),       // Valor del identificador (ordenado)
                            entry.getValue()[1]   // Líneas unificadas
                        });
                    }

                    tablaIdentificadores.setModel(modeloTablaIdentificadores);


                    // Mostrar constantes en la tabla de constantes
                    DefaultTableModel modeloTablaConstantes = new DefaultTableModel();
                    modeloTablaConstantes.addColumn("No.");
                    modeloTablaConstantes.addColumn("Constante");
                    modeloTablaConstantes.addColumn("Tipo");
                    modeloTablaConstantes.addColumn("Valor");

                    contador = 1;
                    for (Constante constante : analizador.getConstantes()) {
                        modeloTablaConstantes.addRow(new Object[]{
                            contador++,
                            constante.getValor(),
                            constante.getTipo(),
                            constante.getCodigo()
                        });
                    }
                    tablaConstantes.setModel(modeloTablaConstantes);
                }
            }
        });
        botonLimpiar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                areaTexto.setText("");
                tablaLexica.setModel(new DefaultTableModel());
                tablaIdentificadores.setModel(new DefaultTableModel());
                tablaConstantes.setModel(new DefaultTableModel());
                tablaErrores.setModel(new DefaultTableModel());
            }
        });
    }
}