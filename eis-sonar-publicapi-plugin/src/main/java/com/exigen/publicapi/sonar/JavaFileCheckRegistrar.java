/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.publicapi.sonar;

import java.util.Arrays;

import org.sonar.plugins.java.api.CheckRegistrar;
import org.sonar.plugins.java.api.JavaCheck;

import com.exigen.publicapi.rules.AvoidUnmapedPackagesRule;
import com.exigen.publicapi.rules.AvoidUseInternalAnnotatedClassInPublicClassRule;
import com.exigen.publicapi.rules.PublicApiAnnotationCheckRule;
import com.exigen.publicapi.rules.PublicApiCheckRule;
import com.exigen.publicapi.rules.SubsystemsPublicApiIllegal;

/**
 * @author agukov
 */

public class JavaFileCheckRegistrar implements CheckRegistrar {

	@Override
	public void register(RegistrarContext registrarContext) {
		registrarContext.registerClassesForRepository(JavaRulesDefinition.REPOSITORY_KEY, Arrays.asList(checkClasses()),
				Arrays.asList(testCheckClasses()));
	}

	public static Class<? extends JavaCheck>[] checkClasses() {
		return new Class[] { PublicApiCheckRule.class, PublicApiAnnotationCheckRule.class,
				AvoidUnmapedPackagesRule.class, AvoidUseInternalAnnotatedClassInPublicClassRule.class,
				SubsystemsPublicApiIllegal.class };
	}

	public static Class<? extends JavaCheck>[] testCheckClasses() {
		return new Class[] {};
	}
}
