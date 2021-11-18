/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.publicapi.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author agukov Structure should be moved outside of plugin for easy
 *         modification
 */
public class SubsystemStructure {
	private static List<List> subsystemListByPackage = new ArrayList<>();

	private static void populateSubsystemListByPackage() {

		List<String> platform = new ArrayList<String>();
		platform.add("com.exigen.ipb.base");
		platform.add("com.exigen.ipb.security");
		platform.add("com.exigen.eis.appmon");
		platform.add("com.exigen.ipb.aspects");
		platform.add("com.exigen.ipb.lookups");
		platform.add("com.exigen.ipb.docgen");
		platform.add("com.exigen.docgen");
		platform.add("com.exigen.ipb.services.docgen");
		platform.add("com.exigen.ipb.efolder");
		platform.add("com.exigen.ipb.search");
		platform.add("com.exigen.epb.print");
		platform.add("com.exigen.ipb.async");
		platform.add("com.exigen.scheduler");
		platform.add("com.exigen.eis.integration.batchjob");
		platform.add("com.exigen.ipb.ledger");
		platform.add("com.exigen.ipb.geo");
		platform.add("com.exigen.ipb.jsf");
		platform.add("com.exigen.ipb.web");
		platform.add("com.exigen.ipb.webflow");
		platform.add("com.exigen.ipb.auth");
		platform.add("com.exigen.ipb.payments");
		platform.add("com.exigen.ipb.e2e");
		platform.add("com.exigen.ipb.selenium");
		platform.add("com.exigen.eis.e2e");
		platform.add("com.exigen.ipb.reports");
		platform.add("com.exigen.base");
		platform.add("com.exigen.eis.docgen");
		platform.add("com.exigen.eis.work");
		platform.add("com.exigen.eis.integration.service");
		platform.add("com.exigen.epb.ipb.efolder");
		platform.add("com.exigen.epb.security");
		platform.add("com.exigen.ipb.admin");
		platform.add("com.exigen.ipb.app.context");
		platform.add("com.exigen.ipb.beans");
		platform.add("com.exigen.ipb.bulletin");
		platform.add("com.exigen.ipb.ead");
		platform.add("com.exigen.ipb.i18n");
		platform.add("com.exigen.ipb.integration");
		platform.add("com.exigen.ipb.springframework");
		platform.add("com.exigen.ipb.scheduler");
		platform.add("com.exigen.ipb.bam");
		platform.add("com.exigen.eis.bulletin");
		platform.add("com.exigen.ipb.businesscalendar");
		platform.add("com.exigen.ipb.bpm");
		platform.add("com.exigen.ipb.bls");
		platform.add("com.exigen.ipb.platform");
		platform.add("com.exigen.ipb.jpa");
		platform.add("com.exigen.ipb.deploy");
		platform.add("com.exigen.eis.base");
		platform.add("com.exigen.ipb.ui");
		platform.add("com.exigen.eis.atmosphere");
		platform.add("com.exigen.eis.exceptions");
		platform.add("com.exigen.eis.security");
		platform.add("com.exigen.eis.serlvet");
		platform.add("com.exigen.ipb.entitycache");
		platform.add("com.exigen.ipb.note");
		platform.add("com.exigen.ipb.swagger");
		subsystemListByPackage.add(platform);

		List<String> crm = new ArrayList<String>();
		crm.add("com.exigen.ipb.crm");
		crm.add("com.exigen.ipb.customercore");
		crm.add("com.exigen.ipb.activity.account");
		crm.add("com.exigen.ipb.activity.campaign");
		crm.add("com.exigen.ipb.activity.customer");
		crm.add("com.exigen.ipb.entitywidget");
		crm.add("com.exigen.ipb.activity.groupbenefits");
		crm.add("com.exigen.ipb.activity.opportunity");
		crm.add("com.exigen.ipb.thirdparty.ui");
		crm.add("com.exigen.ipb.work");
		crm.add("com.exigen.ipb.activity.businessentity");
		crm.add("com.exigen.ipb.activity.divisions");
		subsystemListByPackage.add(crm);

		List<String> party = new ArrayList<String>();
		party.add("com.exigen.eis.party");
		subsystemListByPackage.add(party);

		List<String> product = new ArrayList<String>();
		product.add("com.exigen.ipb.product");
		product.add("com.exigen.ipb.components");
		subsystemListByPackage.add(product);

		List<String> billing = new ArrayList<String>();
		billing.add("com.exigen.ipb.billing");
		subsystemListByPackage.add(billing);

		List<String> commission = new ArrayList<String>();
		commission.add("com.exigen.ipb.commission");
		subsystemListByPackage.add(commission);

		List<String> policy = new ArrayList<String>();
		policy.add("com.exigen.ipb.policy");
		policy.add("com.exigen.ipb.premium");
		policy.add("com.exigen.ipb.fee");
		policy.add("com.exigen.ipb.rating");
		policy.add("com.exigen.ipb.tax");
		policy.add("com.exigen.ipb.cl");
		policy.add("com.exigen.ipb.job");
		policy.add("com.exigen.ipb.processes");
		policy.add("com.exigen.eis.policy");
		policy.add("com.exigen.ipb.tfs");
		subsystemListByPackage.add(policy);

		List<String> claims = new ArrayList<String>();
		claims.add("com.exigen.ipb.claims");
		claims.add("com.exigenservices.ipb.general.integration");
		subsystemListByPackage.add(claims);

		List<String> preconfig = new ArrayList<String>();
		preconfig.add("com.exigen.ipb.preconfig");
		preconfig.add("com.exigen.ipb.preconfig.claims");
		preconfig.add("com.exigen.eis.preconfig.policy.group");
		preconfig.add("com.exigen.ipb.policy.preconfig");
		preconfig.add("com.exigen.eis.preconfig");
		subsystemListByPackage.add(preconfig);

		List<String> selfservice = new ArrayList<String>();
		selfservice.add("com.exigen.ipb.selfservice");
		selfservice.add("com.exigen.ipb.selfService");
		subsystemListByPackage.add(selfservice);

		List<String> operationalreports = new ArrayList<String>();
		operationalreports.add("com.exigen.ipb.operationalreports");
		subsystemListByPackage.add(operationalreports);

		List<String> thirdparty = new ArrayList<String>();
		thirdparty.add("com.exigen.ipb.vendor");
		thirdparty.add("com.exigen.ipb.thirdparty");
		thirdparty.add("com.exigen.ipb.partner");
		thirdparty.add("com.exigen.ipb.broker");
		thirdparty.add("com.exigen.ipb.brand");
		thirdparty.add("com.exigen.ipb.carrier");
		thirdparty.add("com.exigen.ipb.serviceprovider");
		subsystemListByPackage.add(thirdparty);


		/*For test purposes
		List<String> projectA = new ArrayList<String>();
		projectA.add("my.sonar.project.A");
		subsystemListByPackage.add(projectA);

		List<String> projectB = new ArrayList<String>();
		projectB.add("my.sonar.project.B");
		subsystemListByPackage.add(projectB);
		*/
	}

	public static List<String> getPlatformPackages() {
		if (subsystemListByPackage.isEmpty()) {
			populateSubsystemListByPackage();
			return subsystemListByPackage.get(0);
		}
		return subsystemListByPackage.get(0);
	}

	public static List<List> getSubsystemStructure() {
		if (subsystemListByPackage.isEmpty()) {
			populateSubsystemListByPackage();
			return subsystemListByPackage;
		}
		return subsystemListByPackage;
	}
}
