/* Copyright © 2018 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.publicapi.rules;

import java.util.List;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import com.exigen.publicapi.utils.SubsystemToPackageMap;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import com.exigen.publicapi.utils.Constants;
import com.exigen.publicapi.utils.SubsystemToPackageMapHelper;
import com.google.common.collect.ImmutableList;

@Rule(key = "SubsystemsPublicApiIllegal", name = "EIS Coding Rules (Base) - Subsystems PublicAPI Illegal", description = "Each subsystem should use only PublicAPI classes of other subsystem.", priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("10min")
/**
 * 
 * @author tgriusys
 *
 */
public class SubsystemsPublicApiIllegal extends IssuableSubscriptionVisitor {

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
			+ " com.exigen.ipb.jpa, com.exigen.ipb.deploy, com.exigen.eis.base, com.exigen.ipb.ui")
	private String platformPackages;

	@RuleProperty(key = "operationalReportsPackages", description = "Operational Reports packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.operationalreports")
	private String operationalReportsPackages;

	@RuleProperty(key = "CRMPackages", description = "CRM packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.crm,"
			+ " com.exigen.ipb.customercore, com.exigen.ipb.activity.account, com.exigen.ipb.activity.campaign,"
			+ " com.exigen.ipb.activity.customer, com.exigen.ipb.entitywidget, com.exigen.ipb.activity.groupbenefits,"
			+ " com.exigen.ipb.activity.opportunity, com.exigen.ipb.thirdparty.ui, com.exigen.ipb.work")
	private String crmPackages;

	@RuleProperty(key = "partyPackages", description = "Party packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.eis.party")
	private String partyPackages;

	@RuleProperty(key = "pfPackages", description = "PF packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.product, com.exigen.ipb.components")
	private String pfPackages;

	@RuleProperty(key = "billingPackages", description = "Billing packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.billing")
	private String billingPackages;

	@RuleProperty(key = "commisionsPackages", description = "Commisions packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.commission")
	private String commisionsPackages;

	@RuleProperty(key = "thirdpartyPackages", description = "Thirdparty packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.brand, com.exigen.ipb.broker, com.exigen.ipb.partner, com.exigen.ipb.thirdparty, com.exigen.ipb.vendor")
	private String thirdpartyPackages;

	@RuleProperty(key = "policyPackages", description = "Policy packages (Set of comma delimited values)", type = "STRING", defaultValue = "com.exigen.ipb.policy.service.premiums.accruals, com.exigen.ipb.tax, com.exigen.ipb.fee,"
			+ "	com.exigen.ipb.policy, com.exigen.ipb.premiums, com.exigen.ipb.fee, com.exigen.ipb.rating,"
			+ "	com.exigen.ipb.processes, com.exigen.ipb.job, com.exigen.policy.ledger, com.exigen.ipb.core,"
			+ "	com.exigen.ipb.cl, com.exigen.eis.policy")
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

	ClassTree classTree;
	String classSubsystem;
	MethodInvocationTree methodInvocationTree;

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.CLASS, Tree.Kind.INTERFACE, Tree.Kind.METHOD_INVOCATION);
	}

	@Override
	public void visitNode(Tree tree) {
		if (packagesTransferedToArrayLists == false) {
			setSubsystemPackages();
			packagesTransferedToArrayLists = true;
		}

		if (tree.is(Tree.Kind.CLASS) || tree.is(Tree.Kind.INTERFACE)) {

			classTree = (ClassTree) tree;

			// Failing on inner class which is from same subsystem as main class in file
			try {
				String fullClassNameWithPackage = classTree.symbol().type().fullyQualifiedName();
				String className = classTree.simpleName().name();
				String classPackage = fullClassNameWithPackage.substring(0,
						fullClassNameWithPackage.length() - className.length() - 1);

				// remove non package String (in case if class is in other class file) so rule
				// could read subsystem
				classPackage = SubsystemToPackageMapHelper.removeNonPackageString(classPackage);

				classSubsystem = SubsystemToPackageMapHelper.getClassSubsystemByPackage(classPackage);

				reportInheritanceOrImplementationOfInternalAnnotatedClassesFromOtherSubsystems();

				reportInheritanceOrImplementationOfCanInvokeAnnotatedClassesFromOtherSubsystems();
			} catch (NullPointerException e) {
				Loggers.get(SubsystemsPublicApiIllegal.class).info(
						"HERE: Unable to process  inner class of " + classTree.symbol().type().fullyQualifiedName());
			}

		} else if (tree.is(Tree.Kind.METHOD_INVOCATION)) {

			methodInvocationTree = (MethodInvocationTree) tree;

			reportMethodCallsOfInternalAnnotatedClassesFromOtherSubsystems();
		}
	}

	private void reportInheritanceOrImplementationOfInternalAnnotatedClassesFromOtherSubsystems() {
		if (!classTree.symbol().interfaces().isEmpty()) {
			for (int i = 0; i < classTree.symbol().interfaces().size(); i++) {
				Type interfaceType = classTree.symbol().interfaces().get(i);
				if (!isInterfaceSubsystemSameAsCurrentClassSubsystem(interfaceType)
						&& isInterfaceContainsInternalAnnotation(interfaceType)) {
					reportIssue(classTree.simpleName(), interfaceType.fullyQualifiedName()
							+ " should not be used because it is annotated with @Internal and is from another subsystem.");
				}
			}
		}
		if (classTree.symbol().superClass() != null && !isSuperClassSubsystemSameAsCurrentClassSubsystem()
				&& isSuperClassContainsInternalAnnotation()) {
			reportIssue(classTree.simpleName(), classTree.symbol().superClass().fullyQualifiedName()
					+ " should not be used because it is annotated with @Internal and is from another subsystem.");
		}
	}

	private boolean isInterfaceContainsInternalAnnotation(Type interfaceType) {

		return interfaceType.symbol().metadata().isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH);
	}

	private boolean isSuperClassContainsInternalAnnotation() {
		return classTree.symbol().superClass().symbol().metadata().isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH);
	}

	/**
	 * Method is checking if class/interface implements or extends any class or
	 * interface with @CanInvoke annotation from other subsystems.
	 */
	private void reportInheritanceOrImplementationOfCanInvokeAnnotatedClassesFromOtherSubsystems() {
		if (!classTree.symbol().interfaces().isEmpty()) {
			for (int i = 0; i < classTree.symbol().interfaces().size(); i++) {
				Type interfaceType = classTree.symbol().interfaces().get(i);
				if (!isInterfaceSubsystemSameAsCurrentClassSubsystem(interfaceType)
						&& isInterfaceContainsCanInvokeAnnotation(interfaceType)) {
					reportIssue(classTree.simpleName(), interfaceType.fullyQualifiedName()
							+ " should not be used because it is annotated with @CanInvoke and is from another subsystem.");
				}
			}
		}
		if (classTree.symbol().superClass() != null && !isSuperClassSubsystemSameAsCurrentClassSubsystem()
				&& isSuperClassContainsCanInvokeAnnotation()) {
			reportIssue(classTree.simpleName(), classTree.symbol().superClass().fullyQualifiedName()
					+ " should not be used because it is annotated with @CanInvoke and is from another subsystem.");
		}

	}

	private boolean isInterfaceContainsCanInvokeAnnotation(Type interfaceType) {

		return interfaceType.symbol().metadata().isAnnotatedWith(Constants.CAN_INVOKE_ANNOTATION_PATH);
	}

	private boolean isSuperClassContainsCanInvokeAnnotation() {
		return classTree.symbol().superClass().symbol().metadata()
				.isAnnotatedWith(Constants.CAN_INVOKE_ANNOTATION_PATH);
	}

	private boolean isInterfaceSubsystemSameAsCurrentClassSubsystem(Type interfaceType) {
		String fullInterfaceNameWithPackage = interfaceType.fullyQualifiedName();

		String interfacePackage = fullInterfaceNameWithPackage.substring(0,
				fullInterfaceNameWithPackage.length() - interfaceType.name().length() - 1);

		// removing non package String (in case if class is in other class file) so rule
		// could read subsystem
		interfacePackage = SubsystemToPackageMapHelper.removeNonPackageString(interfacePackage);

		String interfaceSubsystem = SubsystemToPackageMapHelper.getClassSubsystemByPackage(interfacePackage);

		if (interfaceSubsystem.equals("No subsystem contains such package")) {
			return false;
		} else if (interfaceSubsystem.equals(classSubsystem)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isSuperClassSubsystemSameAsCurrentClassSubsystem() {
		String fullSuperClassNameWithPackage = classTree.symbol().superClass().fullyQualifiedName();

		String superClassPackage = fullSuperClassNameWithPackage.substring(0,
				fullSuperClassNameWithPackage.length() - classTree.symbol().superClass().name().length() - 1);

		// removing non package String (in case if class is in other class file) so rule
		// could read subsystem
		superClassPackage = SubsystemToPackageMapHelper.removeNonPackageString(superClassPackage);

		String superClassSubsystem = SubsystemToPackageMapHelper.getClassSubsystemByPackage(superClassPackage);

		if (superClassSubsystem.equals("No subsystem contains such package")) {
			return false;
		} else if (superClassSubsystem.equals(classSubsystem)) {
			return true;
		} else {
			return false;
		}
	}

	private void reportMethodCallsOfInternalAnnotatedClassesFromOtherSubsystems() {
		if (!isMethodInvocationObjectClassSameSubsystemAsCurrentClass()
				&& isMethodInvocationObjectClassAnnotatedWithInternalAnnotation()) {
			reportIssue(methodInvocationTree,
					"Method calls of @Internal annotated classes from other subsystems are not allowed.");
		}
	}

	private boolean isMethodInvocationObjectClassSameSubsystemAsCurrentClass() {

		Symbol methodInvocationTreeSymbol = methodInvocationTree.symbol();

		try {

			Symbol methodinvocationOwner = methodInvocationTreeSymbol.owner();
			Type methodInvocationOwnerType = methodinvocationOwner.type();
			String fullObjectClassNameWithPackage = methodInvocationOwnerType.fullyQualifiedName();
			String objectClassName = methodInvocationTree.symbol().owner().name();

			// TODO: deal with !unknownSymbol!
			if (!fullObjectClassNameWithPackage.equals("!unknownSymbol!")) {

				String objectClassPackage = fullObjectClassNameWithPackage.substring(0,
						fullObjectClassNameWithPackage.length() - (objectClassName.length() + 1));

				// remove non package String (in case if class is in other class file) so rule
				// could read subsystem
				objectClassPackage = SubsystemToPackageMapHelper.removeNonPackageString(objectClassPackage);

				String objectClassSubsystem = SubsystemToPackageMapHelper
						.getClassSubsystemByPackage(objectClassPackage);

				if (objectClassSubsystem.equals("No subsystem contains such package")) {
					return false;
				} else if (objectClassSubsystem.equals(classSubsystem)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			Loggers.get(SubsystemsPublicApiIllegal.class)
					.info("HERE: Unable to process  " + methodInvocationTree.firstToken().line() + " line of "
							+ classTree.symbol().type().fullyQualifiedName());
			return true;

		}
	}

	private boolean isMethodInvocationObjectClassAnnotatedWithInternalAnnotation() {
		return methodInvocationTree.symbol().owner().metadata().isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH);
	}

	private void setSubsystemPackages() {
		SubsystemToPackageMapHelper.setPackages(platformPackages, SubsystemToPackageMap.platformPackages);
		SubsystemToPackageMapHelper.setPackages(operationalReportsPackages, SubsystemToPackageMap.operationalReportsPackages);
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