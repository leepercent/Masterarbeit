package Protocol_SR;

import ikr.simlib.entities.demultiplexers.StdDemultiplexer;
import ikr.simlib.exceptions.SimulationException;
import ikr.simlib.messages.Message;
import ikr.simlib.model.SimNode;
import ikr.simlib.ports.Port;

public class IDDemultiplexer extends StdDemultiplexer{
	
	protected int startID;

	public IDDemultiplexer(int noOfPorts, SimNode ownNode) {
		this(noOfPorts, ownNode, 0);
	}
	

	public IDDemultiplexer(int noOfPorts, SimNode ownNode, int offsetID) {
		super(noOfPorts, ownNode);
		this.startID = offsetID;
	}


	@Override
	protected Port determineOutputPort(Message msg) {
		IDMessage IDmsg = (IDMessage) msg;
		int id = IDmsg.getID();
//		System.out.println("id demultiplexer get message id:");
//		System.out.println(id);
		if (id < startID || id >= (startID + outputPorts.size())){
			throw new SimulationException("label out of range:" + id);
		}

		return outputPorts.get(id - startID);
	}

	@Override
	public void removePort(String portName) {
		throw new SimulationException("Method not supported to ensure consistency");
	}
}
