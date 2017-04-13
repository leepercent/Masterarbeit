package protocol;

import ikr.simlib.messages.Message;

public class AckNackMessage extends Message {
  private final boolean ack;

  public AckNackMessage(boolean ack) {
    super();
    this.ack = ack;

  }
  
  public boolean isAck() {
    return this.ack;
  }

}
