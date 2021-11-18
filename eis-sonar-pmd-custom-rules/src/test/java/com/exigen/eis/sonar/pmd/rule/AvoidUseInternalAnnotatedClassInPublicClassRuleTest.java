/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.pmd.rule;

import com.exigen.eis.sonar.pmd.rule.wrapper.SimpleAggregatorWrapper;

/**
 * AvoidUseInternalAnnotatedClassInPublicClassRuleTest.java class test for {@link AvoidUseInternalAnnotatedClassInPublicClassRule}
 *
 * @author avasmanas
 * @since 1.0
 *
 */
public class AvoidUseInternalAnnotatedClassInPublicClassRuleTest extends SimpleAggregatorWrapper {

		@Override
		public String getRuleName() {
			return "AvoidUseInternalAnnotatedClassInPublicClassRule";
		}
		
}
