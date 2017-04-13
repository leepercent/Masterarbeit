package Protocol_SR_Timeout;

import ikr.simlib.messages.Message;

public class IDMessage extends Message{
	
	private int ID;
	
	public int getID(){
		return this.ID;
	}
	
	public void setID(int id){
		this.ID = id;
	}

}
