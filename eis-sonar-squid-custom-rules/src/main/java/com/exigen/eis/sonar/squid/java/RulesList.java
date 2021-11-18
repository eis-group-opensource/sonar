package com.exigen.eis.sonar.squid.java;

import java.util.List;

import org.sonar.plugins.java.api.JavaCheck;

import com.exigen.eis.sonar.squid.java.checks.AvoidDaoMethodsCallsInLoopsRule;
import com.exigen.eis.sonar.squid.java.checks.AvoidInstantiatingDaoObjectsInLoopsRule;
import com.exigen.eis.sonar.squid.java.checks.AvoidUseInternalAnnotatedClassInPublicClassRule;
import com.exigen.eis.sonar.squid.java.checks.DaoClassNamingConventionsRule;
import com.exigen.eis.sonar.squid.java.checks.DomainModelListAnnotationRule;
import com.exigen.eis.sonar.squid.java.checks.EntityMustBeAnnotatedWithDiscriminatorColumnRule;
import com.exigen.eis.sonar.squid.java.checks.PublicApiJavadocMethodRule;
import com.exigen.eis.sonar.squid.java.checks.PublicApiJavadocStyleRule;
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
		return ImmutableList.<Class<? extends JavaCheck>>builder().add(AvoidDaoMethodsCallsInLoopsRule.class)
				.add(AvoidInstantiatingDaoObjectsInLoopsRule.class).add(DomainModelListAnnotationRule.class)
				.add(DaoClassNamingConventionsRule.class).add(EntityMustBeAnnotatedWithDiscriminatorColumnRule.class)
				.add(AvoidUseInternalAnnotatedClassInPublicClassRule.class).add(PublicApiJavadocStyleRule.class)
				.add(PublicApiJavadocMethodRule.class).build();
	}

	public static List<Class<? extends JavaCheck>> getJavaTestChecks() {
		return ImmutableList.<Class<? extends JavaCheck>>builder().build();
	}
}
