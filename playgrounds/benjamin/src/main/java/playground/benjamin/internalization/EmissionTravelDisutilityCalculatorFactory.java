/* *********************************************************************** *
 * project: org.matsim.*
 * EmissionTravelCostCalculatorFactory.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package playground.benjamin.internalization;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.PersonalizableTravelTime;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.vehicles.Vehicle;

import playground.benjamin.emissions.EmissionModule;

/**
 * @author benjamin
 *
 */
public class EmissionTravelDisutilityCalculatorFactory implements TravelDisutilityFactory {

	private final EmissionModule emissionModule;
	private final EmissionCostModule emissionCostModule;

	public EmissionTravelDisutilityCalculatorFactory(EmissionModule emissionModule, EmissionCostModule emissionCostModule) {
		this.emissionModule = emissionModule;
		this.emissionCostModule = emissionCostModule;
	}

	@Override
	public TravelDisutility createTravelDisutility(PersonalizableTravelTime timeCalculator, PlanCalcScoreConfigGroup cnScoringGroup){
		final EmissionTravelDisutilityCalculator etdc = new EmissionTravelDisutilityCalculator(timeCalculator, cnScoringGroup, emissionModule, emissionCostModule);

		return new TravelDisutility(){

			@Override
			public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
				double linkTravelDisutility = etdc.getLinkTravelDisutility(link, time, person, vehicle);
				return linkTravelDisutility;
			}
			
			@Override
			public double getLinkMinimumTravelDisutility(Link link) {
				return etdc.getLinkMinimumTravelDisutility(link);
			}
		};
	}

}
