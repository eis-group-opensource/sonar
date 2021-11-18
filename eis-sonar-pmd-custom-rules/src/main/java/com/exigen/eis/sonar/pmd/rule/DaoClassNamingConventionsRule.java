/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.pmd.rule;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTExtendsList;
import net.sourceforge.pmd.lang.java.ast.ASTImplementsList;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Checks if DAO implementation or DAO child is correctly named.
 * 
 * @author anorkus
 * @since 1.0
 */
public class DaoClassNamingConventionsRule extends AbstractJavaRule {
	
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
    	if (isClassNameEndsWithDao(node)) {
    		return data;
    	}
    	
    	for (int i = 0; i < node.jjtGetNumChildren(); i++) {
    		Node childNode = node.jjtGetChild(i);
    		if (childNode instanceof ASTExtendsList || childNode instanceof ASTImplementsList) {
    			for (int ii = 0; ii < childNode.jjtGetNumChildren(); ii++) {
    				Node extendsOrImplementsNode = childNode.jjtGetChild(ii);
    				if (isClassNameEndsWithDao(extendsOrImplementsNode)) {
    					addViolation(data, extendsOrImplementsNode);
    					return data;
    				}
    			}
    		}
    	}
    	
        return data;
    }
    
    private boolean isClassNameEndsWithDao(Node node) {
    	return StringUtils.endsWithIgnoreCase(node.getImage(), "dao");
    }
    
}
