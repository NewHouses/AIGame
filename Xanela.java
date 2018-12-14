import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

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
    private JButton botonNovoXogo = new JButton("NOVO XOGO");
    private JButton botonPuasa = new JButton("PAUSAR");
    private JButton botonContinuar = new JButton("CONTINUAR");

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
        crearBotonNovoXogo();
        crearBotonPausa();
        crearBotonContinuar();

    }

    public void crearMenu() {
        //Creamos os menus despregabres
        JMenu opcions = new JMenu("Opcións");
        JMenu axuda = new JMenu("Axuda");

        JMenuBar barraMenu = new JMenuBar();

        //Creamos os items do submenu "ǘERBOSE"
        JRadioButtonMenuItem verboseOff = new JRadioButtonMenuItem("OFF", true);
        JRadioButtonMenuItem verboseON = new JRadioButtonMenuItem("ON", false);
        ButtonGroup grupoVerbose = new ButtonGroup();
        grupoVerbose.add(verboseOff);
        grupoVerbose.add(verboseON);

        JMenu verbose = new JMenu("Verbose");
        verbose.add(verboseOff);
        verbose.add(verboseON);

        //Activamos o modo verbose
        verboseON.addActionListener( (ActionEvent e) -> {
            this.meuAxente.setVerboseMode(true);
        });

        //Desactivamos o modo verbose
        verboseOff.addActionListener( (ActionEvent e) -> {
            this.meuAxente.setVerboseMode(false);
        });

        opcions.add(verbose);

        //Creamos os items do submenu "Velocidade"
        JRadioButtonMenuItem lento = new JRadioButtonMenuItem("lento", false);
        JRadioButtonMenuItem medio = new JRadioButtonMenuItem("medio", false);
        JRadioButtonMenuItem rapido = new JRadioButtonMenuItem("rapido", true);
        ButtonGroup grupoVelocidade = new ButtonGroup();
        grupoVelocidade.add(lento);
        grupoVelocidade.add(medio);
        grupoVelocidade.add(rapido);

        JMenu velocidade = new JMenu("Velocidade");
        velocidade.add(lento);
        velocidade.add(medio);
        velocidade.add(rapido);

        //Activamos o modo verbose
        lento.addActionListener( (ActionEvent e) -> {
            this.meuAxente.setVelocidade(1000);
        });

        //Desactivamos o modo verbose
        medio.addActionListener( (ActionEvent e) -> {
            this.meuAxente.setVelocidade(500);
        });

        rapido.addActionListener( (ActionEvent e) -> {
            this.meuAxente.setVelocidade(0);
        });

        opcions.add(velocidade);

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
        Font fonte = new Font("Dialog", Font.ITALIC, 15);
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
        Font fonte = new Font("Dialog", Font.BOLD, 10);
        this.panelClasificacion.setFont(fonte);
        this.panelClasificacion.setBackground(new Color(255, 255, 255));
        this.panelClasificacion.setEditable(false); // Evitamos que se poda escribir no panel
        this.scrollClasificacion.setBounds(1433, 100, 489, 410);
        add(this.scrollClasificacion);
    }

    public void imprimirClasificacion(String clasificacionPraImprimir) {
        this.panelClasificacion.setText(clasificacionPraImprimir);
    }

    public void crearBotonNovoXogo() {
        int x = 400;
        int y = 810;
        int anchura = 400;
        int altura = 80;
        this.botonNovoXogo.setBounds(x, y, anchura, altura);
        add(this.botonNovoXogo);
        this.botonNovoXogo.addActionListener( (ActionEvent e) -> { // Engadimos a resposta do boton "Novo xogo"
            ArrayList<Xogador> xogadores = this.meuAxente.buscarAxentes();
            PopupXogadores meuPopup = new PopupXogadores(this.meuAxente, xogadores, this, true);
            meuPopup.setVisible(true);
        });
    }

    public void crearBotonPausa() {
        int x = 800;
        int y = 810;
        int anchura = 400;
        int altura = 80;
        this.botonPuasa.setBounds(x, y, anchura, altura);
        add(this.botonPuasa);
        this.botonPuasa.addActionListener( (ActionEvent e) -> { // Engadimos a resposta do boton "Novo xogo"
            this.meuAxente.setPausar(true);
        });
    }


    public void crearBotonContinuar() {
        int x = 1200;
        int y = 810;
        int anchura = 400;
        int altura = 80;
        this.botonContinuar.setBounds(x, y, anchura, altura);
        add(this.botonContinuar);
        this.botonContinuar.addActionListener( (ActionEvent e) -> { // Engadimos a resposta do boton "Novo xogo"
            this.meuAxente.setPausar(false);
        });
    }

    private class PopupXogadores extends JDialog {
        private MainAgent meuAxente;
        private ArrayList<Xogador>  xogadores;
        private ArrayList<Xogador>  xogadoresElexidos = new ArrayList<>();
        private JCheckBox[] checkBoxes;
        private JButton botonAceptar = new JButton("ACEPTAR");

        public PopupXogadores(MainAgent meuAxente,ArrayList<Xogador>  xogadores, JFrame jFrame, boolean bool) {
            super(jFrame, bool);
            this.meuAxente = meuAxente;
            setTitle("Xogadores");
            this.xogadores = xogadores;
            this.checkBoxes = new JCheckBox[xogadores.size()];

            setSize(200,  95 + 30 * this.checkBoxes.length);
            setLocationRelativeTo(null);
            getContentPane().setBackground(new Color(25, 178, 172));

            setResizable(false); //Evitamos que o usuario poida cambiar o tamaño da xanela

            setLayout(null);

            crearCheckBoxes();
            crearBotonAceptar();
        }

        private void crearCheckBoxes() {
            for (int i = 0; i < this.checkBoxes.length; i++)
            {
                this.checkBoxes[i] = new JCheckBox(xogadores.get(i).aid.getLocalName());
                this.checkBoxes[i].setBackground(new Color(25, 178, 172));
                this.checkBoxes[i].setSelected(true);
                this.checkBoxes[i].setBounds(10, 10 + 30 * i, 180, 30);
                add(this.checkBoxes[i]);
            }
        }

        private void crearBotonAceptar() {
            int x = 50;
            int y = checkBoxes.length*30+20;
            int anchura = 100;
            int altura = 20;
            this.botonAceptar.setBounds(x, y, anchura, altura);
            add(this.botonAceptar);
            this.botonAceptar.addActionListener( (ActionEvent e) -> { // Engadimos a resposta do boton "Novo xogo"
                obterXogadoresElexidos();
                this.meuAxente.iniciarXogo(xogadoresElexidos);
                dispose();
            });
        }

        private void obterXogadoresElexidos() {
            for (int i = 0; i < this.checkBoxes.length; i++)
                if (this.checkBoxes[i].isSelected()) xogadoresElexidos.add(xogadores.get(i));
        }
    }
}