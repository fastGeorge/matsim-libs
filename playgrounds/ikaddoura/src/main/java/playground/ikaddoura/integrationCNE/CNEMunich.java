/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package playground.ikaddoura.integrationCNE;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup;
import org.matsim.contrib.noise.NoiseConfigGroup;
import org.matsim.contrib.noise.data.NoiseAllocationApproach;
import org.matsim.contrib.noise.utils.MergeNoiseCSVFile;
import org.matsim.contrib.noise.utils.ProcessNoiseImmissions;
import org.matsim.contrib.otfvis.OTFVisFileWriterModule;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl.Builder;
import org.matsim.core.replanning.modules.ReRoute;
import org.matsim.core.replanning.modules.SubtourModeChoice;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import playground.agarwalamit.analysis.modalShare.ModalShareFromEvents;
import playground.agarwalamit.munich.utils.MunichPersonFilter;
import playground.agarwalamit.munich.utils.MunichPersonFilter.MunichUserGroup;
import playground.ikaddoura.analysis.detailedPersonTripAnalysis.PersonTripCongestionNoiseAnalysisMain;
import playground.ikaddoura.integrationCNE.CNEIntegration.CongestionTollingApproach;
import playground.vsp.airPollution.exposure.GridTools;
import playground.vsp.airPollution.exposure.ResponsibilityGridTools;

public class CNEMunich {
	
	private static final Logger log = Logger.getLogger(CNEMunich.class);

	private static final Integer noOfXCells = 160;
	private static final Integer noOfYCells = 120;
	private static final double xMin = 4452550.25;
	private static final double xMax = 4479483.33;
	private static final double yMin = 5324955.00;
	private static final double yMax = 5345696.81;
	private static final Double timeBinSize = 3600.;
	private static final int noOfTimeBins = 30;
	
	private static String outputDirectory;
	private static String configFile;

	private static boolean congestionPricing;
	private static boolean noisePricing;
	private static boolean airPollutionPricing;
	
	private static double sigma;
	
	private static CongestionTollingApproach congestionTollingApproach;
	private static double kP;
	
	public static void main(String[] args) throws IOException {
				
		if (args.length > 0) {
								
			outputDirectory = args[0];
			log.info("Output directory: " + outputDirectory);
			
			configFile = args[1];
			log.info("Config file: " + configFile);
			
			congestionPricing = Boolean.parseBoolean(args[2]);
			log.info("Congestion Pricing: " + congestionPricing);
			
			noisePricing = Boolean.parseBoolean(args[3]);
			log.info("Noise Pricing: " + noisePricing);
			
			airPollutionPricing = Boolean.parseBoolean(args[4]);
			log.info("Air poullution Pricing: " + airPollutionPricing);
			
			sigma = Double.parseDouble(args[5]);
			log.info("Sigma: " + sigma);
			
			String congestionTollingApproachString = args[6];
			
			if (congestionTollingApproachString.equals(CongestionTollingApproach.QBPV3.toString())) {
				congestionTollingApproach = CongestionTollingApproach.QBPV3;
			} else if (congestionTollingApproachString.equals(CongestionTollingApproach.DecongestionPID.toString())) {
				congestionTollingApproach = CongestionTollingApproach.DecongestionPID;
			} else {
				throw new RuntimeException("Unknown congestion pricing approach. Aborting...");
			}
			log.info("Congestion Tolling Approach: " + congestionTollingApproach);
			
			kP = Double.parseDouble(args[7]);
			log.info("kP: " + kP);
			
		} else {
			
			configFile = "../../../runs-svn/cne_test/input/config.xml";
		}

		Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.loadConfig(configFile));
		Controler controler = new Controler(scenario);

		if (outputDirectory != null) {
			controler.getScenario().getConfig().controler().setOutputDirectory(outputDirectory);
		}
		
		GridTools gt = new GridTools(scenario.getNetwork().getLinks(), xMin, xMax, yMin, yMax, noOfXCells, noOfYCells);
		ResponsibilityGridTools rgt = new ResponsibilityGridTools(timeBinSize, noOfTimeBins, gt);

		// CNE Integration
		
		CNEIntegration cne = new CNEIntegration(controler, gt, rgt);
		cne.setCongestionPricing(congestionPricing);
		cne.setNoisePricing(noisePricing);
		cne.setAirPollutionPricing(airPollutionPricing);
		cne.setSigma(sigma);
		cne.setCongestionTollingApproach(congestionTollingApproach);
		cne.setkP(kP);
		controler = cne.prepareControler();
		
		// scenario-specific settings
		
