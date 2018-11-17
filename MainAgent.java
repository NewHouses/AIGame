import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class MainAgent extends Agent {

    /**
     * Este metodo executase cada vez que o axente executase
     */
    public void setup() {
        System.out.println(getLocalName() + " --> Lanzando...");

        Scanner teclado = new Scanner(System.in);
        System.out.println("Prema calquera boton para empezar");
        teclado.nextLine(); //Pusen isto para que aos axente lles de tempo de arrancar antes de inciar a busqueda

        ArrayList<Xogador> xogadores = buscarAxentes();

        Xogo meuXogo = new Xogo(this, xogadores);
        addBehaviour(meuXogo); // Engadimos o comportamento do noso axente principal
    }

    /**
     * Este metodo executase cada vez que o axente remata a súa execución
     */
    public void takeDown() {

    }

    /**
     * Este metodo busca os axentes (xogadoresTotais) disponhibles
     */
    public ArrayList<Xogador> buscarAxentes() {
        ArrayList<Xogador> meusXogadores = new ArrayList<Xogador>();

        System.out.println(getLocalName() + " --> Bucando axentes");

        /* Obtemos a descripcion dos Axentes Xogadores */
        DFAgentDescription descripcionDoAxente = new DFAgentDescription();
        ServiceDescription descripcionDoServizo = new ServiceDescription();
        descripcionDoServizo.setType("Xogador"); // Buscaremos os axentes deste tipo
        descripcionDoAxente.addServices(descripcionDoServizo);

        /* Buscamos os axentes xogadores */
        try {
            DFAgentDescription[] xogadores = DFService.search(this, descripcionDoAxente);
            System.out.println("\nAtopados " + xogadores.length + " xogadores.");

            /* Imprimimos por terminal os xogadores atopados */
            for(int i = 0; i < xogadores.length; i++) {
                Xogador xogador = new Xogador(xogadores[i].getName(), i); // Creamos un xogador e asignamoslle un ID
                meusXogadores.add(xogador);
                System.out.println("\n\n" + i + " --> Xogador " + xogador.aid.getLocalName());
            }

        } catch (FIPAException erro) {
            erro.printStackTrace();
        }

        return meusXogadores;
    }

}


/**
 * Clase para controlar a execucion do xogo, conten o comportamento do Axente principal
 */
class Xogo extends Behaviour {
    private MainAgent axentePrincipal;
    private ArrayList<Xogador> xogadores;
    private String[][] matriz;
    private ParametrosDoXogo parametros = new ParametrosDoXogo();

    public Xogo(MainAgent axentePrincipal, ArrayList<Xogador> xogadores) {
        this.axentePrincipal = axentePrincipal;
        this.xogadores = xogadores;
        parametros.setNumeroXogadores(xogadores.size());
    }

    public void action() {

        System.out.println("\n\n\nIniciando nova partida\n\n\n");
        enviarInformacionAosAxentes(); // Enviamos a informacion aos axentes xogadores
        xogar();

    }

    public boolean done() {
        return true;
    }

    /**
     * Este metodo envia os parametros do xogo a todos os xogadores
     */
    private void enviarInformacionAosAxentes() {

        for (Xogador xogador : this.xogadores) {
            System.out.println("\n Enviando informacion ao xogador " + xogador.aid.getLocalName());
            ACLMessage mensaxe = new ACLMessage(ACLMessage.INFORM);
            mensaxe.setContent("Id#"
                    + xogador.id
                    + "#" + parametros.N
                    + "," + parametros.S
                    + "," + parametros.R
                    + "," + parametros.I
                    + "," + parametros.P);
            mensaxe.addReceiver(xogador.aid);
            myAgent.send(mensaxe);
        }
    }

