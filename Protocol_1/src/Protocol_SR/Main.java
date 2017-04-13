package Protocol_SR;

import ikr.simlib.SimLibFormatter;
import ikr.simlib.control.SimTreeApplication;
import ikr.simlib.model.Model;
import ikr.simlib.parameters.Parameters;

public class Main {

	public static void main(String[] args) {
	      SimLibFormatter.setupAsFormatterForANewRootConsoleHandler();
	      SimTreeApplication app = new SimTreeApplication("SR_AckTimerModel", args) {
	         @Override
	         protected Model createModel(Parameters pars) {
	            return new SR_AckTimeoutModel(pars);
	         }
	      };
	      app.run();
	}

}