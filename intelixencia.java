import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class intelixencia extends Agent
{
	private static final long serialVersionUID = 1L;

	private int ID, numeroFilas;
	private int[][] matriz;
	private boolean elexirFilas = false;
	private final static int Victoria = 1;
	private final static int Empate = 0;
	private final static int Derrota = -1;


	public void setup ()
	{
		System.out.println(getLocalName() + " --> Lanzando...");

		//Rexistramos o axente nas paxinas amarelas
		DFAgentDescription AgentDescripcion = new DFAgentDescription();
		AgentDescripcion.setName(getAID());
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("Xogador");
		serviceDescription.setName("Axente intelixente");
		AgentDescripcion.addServices(serviceDescription);
		try
		{
			DFService.register(this, AgentDescripcion);
		}

		//Se non se pode rexitrar, pechamos o axente
		catch (FIPAException exception)
		{
			exception.printStackTrace();
			doDelete();
		}

		System.out.println(getLocalName() + " --> Rexistrado");

		//Creamos a resposta os mensaxes de tipo "ACLMessage.REQUEST" e de tipo "ACLMessage.INFORM" (so o primeiro para saber o numero de elexirFilas)
		addBehaviour(new RequestBehaviour());
		addBehaviour(new InformBehaviour());
	}

	protected void takeDown ()
	{
		System.out.println(getLocalName() + " --> Pechando...");
	}

	//Behaviour para os mensaxes de tipo "Inform"
	private class InformBehaviour extends CyclicBehaviour
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void action ()
		{
			MessageTemplate buzon = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage mensaxe = receive(buzon);
			if (mensaxe != null)
			{
				//Cando cheganos este mensaxe gardamos o tamanho da matriz e o ID do axente
				if (mensaxe.getContent().startsWith("Id"))
				{
					ID = Integer.parseInt(mensaxe.getContent().split("#")[1]);
					numeroFilas = Integer.parseInt(mensaxe.getContent().split("#")[2].split(",")[1]);
				}

				//Cando empeza un xogo inicializamos a matriz e miramos se eliximos fila ou columna, e pomos a false que a matriz cambiou
				if (mensaxe.getContent().startsWith("NewGame"))
				{
					matriz = new int[numeroFilas][numeroFilas];
					for (int i = 0; i < numeroFilas; i++) for(int j = 0; j < numeroFilas; j++) matriz[i][j] = Empate;
					if (Integer.parseInt(mensaxe.getContent().split("#")[1].split(",")[0]) == ID) elexirFilas = true;
					else elexirFilas = false;
				}

				//Cando cambia a matriz, indicamos iso e reinicamos a matriz ata descubrir os cambios
				if (mensaxe.getContent().startsWith("Changed"))
				{
					if (Float.parseFloat(mensaxe.getContent().split("#")[1]) >= 35.0) // So reiniciamos se a porcentaxe é maior de 35%
						for (int i = 0; i < numeroFilas; i++) for(int j = 0; j < numeroFilas; j++) matriz[i][j] = Empate;
				}

				//Gardamos as posicions na matriz
				if (mensaxe.getContent().startsWith("Results"))
				{
					int fila = Integer.parseInt(mensaxe.getContent().split("#")[1].split(",")[0]);
					int columna = Integer.parseInt(mensaxe.getContent().split("#")[1].split(",")[1]);
					int[] resultado = { Integer.parseInt(mensaxe.getContent().split("#")[2].split(",")[0]), Integer.parseInt(mensaxe.getContent().split("#")[2].split(",")[1]) };
					
					// 
					if(elexirFilas) {
						if(resultado[0] > resultado[1]) 
						{ 
							matriz[fila][columna] = Victoria; 
							if(fila != columna) matriz[columna][fila] = Derrota;
						}
						else 
						{
							matriz[fila][columna] = Derrota;
							if(fila != columna) matriz[columna][fila] = Victoria;
						}
					}
					else
					{
						if(resultado[1] > resultado[0]) 
						{ 
							matriz[fila][columna] = Victoria; 
							if(fila != columna) matriz[columna][fila] = Derrota;
						}
						else 
						{
							matriz[fila][columna] = Derrota;
							if(fila != columna) matriz[columna][fila] = Victoria;
						}
					}
				}
			}
			else
				block();
		}
	}

	//Behaviour para elixila fila ou columna a enviar
	private class RequestBehaviour extends CyclicBehaviour
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void action ()
		{
			MessageTemplate buzon = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage mensaxe = receive(buzon);

			//Se existe a mensaxe, calculamos a resposta
			if (mensaxe != null)
			{
				System.out.println(getLocalName() + " --> Elixindo fila ou columna...");
				Random rand = new Random();
				int eleccion = rand.nextInt(numeroFilas);
				int[] vector = new int[numeroFilas];

				for (int fila = 0; fila < numeroFilas; fila++)
					for (int columna = 0; columna < numeroFilas; columna++)
					{
						System.out.println("Intelixencia: " + fila + " " + columna);
						if (matriz[fila][columna] == Victoria)
						{
							if (elexirFilas) vector[fila]++;
							else vector[columna]++;
						}
						if (matriz[fila][columna] == Derrota)
						{
							if (elexirFilas) vector[fila]--;
							else vector[columna]--;
						}
					}
			

				//Eliximos a opcion con mais positivos
				int maximo = vector[0];
				for (int i = 0; i < vector.length; i++)
					if (vector[i] > maximo)
					{
						maximo = vector[i];
						eleccion = i;
					}

				//Enviamos a mensaxe coa resposta
				System.out.println(getLocalName() + " --> Enviando fila ou columna...");
				ACLMessage resposta = new ACLMessage(ACLMessage.INFORM);
				resposta.setContent("Position#" + eleccion);
				resposta.addReceiver(mensaxe.getSender());
				send(resposta);
			}
			else
				block();
		}
	}
}