    /**
     * Este metodo encargase da execucion dunha partida
     */
    private  void xogar() {
        int numeroDeXogadores = parametros.getNumeroDeXogadores();
        int numeroDeXogos = (numeroDeXogadores * (numeroDeXogadores - 1)) / 2;
        Xogador xogador1;
        Xogador xogador2;
        int ronda = 0;
        String[] resultado;

        /* Xogan todos contra todos */
        for(int i = 0; i < numeroDeXogadores; i++) {
            for(int j = i+1; j < numeroDeXogadores; j++) {
                xogador1 = xogadores.get(i);
                xogador2 = xogadores.get(j);
                ronda = 0;

                xerarMatriz(); // Xeramos a matriz
                imprimirMatriz(); //Imprimimos a matriz

                enviarNovoXogo(xogador1, xogador2); // Informamos a ambolos dous xogadores de que vaise iniciar unha nova partida entre eles dous

                /* Xogamos todas as rondas entre os dous xogadores */
                while (ronda != parametros.getNumeroDeRondas()) {
                    ronda++;
                    xogarRonda(xogador1, xogador2);
                }

                definirGanhador(xogador1, xogador2);

                enviarFinXogo(xogador1, xogador2); // Avisamos a ambolos dous xogadores de que a partida rematou
            }
        }

        System.out.println("\n\n*****************************");
        System.out.println("*           CAMPEON         *");
        System.out.println("*****************************");
    }

    /**
     * Este metodo encargase de xerar a matriz inicialmente
     */
    private void xerarMatriz() {
        int tamanhoMatriz = parametros.getTamanhoMatriz();
        matriz = new String[tamanhoMatriz][tamanhoMatriz];
        Random num = new Random();
        int num1, num2;

        for(int i=0; i< tamanhoMatriz; i++)
            for(int j = 0; j< tamanhoMatriz; j++)
            {
                if(i==j)
                    this.matriz[i][j] = num.nextInt(10) + "," + num.nextInt(10);
                if(i<j)
                {
                    num1 = num.nextInt(10);
                    num2 = num.nextInt(10);
                    this.matriz[i][j] =  num1 + "," + num2;
                    this.matriz[j][i] =  this.matriz[i][j];
                }
            }
    }

    /**
     * Este metodo imprime a matriz por pantalla
     */
    private void imprimirMatriz() {
        int tamanhoMatriz = parametros.getTamanhoMatriz();
        String matrizPraImprimir = new String();

        for(int i=0; i< tamanhoMatriz; i++) {

            matrizPraImprimir = matrizPraImprimir + " \n -----------------------------\n |";
            for(int j = 0; j< tamanhoMatriz; j++)
            {
                matrizPraImprimir = matrizPraImprimir + "(" + matriz[i][j] + ") | ";
            }
        }
        matrizPraImprimir = matrizPraImprimir + " \n -----------------------------\n";


        System.out.println(matrizPraImprimir); // Imprimimos a matriz por consola
    }

    /**
     * Este metodo envia unha mensaxe de aviso de nova partida
     */
    private void enviarNovoXogo(Xogador xogador1, Xogador xogador2) {
        ACLMessage mensaxe = new ACLMessage(ACLMessage.INFORM);
        mensaxe.addReceiver(xogador1.aid);
        mensaxe.addReceiver(xogador2.aid);
        mensaxe.setContent("NewGame#"+xogador1.id+","+xogador2.id);
        axentePrincipal.send(mensaxe);
    }

    /**
     * Este metodo encargase da execucion dunha ronda
     */
    private void xogarRonda(Xogador xogador1, Xogador xogador2) {
        int fila, columna;
        String resultado;
        String[] resultadoAux;
        int resultado1, resultado2;

        /* Obtemos a fila elexida polo xogador 1 */
        fila = obterPosicion(xogador1);
        System.out.println("O xogador " + xogador1.aid.getLocalName() + " elexiu a fila " + fila);

        /* Obtemos a columna elexida polo xogador 2 */
        columna = obterPosicion(xogador2);
        System.out.println("O xogador " + xogador2.aid.getLocalName() + " elexiu a columna " + columna);

        /* Obtemos o resultado e o enviamos */
        resultado = obterResultado(fila, columna);
        enviarResultado(resultado, xogador1, xogador2, fila, columna);

        resultadoAux = resultado.split(",");
        resultado1 = Integer.parseInt(resultadoAux[0]);
        resultado2 = Integer.parseInt(resultadoAux[1]);

        /* Actualizamos as estadisticas dos xogadores */
        xogador1.marcador += resultado1;
        xogador2.marcador += resultado2;
    }

