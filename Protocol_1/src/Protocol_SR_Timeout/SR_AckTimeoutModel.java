package Protocol_SR_Timeout;

import Protocol_SR.Delay;
import Protocol_SR.IDDemultiplexer;
import Protocol_SR.Multiplexer_Output;
import ikr.simlib.control.simulationcontrol.ControlTimer;
import ikr.simlib.distributions.continuous.ConstantDistribution;
import ikr.simlib.distributions.continuous.NegExpDistribution;
import ikr.simlib.distributions.discrete.DiscreteConstantDistribution;
import ikr.simlib.entities.demultiplexers.LabelDemultiplexer;
import ikr.simlib.entities.generators.Generator;
import ikr.simlib.entities.generators.StdGenerator;
import ikr.simlib.entities.multiplexers.StdMultiplexer;
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
import ikr.simlib.ports.input.InputPort;
import ikr.simlib.ports.output.OutputPort;
import ikr.simlib.statistics.distribution.StdDistributionStatistic;
import ikr.simlib.statistics.sample.StdSampleStatistic;

public class SR_AckTimeoutModel extends Model {
	private final Generator generator;
	private final Sender sender1;
	private final Sender sender2;
	private final Sender sender3;
	private final Sender sender4;
	private final Receiver receiver;
	private final Sink sink;
	private final TimeMeter meter1;
	private final TimeMeter meter2;
	private final TimeMeter meter3;
	private final double DropProbablity;
	private final Delay propDelay;
	private final QueuingEntity queue;
	private final StdMultiplexer m2;
	  
	@SuppressWarnings("unused")
	private final ControlTimer controlTimer;
	private Multiplexer_Output m1;
	private IDDemultiplexer m3;

	public SR_AckTimeoutModel(Parameters pars) {
		super("SR_Timeout");

		// parse model parameters
		final int transientPhaseTime = 500; 	// pars.getOrUseDefault(this.simNode,"TransientPhaseTime","50").asInteger();
		final int batchTime = 30000; 	// pars.getOrUseDefault(this.simNode,"BatchTime","100").asInteger();
		this.DropProbablity = pars.get(this.simNode, "dropProbability").asDouble();
		final Duration d = Duration.fromMilliSeconds(1.0);
		

		final SimNode generatorNode = this.simNode.createChildNode("Generator");
		final double numdaMean = pars.get(generatorNode, "arrivalRate").asDouble();
		final double iatMean = 1/numdaMean * 1e-3; 		// convert second to millisecond
		
		// create entities
		this.generator = new StdGenerator(new NegExpDistribution(iatMean), new DiscreteConstantDistribution(1),generatorNode, new IDMessageFactory(), true);
		
		this.queue = QueuingEntity.createUnboundedFIFOQueue(this.simNode.createChildNode("Queue"));
		
		this.sender1 = new Sender(this.simNode.createChildNode("Sender_1"), 1);
		this.sender2 = new Sender(this.simNode.createChildNode("Sender_2"), 2);
		this.sender3 = new Sender(this.simNode.createChildNode("Sender_3"), 3);
		this.sender4 = new Sender(this.simNode.createChildNode("Sender_4"), 4);
		
		this.m1 = new Multiplexer_Output(4, this.simNode.createChildNode("MultiplexerAfterQueue"));
		this.m2 = new StdMultiplexer(4, this.simNode.createChildNode("MultiplexerAfterSenders"));
		this.m3 = new IDDemultiplexer(4, this.simNode.createChildNode("DemultiplexerAfterReceiver"), 1);
		
		this.receiver = new Receiver(this.simNode.createChildNode("Receiver"), this.DropProbablity);
		
		this.sink = new Sink(this.simNode.createChildNode("Sink"));
		
		this.propDelay = new Delay(this.simNode.createChildNode("RoundTripTime"));
		
		// create meters
		this.meter1 = TimeMeter.createWithDoubleStatistic(new StdSampleStatistic(this.simNode.createChildNode("VirtualServiceTime")));
		this.meter2 = TimeMeter.createWithDoubleStatistic(new StdSampleStatistic(this.simNode.createChildNode("QueueTime")));
		this.meter3 = TimeMeter.createWithDoubleStatistic(new StdDistributionStatistic(500, 0, 6.0e-3, this.simNode.createChildNode("ServiceTimeDistribution")));
		
		// create control counter
		this.controlTimer = new ControlTimer(Duration.fromSeconds(transientPhaseTime), Duration.fromSeconds(batchTime));

		// connect entities
		this.generator.getOutput().connect(this.queue.getInput());
		this.queue.getOutput().connect(this.m1.getInput());

		this.m1.getOutput(1).connect(this.sender1.getInput());
		this.m1.getOutput(2).connect(this.sender2.getInput());
		this.m1.getOutput(3).connect(this.sender3.getInput());
		this.m1.getOutput(4).connect(this.sender4.getInput());
		
		this.sender1.getOutputPortAfterFrameProc().connect((InputPort) this.m2.getPortByName("input 1"));
		this.sender2.getOutputPortAfterFrameProc().connect((InputPort) this.m2.getPortByName("input 2"));
		this.sender3.getOutputPortAfterFrameProc().connect((InputPort) this.m2.getPortByName("input 3"));
		this.sender4.getOutputPortAfterFrameProc().connect((InputPort) this.m2.getPortByName("input 4"));
		
		((OutputPort) this.m2.getPortByName("output")).connect(this.propDelay.getInput());
		this.propDelay.getOutput().connect(this.receiver.getInput());
		
		this.receiver.getFeedbackOutput().connect(this.m3.getInput());
		
		this.m3.getOutput(0).connect(this.sender1.getFeedbackInput());
		this.m3.getOutput(1).connect(this.sender2.getFeedbackInput());
		this.m3.getOutput(2).connect(this.sender3.getFeedbackInput());
		this.m3.getOutput(3).connect(this.sender4.getFeedbackInput());
		
		this.receiver.getOutputSink().connect(this.sink.getInput());
		
		// attach meters and control counter
		this.meter1.attachFromPort(this.queue.getOutput());
		this.meter1.attachToPort(this.sink.getInput());

		this.meter2.attachFromPort(this.queue.getInput());
		this.meter2.attachToPort(this.queue.getOutput());
		
		this.meter3.attachFromPort(this.queue.getOutput());
		this.meter3.attachToPort(this.sink.getInput());

	}

}
