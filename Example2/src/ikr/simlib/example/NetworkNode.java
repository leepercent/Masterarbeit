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
import ikr.simlib.entities.branches.BinaryBranch;
import ikr.simlib.entities.multiplexers.StdMultiplexer;
import ikr.simlib.entities.phases.Phase;
import ikr.simlib.entities.phases.StochasticInfiniteServer;
import ikr.simlib.entities.phases.UnitPhase;
import ikr.simlib.entities.queues.QueuingEntity;
import ikr.simlib.events.time.Duration;
import ikr.simlib.model.SimNode;
import ikr.simlib.parameters.Parameters;
import ikr.simlib.ports.input.InputPort;
import ikr.simlib.ports.output.OutputPort;

class NetworkNode extends Entity {
   private final StdMultiplexer multiplexer;
   private final QueuingEntity queue;
   private final StochasticInfiniteServer phase;
   private final BinaryBranch branch;

   public NetworkNode(Parameters pars, SimNode ownNode) {
      super(ownNode);

      // parse node parameters
      final double feedbackProbability = pars.get(this.simNode, "FeedbackProbability").asDouble();

      // create entities
      this.multiplexer = new StdMultiplexer(this.simNode.createChildNode("Multiplexer"));
      this.queue = QueuingEntity.createUnboundedFIFOQueue(this.simNode.createChildNode("Queue"));
      this.phase = StochasticInfiniteServer.createInstance(this.simNode.createChildNode("Phase"), pars);
      this.branch = new BinaryBranch(feedbackProbability, this.simNode.createChildNode("Branch"));

      // connect entities
      this.multiplexer.connect("output", this.queue, "input");
      this.queue.connect("output", this.phase, "input");
      this.phase.connect("output", this.branch, "input");
      this.branch.connect("output 1", this.multiplexer, this.multiplexer.addPort());

      // create alias ports to connect to and from the outside
      this.aliasPort(this.multiplexer, this.multiplexer.addPort(), "input");
      this.aliasPort(this.branch, "output 2", "output");

   }

   public InputPort getInput() {
      final String inPortName = this.multiplexer.addPort();
      System.out.println(inPortName);
      return (InputPort) this.multiplexer.getPortByName(inPortName);
   }

   public OutputPort getOutput() {
      return (OutputPort) this.branch.getPortByName("output 2");
   }
}
