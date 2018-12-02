import javax.swing.*;
import java.awt.*;

public class Xanela extends JFrame
{
	/* Paneis de texto */
	private JTextArea panelMatriz = new JTextArea();

	/* Scrolls para ver o contido dos paneis */
	private JScrollPane scrollMatriz = new JScrollPane(this.panelMatriz);

	public Xanela(MainAgent meuAxente) {
		setTitle("Práctica B");

		/* Estabelecemos a Xanela para que ocupè todo o monitor */
		Dimension screenDimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setSize((int) (screenDimension.getWidth()), (int) (screenDimension.getHeight() * 0.95));

		getContentPane().setBackground(new Color(25, 178, 172));

		setResizable(false); //Evitamos que o usuario poida cambiar o tamaño da xanela
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setLayout(null);

		crearMenu();
		crearPanelMatriz();
	}

	public void crearMenu() {
		//Creamos os menus despregabres
		JMenu opcions = new JMenu("Opcións");
		JMenu axuda = new JMenu("Axuda");

		JMenuBar barraMenu = new JMenuBar();
		barraMenu.add(opcions);
		barraMenu.add(Box.createHorizontalGlue()); // Pomos o boton axuda a dereita
		barraMenu.add(axuda);
		setJMenuBar(barraMenu);
	}

	public void crearPanelMatriz() {
		Font fonte = new Font("Dialog", Font.BOLD, 29);
			this.panelMatriz.setFont(fonte);
			this.panelMatriz.setBackground(new Color(255, 255, 255));
			this.panelMatriz.setEditable(false); // Evitamos que se poda escribir no panel
			this.scrollMatriz.setBounds(800, 100, 489, 410);
			add(this.scrollMatriz);
	}

	public void imprimirMatriz(String matrizPraImprimir) {
		//Pomos a matriz no seu recadro
		this.panelMatriz.setText(matrizPraImprimir);
	}
}