import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Xanela extends JFrame
{
    private MainAgent meuAxente;

    /* Paneis de texto */
    private JTextArea panelMatriz = new JTextArea();
    private JTextArea panelInformacion = new JTextArea();
    private JTextArea panelClasificacion = new JTextArea();

    /* Scrolls para ver o contido dos paneis */
    private JScrollPane scrollMatriz = new JScrollPane(this.panelMatriz);
    private JScrollPane scrollInformacion = new JScrollPane(this.panelInformacion);
    private JScrollPane scrollClasificacion = new JScrollPane(this.panelClasificacion);

    /* Botóns */
    private JButton botonNovoXogo = new JButton("Novo xogo");

    public Xanela(MainAgent meuAxente) {
        setTitle("Práctica B");
        this.meuAxente = meuAxente;
        /* Estabelecemos a Xanela para que ocupè todo o monitor */
        Dimension screenDimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screenDimension.getWidth()), (int) (screenDimension.getHeight() * 0.95));

        getContentPane().setBackground(new Color(25, 178, 172));

        setResizable(false); //Evitamos que o usuario poida cambiar o tamaño da xanela
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(null);

        crearMenu();
        crearPanelMatriz();
        crearPanelInformacion();
        crearPanelClasificacion();
        crarBotonNovoXogo();
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
        this.scrollMatriz.setBounds(740, 100, 489, 410);
        add(this.scrollMatriz);
    }

    public void imprimirMatriz(String matrizPraImprimir) {
        //Pomos a matriz no seu recadro
        this.panelMatriz.setText(matrizPraImprimir);
    }

    public void crearPanelInformacion() {
        Font fonte = new Font("Dialog", Font.ITALIC, 10);
        this.panelInformacion.setFont(fonte);
        this.panelInformacion.setBackground(new Color(255, 255, 255));
        this.panelInformacion.setEditable(false); // Evitamos que se poda escribir no panel
        this.scrollInformacion.setBounds(0, 100, 489, 410);
        add(this.scrollInformacion);
    }

    public void imprimirInformacion(String informacionPraImprimir) {
        this.panelInformacion.append(informacionPraImprimir);
    }

    public void crearPanelClasificacion() {
        Font fonte = new Font("Dialog", Font.ITALIC, 10);
        this.panelClasificacion.setFont(fonte);
        this.panelClasificacion.setBackground(new Color(255, 255, 255));
        this.panelClasificacion.setEditable(false); // Evitamos que se poda escribir no panel
        this.scrollClasificacion.setBounds(1433, 100, 489, 410);
        add(this.scrollClasificacion);
    }

    public void imprimirClasificacion(String clasificacionPraImprimir) {
        this.panelClasificacion.setText(clasificacionPraImprimir);
    }

    public void crarBotonNovoXogo() {
        int x = 200;
        int y = 810;
        int anchura = 400;
        int altura = 80;
        this.botonNovoXogo.setBounds(x, y, anchura, altura);
        add(this.botonNovoXogo);
        this.botonNovoXogo.addActionListener( (ActionEvent e) -> { // Engadimos a resposta do boton "Novo xogo"
            this.meuAxente.iniciarXogo();
        });
    }
}