		// TODO: set files in config
		EmissionsConfigGroup ecg = (EmissionsConfigGroup) controler.getConfig().getModule("emissions");
		ecg.setAverageColdEmissionFactorsFile("../../../shared-svn/projects/detailedEval/emissions/hbefaForMatsim/EFA_ColdStart_vehcat_2005average.txt");
		ecg.setAverageWarmEmissionFactorsFile("../../../shared-svn/projects/detailedEval/emissions/hbefaForMatsim/EFA_HOT_vehcat_2005average.txt");
		ecg.setDetailedColdEmissionFactorsFile("../../../shared-svn/projects/detailedEval/emissions/hbefaForMatsim/EFA_ColdStart_SubSegm_2005detailed.txt");
		ecg.setDetailedWarmEmissionFactorsFile("../../../shared-svn/projects/detailedEval/emissions/hbefaForMatsim/EFA_HOT_SubSegm_2005detailed.txt");
		ecg.setEmissionRoadTypeMappingFile("../../../runs-svn/detEval/emissionCongestionInternalization/iatbr/input/roadTypeMapping.txt");
		ecg.setUsingDetailedEmissionCalculation(true);
	
		controler.getConfig().vehicles().setVehiclesFile("../../../runs-svn/detEval/emissionCongestionInternalization/iatbr/input/emissionVehicles_1pct.xml.gz");
					
