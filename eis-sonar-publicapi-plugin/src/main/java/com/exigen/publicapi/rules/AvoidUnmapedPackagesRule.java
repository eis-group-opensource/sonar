/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.publicapi.rules;

import java.util.List;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import com.exigen.publicapi.utils.SubsystemToPackageMap;
import com.exigen.publicapi.utils.SubsystemToPackageMapHelper;
import com.google.common.collect.ImmutableList;

@Rule(key = "EIS Coding Rules - package mapping", name = "EIS Coding Rules - EIS package to subsystem mapping", description = "Check if package is EIS package and if it is maped to a subsystem", priority = org.sonar.check.Priority.CRITICAL)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("10min")
/**
 * 
 * @author tgriusys
 *
 */
public class AvoidUnmapedPackagesRule extends IssuableSubscriptionVisitor {

	@RuleProperty(key = "platformPackages", description = "Platform packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.bam,"
			+ " com.exigen.ipb.base, com.exigen.ipb.security, com.exigen.eis.appmon, com.exigen.ipb.aspects,"
			+ " com.exigen.ipb.lookups, com.exigen.ipb.docgen, com.exigen.docgen, com.exigen.ipb.services.docgen,"
			+ " com.exigen.ipb.efolder, com.exigen.ipb.platform, com.exigen.ipb.search, com.exigen.epb.print,"
			+ " com.exigen.ipb.async, com.exigen.scheduler, com.exigen.eis.integration.batchjob,"
			+ " com.exigen.ipb.ledger, com.exigen.ipb.geo, com.exigen.ipb.jsf, com.exigen.ipb.web,"
			+ " com.exigen.ipb.webflow, com.exigen.ipb.auth, com.exigen.ipb.payments, com.exigen.ipb,"
			+ " com.exigen.ipb.e2e, com.exigen.ipb.selenium, com.exigen.eis.e2e, com.exigen.ipb.reports,"
			+ " com.exigen.base, com.exigen.eis.docgen, com.exigen.eis.work, com.exigen.eis.integration.service,"
			+ " com.exigen.epb.ipb.efolder, com.exigen.epb.security, com.exigen.ipb.admin,"
			+ " com.exigen.ipb.app.context, com.exigen.ipb.beans, com.exigen.ipb.bulletin, com.exigen.ipb.ead,"
			+ " com.exigen.ipb.i18n, com.exigen.ipb.integration, com.exigen.ipb.springframework,"
			+ " com.exigen.ipb.scheduler, com.exigen.ipb.businesscalendar, com.exigen.ipb.bls, com.exigen.ipb.bpm,"
			+ " com.exigen.ipb.jpa, com.exigen.ipb.deploy, com.exigen.eis.base, com.exigen.ipb.ui,"
			+ " com.exigen.eis.atmosphere, com.exigen.eis.exceptions, com.exigen.eis.security,"
			+ " com.exigen.eis.serlvet, com.exigen.ipb.entitycache, com.exigen.ipb.note, com.exigen.ipb.swagger")
	private String platformPackages;

	@RuleProperty(key = "operationalReportsPackages", description = "Operational Reports packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.operationalreports")
	private String operationalReportsPackages;

	@RuleProperty(key = "CRMPackages", description = "CRM packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.crm,"
			+ " com.exigen.ipb.customercore, com.exigen.ipb.activity.account, com.exigen.ipb.activity.campaign,"
			+ " com.exigen.ipb.activity.customer, com.exigen.ipb.entitywidget, com.exigen.ipb.activity.groupbenefits,"
			+ " com.exigen.ipb.activity.opportunity, com.exigen.ipb.thirdparty.ui, com.exigen.ipb.work,"
			+ " com.exigen.ipb.activity.businessentity, com.exigen.ipb.activity.divisions")
	private String crmPackages;

	@RuleProperty(key = "partyPackages", description = "Party packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.eis.party")
	private String partyPackages;

	@RuleProperty(key = "pfPackages", description = "PF packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.product, com.exigen.ipb.components")
	private String pfPackages;

	@RuleProperty(key = "billingPackages", description = "Billing packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.billing")
	private String billingPackages;

	@RuleProperty(key = "commisionsPackages", description = "Commisions packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.commission")
	private String commisionsPackages;

