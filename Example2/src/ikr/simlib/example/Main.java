/*
 * Copyright (C) 2007-2014 University of Stuttgart, IKR
 * 
 * This file is part of IKR SimLib, Java Edition, as available from
 * http://www.ikr.uni-stuttgart.de/IKRSimLib.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (LGPL) as published by the
 * Free Software Foundation, in version 2.1 as it comes in the file "COPYING" in
 * directory doc of IKR SimLib.
 * 
 * IKR SimLib is distributed in the hope that it will be useful, but without any
 * warranty of any kind. See the LGPL for details or contact the IKR if you need
 * additional information (http://www.ikr.uni-stuttgart.de).
 * 
 * Alternatively you may license IKR SimLib under a different (non-free)
 * license. Please contact the IKR for further details.
 */

package ikr.simlib.example;

import ikr.simlib.SimLibFormatter;
import ikr.simlib.control.SimTreeApplication;
import ikr.simlib.model.Model;
import ikr.simlib.parameters.Parameters;

public class Main {
   public static void main(final String[] args) {
      SimLibFormatter.setupAsFormatterForANewRootConsoleHandler();
      SimTreeApplication app = new SimTreeApplication("FeedbackModelApplication", args) {
         @Override
         protected Model createModel(Parameters pars) {
            return new FeedbackModel(pars);
         }
      };
      app.run();
   }
}