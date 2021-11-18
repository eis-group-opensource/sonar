/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.checkstyle.checks.javadoc;

import static com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocStyleCheck.MSG_JAVADOC_MISSING;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;

import com.exigen.eis.sonar.checkstyle.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Unit test for {@link PublicApiJavadocStyleCheck}. 
 * 
 * @author anorkus
 * @since 1.0
 */
public class PublicApiJavadocStyleCheckTest extends BaseCheckTestSupport {

	private DefaultConfiguration checkConfig;

	@Before
	public void setUp() {
		checkConfig = createCheckConfig(PublicApiJavadocMethodCheck.class);
	}

	@Test
	public void shouldFailWhenJavadocMissingForCanInvokePublicApi() throws Exception {
		final String[] expected = { "9:9: " + getCheckMessage(MSG_JAVADOC_MISSING), };
		verify(checkConfig, getPath("checks/javadoc/EisPublicApi_01.java"), expected);
	}

	@Test
	public void shouldFailWhenJavadocMissingForCanExtendPublicApi() throws Exception {
		final String[] expected = { "9:9: " + getCheckMessage(MSG_JAVADOC_MISSING), };
		verify(checkConfig, getPath("checks/javadoc/EisPublicApi_02.java"), expected);
	}

	@Test
	public void shouldPassWhenJavadocExistForPublicApiMethod() throws Exception {
		final String[] expected = ArrayUtils.EMPTY_STRING_ARRAY;
		verify(checkConfig, getPath("checks/javadoc/EisPublicApi_03.java"), expected);
	}

	@Test
	public void shouldPassWhenJavadocMissingForInternalClass() throws Exception {
		final String[] expected = ArrayUtils.EMPTY_STRING_ARRAY;
		verify(checkConfig, getPath("checks/javadoc/EisInternalApi_01.java"), expected);
	}

	@Test
	public void shouldFailWhenJavadocMissingForCanInvokePublicApiInterface() throws Exception {
		final String[] expected = { "9:9: " + getCheckMessage(MSG_JAVADOC_MISSING), };
		verify(checkConfig, getPath("checks/javadoc/EisPublicApiInterface_01.java"), expected);
	}

	@Test
	public void shouldPassWhenCheckOnlyInterfacesFlagTrueAndJavadocMissingForCanInvokePublicApiClass()
			throws Exception {
		final String[] expected = ArrayUtils.EMPTY_STRING_ARRAY;
		checkConfig.addAttribute("checkOnlyInterfaces", "true");
		verify(checkConfig, getPath("checks/javadoc/EisPublicApi_01.java"), expected);
	}

	@Test
	public void shouldFailWhenJavadocMissingForCanExtendPublicApiInterface() throws Exception {
		final String[] expected = { "9:9: " + getCheckMessage(MSG_JAVADOC_MISSING), };
		verify(checkConfig, getPath("checks/javadoc/EisPublicApiInterface_02.java"), expected);
	}

	@Test
	public void shouldPassWhenCheckOnlyInterfacesFlagTrueAndJavadocMissingForCanExtendPublicApiClass()
			throws Exception {
		final String[] expected = ArrayUtils.EMPTY_STRING_ARRAY;
		checkConfig.addAttribute("checkOnlyInterfaces", "true");
		verify(checkConfig, getPath("checks/javadoc/EisPublicApi_02.java"), expected);
	}

	@Test
	public void shouldPassWhenJavadocExistForPublicApiInterfaceMethod() throws Exception {
		final String[] expected = ArrayUtils.EMPTY_STRING_ARRAY;
		verify(checkConfig, getPath("checks/javadoc/EisPublicApiInterface_03.java"), expected);
	}

	@Test
	public void shouldPassWhenJavadocMissingForInternalInterface() throws Exception {
		final String[] expected = ArrayUtils.EMPTY_STRING_ARRAY;
		verify(checkConfig, getPath("checks/javadoc/EisInternalApiInterface_01.java"), expected);
	}
}
