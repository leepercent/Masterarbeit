package others;

import ikr.simlib.distributions.continuous.ConstantDistribution;
import ikr.simlib.distributions.continuous.ContinuousDistribution;
import ikr.simlib.entities.Entity;
import ikr.simlib.entities.phases.Phase;
import ikr.simlib.entities.phases.StdPhase;
import ikr.simlib.events.time.Duration;
import ikr.simlib.exceptions.SimulationException;
import ikr.simlib.messages.Message;
import ikr.simlib.model.SimNode;
import ikr.simlib.ports.input.InputPort;
import ikr.simlib.ports.output.OutputPort;

public class frameProcTime extends Phase{
	
	private final OutputPort out;
	
	protected ContinuousDistribution serviceTime;
	
	public frameProcTime(ContinuousDistribution d, SimNode ownNode) {
		super(ownNode);
		serviceTime = d;

		this.out = new OutputPort(this) {
			@Override
			protected boolean handleIsMessageAvailable() {
				return inputPort.isMessageAvailable();
			}

			@Override
			protected Message handleGetMessage() {
				return inputPort.getMessage();
			}
		};
	}

	@Override
	protected Duration getMessageProcessingDuration(Message msg) {
		if (serviceTime == null)
			throw new SimulationException("undefined IAT distribution in " + simNode.getFullName());

		return Duration.fromSeconds(serviceTime.next());
	}

//	@Override
//	protected void forwardProcessedMessage() {
//		out.messageIndication(message);
//	}
	

}
