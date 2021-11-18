/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.pmd.plugin;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

/**
 * PMD rule definition for SonarQube plugin.
 * 
 * @author anorkus
 * @since 1.0
 */
public class PmdCustomRulesDefinition implements RulesDefinition {
	
	/** Must match basePlugin from sonar packaging maven plugin */
	private static final String REPOSITORY_KEY = "pmd";
	
	private static final String LANGUAGE_KEY = "java";
	
	private final RulesDefinitionXmlLoader xmlLoader;
	
	public PmdCustomRulesDefinition(RulesDefinitionXmlLoader xmlLoader) {
		this.xmlLoader = xmlLoader;
	}
	
	public void define(Context context) {
		
		NewRepository repository = (NewRepository) context.extendRepository(REPOSITORY_KEY, LANGUAGE_KEY);

		xmlLoader.load(repository, getClass().getResourceAsStream("/sonar/pmd-extension.xml"), "utf-8");
		repository.done();
	}
}
