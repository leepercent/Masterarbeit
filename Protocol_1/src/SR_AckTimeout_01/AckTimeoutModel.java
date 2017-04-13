package SR_AckTimeout_01;

import ikr.simlib.control.simulationcontrol.ControlTimer;
import ikr.simlib.distributions.continuous.ConstantDistribution;
import ikr.simlib.distributions.continuous.NegExpDistribution;
import ikr.simlib.distributions.discrete.DiscreteConstantDistribution;
import ikr.simlib.entities.generators.Generator;
import ikr.simlib.entities.generators.StdGenerator;
import ikr.simlib.entities.phases.DInfiniteServer;
import ikr.simlib.entities.phases.InfiniteServer;
import ikr.simlib.entities.phases.StdPhase;
import ikr.simlib.entities.queues.QueuingEntity;
import ikr.simlib.entities.sinks.Sink;
import ikr.simlib.events.time.Duration;
import ikr.simlib.meters.time.TimeMeter;
import ikr.simlib.model.Model;
import ikr.simlib.model.SimNode;
import ikr.simlib.parameters.Parameters;
import ikr.simlib.statistics.distribution.StdDistributionStatistic;
import ikr.simlib.statistics.sample.StdSampleStatistic;

public class AckTimeoutModel extends Model {
	private final Generator generator;
	private final Sender sender;
	private final Receiver receiver;
	private final Sink sink;
	private final TimeMeter meter1;
	private final TimeMeter meter2;
	private final TimeMeter meter3;
	private final double DropProbablity;
	private final StdPhase constDelay;
//	private final StdPhase variableDelay;
	private final QueuingEntity queue;
	  
	@SuppressWarnings("unused")
	private final ControlTimer controlTimer;

	public AckTimeoutModel(Parameters pars) {
		super("AckTimerModel");

		// parse model parameters
		final int transientPhaseTime = 500; 	// pars.getOrUseDefault(this.simNode,"TransientPhaseTime","50").asInteger();
		final int batchTime = 1000; 	// pars.getOrUseDefault(this.simNode,"BatchTime","100").asInteger();
		this.DropProbablity = pars.get(this.simNode, "dropProbability").asDouble();
//		final Duration d = Duration.fromMilliSeconds(1.0);
		

		final SimNode generatorNode = this.simNode.createChildNode("Generator");
		final double numdaMean = pars.get(generatorNode, "arrivalRate").asDouble();
		final double iatMean = 1/numdaMean * 1e-3; 		// convert second to millisecond
		
		// create entities
		this.generator = new StdGenerator(new NegExpDistribution(iatMean), new DiscreteConstantDistribution(1),generatorNode, new AutoIncrementedLabelMessageFactory(1, 0), true);
		this.queue = QueuingEntity.createUnboundedFIFOQueue(this.simNode.createChildNode("Queue"));

		this.sender = new Sender(this.simNode.createChildNode("Sender"));
		this.receiver = new Receiver(this.simNode.createChildNode("Receiver"), this.DropProbablity);
		this.sink = new Sink(this.simNode.createChildNode("Sink"));
		this.constDelay = new StdPhase(new ConstantDistribution(1.0e-3), this.simNode.createChildNode("ConstDelay"));
//		this.variableDelay = new StdPhase(new NegExpDistribution(0.05e-3) ,this.simNode.createChildNode("VariableDelay"));
		
		// create meters
		this.meter1 = TimeMeter.createWithDoubleStatistic(new StdSampleStatistic(this.simNode.createChildNode("VirtualServiceTime")));
		this.meter2 = TimeMeter.createWithDoubleStatistic(new StdSampleStatistic(this.simNode.createChildNode("FlowTime")));
		this.meter3 = TimeMeter.createWithDoubleStatistic(new StdDistributionStatistic(500, 0, 6.0e-3, this.simNode.createChildNode("ServiceTimeDistribution")));
		
		// create control counter
		this.controlTimer = new ControlTimer(Duration.fromSeconds(transientPhaseTime), Duration.fromSeconds(batchTime));

		// connect entities
		this.generator.getOutput().connect(this.queue.getInput());
		this.queue.getOutput().connect(this.sender.getInput());
		this.sender.getOutputPortAfterFrameProc().connect(this.constDelay.getInput());
		this.constDelay.getOutput().connect(this.receiver.getInput());
//		this.variableDelay.getOutput().connect(this.receiver.getInput());
		this.receiver.getOutput().connect(this.sender.getFeedbackInput());
		this.receiver.getOutputSink().connect(this.sink.getInput());

		
		// attach meters and control counter
		this.meter1.attachFromPort(this.queue.getOutput());
		this.meter1.attachToPort(this.sink.getInput());

		this.meter2.attachFromPort(this.generator.getOutput());
		this.meter2.attachToPort(this.sink.getInput());
		
		this.meter3.attachFromPort(this.queue.getOutput());
		this.meter3.attachToPort(this.sink.getInput());
	}
}