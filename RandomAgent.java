import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RandomAgent extends Agent
{
	private int numeroFilas;

	public void setup ()
	{
		System.out.println(getLocalName() + " --> Lanzando...");

		//Rexistramos o axente nas paxinas amarelas
		DFAgentDescription AgentDescripcion = new DFAgentDescription();
		AgentDescripcion.setName(getAID());
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("Xogador");
		serviceDescription.setName("Axente aleatorio");
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

		//Creamos a resposta os mensaxes de tipo "ACLMessage.REQUEST" e de tipo "ACLMessage.INFORM" (so o primeiro para saber o numero de filas)
		addBehaviour(new RequestBehaviour());
		addBehaviour(new InformBehaviour());
	}

	protected void takeDown ()
	{
		System.out.println(getLocalName() + " --> Pechando...");
	}

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
				//Se chega unha mensaxe coa informacion da partida, gardamos o numero de filas
				if (mensaxe.getContent().startsWith("Id"))
					numeroFilas = Integer.parseInt(mensaxe.getContent().split("#")[2].split(",")[1]);
			}
			else
				block();
		}
	}

	private class RequestBehaviour extends CyclicBehaviour
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void action ()
		{
			Random rand = new Random();
			MessageTemplate buzon = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage mensaxe = receive(buzon);
			if (mensaxe != null)
			{
				System.out.println(getLocalName() + " --> Enviando fila ou columna...");
				ACLMessage resposta = new ACLMessage(ACLMessage.INFORM);
				resposta.setContent("Position#" + rand.nextInt(numeroFilas));
				resposta.addReceiver(mensaxe.getSender());
				send(resposta);
			}
			else
				block();
		}
	}
}
