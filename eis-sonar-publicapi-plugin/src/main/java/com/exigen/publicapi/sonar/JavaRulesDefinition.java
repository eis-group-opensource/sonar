/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.publicapi.sonar;

import org.sonar.plugins.java.Java;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.squidbridge.annotations.AnnotationBasedRulesDefinition;

import com.exigen.publicapi.sonar.RulesList;

/**
 * @author agukov
 */

public class JavaRulesDefinition implements RulesDefinition {

    public static final String REPOSITORY_KEY = "Exigen";

    @Override
    public void define(Context context) {
        NewRepository repository = context.createRepository(REPOSITORY_KEY, Java.KEY);
        repository.setName("Exigen");

        AnnotationBasedRulesDefinition.load(repository, "java", RulesList.getChecks());
        repository.done();
    }

}
