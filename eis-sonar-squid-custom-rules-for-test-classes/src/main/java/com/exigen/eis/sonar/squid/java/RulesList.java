package com.exigen.eis.sonar.squid.java;

import java.util.List;

import org.sonar.plugins.java.api.JavaCheck;

import com.exigen.eis.sonar.squid.java.checks.AvoidEmptyTestCaseIdParameterRule;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author tgriusys
 *
 */
public final class RulesList {

	private RulesList() {
	}

	public static List<Class> getChecks() {
		return ImmutableList.<Class>builder().addAll(getJavaChecks()).addAll(getJavaTestChecks()).build();
	}

	public static List<Class<? extends JavaCheck>> getJavaChecks() {
		return ImmutableList.<Class<? extends JavaCheck>>builder().add(AvoidEmptyTestCaseIdParameterRule.class).build();
	}

	public static List<Class<? extends JavaCheck>> getJavaTestChecks() {
		return ImmutableList.<Class<? extends JavaCheck>>builder().build();
	}
}
