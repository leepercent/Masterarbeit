package SR_AckTimeout_01;

import ikr.simlib.entities.Entity;
import ikr.simlib.entities.phases.DInfiniteServer;
import ikr.simlib.entities.phases.InfiniteServer;
import ikr.simlib.entities.phases.UnitPhase;
import ikr.simlib.entities.queues.QueuingEntity;
import ikr.simlib.events.time.Duration;
import ikr.simlib.model.SimNode;
import ikr.simlib.ports.input.InputPort;
import ikr.simlib.ports.output.OutputPort;

public class Delay extends Entity {
	
	private final InfiniteServer propDelay;
	private final UnitPhase frameTransmissionTime;
	private QueuingEntity queue;
	
	public Delay(SimNode ownNode) {
		super(ownNode);
		
		this.propDelay = new DInfiniteServer(Duration.fromMilliSeconds(0.5), this.simNode.createChildNode("PropergationDelay"));
		
		this.frameTransmissionTime = new UnitPhase(Duration.fromMilliSeconds(0.5), this.simNode.createChildNode("FrameTransmissionTime"));
		
		this.queue = QueuingEntity.createUnboundedFIFOQueue(this.simNode.createChildNode("Queue_Channel"));
		
		this.queue.getOutput().connect(this.frameTransmissionTime.getInput());
		this.frameTransmissionTime.getOutput().connect(this.propDelay.getInput());
		
	}
	
	public OutputPort getOutput(){
		return this.propDelay.getOutput();
	}
	
	
	public InputPort getInput(){
		return this.queue.getInput();
	}

}
