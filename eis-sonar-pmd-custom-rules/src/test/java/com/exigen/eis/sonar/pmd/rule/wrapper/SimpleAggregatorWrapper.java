/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.pmd.rule.wrapper;

import net.sourceforge.pmd.testframework.SimpleAggregatorTst;

/**
 * SimpleAggregatorWrapper.java class for tests
 *
 * @author avasmanas
 * @since 1.0
 *
 */
public abstract class SimpleAggregatorWrapper extends SimpleAggregatorTst {
	
	private static final String RULESET = "sonar/pmd-extension-ruleset.xml";
	
    @Override
    public void setUp() {
        addRule(RULESET, getRuleName());
    }
    
    public abstract String getRuleName();
    

}
