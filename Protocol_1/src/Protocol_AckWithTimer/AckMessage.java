package Protocol_AckWithTimer;

import ikr.simlib.messages.Message;

public class AckMessage extends Message {
  private final boolean ack;

  public AckMessage(boolean ack) {
    super();
    this.ack = ack;

  }
  
  public boolean isAck() {
    return this.ack;
  }

}
