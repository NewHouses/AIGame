import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FixedAgent extends Agent
{

	public void setup ()
	{
		System.out.println(getLocalName() + " --> Lanzando...");

		//Rexistramos o axente nas paxinas amarelas
		DFAgentDescription AgentDescripcion = new DFAgentDescription();
		AgentDescripcion.setName(getAID());
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("Xogador");
		serviceDescription.setName("Axente fixo");
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

		//Creamos a resposta os mensaxes de tipo "ACLMessage.REQUEST", os de tipo "ACLMessage.INFORM" obviamolos porque non influen en nada
		addBehaviour(new RequestBehaviour());
	}

	protected void takeDown ()
	{
		System.out.println(getLocalName() + " --> Pechando...");
	}

	private class RequestBehaviour extends CyclicBehaviour
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void action ()
		{
			MessageTemplate buzon = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); // Buzon para as mensaxes
			ACLMessage mensaxe = receive(buzon); // Obtemos as mensaxes procedentes do MainAgent
			if (mensaxe != null)
			{
				System.out.println(getLocalName() + " --> Enviando fila ou columna...");
				ACLMessage resposta = new ACLMessage(ACLMessage.INFORM);
				resposta.setContent("Position#0");
				resposta.addReceiver(mensaxe.getSender()); // Enviamos a posicion ao MainAgent
				send(resposta);
			}
			else
				block();
		}
	}
}
