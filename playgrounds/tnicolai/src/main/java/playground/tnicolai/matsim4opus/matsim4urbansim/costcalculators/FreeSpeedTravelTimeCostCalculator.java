/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.tnicolai.matsim4opus.matsim4urbansim.costcalculators;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.TravelDisutility;

/**
 * This cost calulator is based on freespeed travel times 
 * tnicolai feb'12
 * 
 * @author thomas
 *
 */
public class FreeSpeedTravelTimeCostCalculator implements TravelDisutility {
	
	@Override
	public double getLinkTravelDisutility(Link link, double time) {
		return link.getLength() / link.getFreespeed();
	}

	@Override
	public double getLinkMinimumTravelDisutility(Link link) {
		return link.getLength() / link.getFreespeed();
	}

}
