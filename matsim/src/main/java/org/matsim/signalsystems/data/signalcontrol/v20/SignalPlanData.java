/* *********************************************************************** *
 * project: org.matsim.*
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
package org.matsim.signalsystems.data.signalcontrol.v20;

import java.util.SortedMap;

import org.matsim.api.core.v01.Id;
/**
 * 
 * @author dgrether
 *
 */
public interface SignalPlanData {

	public Id getId();

	public void addSignalGroupSettings(
			SignalGroupSettingsData signalGroupSettings);

	public SortedMap<Id, SignalGroupSettingsData> getSignalGroupSettingsDataByGroupId();

	/**
	 * @return null if not set
	 */
	public Double getStartTime();

	public void setStartTime(Double seconds);
	/**
	 * @return null if not set
	 */
	public Double getEndTime();

	public void setEndTime(Double seconds);
	/**
	 * @return null if not set
	 */
	public Integer getCycleTime();

	public void setCycleTime(Integer cycleTime);
	/**
	 * @return null if not set
	 */
	public Integer getOffset();

	public void setOffset(Integer seconds);
	
}