package Protocol_SR;


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
