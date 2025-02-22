package views;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTable;

public class RegexView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTable table;
	private JLabel lblTitulo;
	private JTextArea textArea;
	private JButton btnAceptar;
	private JButton btnLimpiar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RegexView frame = new RegexView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public RegexView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200,720);
		setTitle("Regex - Programación de sistemas");
		setLocationRelativeTo(null);
		
		String text = "“En el último trimestre del año 2024, la empresa reportó ingresos por 5,000,000.50 dólares,"
				+ " lo que representa un aumento del 7.8% en comparación con el mismo período del año anterior. "
				+ "El número de productos vendidos alcanzó los 120,000, con un promedio de 500 unidades por día. "
				+ "La tasa de retorno de inversión fue del 15.2%, superando las expectativas iniciales del 10%. Además, "
				+ "se estableció una meta de crecimiento del 20% para el próximo año, proyectando ventas de más de 6 millones "
				+ "de unidades. Los costos operativos aumentaron un 3.5%, pero el margen de ganancias sigue siendo saludable, "
				+ "con un 25%. En cuanto a la eficiencia energética, la planta redujo el consumo de electricidad en un 12.4%, "
				+ "lo que se traduce en un ahorro de aproximadamente $150,000 anuales. En el departamento de marketing, el "
				+ "presupuesto se incrementó en un 8%, alcanzando los $1,200,000 para cubrir campañas internacionales. "
				+ "La empresa también planea abrir 10 nuevas tiendas, lo que generará alrededor de 500 empleos. La duración "
				+ "promedio de los proyectos fue de 8.5 meses, lo que demuestra un considerable avance en la optimización de "
				+ "los procesos. Los números de atención al cliente mostraron una mejora del 4.3%, con más de 95,000 consultas "
				+ "resueltas. En el último mes, el precio de las acciones subió un 2.5%, alcanzando un valor de $75.25 por acción. "
				+ "Finalmente, el porcentaje de satisfacción de los empleados ha mejorado al 89.7%, un reflejo del esfuerzo por "
				+ "mantener un entorno laboral saludable.”"; 
		
        String[] lines = text.split("\n");
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setBackground(new Color(233, 241, 252 ));
		contentPane.setLayout(null);
		
		lblTitulo = new JLabel("Regex - Programación");
		lblTitulo.setOpaque(true);
		lblTitulo.setBackground(new Color(108, 196, 240));
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setBounds(30, 25, 1130, 65);
		contentPane.add(lblTitulo);
		
		// Crear JTextArea con el texto
		textArea = new JTextArea(text);
		textArea.setFont(new Font("Tahoma", Font.PLAIN, 15));
		textArea.setBackground(Color.WHITE);
		textArea.setLineWrap(true); // Ajusta el texto a la línea
		textArea.setWrapStyleWord(true); // Evita cortes de palabra
		textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 

		JScrollPane scrollTextArea = new JScrollPane(textArea);
		scrollTextArea.setBounds(30, 120, 550, 476);
		contentPane.add(scrollTextArea);

		table = new JTable();
		JScrollPane tablaScroll = new JScrollPane(table);
		tablaScroll.setFont(new Font("Tahoma", Font.PLAIN, 12));
		tablaScroll.setBounds(640, 120, 520, 476);
		contentPane.add(tablaScroll);
		setResizable(false);
		
		btnAceptar = new JButton("Aceptar");
		btnAceptar.setFocusable(false);
		btnAceptar.setBackground(new Color(115, 232, 109));
		btnAceptar.setBounds(157, 622, 130, 30);
		contentPane.add(btnAceptar);
		
		btnLimpiar = new JButton("Limpiar");
		btnLimpiar.setFocusable(false);
		btnLimpiar.setBounds(317, 622, 130, 30);
		btnLimpiar.setBackground(new Color(49, 138, 255));
		contentPane.add(btnLimpiar);
		
		
		
	}
}
