/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.checkstyle.plugin;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.SonarPlugin;

/**
 * SonarQube plugin for checkstyle custom rules.
 * 
 * @author anorkus
 * @since 1.0
 */
public class CheckstyleCustomRulesExtensionPlugin extends SonarPlugin {

	@SuppressWarnings("rawtypes")
	public List getExtensions() {
		return Arrays.asList(CheckstyleCustomRulesDefinition.class);
	}
}
