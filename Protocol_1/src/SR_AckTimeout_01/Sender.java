package SR_AckTimeout_01;

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
	private LabelMessage message;
	private final List<LabelMessage> windowBuffer;
	private EventToken timeoutEvent;
	private final StdPhase frameProcTime;
	
  public Sender(SimNode ownNode) {
	  super(ownNode);

	  this.windowBuffer = new ArrayList<>(4);

	  this.frameProcTime = new StdPhase(new ConstantDistribution(0.1e-3), simNode.createChildNode("FrameProcessingTime"));

	  this.Out = new SynchronousOutputPort(this);
	  this.OutputPortAfterFrameProc = this.frameProcTime.getOutput();

	  this.In = new InputPort(this) {
		  @Override
		  protected void handleMessageIndication(Message msg) {
			  if (isServerNotBusy()) {
				  if (windowBuffer.size() < 4){
					  for (int i=3; i > windowBuffer.size()-1;i--){
						  if(In.isMessageAvailable()){
							  message = (LabelMessage) In.getMessage();
							  windowBuffer.add(message);
							  System.out.println("**********Transmission1************");
							  System.out.println(message.getLabel());
							  Out.sendMessage(message);
							  startTimer();
						  }
					  }
				  }
			  } 
		  }
	  };

	  this.FeedbackIn = new InputPort(this) {
		  @Override
		  protected void handleMessageIndication(Message msg) {
			  LabelMessage AckMsg = (LabelMessage) FeedbackIn.getMessage();
			  int id = AckMsg.getLabel();
			  System.out.println(id);
			  if (((AckMessage) msg).isAck()) {
				  if (timeoutEvent.isPending()){   // timeout has not reached
					  timeoutEvent.cancel();   // stop the Timer
					  for (int i=0; i<=windowBuffer.size();i++){
						  if(windowBuffer.get(i).getLabel()==id){
							  System.out.println(windowBuffer.get(i).getLabel());
							  windowBuffer.remove(i);
							  System.out.println("**********Acked************");
						  }
					  }
					  if (windowBuffer.size() < 4){
						  for (int i=3; i > windowBuffer.size()-1;i--){
							  if(In.isMessageAvailable()){
								  message = (LabelMessage) In.getMessage();
								  windowBuffer.add(message);
								  System.out.println("**********Transmission2************");
								  System.out.println(message.getLabel());
								  Out.sendMessage(message);
								  startTimer();
							  }
						  }
					  }
				  }
			  }
		  }
	  };

    // connect sender to constant delay
    this.Out.connect(this.frameProcTime.getInput());
    
  }
  
  
  public void timeOutAction(){
    System.out.println("*************Retransmission*************");
//	System.out.println(this.message.getLabel());
	((SynchronousOutputPort) this.OutputPortAfterFrameProc).sendMessage(this.message);
	System.out.println(this.message.getLabel());
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