package Protocol_SR_Timeout;


import java.util.ArrayList;
import java.util.List;

import ikr.simlib.distributions.continuous.ConstantDistribution;
import ikr.simlib.entities.Entity;
import ikr.simlib.entities.phases.StdPhase;
import ikr.simlib.events.CalendarCallback;
import ikr.simlib.events.EventToken;
import ikr.simlib.events.time.Duration;
import ikr.simlib.messages.LabelMessage;
import ikr.simlib.messages.Message;
import ikr.simlib.model.SimNode;
import ikr.simlib.ports.input.InputPort;
import ikr.simlib.ports.output.OutputPort;
import ikr.simlib.ports.output.SynchronousOutputPort;
import ikr.simlib.events.calendar.Calendar;


public class Sender extends Entity {
	
	private final InputPort In;
	private final SynchronousOutputPort Out;
	private final OutputPort OutputPortAfterFrameProc;
	private final InputPort FeedbackIn;
	private final List<IDMessage> messages;
	private EventToken timeoutEvent;
	private final StdPhase frameProcTime;
	private Message message;
	
  public Sender(SimNode ownNode, int id) {
    super(ownNode);
    
    this.messages = new ArrayList<>();
    this.frameProcTime = new StdPhase(new ConstantDistribution(0.1e-3), simNode.createChildNode("FrameProcessingTime"));
    
    this.Out = new SynchronousOutputPort(this);
    this.OutputPortAfterFrameProc = this.frameProcTime.getOutput();
    
    this.In = new InputPort(this) {
      @Override
      protected void handleMessageIndication(Message msg) {
    	  if (isServerNotBusy()) {
    		  IDMessage message = (IDMessage) In.getMessage();
    		  message.setID(id);
//    		  System.out.println("######### "+(new Integer(id).toString())+" Send #########");
//    		  System.out.println("sender get meesage id from input port:");
//    		  System.out.println(message.getID());
    		  Out.sendMessage(message);
    		  startTimer();
    	  } 
      }
    };
    
    this.FeedbackIn = new InputPort(this) {
    	@Override
    	protected void handleMessageIndication(Message msg) {
    		msg = FeedbackIn.getMessage();
    		if (((AckMessage) msg).isAck()) {
    			if (timeoutEvent.isPending()){   // timeout has not reached
//    	    		System.out.println("********* "+(new Integer(id).toString())+" Acked *********");
    				timeoutEvent.cancel();   // stop the Timer
	    			if (In.isMessageAvailable()){  
	    	    		IDMessage message = (IDMessage) In.getMessage();
	    	    		message.setID(id);
	    				Out.sendMessage(message);
//	    				System.out.println("######### "+(new Integer(id).toString())+" Send #########");
	    				startTimer();
	    			}
    		}
        }
      }
    };
    
    // connect sender to constant delay
    this.Out.connect(this.frameProcTime.getInput());
    
  }
  
  
  public void timeOutAction(){
//	System.out.println("+++++++++ "+(new Integer(this.id).toString())+" Retransmit +++++++++");
	((SynchronousOutputPort) this.OutputPortAfterFrameProc).sendMessage(this.message);
	startTimer();
  }
  
  
  public void startTimer(){
		  CalendarCallback onExecute = new CalendarCallback(){
			  @Override
			  public void execute() {
				  timeOutAction();
			  }
		  };
		  
		  this.timeoutEvent = Calendar.getInstance().postEvent(onExecute, null, Calendar.getInstance().getSystemTime().plus(Duration.fromMilliSeconds(1.5)));
  }
  
  
  protected boolean isServerNotBusy(){
	  if (this.timeoutEvent == null) {
		  return true;
	  } else {
	  return !this.timeoutEvent.isPending();
	  }
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
	  return this.OutputPortAfterFrameProc;
  }
  
}
