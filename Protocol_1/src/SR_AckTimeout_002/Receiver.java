package SR_AckTimeout_002;

import java.util.ArrayList;

import ikr.simlib.entities.Entity;
import ikr.simlib.events.CalendarCallback;
import ikr.simlib.events.calendar.Calendar;
import ikr.simlib.events.time.Duration;
import ikr.simlib.events.time.PointInTime;
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
  private final ArrayList<Integer> ackedMsgs;
  private int receivedMsgCounter;

  public Receiver(SimNode ownNode, final double dropPrbability) {
    super(ownNode);

    this.sinkOut = new SynchronousOutputPort(this);
    this.out = new SynchronousOutputPort(this);  
    this.receivedMsgCounter = 0;
    this.ackedMsgs = new ArrayList<>(4);

    final StdRandomNumberGenerator r = new StdRandomNumberGenerator();

    this.in = new InputPort(this) {
    	@Override
    	protected void handleMessageIndication(Message msg) {
    		m = (LabelMessage) in.getMessage();
    		id = m.getLabel(); 
    		
    		if(r.next() < 1-dropPrbability) {
    			
    			PointInTime now = Calendar.getInstance().getSystemTime();
    			Double time = now.toMilliSeconds();
    			System.out.println(String.format("%.3f", time) + "ms, Receiver Send Ack with Label: " + String.valueOf(id));
    			if (receivedMsgCounter<id){
    				out.sendMessage(new AckMessage(true, id));
    				sinkOut.sendMessage(m);
    				receivedMsgCounter++;
    			} else {
    				System.out.println(String.format("%.3f", time) + "ms, duplicated received message"+ String.valueOf(id));
    			}
//    			ackedMsgs.add(id);
//    			System.out.println(String.format("%.3f", time)+" 1receive window size: " + String.valueOf(ackedMsgs.size()));
//    			Calendar.getInstance().postEvent(new CalendarCallback() {
//					@Override
//					public void execute() {
//						ackedMsgs.remove(id);
//					}
//				}, null, now.plus(Duration.fromMilliSeconds(1.0)));
//    			
//    			if (ackedMsgs.contains(id)) {
//    				System.out.println(String.format("%.3f", Calendar.getInstance().getSystemTime().toMilliSeconds())
//    												+" 2receive window size: " + String.valueOf(ackedMsgs.size()));
//    				sinkOut.sendMessage(m);
//				}else {
//					System.out.println(String.format("%.3f", Calendar.getInstance().getSystemTime().toMilliSeconds())
//													+" Duplicated Message!");
//				}
    			
    		}else{Double time = Calendar.getInstance().getSystemTime().toMilliSeconds();
			System.out.println(String.format("%.3f", time) + "ms, message " + String.valueOf(id)+" droped");
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