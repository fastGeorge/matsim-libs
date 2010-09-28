/* *********************************************************************** *
 * project: org.matsim.*
 * SignalControlDataFactoryImpl
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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

import org.matsim.api.core.v01.Id;


/**
 * @author dgrether
 *
 */
public class SignalControlDataFactoryImpl implements SignalControlDataFactory {

	@Override
	public SignalGroupSettingsData createSignalGroupSettingsData(Id signalGroupId) {
		return new SignalGroupSettingsDataImpl(signalGroupId);
	}

	@Override
	public SignalPlanData createSignalPlanData(Id id) {
		return new SignalPlanDataImpl(id);
	}

	@Override
	public SignalSystemControllerData createSignalSystemControllerData(Id signalSystemId) {
		return new SignalSystemControllerDataImpl(signalSystemId);
	}


}
