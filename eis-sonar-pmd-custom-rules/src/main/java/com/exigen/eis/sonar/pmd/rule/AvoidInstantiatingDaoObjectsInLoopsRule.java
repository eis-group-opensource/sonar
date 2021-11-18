/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.pmd.rule;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.lang.java.ast.ASTAllocationExpression;
import net.sourceforge.pmd.lang.java.rule.optimizations.AvoidInstantiatingObjectsInLoopsRule;

/**
 * Checks if DAO objects are instantiating in loops.
 * 
 * @author anorkus
 * @since 1.0
 */
public class AvoidInstantiatingDaoObjectsInLoopsRule extends AvoidInstantiatingObjectsInLoopsRule {
	
	@Override
    public Object visit(ASTAllocationExpression node, Object data) {
        if (containsDaoInName(node)) {
        	super.visit(node, data);
        }
        return data;
    }
    
    private boolean containsDaoInName(ASTAllocationExpression node) {
    	String className = node.jjtGetChild(0).getImage();
    	if (StringUtils.endsWithIgnoreCase(className, "dao")) {
    		return true;
    	}
    	return false;
    }
    
}
