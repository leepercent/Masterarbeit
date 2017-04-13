package SR_AckTimeout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ikr.simlib.entities.Entity;
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
	private final InputPort FeedbackIn;
	private final List<LabelMessage> sendWindow;
	private EventToken timeoutEvent;
	private final HashMap<Integer, EventToken> map;
	private int lastSentMsgID;
	private int lastUnackedMsgID;
	private Entry<Integer, EventToken> lastUnackedMsg;


	public Sender(SimNode ownNode) {
		super(ownNode);
		
		this.lastUnackedMsgID = 0;
		this.lastUnackedMsg = null;
		this.sendWindow = new ArrayList<>(4);
		this.map = new HashMap<Integer, EventToken>();
		this.Out = new SynchronousOutputPort(this);

		this.In = new InputPort(this) {
			@Override
			protected void handleMessageIndication(Message msg) {
				if (In.isMessageAvailable()){
					if (sendWindow.size() < 4) {   // check if window is full
						bufferMessage();
					}
				}
			}
		};


		this.FeedbackIn = new InputPort(this) {
			@Override
			protected void handleMessageIndication(Message msg) {
				LabelMessage AckMsg = (LabelMessage) FeedbackIn.getMessage();
				int id = ((LabelMessage) msg).getLabel();

				if (((AckMessage) AckMsg).isAck()) {
					if (timeoutEvent.isPending()){   // timeout has not reached
						if (id>lastUnackedMsgID){   // duplicated ack detection
							ackMessages(id);
						}else{
							System.out.println(String.format("%.3f", Calendar.getInstance().getSystemTime().toMilliSeconds())
									+ "ms, duplicated message "
									+ String.valueOf(id));
						}
					}
					if (In.isMessageAvailable()){
						if (sendWindow.size() < 4) {   // check if window is full
							bufferMessage();
						}
					}
				}
			}
		};   

	}


	public void bufferMessage() {
		LabelMessage message = (LabelMessage) In.getMessage();
		sendWindow.add(message);
		System.out.println("**********Transmision Start************");
		System.out.println(String.format("%.3f" , Calendar.getInstance().getSystemTime().toMilliSeconds())
				+ "ms, message "
				+ String.valueOf(message.getLabel())
				+ " buffered");

		Calendar.getInstance().postEvent(new CalendarCallback() {
			@Override
			public void execute() {
				SendMessageFromBuffer(message);
			}

		}, null, Calendar.getInstance().getSystemTime().plus(Duration.fromMilliSeconds(0.1)));
	}	


	public void SendMessageFromBuffer(LabelMessage msg){
		Out.sendMessage(msg);
		this.map.put(msg.getLabel(), startTimer(msg.getLabel()));

		Double time = Calendar.getInstance().getSystemTime().toMilliSeconds();
		System.out.println(String.format("%.3f", time)
				+ "ms, Send message "
				+ String.valueOf(msg.getLabel())
				+ "; Window Size: "
				+ String.valueOf(sendWindow.size())
				+ "; Event Map Size: "
				+ String.valueOf(this.map.size()));
	}



	public void ackMessages(int id) {
		//cancel event
		EventToken event = (EventToken) this.map.get(id-1);
		this.map.remove(id-1);
		event.cancel(); 
		
		// get last unacked event
		for (Map.Entry<Integer, EventToken> entry: map.entrySet()){
			if (lastUnackedMsg ==null || lastUnackedMsg.getKey() > entry.getKey()){
				lastUnackedMsg = entry;
				lastUnackedMsgID = entry.getKey();
				System.out.println(lastSentMsgID);
			}
		}
		
		// remove message from send window
		Iterator<LabelMessage> it = sendWindow.iterator();
		while (it.hasNext()) {
			LabelMessage nextMessage = it.next();
			if (nextMessage.getLabel() == (id-1) ) {
				it.remove();
				Double time = Calendar.getInstance().getSystemTime().toMilliSeconds();
				System.out.println(String.format("%.3f", time)
						+ "ms, Message Acked and removed from Window: "
						+ String.valueOf(nextMessage.getLabel())
						+ "; Window Size: "
						+ String.valueOf(sendWindow.size())
						+ "; Event Map Size: "
						+ String.valueOf(this.map.size()));
				System.out.println("*******Acked***********");
			}
		}
	}


	public void timeOutAction(int retransmitID) {
		System.out.println("*************Retransmission*************");
		Double time = Calendar.getInstance().getSystemTime().toMilliSeconds();
		for (Iterator<LabelMessage> it = sendWindow.iterator(); it.hasNext();) {
			LabelMessage message = it.next();
			if (message.getLabel() == retransmitID) {
				Out.sendMessage(message);
				this.map.put(message.getLabel(), startTimer(retransmitID));
				System.out.println(String.format("%.3f", time)
						+ "ms, Retransmit message "
						+ String.valueOf(retransmitID)+ "; Event Map Size: "
						+ String.valueOf(this.map.size()));
			}
		}
	}


	public EventToken startTimer(int id){
		return this.timeoutEvent = Calendar.getInstance().postEvent(new CalendarCallback(){
			@Override
			public void execute() {
				timeOutAction(id);
			}
		}, null, Calendar.getInstance().getSystemTime().plus(Duration.fromMilliSeconds(1.5)));

	}

//bullshit

	public InputPort getInput() {
		return this.In;
	}


	public InputPort getFeedbackInput(){
		return this.FeedbackIn;
	}


	public OutputPort getOutput() {
		return this.Out;
	}

}
