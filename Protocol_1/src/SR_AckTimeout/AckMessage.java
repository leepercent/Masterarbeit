package SR_AckTimeout;

import ikr.simlib.messages.LabelMessage;

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
