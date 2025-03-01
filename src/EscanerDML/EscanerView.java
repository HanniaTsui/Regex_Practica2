package EscanerDML;

import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.border.LineBorder;

public class EscanerView extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel panelContenido;
    private JTable tablaLexica;
    private JTable tablaIdentificadores;
    private JTable tablaConstantes;
    private JLabel etiquetaTitulo;
    private JTextArea areaTexto;
    private JButton botonAceptar;
    private JButton botonLimpiar;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                	EscanerView ventana = new EscanerView();
                    ventana.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public EscanerView() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setTitle("AV01_Escáner DML - Programación de sistemas");
        setLocationRelativeTo(null);

        String sentenciaSQL = "SELECT ANOMBRE, CALIFICACION, TURNO\r\n"
        		+ "FROM ALUMNOS, INSCRITOS, MATERIAS, CARRERAS\r\n"
        		+ "WHERE MNOMBRE=’LENAUT2’ AND TURNO = ‘TM’\r\n"
        		+ "AND CNOMBRE=’ISC’ AND SEMESTRE=’2023I’ AND CALIFICACION >= 70";

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
        
        
        JTextArea areaTextoErrores = new JTextArea("");
        areaTextoErrores.setEditable(false);
        areaTextoErrores.setEnabled(false);
        areaTextoErrores.setLineWrap(true);
        areaTextoErrores.setWrapStyleWord(true);
        areaTextoErrores.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        areaTextoErrores.setFont(new Font("Tahoma", Font.PLAIN, 13));
        areaTextoErrores.setBackground(Color.WHITE);
        panelContenido.add(areaTextoErrores);
        
        JScrollPane scrollAreaTextoErrores = new JScrollPane(areaTextoErrores);
        scrollAreaTextoErrores.setBounds(30, 432, 578, 198);
        panelContenido.add(scrollAreaTextoErrores);
        
        JLabel lblErrores = new JLabel("Errores");
        lblErrores.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblErrores.setHorizontalAlignment(SwingConstants.CENTER);
        lblErrores.setOpaque(true);
        lblErrores.setBackground(new Color(137, 240, 108));
        lblErrores.setBounds(30, 409, 97, 25);
        panelContenido.add(lblErrores);

        botonAceptar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               
            }
        });


        botonLimpiar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
            }
        });
    }
}