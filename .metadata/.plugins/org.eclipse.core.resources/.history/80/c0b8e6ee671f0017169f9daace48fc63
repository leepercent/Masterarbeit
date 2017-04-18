package SW_AckTimeout;

import ikr.simlib.messages.LabelMessage;
import ikr.simlib.messages.factories.LabelMessageFactory;

public class AutoIncrementedLabelMessageFactory extends LabelMessageFactory {
	
	private int id = 0;
	
	public AutoIncrementedLabelMessageFactory(long length, int label) {
		super(length, label);
	}
	
	public AutoIncrementedLabelMessageFactory(){
		this(1,0);
	}

	@Override
	public LabelMessage create() {
		id ^= 1;
		return new LabelMessage(1,id);
	}
	
	
}
