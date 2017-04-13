package protocol;

import ikr.simlib.entities.Entity;
import ikr.simlib.entities.phases.StochasticInfiniteServer;
import ikr.simlib.entities.queues.QueuingEntity;
import ikr.simlib.model.SimNode;
import ikr.simlib.events.time.Duration;
import ikr.simlib.events.time.TimeUnits;
import ikr.simlib.entities.phases.DInfiniteServer;
import ikr.simlib.entities.phases.InfiniteServer;
import ikr.simlib.ports.input.InputPort;
import ikr.simlib.ports.output.OutputPort;

public class Delay extends Entity {
	private final InfiniteServer delay;

	public Delay(SimNode ownNode, double duration) {
		super(ownNode);
		
		final Duration d = Duration.fromMilliSeconds(duration);
		this.delay = new DInfiniteServer(d, this.simNode.createChildNode("DelayServer"));

	}
	
	public InputPort getInput(){
		return this.delay.getInput();
	}
	
	public OutputPort getOutput(){
		return this.delay.getOutput();
	}

}
