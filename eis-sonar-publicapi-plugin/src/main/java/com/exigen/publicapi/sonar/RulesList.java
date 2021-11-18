/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.publicapi.sonar;

import java.util.List;

import org.sonar.plugins.java.api.JavaCheck;

import com.exigen.publicapi.rules.AvoidUnmapedPackagesRule;
import com.exigen.publicapi.rules.AvoidUseInternalAnnotatedClassInPublicClassRule;
import com.exigen.publicapi.rules.PublicApiAnnotationCheckRule;
import com.exigen.publicapi.rules.PublicApiCheckRule;
import com.exigen.publicapi.rules.SubsystemsPublicApiIllegal;
import com.google.common.collect.ImmutableList;

/**
 * @author agukov
 * @author tgriusys
 */
public class RulesList {

	private RulesList() {

	}

	public static List<Class> getChecks() {
		return ImmutableList.<Class>builder().addAll(getJavaChecks()).addAll(getJavaTestChecks()).build();
	}

	public static List<Class<? extends JavaCheck>> getJavaChecks() {
		return ImmutableList.<Class<? extends JavaCheck>>builder()
				.add(AvoidUseInternalAnnotatedClassInPublicClassRule.class).add(PublicApiCheckRule.class)
				.add(AvoidUnmapedPackagesRule.class).add(PublicApiAnnotationCheckRule.class)
				.add(SubsystemsPublicApiIllegal.class).build();
	}

	public static ImmutableList<Class> getJavaTestChecks() {
		return ImmutableList.<Class>builder().build();
	}
}
