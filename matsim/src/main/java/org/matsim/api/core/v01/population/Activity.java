/* *********************************************************************** *
 * project: org.matsim.*
 * Activity.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2009 by the members listed in the COPYING,  *
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

package org.matsim.api.core.v01.population;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.misc.Time;
import org.matsim.facilities.ActivityFacility;

/**
 * Specifies the kind of activity an agent performs during its day.
 */
public interface Activity extends PlanElement {

	default boolean isEndTimeUndefined() {
		try {
			double endTime = getEndTime();
			//only ActivityImpl has been adapted to Double, so we need to keep this additional check for other Activity classes
			return Time.isUndefinedTime(endTime);
		} catch (NullPointerException e) {
			return true;
		}
	}

	default boolean isStartTimeUndefined() {
		try {
			double startTime = getStartTime();
			//only ActivityImpl has been adapted to Double, so we need to keep this additional check for other Activity classes
			return Time.isUndefinedTime(startTime);
		} catch (NullPointerException e) {
			return true;
		}
	}

	default boolean isMaximumDurationUndefined() {
		try {
			double maxDuration = getMaximumDuration();
			//only ActivityImpl has been adapted to Double, so we need to keep this additional check for other Activity classes
			return Time.isUndefinedTime(maxDuration);
		} catch (NullPointerException e) {
			return true;
		}
	}


	public double getEndTime();

	public void setEndTime(final double seconds);

	public String getType();

	public void setType(final String type);

	/**
	 * @return the coordinate of the activity, possibly null.
	 * <p></p>
	 * Note that there is deliberately no way to set the coordinate except at creation.  
	 * We might consider something like moveActivityTo( linkid, coord ).  kai, aug'10 
	 */
	public Coord getCoord();

	public double getStartTime();

	/**
	 * Used for reporting outcomes in the scoring. Not interpreted for the demand.
	 */
	public void setStartTime(double seconds);
	
	public double getMaximumDuration() ;
	
	public void setMaximumDuration(double seconds) ;

	/**
	 * @return the if of the link to which the activity is attached.  This may start as null, but
	 * is usually set automatically by the control(l)er before the zeroth iteration.
	 * <p></p>
	 * Note that there is deliberately no way to set the link id except at creation.  
	 * We might consider something like moveActivityTo( linkid, coord ).  kai, aug'10 
	 */
	public Id<Link> getLinkId();

	public Id<ActivityFacility> getFacilityId();

	public void setLinkId(final Id<Link> id);
	
	public void setFacilityId(final Id<ActivityFacility> id);

	void setCoord(Coord coord);

}