		controler.addOverridingModule(new OTFVisFileWriterModule());
		controler.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				final Provider<TripRouter> tripRouterProvider = binder().getProvider(TripRouter.class);
				String ug = "Rev_Commuter";
				addPlanStrategyBinding(DefaultPlanStrategiesModule.DefaultStrategy.SubtourModeChoice.name().concat("_").concat(ug)).toProvider(new javax.inject.Provider<PlanStrategy>() {
					final String[] availableModes = {"car", "pt_".concat(ug)};
					final String[] chainBasedModes = {"car", "bike"};
					@Inject
					Scenario sc;

					@Override
					public PlanStrategy get() {
						final Builder builder = new Builder(new RandomPlanSelector<>());
						builder.addStrategyModule(new SubtourModeChoice(sc.getConfig().global().getNumberOfThreads(), availableModes, chainBasedModes, false, tripRouterProvider));
						builder.addStrategyModule(new ReRoute(sc, tripRouterProvider));
						return builder.build();
					}
				});
			}
		});
		
		// noise
		
		NoiseConfigGroup noiseParameters = (NoiseConfigGroup) controler.getScenario().getConfig().getModule("noise");

		noiseParameters.setTimeBinSizeNoiseComputation(timeBinSize);

		noiseParameters.setReceiverPointGap(100.);
		
		String[] consideredActivitiesForReceiverPointGrid = {""};
		noiseParameters.setConsideredActivitiesForReceiverPointGridArray(consideredActivitiesForReceiverPointGrid);
		noiseParameters.setReceiverPointsGridMinX(xMin);
		noiseParameters.setReceiverPointsGridMaxX(xMax);
		noiseParameters.setReceiverPointsGridMinY(yMin);
		noiseParameters.setReceiverPointsGridMaxY(yMax);
			
		String[] consideredActivitiesForDamages = new String[controler.getConfig().planCalcScore().getActivityParams().size()];
		int counter = 0;
		for (Iterator<ActivityParams> iterator = controler.getConfig().planCalcScore().getActivityParams().iterator(); iterator.hasNext();) {
			ActivityParams actParams = (ActivityParams) iterator.next();
			String actType = actParams.getActivityType();
			consideredActivitiesForDamages[counter] = actType;
			counter++;
		}
		noiseParameters.setConsideredActivitiesForDamageCalculationArray(consideredActivitiesForDamages);
		
		String[] hgvIdPrefixes = { "gv" };
		noiseParameters.setHgvIdPrefixesArray(hgvIdPrefixes);
						
		noiseParameters.setNoiseAllocationApproach(NoiseAllocationApproach.MarginalCost);
				
		noiseParameters.setScaleFactor(100.);

		Set<Id<Link>> tunnelLinkIDs = new HashSet<Id<Link>>();
		tunnelLinkIDs.add(Id.create("591881193", Link.class));
		tunnelLinkIDs.add(Id.create("589846808-593507025", Link.class));
		tunnelLinkIDs.add(Id.create("595958075", Link.class));
		tunnelLinkIDs.add(Id.create("591541992", Link.class));
		tunnelLinkIDs.add(Id.create("595131857", Link.class));
		tunnelLinkIDs.add(Id.create("593815853", Link.class));
		tunnelLinkIDs.add(Id.create("591002378-586890230", Link.class));
		tunnelLinkIDs.add(Id.create("594773948", Link.class));
		tunnelLinkIDs.add(Id.create("562772343-595823247-67627622", Link.class));
		tunnelLinkIDs.add(Id.create("562772343-595823247-67627622", Link.class));
		tunnelLinkIDs.add(Id.create("554338620-592320125-59404904-591436217", Link.class));
		tunnelLinkIDs.add(Id.create("52810973", Link.class));
		tunnelLinkIDs.add(Id.create("52810959-52810960", Link.class));
		tunnelLinkIDs.add(Id.create("52804319-594971775", Link.class));
		tunnelLinkIDs.add(Id.create("592627223-52804320", Link.class));
		tunnelLinkIDs.add(Id.create("595801512", Link.class));
		tunnelLinkIDs.add(Id.create("594783923", Link.class));
		tunnelLinkIDs.add(Id.create("595128428", Link.class));
		tunnelLinkIDs.add(Id.create("562762175", Link.class));
		tunnelLinkIDs.add(Id.create("593958968", Link.class));
		tunnelLinkIDs.add(Id.create("594995477", Link.class));
		tunnelLinkIDs.add(Id.create("52807999-576295186-589861082", Link.class));
		tunnelLinkIDs.add(Id.create("591705342-576295185-53265241", Link.class));
		tunnelLinkIDs.add(Id.create("592921817-562763527-562763524-562763523-595224818", Link.class));
		tunnelLinkIDs.add(Id.create("593949767-594635243-562763528-595166202", Link.class));
		tunnelLinkIDs.add(Id.create("595877742", Link.class));
		tunnelLinkIDs.add(Id.create("595870463-586909531-594897602", Link.class));
		noiseParameters.setTunnelLinkIDsSet(tunnelLinkIDs);
		
		controler.run();
			
		// scenario-specific analysis
		
		PersonTripCongestionNoiseAnalysisMain analysis = new PersonTripCongestionNoiseAnalysisMain(controler.getConfig().controler().getOutputDirectory());
		analysis.run();
		
		String immissionsDir = controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration() + "/immissions/";
		String receiverPointsFile = controler.getConfig().controler().getOutputDirectory() + "/receiverPoints/receiverPoints.csv";
		
		NoiseConfigGroup ncg =  (NoiseConfigGroup) controler.getConfig().getModule("noise");
		ProcessNoiseImmissions processNoiseImmissions = new ProcessNoiseImmissions(immissionsDir, receiverPointsFile, ncg.getReceiverPointGap());
		processNoiseImmissions.run();
		
		final String[] labels = { "immission", "consideredAgentUnits" , "damages_receiverPoint" };
		final String[] workingDirectories = { controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration() + "/immissions/" , controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration() + "/consideredAgentUnits/" , controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration()  + "/damages_receiverPoint/" };

		MergeNoiseCSVFile merger = new MergeNoiseCSVFile() ;
		merger.setReceiverPointsFile(receiverPointsFile);
		merger.setOutputDirectory(controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration() + "/");
		merger.setTimeBinSize(ncg.getTimeBinSizeNoiseComputation());
		merger.setWorkingDirectory(workingDirectories);
		merger.setLabel(labels);
		merger.run();
		
		// delete unnecessary iterations folder here.
		int firstIt = controler.getConfig().controler().getFirstIteration();
		int lastIt = controler.getConfig().controler().getLastIteration();
		String OUTPUT_DIR = controler.getConfig().controler().getOutputDirectory();
		for (int index =firstIt+1; index <lastIt; index ++){
			String dirToDel = OUTPUT_DIR+"/ITERS/it."+index;
			log.info("Deleting the directory "+dirToDel);
			IOUtils.deleteDirectory(new File(dirToDel),false);
		}
	
		new File(OUTPUT_DIR+"/user-group-analysis/").mkdir();
		String outputEventsFile = OUTPUT_DIR+"/output_events.xml.gz";
		
		{
			String userGroup = MunichUserGroup.Urban.toString();
			ModalShareFromEvents msc = new ModalShareFromEvents(outputEventsFile, userGroup, new MunichPersonFilter());
			msc.run();
			msc.writeResults(OUTPUT_DIR+"/user-group-analysis/modalShareFromEvents_"+userGroup+".txt");	
		}
		{
			String userGroup = MunichUserGroup.Rev_Commuter.toString();
			ModalShareFromEvents msc = new ModalShareFromEvents(outputEventsFile, userGroup, new MunichPersonFilter());
			msc.run();
			msc.writeResults(OUTPUT_DIR+"/user-group-analysis/modalShareFromEvents_"+userGroup+".txt");
		}
	
	}

}