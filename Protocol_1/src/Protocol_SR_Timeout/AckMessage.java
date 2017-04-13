package Protocol_SR_Timeout;


public class AckMessage extends IDMessage {
  private final boolean ack;

  public AckMessage(boolean ack, int messageID) {
    this.ack = ack;
    this.setID(messageID);
  }
  
  public boolean isAck() {
    return this.ack;
  }
  
}
