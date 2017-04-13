package protocol;

import ikr.simlib.control.simulationcontrol.ControlTimer;
import ikr.simlib.distributions.continuous.NegExpDistribution;
import ikr.simlib.distributions.discrete.DiscreteConstantDistribution;
import ikr.simlib.entities.generators.Generator;
import ikr.simlib.entities.generators.StdGenerator;
import ikr.simlib.entities.queues.QueuingEntity;
import ikr.simlib.entities.sinks.Sink;
import ikr.simlib.events.time.Duration;
import ikr.simlib.meters.time.TimeMeter;
import ikr.simlib.model.Model;
import ikr.simlib.model.SimNode;
import ikr.simlib.parameters.Parameters;
import ikr.simlib.statistics.distribution.StdDistributionStatistic;
import ikr.simlib.statistics.sample.StdSampleStatistic;

public class AckNackModel extends Model {
	private final Generator generator;
	private final Sender sender;
	private final Receiver receiver;
	private final Sink sink;
	private final TimeMeter meter1;
	private final TimeMeter meter2;
	private final double DropProbablity;
	private final Delay forwardDelay;
	private final double RRT;
//	private final Delay backwardDelay;
//	private final QueuingEntity queue;

	@SuppressWarnings("unused")
	private final ControlTimer controlTimer;

	public AckNackModel(Parameters pars) {
		super("AckNackModel");

		// parse model parameters
		final int transientPhaseTime = 500; 	// pars.getOrUseDefault(this.simNode,"TransientPhaseTime","50").asInteger();
		final int batchTime = 1000; 	// pars.getOrUseDefault(this.simNode,"BatchTime","100").asInteger();
		this.DropProbablity = 0.9;
		this.RRT=1.0;
		
		// create entities
		final SimNode generatorNode = this.simNode.createChildNode("Generator");
		final double iatMean = pars.get(generatorNode, "IAT").asDouble();

		// final double msgLength = pars.get(generatorNode,"msgLength").asDouble();

		this.generator = new StdGenerator(new NegExpDistribution(iatMean), new DiscreteConstantDistribution(1),generatorNode, null, true);
//		this.queue = QueuingEntity.createUnboundedFIFOQueue(this.simNode.createChildNode("Queue"));
		this.sender = new Sender(this.simNode.createChildNode("Sender"));
		this.receiver = new Receiver(this.simNode.createChildNode("Receiver"), this.DropProbablity);
		this.sink = new Sink(this.simNode.createChildNode("Sink"));
		this.forwardDelay = new Delay(this.simNode.createChildNode("ForwardDelay"), this.RRT);
//		this.backwardDelay = new Delay (this.simNode.createChildNode("BackwardDelay"), 0.5);
		
		// create meters
		this.meter1 = TimeMeter.createWithDoubleStatistic(new StdSampleStatistic(this.simNode.createChildNode("VirtualServiceTime")));
		this.meter2 = TimeMeter.createWithDoubleStatistic(new StdSampleStatistic(this.simNode.createChildNode("FlowTime")));

		// create control counter
		this.controlTimer = new ControlTimer(Duration.fromSeconds(transientPhaseTime), Duration.fromSeconds(batchTime));

		// connect entities
		this.generator.getOutput().connect(this.sender.getInput());
//		this.queue.getOutput().connect(this.sender.getInput());
		this.sender.getOutputAfterFrameProc().connect(this.forwardDelay.getInput());
		this.forwardDelay.getOutput().connect(this.receiver.getInput());
		this.receiver.getOutput().connect(this.sender.getFeedbackInput());
//		this.backwardDelay.getOutput().connect(this.sender.getFeedbackInput());
		
		this.receiver.getOutputSink().connect(this.sink.getInput());

		
		// attach meters and control counter
		this.meter1.attachFromPort(this.sender.getOutput());
		this.meter1.attachToPort(this.sink.getInput());

		this.meter2.attachFromPort(this.generator.getOutput());
		this.meter2.attachToPort(this.sink.getInput());

	}

}
