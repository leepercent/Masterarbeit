package protocol;

import ikr.simlib.entities.Entity;
import ikr.simlib.entities.phases.DInfiniteServer;
import ikr.simlib.entities.phases.InfiniteServer;
import ikr.simlib.events.time.Duration;
import ikr.simlib.messages.Message;
import ikr.simlib.model.SimNode;
import ikr.simlib.ports.input.InputPort;
import ikr.simlib.ports.output.OutputPort;
import ikr.simlib.ports.output.SynchronousOutputPort;


import java.util.ArrayList;
import java.util.List;

public class Sender extends Entity {

  private final InputPort generatorInput;
  private final SynchronousOutputPort out;
  private final InputPort FeedbackIn;
  private final OutputPort outAfterFrameProc;

  private boolean waitForFeedback = false;
  private Message message;
  private final List<Message> buffer;
  
  
  private final InfiniteServer frameProcTime;

  public Sender(SimNode ownNode) {
    super(ownNode);
    this.buffer = new ArrayList<>();
 
    // put the frame Processing time inside Sender
    final Duration d = Duration.fromMilliSeconds(0.1);
    this.frameProcTime = new DInfiniteServer(d, this.simNode.createChildNode("FrameProcessingTime"));
    this.outAfterFrameProc = this.frameProcTime.getOutput();
 
    this.out = new SynchronousOutputPort(this);
    this.generatorInput = new InputPort(this) {
      @Override
      protected void handleMessageIndication(Message msg) {
//    	  System.out.println(waitForFeedback);
        if (!waitForFeedback) {
          if (buffer.isEmpty()){
        	  message = generatorInput.getMessage();
        	  out.sendMessage(message);
        	  waitForFeedback = true;
          }else{
        	  System.out.println("transmit from queue");
        	  message = buffer.get(0);
        	  out.sendMessage(buffer.remove(0));
        	  waitForFeedback = true;
          }
        } else {
          buffer.add(getMessage());
        }
      }
    };

    this.FeedbackIn = new InputPort(this) {
      @Override
      protected void handleMessageIndication(Message msg) {
    	msg = FeedbackIn.getMessage();
    	
        if (((AckNackMessage) msg).isAck()) {
          if (buffer.isEmpty()){
        	  waitForFeedback = false;
          }else{
        	  out.sendMessage(buffer.remove(0));        	  
        	  waitForFeedback = true;
          }
        } else {
        	out.sendMessage(message);
//     	((SynchronousOutputPort) outAfterFrameProc).sendMessage(message); //retransmission
        	waitForFeedback = true;
        }
      }
    };
       
    // connect Sender output with the Delay Entity input
    this.out.connect(this.frameProcTime.getInput());
  }
  
  public InputPort getInput() {
	  return this.generatorInput;
  }
  
  public InputPort getFeedbackInput(){
	  return this.FeedbackIn;
  }
  
  public OutputPort getOutput() {
	  return this.out;
  }
  
  public OutputPort getOutputAfterFrameProc(){
	  return this.outAfterFrameProc;
  }

}
