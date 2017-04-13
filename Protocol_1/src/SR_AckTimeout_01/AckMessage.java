package SR_AckTimeout_01;

import ikr.simlib.messages.LabelMessage;
import ikr.simlib.messages.Message;

public class AckMessage extends LabelMessage {
  private final boolean ack;

  public AckMessage(boolean ack, int label) {
    super(1, label);
    this.ack = ack;

  }
  
  public boolean isAck() {
    return this.ack;
  }

}
