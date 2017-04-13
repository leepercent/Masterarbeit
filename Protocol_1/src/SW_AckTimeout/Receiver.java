package SW_AckTimeout;

import ikr.simlib.entities.Entity;
import ikr.simlib.events.calendar.Calendar;
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

  public Receiver(SimNode ownNode, final double dropPrbability) {
    super(ownNode);

    this.sinkOut = new SynchronousOutputPort(this);
    this.out = new SynchronousOutputPort(this);    

    final StdRandomNumberGenerator r = new StdRandomNumberGenerator();
    
    this.in = new InputPort(this) {
      @Override
      protected void handleMessageIndication(Message msg) {
        Message m = in.getMessage();
        if(r.next() < 1-dropPrbability) {
//          System.out.println(String.format("%.3f", Calendar.getInstance().getSystemTime().toMilliSeconds())
//        		  + "ms, message received");
          out.sendMessage(new AckMessage(true));
          sinkOut.sendMessage(m);
        }
//        else{
//        	System.out.println(String.format("%.3f", Calendar.getInstance().getSystemTime().toMilliSeconds())
//          		  + "ms, message droped");
//        } 
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