package views;

import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.ActionEvent;

public class RegexView extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel panelContenido;
    private JTable tablaResultados;
    private JTable tablaInvalidos;
    private JLabel etiquetaTitulo;
    private JTextArea areaTexto;
    private JButton botonAceptar;
    private JButton botonLimpiar;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    RegexView ventana = new RegexView();
                    ventana.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public RegexView() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setTitle("Regex - Programación de sistemas");
        setLocationRelativeTo(null);

        String texto = "En el último trimestre del año 2024, la empresa reportó ingresos por 5,000,000.50 dólares, lo\r\n"
                + "que representa un aumento del 7.8% en comparación con el mismo período del año anterior. El\r\n"
                + "número de productos vendidos alcanzó los 120,000, con un promedio de 500 unidades por día.\r\n"
                + "La tasa de retorno de inversión fue del 15.2%, superando las expectativas iniciales del 10%. \r\n"
                + "Además, se estableció una meta de crecimiento del 20% para el próximo año, proyectando\r\n"
                + "ventas de más de 6 millones de unidades. Los costos operativos aumentaron un 3.5%, pero el \r\n"
                + "margen de ganancias sigue siendo saludable, con un 25%. En cuanto a la eficiencia energética, \r\n"
                + "la planta redujo el consumo de electricidad en un 12.4%, lo que se traduce en un ahorro de\r\n"
                + "aproximadamente $150,000 anuales. En el departamento de marketing, el presupuesto se\r\n"
                + "incrementó en un 8%, alcanzando los $1,200,000 para cubrir campañas internacionales. La\r\n"
                + "empresa también planea abrir 10 nuevas tiendas, lo que generará alrededor de 500 empleos. La\r\n"
                + "duración promedio de los proyectos fue de 8.5 meses, lo que demuestra un considerable avance\r\n"
                + "en la optimización de los procesos. Los números de atención al cliente mostraron una mejora \r\n"
                + "del 4.3%, con más de 95,000 consultas resueltas. En el último mes, el precio de las acciones \r\n"
                + "subió un 2.5%, alcanzando un valor de $75.25 por acción. Finalmente, el porcentaje de \r\n"
                + "satisfacción de los empleados ha mejorado al 89.7%, un reflejo del esfuerzo por mantener un \r\n"
                + "entorno laboral saludable.";

        panelContenido = new JPanel();
        panelContenido.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(panelContenido);
        panelContenido.setBackground(new Color(233, 241, 252));
        panelContenido.setLayout(null);

        etiquetaTitulo = new JLabel("Regex - Programación");
        etiquetaTitulo.setOpaque(true);
        etiquetaTitulo.setBackground(new Color(108, 196, 240));
        etiquetaTitulo.setFont(new Font("Tahoma", Font.BOLD, 18));
        etiquetaTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        etiquetaTitulo.setBounds(30, 25, 1130, 65);
        panelContenido.add(etiquetaTitulo);

        areaTexto = new JTextArea(texto);
        areaTexto.setFont(new Font("Tahoma", Font.PLAIN, 13));
        areaTexto.setBackground(Color.WHITE);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollAreaTexto = new JScrollPane(areaTexto);
        scrollAreaTexto.setBounds(30, 120, 580, 476);
        panelContenido.add(scrollAreaTexto);

        JTabbedPane pestañasTablas = new JTabbedPane();
        pestañasTablas.setBounds(640, 120, 520, 480);
        panelContenido.add(pestañasTablas);

        tablaResultados = new JTable();
        JScrollPane scrollTablaResultados = new JScrollPane(tablaResultados);
        pestañasTablas.addTab("Resultados Válidos", scrollTablaResultados);

        tablaInvalidos = new JTable();
        JScrollPane scrollTablaInvalidos = new JScrollPane(tablaInvalidos);
        pestañasTablas.addTab("Resultados Inválidos", scrollTablaInvalidos);

        setResizable(false);

        botonAceptar = new JButton("Aceptar");
        botonAceptar.setFocusable(false);
        botonAceptar.setBackground(new Color(115, 232, 109));
        botonAceptar.setBounds(157, 622, 130, 30);
        panelContenido.add(botonAceptar);

        botonLimpiar = new JButton("Limpiar");
        botonLimpiar.setFocusable(false);
        botonLimpiar.setBounds(317, 622, 130, 30);
        botonLimpiar.setBackground(new Color(49, 138, 255));
        panelContenido.add(botonLimpiar);

        botonAceptar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String texto = areaTexto.getText();
                List<String[]> resultados = RegexAnalisis.analizarTexto(texto);
                List<String[]> invalidos = RegexAnalisis.obtenerInvalidos(texto);

                // Modelo para la tabla de resultados válidos
                DefaultTableModel modelo = new DefaultTableModel();
                modelo.setColumnIdentifiers(new String[]{"No.", "No. Línea", "Cadena", "Tipo"});

                int indexValido = 1; 
                for (String[] fila : resultados) {
                    modelo.addRow(new String[]{String.valueOf(indexValido++), fila[1], fila[2], fila[3]});
                }
                tablaResultados.setModel(modelo);

                // Modelo para la tabla de resultados inválidos
                DefaultTableModel modeloInvalidos = new DefaultTableModel();
                modeloInvalidos.setColumnIdentifiers(new String[]{"No.", "No. Línea", "Cadena", "Tipo"});

                int indexInvalido = 1; // Contador para los resultados inválidos
                for (String[] fila : invalidos) {
                    modeloInvalidos.addRow(new String[]{String.valueOf(indexInvalido++), fila[1], fila[2], fila[3]});
                }
                tablaInvalidos.setModel(modeloInvalidos);
            }
        });


        botonLimpiar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                areaTexto.setText("");
                tablaResultados.setModel(new DefaultTableModel());
                tablaInvalidos.setModel(new DefaultTableModel());
            }
        });
    }
}