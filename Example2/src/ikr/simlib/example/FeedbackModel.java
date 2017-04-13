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

import ikr.simlib.control.simulationcontrol.ControlTimer;
import ikr.simlib.distributions.continuous.NegExpDistribution;
import ikr.simlib.distributions.discrete.DiscreteConstantDistribution;
import ikr.simlib.entities.generators.Generator;
import ikr.simlib.entities.generators.StdGenerator;
import ikr.simlib.entities.sinks.Sink;
import ikr.simlib.events.time.Duration;
import ikr.simlib.meters.time.TimeMeter;
import ikr.simlib.model.Model;
import ikr.simlib.model.SimNode;
import ikr.simlib.parameters.Parameters;
import ikr.simlib.statistics.distribution.StdDistributionStatistic;
import ikr.simlib.statistics.sample.StdSampleStatistic;

/**
 * Simulation of a system with feedback.
 * 
 */
public class FeedbackModel extends Model {

   private final Generator generator;
   private final NetworkLink link1;
   private final NetworkNode node;
   private final NetworkLink link2;
   private final Sink sink;

   private final TimeMeter meter1;
   private final TimeMeter meter2;

   @SuppressWarnings("unused")
   private final ControlTimer controlTimer;

   public FeedbackModel(Parameters pars) {
      super("FeedbackModel"); //initialize simNde with the name of FeedbackModel as rootNode
      
      // parse model parameters
      final int transientPhaseTime = pars.getOrUseDefault(this.simNode, "TransientPhaseTime", "50").asInteger();
      final int batchTime = pars.getOrUseDefault(this.simNode, "BatchTime", "100").asInteger();
      // create entities
      final SimNode generatorNode = this.simNode.createChildNode("Generator");	//first step: create a node in the Tree structure.
      //System.out.println(generatorNode.getFullName());//FeedbackModel.Generator
      //this.generator = pars.get(generatorNode.getFullName()).asInstance(Generator.class, generatorNode, pars);
      
      final double iatMean = pars.get(generatorNode, "IAT").asDouble();	//read the IAT from sim.par file as "FeedbackModel.Generator.IAT"
      this.generator = new StdGenerator(new NegExpDistribution(iatMean), new DiscreteConstantDistribution(1), generatorNode, null, true);	//create a generator of Standard Generator class
      this.link1 = new NetworkLink(pars, this.simNode.createChildNode("Link1"));
      this.node = new NetworkNode(pars, this.simNode.createChildNode("Node"));
      this.link2 = new NetworkLink(pars, this.simNode.createChildNode("Link2"));
      this.sink = new Sink(this.simNode.createChildNode("Sink"));

      // create meters
      this.meter1 = TimeMeter.createWithDoubleStatistic(new StdSampleStatistic(this.simNode.createChildNode("TransmissionTimeMeter")));
      this.meter2 = TimeMeter.createWithDoubleStatistic(new StdDistributionStatistic(10, 0, 10, this.simNode.createChildNode("TransmissionTimeDistribution")));

      // create control counter
      this.controlTimer = new ControlTimer(Duration.fromSeconds(transientPhaseTime), Duration.fromSeconds(batchTime));

      // connect entities
      this.generator.getOutput().connect(this.link1.getInput());
      this.link1.getOutput().connect(this.node.getInput());
      this.node.getOutput().connect(this.link2.getInput());
      this.link2.getOutput().connect(this.sink.getInput());

      // attach meters and control counter
      this.meter1.attachFromPort(this.generator.getOutput());
      this.meter1.attachToPort(this.sink.getInput());

      this.meter2.attachFromPort(this.generator.getOutput());
      this.meter2.attachToPort(this.sink.getInput());

   }
}