    /**
     * Este metodo encargase de pedir a un xogador que elixa a posicion desexada
     */
    private int obterPosicion(Xogador xogador) {
        ACLMessage mensaxe = new ACLMessage(ACLMessage.REQUEST);
        mensaxe.setContent("Position");
        mensaxe.addReceiver(xogador.aid);
        axentePrincipal.send(mensaxe);

        ACLMessage posicion = axentePrincipal.blockingReceive(); // Esperamos ata que recibimos a resposta
        return Integer.parseInt(posicion.getContent().split("#")[1]);
    }

    /**
     * Este metodo encargase de obter o vecto elexido da matriz
     */
    private String obterResultado(int fila, int columna) {
        return matriz[fila][columna];
    }

    /**
     * Este metodo encargase de enviar o resultado aos xogadores
     */
    private void enviarResultado(String resultado, Xogador xogador1, Xogador xogador2, int fila, int columna) {
        ACLMessage mensaxe = new ACLMessage(ACLMessage.INFORM);
        mensaxe.addReceiver(xogador1.aid);
        mensaxe.addReceiver(xogador2.aid);
        mensaxe.setContent("Results#" + fila + "," + columna + "#" + resultado);
        axentePrincipal.send(mensaxe);
    }

    /**
     * Este metodo encargase de actualizar os campos 'Victorias', 'Derrotas' e 'Empates' dos xogadores
     */
    private void definirGanhador(Xogador xogador1, Xogador xogador2) {

        if (xogador1.marcador > xogador2.marcador) {
            System.out.println("\nO xogador " + xogador1.aid.getLocalName() + " acada a victoria coa incríbel cifra de " + xogador1.marcador + " puntos");
            System.out.println("O xogador " + xogador2.aid.getLocalName() + " conseguiu " + xogador2.marcador + " puntos");
            xogador1.Victorias++;
            xogador2.Derrotas++;
        } else if (xogador2.marcador > xogador1.marcador) {
            System.out.println("\nO xogador " + xogador2.aid.getLocalName() + " acada a victoria coa incríbel cifra de " + xogador2.marcador + " puntos");
            System.out.println("O xogador " + xogador1.aid.getLocalName() + " conseguiu " + xogador1.marcador + " puntos");
            xogador1.Derrotas++;
            xogador2.Victorias++;
        } else {
            System.out.println("\nAmbos xogadores empatan coa cifra de " + xogador1.marcador + " puntos");
            xogador1.Empates++;
            xogador2.Empates++;
        }

        /* Reinicializamos os marcadores dos xogadores */
        xogador1.marcador = 0;
        xogador2.marcador = 0;
    }

    /**
     * Este metodo encargase de avisar aos xogadores de que a partida rematou
     */
    private void enviarFinXogo(Xogador xogador1, Xogador xogador2) {
        ACLMessage mensaxe = new ACLMessage(ACLMessage.INFORM);
        mensaxe.addReceiver(xogador1.aid);
        mensaxe.addReceiver(xogador2.aid);
        mensaxe.setContent("EndGame");
        axentePrincipal.send(mensaxe);
    }
}

class Xogador {
    AID aid;
    int id;
    int marcador = 0;
    int Victorias = 0;
    int Empates = 0;
    int Derrotas = 0;
    int puntuacion = 0;

    public Xogador(AID aid, int id) {
        this.aid = aid;
        this.id = id;
    }

    public void actualizarPuntuacion() {
        puntuacion = 3 * Victorias + 1 * Empates;
    }

    @Override
    public boolean equals(Object o) {
        return this.aid.equals(o);
    }
}


class ParametrosDoXogo {

    int N;
    int S;
    int R;
    int I;
    int P;

    public ParametrosDoXogo() {
        N = 2;
        S = 3;
        R = 50;
        I = 0;
        P = 10;
    }

    public void setNumeroXogadores(int n) {
        N = n;
    }

    public void setTamanhoMatriz(int s) {
        S = s;
    }

    public void setNumeroRondas(int r) {
        R = r;
    }

    public void setNUmeroRondasPraCambiarMatriz(int i) {
        I = i;
    }

    public void setPorcentaxeCambioMatriz(int p) {
        P = p;
    }

    public int getNumeroDeXogadores() {
        return N;
    }

    public int getTamanhoMatriz() {
        return S;
    }

    public int getNumeroDeRondas() {
        return R;
    }
}