	@RuleProperty(key = "thirdpartyPackages", description = "Thirdparty packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.brand, com.exigen.ipb.broker, com.exigen.ipb.partner, com.exigen.ipb.thirdparty, com.exigen.ipb.vendor, com.exigen.ipb.serviceprovider.dto")
	private String thirdpartyPackages;

	@RuleProperty(key = "policyPackages", description = "Policy packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.policy.service.premiums.accruals, com.exigen.ipb.tax, com.exigen.ipb.fee,"
			+ "	com.exigen.ipb.policy, com.exigen.ipb.premiums, com.exigen.ipb.fee, com.exigen.ipb.rating,"
			+ "	com.exigen.ipb.processes, com.exigen.ipb.job, com.exigen.policy.ledger, com.exigen.ipb.core,"
			+ "	com.exigen.ipb.cl, com.exigen.eis.policy, com.exigen.ipb.tfs")
	private String policyPackages;

	@RuleProperty(key = "claimsPackages", description = "Claims packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.claims,"
			+ "	com.exigenservices.ipb.general.integration, com.exigen.ipb.work.integration")
	private String claimsPackages;

	@RuleProperty(key = "preconfigPackages", description = "Preconfig packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.preconfig,"
			+ "	com.exigen.ipb.policy.preconfig, com.exigen.ipb.preconfig.claims,"
			+ "	com.exigen.eis.preconfig.policy.group, com.exigen.eis.preconfig.policy, com.exigen.eis.preconfig")
	private String preconfigPackages;

	@RuleProperty(key = "selfServicePackages", description = "SelfService packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.selfservice")
	private String selfServicePackages;

	private boolean packagesTransferedToArrayLists = false;

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.CLASS, Tree.Kind.INTERFACE);
	}

	@Override
	public void visitNode(Tree tree) {
		if (packagesTransferedToArrayLists == false) {
			setSubsystemPackages();
			packagesTransferedToArrayLists = true;
		}
		ClassTree classTree = (ClassTree) tree;
		// NullPointerException in AddGBCommissionTab:41 even if it is not a class. Even
		// if it is, it is in same subsystem as regular class, so subsystem is also same
		try {
			String fullClassNameWithPackage = classTree.symbol().type().fullyQualifiedName();

			String classPackage = fullClassNameWithPackage.substring(0,
					fullClassNameWithPackage.length() - classTree.simpleName().name().length() - 1);

			// remove non package String (in case if class is in other class file) so rule
			// could read subsystem
			classPackage = SubsystemToPackageMapHelper.removeNonPackageString(classPackage);

			if (!SubsystemToPackageMapHelper.isMappedToSubsystem(classPackage)
					&& SubsystemToPackageMapHelper.isEISPackage(classPackage)) {
				reportIssue(classTree.simpleName(),
						"Package " + classPackage + " is EIS and is not mapped to any subsystem");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void setSubsystemPackages() {
		SubsystemToPackageMapHelper.setPackages(platformPackages, SubsystemToPackageMap.platformPackages);
		SubsystemToPackageMapHelper.setPackages(operationalReportsPackages,
				SubsystemToPackageMap.operationalReportsPackages);
		SubsystemToPackageMapHelper.setPackages(crmPackages, SubsystemToPackageMap.crmPackages);
		SubsystemToPackageMapHelper.setPackages(partyPackages, SubsystemToPackageMap.partyPackages);
		SubsystemToPackageMapHelper.setPackages(pfPackages, SubsystemToPackageMap.pfPackages);
		SubsystemToPackageMapHelper.setPackages(billingPackages, SubsystemToPackageMap.billingPackages);
		SubsystemToPackageMapHelper.setPackages(commisionsPackages, SubsystemToPackageMap.commisionsPackages);
		SubsystemToPackageMapHelper.setPackages(thirdpartyPackages, SubsystemToPackageMap.thirdpartyPackages);
		SubsystemToPackageMapHelper.setPackages(policyPackages, SubsystemToPackageMap.policyPackages);
		SubsystemToPackageMapHelper.setPackages(claimsPackages, SubsystemToPackageMap.claimsPackages);
		SubsystemToPackageMapHelper.setPackages(preconfigPackages, SubsystemToPackageMap.preconfigPackages);
		SubsystemToPackageMapHelper.setPackages(selfServicePackages, SubsystemToPackageMap.selfServicePackages);
	}
}
