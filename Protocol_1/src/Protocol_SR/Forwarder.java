package Protocol_SR;

import ikr.simlib.entities.Entity;
import ikr.simlib.messages.Message;
import ikr.simlib.model.SimNode;
import ikr.simlib.ports.input.InputPort;
import ikr.simlib.ports.output.OutputPort;

public class Forwarder extends Entity {

   private InputPort in;
   private OutputPort out;

   public Forwarder(SimNode ownNode) {
      super(ownNode);

      this.in = new InputPort(this) {

         @Override
         protected void handleMessageIndication(Message msg) {
            out.messageIndication(msg);
         }
      };
      
      this.out = new OutputPort(this) {
         
         @Override
         protected boolean handleIsMessageAvailable() {
            return in.isMessageAvailable();
         }
         
         @Override
         protected Message handleGetMessage() {
            return in.getMessage();
         }
      };
   }

}
