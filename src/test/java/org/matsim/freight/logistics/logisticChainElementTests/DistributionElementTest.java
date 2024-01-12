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

package org.matsim.freight.logistics.logisticChainElementTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.freight.carriers.*;
import org.matsim.freight.carriers.CarrierCapabilities.FleetSize;
import org.matsim.freight.logistics.LSPCarrierResource;
import org.matsim.freight.logistics.LSPResource;
import org.matsim.freight.logistics.LSPUtils;
import org.matsim.freight.logistics.LogisticChainElement;
import org.matsim.freight.logistics.resourceImplementations.DistributionCarrierUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

public class DistributionElementTest {

	private LSPCarrierResource adapter;
	private LogisticChainElement distributionElement;


	@BeforeEach
	public void initialize() {
		Config config = new Config();
		config.addCoreModules();
		Scenario scenario = ScenarioUtils.createScenario(config);
		new MatsimNetworkReader(scenario.getNetwork()).readFile("scenarios/2regions/2regions-network.xml");
		Network network = scenario.getNetwork();

		Id<Carrier> carrierId = Id.create("DistributionCarrier", Carrier.class);
		Id<VehicleType> vehicleTypeId = Id.create("DistributionCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder vehicleTypeBuilder = CarrierVehicleType.Builder.newInstance(vehicleTypeId);
		vehicleTypeBuilder.setCapacity(10);
		vehicleTypeBuilder.setCostPerDistanceUnit(0.0004);
		vehicleTypeBuilder.setCostPerTimeUnit(0.38);
		vehicleTypeBuilder.setFixCost(49);
		vehicleTypeBuilder.setMaxVelocity(50 / 3.6);
		VehicleType distributionType = vehicleTypeBuilder.build();

		Id<Link> distributionLinkId = Id.createLinkId("(4 2) (4 3)");
		Id<Vehicle> distributionVehicleId = Id.createVehicleId("DistributionVehicle");
		CarrierVehicle carrierVehicle = CarrierVehicle.newInstance(distributionVehicleId, distributionLinkId, distributionType);

		CarrierCapabilities.Builder capabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		capabilitiesBuilder.addType(distributionType);
		capabilitiesBuilder.addVehicle(carrierVehicle);
		capabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		CarrierCapabilities capabilities = capabilitiesBuilder.build();
		Carrier carrier = CarriersUtils.createCarrier(carrierId);
		carrier.setCarrierCapabilities(capabilities);


		Id<LSPResource> adapterId = Id.create("DistributionCarrierResource", LSPResource.class);
		adapter = DistributionCarrierUtils.DistributionCarrierResourceBuilder.newInstance(carrier, network)
				.setDistributionScheduler(DistributionCarrierUtils.createDefaultDistributionCarrierScheduler())
				.setLocationLinkId(distributionLinkId)
				.build();

		Id<LogisticChainElement> elementId = Id.create("DistributionElement", LogisticChainElement.class);
		LSPUtils.LogisticChainElementBuilder distributionBuilder = LSPUtils.LogisticChainElementBuilder.newInstance(elementId);
		distributionBuilder.setResource(adapter);
		distributionElement = distributionBuilder.build();

	}

	@Test
	public void testDistributionElement() {
		assertNotNull(distributionElement.getIncomingShipments());
		assertNotNull(distributionElement.getIncomingShipments().getShipments());
		assertTrue(distributionElement.getIncomingShipments().getSortedShipments().isEmpty());
		assertNotNull(distributionElement.getAttributes());
		assertTrue(distributionElement.getAttributes().isEmpty());
//		assertNull(distributionElement.getEmbeddingContainer() );
		assertNull(distributionElement.getNextElement());
		assertNotNull(distributionElement.getOutgoingShipments());
		assertNotNull(distributionElement.getOutgoingShipments().getShipments());
		assertTrue(distributionElement.getOutgoingShipments().getSortedShipments().isEmpty());
		assertNull(distributionElement.getPreviousElement());
		assertSame(distributionElement.getResource(), adapter);
		assertSame(distributionElement.getResource().getClientElements().iterator().next(), distributionElement);
	}
}
