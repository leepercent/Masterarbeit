package Protocol;

import ikr.simlib.distributions.continuous.ConstantDistribution;
import ikr.simlib.entities.Entity;
import ikr.simlib.entities.phases.StdPhase;
import ikr.simlib.events.calendar.Calendar;
import ikr.simlib.messages.Message;
import ikr.simlib.model.SimNode;
import ikr.simlib.ports.input.InputPort;
import ikr.simlib.ports.output.OutputPort;
import ikr.simlib.ports.output.SynchronousOutputPort;

public class Sender extends Entity {

	private final InputPort In;
	private final SynchronousOutputPort Out;
	private final InputPort FeedbackIn;
	private final OutputPort OutAfterFrameProc;
	private final StdPhase frameProcTime;
	private boolean waitForFeedback = false;
	private Message message;


  public Sender(SimNode ownNode) {
    super(ownNode);
    
    this.frameProcTime = new StdPhase(new ConstantDistribution(0.1e-3), simNode.createChildNode("FrameProcessingTime"));
    this.Out = new SynchronousOutputPort(this);
    this.OutAfterFrameProc = this.frameProcTime.getOutput();
    
    this.In = new InputPort(this) {
      @Override
      protected void handleMessageIndication(Message msg) {
    	  if (!waitForFeedback) {
    		  System.out.println(Calendar.getInstance().getSystemTime());
    		  message = In.getMessage();
    		  Out.sendMessage(message);
    		  waitForFeedback = true;
    	  } 
      }
    };

    this.FeedbackIn = new InputPort(this) {
      @Override
      protected void handleMessageIndication(Message msg) {
    	msg = FeedbackIn.getMessage();
        if (((AckNackMessage) msg).isAck()) {
          if (!In.isMessageAvailable()){
        	  System.out.println(Calendar.getInstance().getSystemTime());
        	  waitForFeedback = false;
          }else{
        	  System.out.println("*****************************");
        	  System.out.println(Calendar.getInstance().getSystemTime());
        	  message = In.getMessage();
        	  Out.sendMessage(message);        	  
        	  waitForFeedback = true;
          }
        } else {
//        	Out.sendMessage(message);
      	  System.out.println("*****************************");
        	System.out.println(Calendar.getInstance().getSystemTime());
        	((SynchronousOutputPort) OutAfterFrameProc).sendMessage(message); //retransmission
        	waitForFeedback = true;
        }
      }
    };
    
    this.Out.connect(this.frameProcTime.getInput());
       
  }
  
  public InputPort getInput() {
	  return this.In;
  }
  
  public InputPort getFeedbackInput(){
	  return this.FeedbackIn;
  }
  
  public OutputPort getOutput() {
	  return this.Out;
  }
  
  public OutputPort getOutputPortAfterFrameProc(){
	  return this.OutAfterFrameProc;
  }

}
