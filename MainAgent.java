import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

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
        teclado.nextLine();

        buscarAxentes();

        Xogo meuXogo = new Xogo();
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
    public void buscarAxentes() {
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
                System.out.println("\n\n" + i + "--> Xogador " + xogadores[i].getName().getLocalName());
            }

        } catch (FIPAException erro) {
            erro.printStackTrace();
        }
    }

}


/**
 * Clase para controlar a execucion do xogo, conten o comportamento do Axente principal
 */
 class Xogo extends Behaviour {
    private String[][] matriz;
    private int tamanhoMatriz = 3;

     public void action() {

         System.out.println("\n\n\nIniciando nova partida\n\n\n");
         xerarMatriz(); // Xeramos a matriz
         imprimirMatriz(); //Imprimimos a matriz
     }

     public boolean done() {
         return true;
     }

    public void xerarMatriz() {
        matriz = new String[tamanhoMatriz][tamanhoMatriz];
        Random num = new Random();
        int num1, num2;

        for(int i=0; i< this.tamanhoMatriz; i++)
            for(int j = 0; j< this.tamanhoMatriz; j++)
            {
                if(i==j)
                    this.matriz[i][j] = "(" + num.nextInt(10) + ", " + num.nextInt(10) + ")";
                if(i<j)
                {
                    num1 = num.nextInt(10);
                    num2 = num.nextInt(10);
                    this.matriz[i][j] =  "(" + num1 + ", " + num2 + ")";
                    this.matriz[j][i] =  this.matriz[i][j];
                }
            }
    }

    public void imprimirMatriz() {
        String matrizPraImprimir = new String();

        for(int i=0; i< tamanhoMatriz; i++) {

            matrizPraImprimir = matrizPraImprimir + " \n -----------------------------\n |";
            for(int j = 0; j< tamanhoMatriz; j++)
            {
                matrizPraImprimir = matrizPraImprimir + matriz[i][j] + " | ";
            }
        }
        matrizPraImprimir = matrizPraImprimir + " \n -----------------------------\n";


        System.out.println(matrizPraImprimir); // Imprimimos a matriz por consola
    }
 }
