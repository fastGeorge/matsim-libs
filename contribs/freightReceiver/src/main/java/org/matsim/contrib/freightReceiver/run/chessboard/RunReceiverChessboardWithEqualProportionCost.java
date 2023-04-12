/**
 * ********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 * *
 * copyright       : (C) 2023 by the members listed in the COPYING,        *
 * LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 * *
 * *********************************************************************** *
 * *
 * This program is free software; you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation; either version 2 of the License, or     *
 * (at your option) any later version.                                   *
 * See also COPYING, LICENSE and WARRANTY file                           *
 * *
 * ***********************************************************************
 */

package org.matsim.contrib.freightReceiver.run.chessboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.controler.FreightUtils;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.contrib.freightReceiver.ReceiverModule;
import org.matsim.contrib.freightReceiver.ReceiverReplanningType;
import org.matsim.contrib.freightReceiver.ReceiverUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;

import java.io.File;
import java.util.Locale;

public class RunReceiverChessboardWithEqualProportionCost {
	private final static Logger LOG = LogManager.getLogger(RunReceiverChessboardWithEqualProportionCost.class);
	private final static int FIRST_RUN = 1;
	private final static int LAST_RUN = 2;

	public static void main(String[] args) {
		if (args.length > 0) {
			throw new IllegalArgumentException("This class should be self-contained and run without any arguments.");
		}
		for (int run = FIRST_RUN; run <= LAST_RUN; run++) {
			run(run);
		}
	}

	static void run(int run) {
		LOG.info("Executing run '" + run + "'...");

		/* Prepare the scenario. */
		String folder = String.format(Locale.US, "%s/run_%03d/", ReceiverChessboardParameters.OUTPUT_FOLDER, run);
		boolean created = new File(folder).mkdirs();
		if (!created) {
			LOG.error("Could not create output folder. This may cause code to crash down the line.");
		}
		Scenario scenario = ReceiverChessboardScenario.createChessboardScenario(
			run * ReceiverChessboardParameters.SEED_BASE,
			ReceiverChessboardParameters.NUMBER_OF_RECEIVERS,
			folder,
			true);

		Controler controler = new Controler(scenario);
		controler.getConfig().controler().setLastIteration(50);

		/* Set up the receiver module. */
		ReceiverModule receiverModule = new ReceiverModule(ReceiverUtils.createEqualProportionCostAllocation());
		receiverModule.setReplanningType(ReceiverReplanningType.serviceTime);
		controler.addOverridingModule(receiverModule);

		/* Carrier statistics. */
		prepareFreightOutputDataAndStats(controler);

		controler.run();
	}

	/**
	 * This method ensures that the {@link Carriers}' scores are also updated in the output.
	 * TODO This is less elegant than the embedded score stats for the receiver.
	 */
	static void prepareFreightOutputDataAndStats(MatsimServices controler) {
		CarrierScoreStats scoreStats = new CarrierScoreStats(FreightUtils.getCarriers(controler.getScenario()), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);
		controler.addControlerListener(scoreStats);
	}

}
