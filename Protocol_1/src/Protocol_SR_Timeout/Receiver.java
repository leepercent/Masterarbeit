package Protocol_SR_Timeout;

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
  private final SynchronousOutputPort feedbackOut;
  private int id;

  public Receiver(SimNode ownNode, final double dropPrbability) {
    super(ownNode);

    this.sinkOut = new SynchronousOutputPort(this);
    this.feedbackOut = new SynchronousOutputPort(this);    

    final StdRandomNumberGenerator r = new StdRandomNumberGenerator();
    
    this.in = new InputPort(this) {
      @Override
      protected void handleMessageIndication(Message msg) {
        IDMessage m = (IDMessage) in.getMessage();
        id = m.getID();
        if(r.next() < 1-dropPrbability) {
          sinkOut.sendMessage(m);
          feedbackOut.sendMessage(new AckMessage(true, id));
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
  
  public OutputPort getFeedbackOutput(){
	  return this.feedbackOut;
  }
}
