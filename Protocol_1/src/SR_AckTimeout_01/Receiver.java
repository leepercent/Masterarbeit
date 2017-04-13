package SR_AckTimeout_01;

import ikr.simlib.entities.Entity;
import ikr.simlib.messages.LabelMessage;
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
  private int id;
  private LabelMessage m;

  public Receiver(SimNode ownNode, final double dropPrbability) {
    super(ownNode);

    this.sinkOut = new SynchronousOutputPort(this);
    this.out = new SynchronousOutputPort(this);    

    final StdRandomNumberGenerator r = new StdRandomNumberGenerator();
    
    this.in = new InputPort(this) {
      @Override
      protected void handleMessageIndication(Message msg) {
        m = (LabelMessage) in.getMessage();
        id = m.getLabel(); 
        if(r.next() < 1-dropPrbability) {
          sinkOut.sendMessage(m);
          out.sendMessage(new AckMessage(true, id));
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
