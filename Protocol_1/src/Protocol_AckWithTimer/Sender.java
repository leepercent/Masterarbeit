package Protocol_AckWithTimer;

import ikr.simlib.distributions.continuous.ConstantDistribution;
import ikr.simlib.entities.Entity;
import ikr.simlib.entities.phases.StdPhase;
import ikr.simlib.events.CalendarCallback;
import ikr.simlib.events.EventToken;
import ikr.simlib.events.time.Duration;
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
	private Message message;
	private EventToken timeoutEvent;
	private final StdPhase frameProcTime;
	
  public Sender(SimNode ownNode) {
    super(ownNode);
    
    this.frameProcTime = new StdPhase(new ConstantDistribution(0.1e-3), simNode.createChildNode("FrameProcessingTime"));
    
    this.Out = new SynchronousOutputPort(this);
    this.OutputPortAfterFrameProc = this.frameProcTime.getOutput();
    
    this.In = new InputPort(this) {
      @Override
      protected void handleMessageIndication(Message msg) {
    	  if (isServerNotBusy()) {
//    		  System.out.println("----");
//    		  System.out.println(Calendar.getInstance().getSystemTime().toMilliSeconds());
    		  message = In.getMessage();
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
//    	    		System.out.println(Calendar.getInstance().getSystemTime().toMilliSeconds());
    				timeoutEvent.cancel();   // stop the Timer
	    			if (In.isMessageAvailable()){  
//	    	        	System.out.println("************from Queue***************");
//	    				System.out.println(Calendar.getInstance().getSystemTime().toMilliSeconds());
	    				message = In.getMessage();
	    				Out.sendMessage(message);
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
//    System.out.println("*************Retransmission*************");
//	System.out.println(Calendar.getInstance().getSystemTime().toMilliSeconds());	 
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
		  
		  this.timeoutEvent = Calendar.getInstance().postEvent(onExecute, null, Calendar.getInstance().getSystemTime().plus(Duration.fromMilliSeconds(1.8)));
  }
  
  
  private boolean isServerNotBusy(){
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
