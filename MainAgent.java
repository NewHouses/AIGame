import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class MainAgent extends Agent {
    Xanela minhaXanela = new Xanela(this);
    private boolean pausar = false;
    private boolean verboseMode = false;
    private int velocidade = 0;

    /**
     * Este metodo executase cada vez que o axente executase
     */
    public void setup() {
        System.out.println(getLocalName() + " --> Lanzando...");

        this.minhaXanela.setVisible(true);

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

        this.minhaXanela.imprimirInformacion("   " + getLocalName() + " --> Bucando axentes" + "\n");

        /* Obtemos a descripcion dos Axentes Xogadores */
        DFAgentDescription descripcionDoAxente = new DFAgentDescription();
        ServiceDescription descripcionDoServizo = new ServiceDescription();
        descripcionDoServizo.setType("Xogador"); // Buscaremos os axentes deste tipo
        descripcionDoAxente.addServices(descripcionDoServizo);

        /* Buscamos os axentes xogadores */
        try {
            DFAgentDescription[] xogadores = DFService.search(this, descripcionDoAxente);
            this.minhaXanela.imprimirInformacion("   Atopados " + xogadores.length + " xogadores." + "\n" + "\n");

            /* Imprimimos por terminal os xogadores atopados */
            for(int i = 0; i < xogadores.length; i++) {
                Xogador xogador = new Xogador(xogadores[i].getName(), i); // Creamos un xogador e asignamoslle un ID
                meusXogadores.add(xogador);
                this.minhaXanela.imprimirInformacion("   " + i + " --> Xogador " + xogador.aid.getLocalName() + "\n" + "\n");
            }

        } catch (FIPAException erro) {
            erro.printStackTrace();
        }

        return meusXogadores;
    }

    public void iniciarXogo( ArrayList<Xogador> xogadores) {

        if(xogadores.size() < 2)
        {

            this.minhaXanela.imprimirInformacion("   ERRO: Non se pode inicalizar un xogo con menos de dous xogadores!!!" + "\n");
        }
        else
        {
            Xogo meuXogo = new Xogo(this, xogadores);
            addBehaviour(meuXogo); // Engadimos o comportamento do noso axente principal
        }
    }

    public void setPausar(boolean pausar) {
        this.pausar = pausar;
    }

    public boolean isPausar() {
        return pausar;
    }

    public void setVerboseMode(boolean verboseMode) { this.verboseMode = verboseMode; }

    public boolean isVerboseMode() { return verboseMode;}

    public void setVelocidade(int velocidade) {
        this.velocidade = velocidade;
    }

    public int getVelocidade() {
        return this.velocidade;
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
    private TreeMap<Integer, Xogador> clasificacion;


    public Xogo(MainAgent axentePrincipal, ArrayList<Xogador> xogadores) {
        this.axentePrincipal = axentePrincipal;
        this.xogadores = xogadores;
        parametros.setNumeroXogadores(xogadores.size());
        parametros.setIteracionsCambioMatriz(0);
    }

    public void action() {

        this.axentePrincipal.minhaXanela.imprimirInformacion("\n\n\n   Iniciando nova partida\n\n\n");
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
            if (this.axentePrincipal.isVerboseMode()) this.axentePrincipal.minhaXanela.imprimirInformacion("   Enviando informacion ao xogador " + xogador.aid.getLocalName() + "\n");
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
                    while (this.axentePrincipal.isPausar()) {
                        try
                        {
                            Thread.sleep(500);
                        }              catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    ronda++;
                    if(parametros.getIteracionsCambioMatriz() > 0)
                        if((ronda % parametros.getIteracionsCambioMatriz()) == 0) {
                            float porcentaxeMatrizCambiada = cambiarMatriz(); imprimirMatriz();
                            enviarCambioMatriz(xogador1, xogador2, porcentaxeMatrizCambiada);
                            if (this.axentePrincipal.isVerboseMode()) this.axentePrincipal.minhaXanela.imprimirInformacion("   A matriz cambiou un " + porcentaxeMatrizCambiada + "%\n");
                        }
                    xogarRonda(xogador1, xogador2);
                    try
                    {
                        Thread.sleep(this.axentePrincipal.getVelocidade());
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                definirGanhador(xogador1, xogador2);

                enviarFinXogo(xogador1, xogador2); // Avisamos a ambolos dous xogadores de que a partida rematou
            }
        }

        actualizarPuntuacions();
        actualizarClasificacion();

        this.axentePrincipal.minhaXanela.imprimirInformacion("\n\n   *****************************" + "\n");
        this.axentePrincipal.minhaXanela.imprimirInformacion("   *           CAMPEON         *" + "\n");
        this.axentePrincipal.minhaXanela.imprimirInformacion("   *****************************" + "\n");
        this.axentePrincipal.minhaXanela.imprimirInformacion("                       " + clasificacion.get(1).aid.getLocalName() + "\n");

        String clasificacionPraImprimir = "\n\n*************************************************************************************************" + "\n";
        clasificacionPraImprimir += "                                                                   Clasificación           " + "\n";
        clasificacionPraImprimir += "*************************************************************************************************" + "\n\n\n";
        Iterator<Integer> it = clasificacion.keySet().iterator();
        while (it.hasNext()) {
            int posicion = it.next();
            Xogador xogador = clasificacion.get(posicion);
            clasificacionPraImprimir += " " + posicion  +"º " + xogador.aid.getLocalName() + " Victorias: " +  xogador.Victorias + " Empates: " + xogador.Empates + " Derrotas: " + xogador.Derrotas + "  Puntuacion: " + xogador.puntuacion + " Puntos Totais: " + xogador.marcadorTotal + "\n\n";
        }

        this.axentePrincipal.minhaXanela.imprimirClasificacion(clasificacionPraImprimir);
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
                    this.matriz[j][i] =  num2 + "," + num1;
                }
            }
    }

    /**
     * Este metodo encargase de cambiar a matriz cumpridas as iteracions seleccionadas
     * cun porcentaxe de cambio estabelecido inicialmente
     */
    private float cambiarMatriz() {
        int tamanhoMatriz = parametros.getTamanhoMatriz();
        float porcentaxeDeCambio = (float) parametros.getPorcentaxeDeCambio();
        float porcentaxeCambiado = 0;
        Random num = new Random();
        int fila, columna;
        int num1, num2;
        String celda;
        ArrayList<String> celdasCambiadas = new ArrayList<String>();

        while(true)
        {
            // Eleximos ao azar unha fila e unha columna e gardamos a celda
            fila = num.nextInt(tamanhoMatriz);
            columna= num.nextInt(tamanhoMatriz);
            celda = fila + "," + columna;

            // Se a celda xa foi modificada volvemos ao principio
            if(celdasCambiadas.contains(celda)) continue;
            celdasCambiadas.add(celda);

            /*
             * Por regra de 3:
             *
             *                tamanhoMatriz²     ---- 100%
             *           numeroCeldasModificadas ----  x%
             *
             *           x = (numeroCeldasModificadas * 100) / tamanhoMatriz²
             *
             */
            if(fila==columna) {
                porcentaxeCambiado += (1 * 100) / (tamanhoMatriz * tamanhoMatriz);
                // Se superamos a porcentaxe maxima saimos do while e restamos a porcentaxe da celda que non chegaamos a cambiar
                if(porcentaxeCambiado > porcentaxeDeCambio){
                    porcentaxeCambiado -= (1 * 100) / (tamanhoMatriz * tamanhoMatriz);
                    break;
                }
                this.matriz[fila][columna] = num.nextInt(10) + "," + num.nextInt(10);

            }
            if(fila<columna)
            {
                porcentaxeCambiado += (2 * 100) / (tamanhoMatriz * tamanhoMatriz);
                if(porcentaxeCambiado > porcentaxeDeCambio) {
                    // Comprobamos se cambiando unha celda ainda cumprese a porcentaxe
                    porcentaxeCambiado -= (1 * 100) / (tamanhoMatriz * tamanhoMatriz);
                    if(porcentaxeCambiado > porcentaxeDeCambio) {
                        porcentaxeCambiado -= (1 * 100) / (tamanhoMatriz * tamanhoMatriz);
                        break;
                    }
                    fila = num.nextInt(tamanhoMatriz);
                    num1 = num.nextInt(10);
                    num2 = num.nextInt(10);
                    this.matriz[fila][fila] =  num1 + "," + num2;
                    break;
                }
                num1 = num.nextInt(10);
                num2 = num.nextInt(10);
                this.matriz[fila][columna] =  num1 + "," + num2;
                this.matriz[columna][fila] =  num2 + "," + num1;
            }
        }

        return porcentaxeCambiado;
    }

    /**
     * Este metodo imprime a matriz por pantalla
     */
    private void imprimirMatriz() {
        int tamanhoMatriz = parametros.getTamanhoMatriz();
        String matrizPraImprimir = new String();
        String separacion = "---------";

        for(int i=0; i< tamanhoMatriz; i++) {

            matrizPraImprimir = matrizPraImprimir + " \n ";
            for(int k = 0; k < tamanhoMatriz; k++)  matrizPraImprimir = matrizPraImprimir + separacion;
            matrizPraImprimir = matrizPraImprimir + "\n |";

            for(int j = 0; j< tamanhoMatriz; j++)
            {
                matrizPraImprimir = matrizPraImprimir + "(" + matriz[i][j] + ") | ";
            }
        }

        matrizPraImprimir = matrizPraImprimir + " \n ";
        for(int k = 0; k < tamanhoMatriz; k++)  matrizPraImprimir = matrizPraImprimir + separacion;
        matrizPraImprimir = matrizPraImprimir + "\n";


        // System.out.println(matrizPraImprimir); // Imprimimos a matriz por consola
        this.axentePrincipal.minhaXanela.imprimirMatriz(matrizPraImprimir);
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
        if (this.axentePrincipal.isVerboseMode()) this.axentePrincipal.minhaXanela.imprimirInformacion("   O xogador " + xogador1.aid.getLocalName() + " elexiu a fila " + fila + "\n");

        /* Obtemos a columna elexida polo xogador 2 */
        columna = obterPosicion(xogador2);
        if (this.axentePrincipal.isVerboseMode()) this.axentePrincipal.minhaXanela.imprimirInformacion("   O xogador " + xogador2.aid.getLocalName() + " elexiu a columna " + columna + "\n");

        /* Obtemos o resultado e o enviamos */
        resultado = obterResultado(fila, columna);

        if (this.axentePrincipal.isVerboseMode()) this.axentePrincipal.minhaXanela.imprimirInformacion("   A celda seleccionada contén " + resultado + "\n");

        enviarResultado(resultado, xogador1, xogador2, fila, columna);

        resultadoAux = resultado.split(",");
        resultado1 = Integer.parseInt(resultadoAux[0]);
        resultado2 = Integer.parseInt(resultadoAux[1]);

        this.axentePrincipal.minhaXanela.imprimirInformacion("   O xogador  " + xogador1.aid.getLocalName() + " obtén " + resultado1 + "puntos\n");
        this.axentePrincipal.minhaXanela.imprimirInformacion("   O xogador  " + xogador2.aid.getLocalName() + " obtén " + resultado2 + "puntos\n");


        /* Actualizamos as estadisticas dos xogadores */
        xogador1.marcador += resultado1;
        xogador2.marcador += resultado2;

        this.axentePrincipal.minhaXanela.imprimirInformacion("   O xogador  " + xogador1.aid.getLocalName() + " ten agora " + xogador1.marcador + "puntos\n");
        this.axentePrincipal.minhaXanela.imprimirInformacion("   O xogador  " + xogador2.aid.getLocalName() + " ten agora " + xogador2.marcador + "puntos\n");

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
            this.axentePrincipal.minhaXanela.imprimirInformacion("\n   O xogador " + xogador1.aid.getLocalName() + " acada a victoria coa incríbel cifra de " + xogador1.marcador + " puntos" + "\n");
            this.axentePrincipal.minhaXanela.imprimirInformacion("   O xogador " + xogador2.aid.getLocalName() + " conseguiu " + xogador2.marcador + " puntos" + "\n");
            xogador1.Victorias++;
            xogador2.Derrotas++;
        } else if (xogador2.marcador > xogador1.marcador) {
            this.axentePrincipal.minhaXanela.imprimirInformacion("\n   O xogador " + xogador2.aid.getLocalName() + " acada a victoria coa incríbel cifra de " + xogador2.marcador + " puntos" + "\n");
            this.axentePrincipal.minhaXanela.imprimirInformacion("   O xogador " + xogador1.aid.getLocalName() + " conseguiu " + xogador1.marcador + " puntos" + "\n");
            xogador1.Derrotas++;
            xogador2.Victorias++;
        } else {
            this.axentePrincipal.minhaXanela.imprimirInformacion("   Ambos xogadores empatan coa cifra de " + xogador1.marcador + " puntos" + "\n");
            xogador1.Empates++;
            xogador2.Empates++;
        }

        xogador1.marcadorTotal += xogador1.marcador;
        xogador2.marcadorTotal += xogador2.marcador;

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

    private void enviarCambioMatriz(Xogador xogador1, Xogador xogador2, float porcentaxeMatrizCambiada) {
        ACLMessage mensaxe = new ACLMessage(ACLMessage.INFORM);
        mensaxe.addReceiver(xogador1.aid);
        mensaxe.addReceiver(xogador2.aid);
        mensaxe.setContent("Changed#" + porcentaxeMatrizCambiada);
        axentePrincipal.send(mensaxe);
    }

    private void  actualizarPuntuacions() {
        for (int numXogador = 0; numXogador < xogadores.size(); numXogador++) xogadores.get(numXogador).actualizarPuntuacion();
    }

    private void actualizarClasificacion() {
        ArrayList<Integer> puntuacions = new ArrayList<Integer>();
        ArrayList<Xogador> clasificacion = new ArrayList<Xogador>();
        ArrayList<Xogador> clasificacionAux = new ArrayList<Xogador>();

        this.clasificacion = new TreeMap<Integer, Xogador>();
        for (int numXogador = 0; numXogador < xogadores.size(); numXogador++)
            puntuacions.add(xogadores.get(numXogador).puntuacion);
        Collections.sort(puntuacions);
        Collections.reverse(puntuacions);

        /* Ordeamos por puntos acadados */
        for (int posicion = 0; posicion < puntuacions.size(); posicion++)
            for (int numXogador = 0; numXogador < xogadores.size(); numXogador++)
                if (puntuacions.get(posicion) == xogadores.get(numXogador).puntuacion) {
                    if(!estaXogadorNaClasificacion(xogadores.get(numXogador))) {
                        clasificacion.add(xogadores.get(numXogador));
                        clasificacionAux.add(xogadores.get(numXogador));
                        this.clasificacion.put(posicion + 1, xogadores.get(numXogador));
                        break;
                    }
                }

        this.clasificacion = new TreeMap<Integer, Xogador>();

        /* Ordeamos as posicións en caso de empate a puntos según o marcador total */
        for (int posicion1 = 0; posicion1 < clasificacion.size(); posicion1++){
            for (int posicion2 = posicion1 + 1; posicion2 < clasificacion.size(); posicion2++)
                if (clasificacion.get(posicion1).puntuacion == clasificacion.get(posicion2).puntuacion) {
                    if(clasificacion.get(posicion1).marcadorTotal < clasificacion.get(posicion2).marcadorTotal) {
                        clasificacion.set(posicion2, clasificacionAux.get(posicion1));
                        clasificacion.set(posicion1, clasificacionAux.get(posicion2));
                        clasificacionAux.set(posicion2, clasificacion.get(posicion2));
                        clasificacionAux.set(posicion1, clasificacion.get(posicion1));
                    }
                }

        }

        for (int posicion = 1; posicion <= clasificacion.size(); posicion++) this.clasificacion.put(posicion, clasificacion.get(posicion-1));

    }

    private boolean estaXogadorNaClasificacion(Xogador xogador) {
        if(this.clasificacion.isEmpty()) return false;
        Iterator<Integer> it = clasificacion.keySet().iterator();
        while (it.hasNext()) if(xogador.aid.equals(clasificacion.get(it.next()).aid)) return true;
        return false;
    }
}

class Xogador {
    AID aid;
    int id;
    int marcador = 0;
    int marcadorTotal = 0;
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
        S = 4;
        R = 50;
        I = 3;
        P = 50;
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

    public void setIteracionsCambioMatriz(int i) {
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

    public int getIteracionsCambioMatriz() {
        return I;
    }

    public int getPorcentaxeDeCambio() {
        return P;
    }
}
