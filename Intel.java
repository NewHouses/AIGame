import java.util.Random;

import com.sun.tools.javac.Main;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class Intel  extends Agent {

    private static final long serialVersionUID = 1L;

    private int ID, tamanhoMatriz;
    private int[][] matriz;
    private int[][] matrizPuntos;
    private int rondas;
    private int[] selecciones;
    private float[] probabilidades;
    private boolean elexirAnterior = false;
    private int anteriorEleccion = 0;
    private int anteriorEleccionRival = 0;
    private int eleccionMaisVictorias;
    private int eleccionMaisPuntos;
    private int segundaEleccionMaisVictorias;
    private int segundaEleccionMaisPuntos;
    private boolean elexirFilas = false;
    private final static int Victoria = 2;
    private final static int Empate = 1;
    private final static int Derrota = 0;


    public void setup () {
        System.out.println("[Sergio Simons]: " + getLocalName() + " --> Lanzando ...");

        //Rexistramos o axente nas paxinas amarelas
        DFAgentDescription AgentDescripcion = new DFAgentDescription();
        AgentDescripcion.setName(getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("Xogador");
        serviceDescription.setName("Xogador intelixente");
        AgentDescripcion.addServices(serviceDescription);
        try {
            DFService.register(this, AgentDescripcion);
        }
        //Se non se pode rexitrar, pechamos o axente
        catch (FIPAException exception) {
            exception.printStackTrace();
            doDelete();
        }

        System.out.println("[Sergio Simons]: " + getLocalName() + " --> Rexistrado");

        //Creamos a resposta os mensaxes de tipo "ACLMessage.REQUEST" e de tipo "ACLMessage.INFORM" (so o primeiro para saber o numero de elexirFilas)
        addBehaviour(new RequestBehaviour());
        addBehaviour(new InformBehaviour());
    }

    protected void takeDown () {
        System.out.println("[Sergio Simons]: " + getLocalName() + " --> Pechando...");
    }

    //Behaviour para os mensaxes de tipo "Inform"
    private class InformBehaviour extends CyclicBehaviour {
        private static final long serialVersionUID = 1L;

        @Override
        public void action () {
            MessageTemplate buzon = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage mensaxe = receive(buzon);

            if (mensaxe != null)
            {
                switch(mensaxe.getContent().split("#")[0]) {
                    //Obtemos o tamanho da matriz e o ID do axente
                    case "Id": obterParametros(mensaxe);
                        break;
                    //Inicializamos a matriz e miramos se eliximos fila ou columna, e pomos a false que a matriz cambiou
                    case "NewGame": inicializarXogo(mensaxe);
                        break;
                    //Cando cambia a matriz, indicamos iso e reinicamos a matriz ata descubrir os cambios
                    case "Changed":reiniciarMatriz(mensaxe);
                        break;
                    //Gardamos as posicions na matriz
                    case "Results": obterResultado(mensaxe);
                        break;
                }
            }
            else
                block();
        }
    }

    private void obterParametros(ACLMessage mensaxe) {
        ID = Integer.parseInt(mensaxe.getContent().split("#")[1]);
        tamanhoMatriz = Integer.parseInt(mensaxe.getContent().split("#")[2].split(",")[1]);
    }


    private void inicializarXogo(ACLMessage mensaxe) {
        matriz = new int[tamanhoMatriz][tamanhoMatriz];
        matrizPuntos = new int[tamanhoMatriz][tamanhoMatriz];
        probabilidades = new float[tamanhoMatriz];
        selecciones = new int[tamanhoMatriz];
        rondas = 0;
        for (int i = 0; i < tamanhoMatriz; i++) for(int j = 0; j < tamanhoMatriz; j++) {
            matrizPuntos[i][j] = 0;
            matriz[i][j] = -1;
        }
        for (int i = 0; i < tamanhoMatriz; i++) probabilidades[i] = 100 / tamanhoMatriz;
        if (Integer.parseInt(mensaxe.getContent().split("#")[1].split(",")[0]) == ID) elexirFilas = true;
        else elexirFilas = false;
    }

    private void reiniciarMatriz(ACLMessage mensaxe) {
        boolean reiniciarProbabilidades = true;
        for (int i = 0; i < tamanhoMatriz; i++){
            if(probabilidades[i] == 100)  reiniciarProbabilidades = false;
        }

        if (Float.parseFloat(mensaxe.getContent().split("#")[1]) >= 30.0) // So reiniciamos se a porcentaxe é maior de 30%
            if(reiniciarProbabilidades) {
                probabilidades = new float[tamanhoMatriz];
                rondas = 0;
            }
        for (int i = 0; i < tamanhoMatriz; i++) for(int j = 0; j < tamanhoMatriz; j++) {
            matrizPuntos[i][j] = 0;
            matriz[i][j] = -1;
        }
    }

    private void obterResultado(ACLMessage mensaxe) {
        int fila = Integer.parseInt(mensaxe.getContent().split("#")[1].split(",")[0]);
        int columna = Integer.parseInt(mensaxe.getContent().split("#")[1].split(",")[1]);
        int[] resultado = { Integer.parseInt(mensaxe.getContent().split("#")[2].split(",")[0]), Integer.parseInt(mensaxe.getContent().split("#")[2].split(",")[1]) };

        rondas++;

        if(elexirFilas) {
            anteriorEleccionRival = columna;
            matrizPuntos[fila][columna] = resultado[0];
            recalcularProbabilidades(columna);
            if(fila != columna)  matrizPuntos[columna][fila] = resultado[1];
            if(resultado[0] > resultado[1]) {
                matriz[fila][columna] = Victoria;
                elexirAnterior = true;
                if(fila != columna) matriz[columna][fila] = Derrota;
            } else {
                matriz[fila][columna] = Derrota;
                elexirAnterior = false;
                if(fila != columna) matriz[columna][fila] = Victoria;
            }

        } else {
            anteriorEleccionRival = fila;
            matrizPuntos[fila][columna] = resultado[1];
            recalcularProbabilidades(fila);
            if(fila != columna)  matrizPuntos[columna][fila] = resultado[0];
            if(resultado[1] > resultado[0]) {
                matriz[fila][columna] = Victoria;
                if(fila != columna) matriz[columna][fila] = Derrota;
            } else {
                matriz[fila][columna] = Derrota;
                if(fila != columna) matriz[columna][fila] = Victoria;
            }
        }
    }

    private void recalcularProbabilidades(int seleccion) {
        selecciones[seleccion]++;
        for (int i = 0; i < tamanhoMatriz; i++) probabilidades[i] = (selecciones[i] * 100) / rondas;
    }

    //Behaviour para elixila fila ou columna a enviar
    private class RequestBehaviour extends CyclicBehaviour {
        private static final long serialVersionUID = 1L;
        int eleccion;
        int[] vectorVictorias;
        int[] vectorPuntos;

        @Override
        public void action () {
            MessageTemplate buzon = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage mensaxe = receive(buzon);

            //Se existe a mensaxe, calculamos a resposta
            if (mensaxe != null) {
                if(elexirFilas) System.out.println("[Sergio Simons]: " + getLocalName() + " --> Elixindo fila...");
                else System.out.println("[Sergio Simons]: " + getLocalName() + " --> Elixindo columna...");

                Random rand = new Random();
                eleccion = rand.nextInt(tamanhoMatriz);
                vectorVictorias = new int[tamanhoMatriz];
                vectorPuntos = new int[tamanhoMatriz];

                estratexiaFixo();
                anteriorEleccion = eleccion;
                //Enviamos a mensaxe coa resposta
                if(elexirFilas) System.out.println("[Sergio Simons]: " + getLocalName() + " --> Enviando fila...");
                else System.out.println("[Sergio Simons]: " + getLocalName() + " --> Enviando columna...");
                ACLMessage resposta = new ACLMessage(ACLMessage.INFORM);
                resposta.setContent("Position#" + eleccion);
                resposta.addReceiver(mensaxe.getSender());
                send(resposta);
            }
            else
                block();
        }

        private void estratexiaFixo() {
            Random rand = new Random();
            for (int fila = 0; fila < tamanhoMatriz; fila++) {
                for (int columna = 0; columna < tamanhoMatriz; columna++) {
                    if (elexirFilas) vectorPuntos[fila] += matrizPuntos[fila][columna];
                    else vectorPuntos[columna] += matrizPuntos[fila][columna];

                    if (matriz[fila][columna] == Victoria) {
                        if (elexirFilas) vectorVictorias[fila]++;
                        else vectorVictorias[columna]++;
                    }

                    System.out.print(matriz[fila][columna] + " | ");
                }
                System.out.println("\n");
            }

            int maximo = vectorVictorias[0];
            int segundoMaximo = -1;
            for (int i = 0; i < vectorVictorias.length; i++) {
                if (vectorVictorias[i] > maximo) {
                    maximo = vectorVictorias[i];
                    eleccionMaisVictorias = i;
                    eleccion = i;
                    if (maximo == tamanhoMatriz) { // Se sempre gañamos en todas as celdas da fila a collemos sempre
                        return;
                    }
                }
            }

            if(rondas > 3) {
                int eleccionProbabilidade = elexirPorProbabilidade();
                if (eleccionProbabilidade != -1) {
                    eleccion = eleccionProbabilidade;
                    return;
                }
            }

            for (int i = 0; i < vectorVictorias.length; i++) {
                if (vectorVictorias[i] > segundoMaximo && vectorVictorias[i] < maximo) {
                    segundoMaximo = vectorVictorias[i];
                    segundaEleccionMaisVictorias = i;
                }
            }

            int maximoPuntos = 0;
            int segundoMaximoPuntos = 0;
            for (int i = 0; i < vectorPuntos.length; i++) {
                if (vectorPuntos[i] > maximoPuntos) {
                    maximoPuntos = vectorPuntos[i];
                    eleccionMaisPuntos = i;
                }
            }
            for (int i = 0; i < vectorPuntos.length; i++) {
                if (vectorPuntos[i] > segundoMaximoPuntos && vectorPuntos[i] < maximoPuntos) {
                    segundoMaximoPuntos = vectorPuntos[i];
                    segundaEleccionMaisPuntos = i;
                }
            }

            for (int selec = 0; selec < tamanhoMatriz; selec++) {
                if(elexirFilas) if(matriz[eleccionMaisVictorias][selec] == Victoria && probabilidades[selec] >= 30) eleccion = eleccionMaisVictorias;
                else if(matriz[selec][eleccionMaisVictorias] == Victoria && probabilidades[selec] >= 30) eleccion = eleccionMaisVictorias;
            }

            if(eleccionMaisPuntos == eleccionMaisVictorias) {
                eleccion = eleccionMaisVictorias;
                for (int selec = 0; selec < tamanhoMatriz; selec++) {
                    if(elexirFilas) if(matriz[eleccion][selec] == Derrota && anteriorEleccionRival == selec) eleccion = segundaEleccionMaisVictorias;
                    else if(matriz[selec][eleccion] == Derrota && anteriorEleccionRival == selec) eleccion = segundaEleccionMaisVictorias;
                }
                return;
            }

            if(eleccionMaisPuntos == segundaEleccionMaisVictorias) {
                eleccion = segundaEleccionMaisVictorias;
                for (int selec = 0; selec < tamanhoMatriz; selec++) {
                    if(elexirFilas) if(matriz[eleccion][selec] == Derrota && anteriorEleccionRival == selec) eleccion = eleccionMaisVictorias;
                    else if(matriz[selec][eleccion] == Derrota && anteriorEleccionRival == selec) eleccion = eleccionMaisVictorias;
                }
                return;
            }


            if(segundaEleccionMaisPuntos == eleccionMaisVictorias) {
                eleccion = eleccionMaisVictorias;
                for (int selec = 0; selec < tamanhoMatriz; selec++) {
                    if(elexirFilas) if(matriz[eleccion][selec] == Derrota && anteriorEleccionRival == selec) eleccion = segundaEleccionMaisVictorias;
                    else if(matriz[selec][eleccion] == Derrota && anteriorEleccionRival == selec) eleccion = segundaEleccionMaisVictorias;
                }
            }

            if(segundaEleccionMaisPuntos == segundaEleccionMaisVictorias) {
                eleccion = segundaEleccionMaisVictorias;
                for (int selec = 0; selec < tamanhoMatriz; selec++) {
                    if(elexirFilas) if(matriz[eleccion][selec] == Derrota && anteriorEleccionRival == selec) eleccion = eleccionMaisVictorias;
                    else if(matriz[selec][eleccion] == Derrota && anteriorEleccionRival == selec) eleccion = eleccionMaisVictorias;
                }
            }

            if(!elexirAnterior && eleccion == anteriorEleccion) eleccion = rand.nextInt(tamanhoMatriz);



        }
    }

    public int elexirPorProbabilidade() {
        int celdaMaxPuntos = -10;
        int eleccionCeldaMaxPuntos = -1;
        for (int selec = 0; selec < tamanhoMatriz; selec++) {
            System.out.print(probabilidades[selec] + " ");
            if (probabilidades[selec] >= 45) {
                if (elexirFilas) {
                    for (int i = 0; i < tamanhoMatriz; i++) {
                        if (matriz[i][selec] == -1) {
                            return i;
                        }
                        if (matriz[i][selec] == Victoria && celdaMaxPuntos < matriz[i][selec]) {
                            celdaMaxPuntos = matriz[i][selec];
                            eleccionCeldaMaxPuntos = i;
                        }
                    }
                    if (eleccionCeldaMaxPuntos != -1) return eleccionCeldaMaxPuntos;

                    for (int i = 0; i < tamanhoMatriz; i++) {
                        if (celdaMaxPuntos < matrizPuntos[i][selec]) {
                            celdaMaxPuntos = matrizPuntos[i][selec];
                            eleccionCeldaMaxPuntos = i;
                        }
                    }
                } else {
                    for (int i = 0; i < tamanhoMatriz; i++) {
                        if (matriz[selec][i] == -1) {
                            return i;
                        }
                        if (matriz[selec][i] == Victoria && celdaMaxPuntos < matriz[selec][i]) {
                            celdaMaxPuntos = matriz[selec][i];
                            eleccionCeldaMaxPuntos = i;
                        }
                    }
                    if (eleccionCeldaMaxPuntos != -1) return eleccionCeldaMaxPuntos;

                    for (int i = 0; i < tamanhoMatriz; i++) {
                        if (celdaMaxPuntos < matrizPuntos[selec][i]) {
                            celdaMaxPuntos = matrizPuntos[selec][i];
                            eleccionCeldaMaxPuntos = i;
                        }
                    }
                }
                return eleccionCeldaMaxPuntos;
            }
        }

        System.out.println("\n");
        return eleccionCeldaMaxPuntos;
    }
}
