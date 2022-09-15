/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2022 by the members listed in the COPYING,        *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

package example.lsp.simulationTrackers;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.events.FreightServiceEndEvent;
import org.matsim.contrib.freight.events.FreightServiceStartEvent;
import org.matsim.contrib.freight.events.eventhandler.FreightServiceEndEventHandler;
import org.matsim.contrib.freight.events.eventhandler.FreightServiceStartEventHandler;
import org.matsim.contrib.freight.utils.FreightUtils;

import java.util.ArrayList;
import java.util.Collection;


/*package-private*/ class CollectionServiceHandler implements FreightServiceStartEventHandler, FreightServiceEndEventHandler {

	private final Carriers carriers;
	private final Collection<ServiceTuple> tuples;
	private double totalLoadingCosts;
	private int totalNumberOfShipments;
	private int totalWeightOfShipments;

	public CollectionServiceHandler(Scenario scenario) {
		this.carriers = FreightUtils.addOrGetCarriers(scenario);
		this.tuples = new ArrayList<>();
	}

	@Override
	public void reset(int iteration) {
		tuples.clear();
		totalNumberOfShipments = 0;
		totalWeightOfShipments = 0;
	}

	@Override
	public void handleEvent(FreightServiceEndEvent event) {
		System.out.println("Service Ends");
		double loadingCosts;
		for (ServiceTuple tuple : tuples) {
			if (tuple.getServiceId() == event.getServiceId()) {
				double serviceDuration = event.getTime() - tuple.getStartTime();
				Carrier carrier = carriers.getCarriers().get(event.getCarrierId());
				CarrierVehicle carrierVehicle = CarrierUtils.getCarrierVehicle(carrier, event.getVehicleId());
				loadingCosts = serviceDuration * carrierVehicle.getType().getCostInformation().getCostsPerSecond();
				totalLoadingCosts = totalLoadingCosts + loadingCosts;
				tuples.remove(tuple);
				break;
			}
		}
	}

	@Override
	public void handleEvent(FreightServiceStartEvent event) {
		totalNumberOfShipments++;
		totalWeightOfShipments = totalWeightOfShipments + event.getCapacityDemand();
		tuples.add(new ServiceTuple(event.getServiceId(), event.getTime()));
	}

	public double getTotalLoadingCosts() {
		return totalLoadingCosts;
	}

	public int getTotalNumberOfShipments() {
		return totalNumberOfShipments;
	}

	public int getTotalWeightOfShipments() {
		return totalWeightOfShipments;
	}

	private static class ServiceTuple {
		private final Id<CarrierService> serviceId;
		private final double startTime;

		public ServiceTuple(Id<CarrierService> serviceId, double startTime) {
			this.serviceId = serviceId;
			this.startTime = startTime;
		}

		public Id<CarrierService> getServiceId() {
			return serviceId;
		}

		public double getStartTime() {
			return startTime;
		}

	}

}
