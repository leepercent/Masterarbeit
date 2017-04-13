package Protocol;

import ikr.simlib.entities.Entity;
import ikr.simlib.messages.Message;
import ikr.simlib.model.SimNode;
import ikr.simlib.ports.input.InputPort;
import ikr.simlib.ports.output.OutputPort;
import ikr.simlib.ports.output.SynchronousOutputPort;
import ikr.simlib.random.StdRandomNumberGenerator;

public class Receiver extends Entity{
  
  private final InputPort in;
  private final SynchronousOutputPort sinkOut;
  private final SynchronousOutputPort out;
  private final double passProbability;

  public Receiver(SimNode ownNode, final double dropPrbability) {
    super(ownNode);

    this.sinkOut = new SynchronousOutputPort(this);
    this.out = new SynchronousOutputPort(this);    
    this.passProbability = 1 - dropPrbability;

    final StdRandomNumberGenerator r = new StdRandomNumberGenerator();
    
    this.in = new InputPort(this) {
      @Override
      protected void handleMessageIndication(Message msg) {
        Message m = in.getMessage();
        if(r.next() < passProbability) {
          sinkOut.sendMessage(m);
          out.sendMessage(new AckNackMessage(true));
        } else {
          out.sendMessage(new AckNackMessage(false));
        }
      }
    };
  }

  
  public InputPort getInput() {
	  return this.in;
  }
  
  public OutputPort getOutputSink() {
	  return this.sinkOut;
  }
  
  public OutputPort getOutput(){
	  return this.out;
  }
}
