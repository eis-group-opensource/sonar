/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.pmd.rule;

import com.exigen.eis.sonar.pmd.rule.wrapper.SimpleAggregatorWrapper;

/**
 * Unit test for {@link AvoidInstantiatingDaoObjectsInLoopsRule}.
 * 
 * See AvoidInstantiatingDaoObjectsInLoopsRule.xml for test data.
 * 
 * @author anorkus
 * @since 1.0
 */
public class AvoidInstantiatingDaoObjectsInLoopsRuleTest extends SimpleAggregatorWrapper {
	
    @Override
	public String getRuleName() {
		return "AvoidInstantiatingDaoObjectsInLoopsRule";
	}
	
}
