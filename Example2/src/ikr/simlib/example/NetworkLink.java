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

import ikr.simlib.entities.Entity;
import ikr.simlib.entities.phases.DInfiniteServer;
import ikr.simlib.entities.phases.InfiniteServer;
import ikr.simlib.events.time.Duration;
import ikr.simlib.model.SimNode;
import ikr.simlib.parameters.Parameters;
import ikr.simlib.ports.input.InputPort;
import ikr.simlib.ports.output.OutputPort;

public class NetworkLink extends Entity {

   private final InfiniteServer delay;

   public NetworkLink(Parameters pars, SimNode ownNode) {
      super(ownNode);	//the ownNode parsed here is "FeedbackModel.Link"

      // parse node parameters
      final Duration d = pars.get(simNode, "Delay").asInstance(Duration.class, simNode.createChildNode("Delay"), pars);

      // create delay entity
      this.delay = new DInfiniteServer(d, this.simNode.createChildNode("DelayServer"));
   }

   public InputPort getInput() {
      return this.delay.getInput();
   }

   public OutputPort getOutput() {
      return this.delay.getOutput();
   }
}
