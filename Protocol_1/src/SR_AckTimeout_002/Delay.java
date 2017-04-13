package SR_AckTimeout_002;

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
	
	private final InfiniteServer delay;
	private final UnitPhase frameTransmissionTime;
	private QueuingEntity queue;
	
	public Delay(SimNode ownNode, double frameTransTime, double propDelay) {
		super(ownNode);
		
		this.frameTransmissionTime = new UnitPhase(Duration.fromMilliSeconds(frameTransTime), this.simNode.createChildNode("FrameTransmissionTime"));
		this.delay = new DInfiniteServer(Duration.fromMilliSeconds(propDelay), this.simNode.createChildNode("PropergationDelay"));		
		this.queue = QueuingEntity.createUnboundedFIFOQueue(this.simNode.createChildNode("Queue_Channel"));
		
		this.queue.getOutput().connect(this.frameTransmissionTime.getInput());
		this.frameTransmissionTime.getOutput().connect(this.delay.getInput());
		
	}
	
	
	public InputPort getInput(){
		return this.queue.getInput();
	}	
	
	
	public OutputPort getOutput(){
		return this.delay.getOutput();
	}
	
}
