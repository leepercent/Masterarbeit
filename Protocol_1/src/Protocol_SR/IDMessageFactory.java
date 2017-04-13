package Protocol_SR;

import ikr.simlib.messages.Message;
import ikr.simlib.messages.factories.MessageFactory;

public class IDMessageFactory extends MessageFactory{

	@Override
	public IDMessage create() {
		return new IDMessage();
	}
}
