package com.exigen.publicapi.utils;

import java.util.ArrayList;

/**
 * 
 * @author tgriusys
 *
 */
public class SubsystemToPackageMapHelper {

	public static boolean isEISPackage(String packageName) {
		return packageName.contains("com.exigen");
	}

	public static boolean isMappedToSubsystem(String packageName) {

		if (SubsystemToPackageMap.operationalReportsPackages.contains(packageName)
				|| SubsystemToPackageMap.platformPackages.contains(packageName)
				|| SubsystemToPackageMap.crmPackages.contains(packageName)
				|| SubsystemToPackageMap.partyPackages.contains(packageName)
				|| SubsystemToPackageMap.pfPackages.contains(packageName)
				|| SubsystemToPackageMap.billingPackages.contains(packageName)
				|| SubsystemToPackageMap.commisionsPackages.contains(packageName)
				|| SubsystemToPackageMap.thirdpartyPackages.contains(packageName)
				|| SubsystemToPackageMap.policyPackages.contains(packageName)
				|| SubsystemToPackageMap.claimsPackages.contains(packageName)
				|| SubsystemToPackageMap.preconfigPackages.contains(packageName)
				|| SubsystemToPackageMap.selfServicePackages.contains(packageName)) {
			return true;
		} else if (packageName.contains(".")) {
			String[] splittedPackageName = packageName.split("\\.");
			return isMappedToSubsystem(
					packageName.replace("." + splittedPackageName[splittedPackageName.length - 1], ""));
		} else {
			return false;
		}
	}

	public static String getClassSubsystemByPackage(String packageName) {
		if (SubsystemToPackageMap.operationalReportsPackages.contains(packageName)) {
			return "OperationalReports";
		} else if (SubsystemToPackageMap.platformPackages.contains(packageName)) {
			return "Platform";
		} else if (SubsystemToPackageMap.crmPackages.contains(packageName)) {
			return "CRM";
		} else if (SubsystemToPackageMap.partyPackages.contains(packageName)) {
			return "Party";
		} else if (SubsystemToPackageMap.pfPackages.contains(packageName)) {
			return "PF";
		} else if (SubsystemToPackageMap.billingPackages.contains(packageName)) {
			return "Billing";
		} else if (SubsystemToPackageMap.commisionsPackages.contains(packageName)) {
			return "Commisions";
		} else if (SubsystemToPackageMap.thirdpartyPackages.contains(packageName)) {
			return "Thirdparty";
		} else if (SubsystemToPackageMap.policyPackages.contains(packageName)) {
			return "Policy";
		} else if (SubsystemToPackageMap.claimsPackages.contains(packageName)) {
			return "Claims";
		} else if (SubsystemToPackageMap.preconfigPackages.contains(packageName)) {
			return "Preconfig";
		} else if (SubsystemToPackageMap.selfServicePackages.contains(packageName)) {
			return "SelfService";
		} else if (packageName.contains(".")) {
			String[] splittedPackageName = packageName.split("\\.");
			return getClassSubsystemByPackage(
					packageName.replace("." + splittedPackageName[splittedPackageName.length - 1], ""));
		} else {
			return "No subsystem contains such package";
		}

	}

	public static void setPackages(String packages, ArrayList<String> packagesList) {
		if (packages != null && packages.contains(",")) {
			String[] packagesToSet = packages.split(",");

			// for multiple packages
			for (int i = 0; i < packagesToSet.length; i++) {
				packagesList.add(packagesToSet[i].trim());
			}
		} else if (packages != null && packages.trim().length() > 0) {
			packagesList.add(packages.trim());
		}
	}

	public static String removeNonPackageString(String packageName) {
		String[] splitedPackage = packageName.split("\\.");
		if (Character.isUpperCase(splitedPackage[splitedPackage.length - 1].charAt(0))) {
			packageName = packageName.replace("." + splitedPackage[splitedPackage.length - 1], "");
		}
		return packageName;
	}
}