package Protocol_AckWithTimer;

import ikr.simlib.SimLibFormatter;
import ikr.simlib.control.SimTreeApplication;
import ikr.simlib.model.Model;
import ikr.simlib.parameters.Parameters;

public class Main {

	public static void main(String[] args) {
	      SimLibFormatter.setupAsFormatterForANewRootConsoleHandler();
	      SimTreeApplication app = new SimTreeApplication("AckTimerModelApplication", args) {
	         @Override
	         protected Model createModel(Parameters pars) {
	            return new AckTimeoutModel(pars);
	         }
	      };
	      app.run();
	}

}