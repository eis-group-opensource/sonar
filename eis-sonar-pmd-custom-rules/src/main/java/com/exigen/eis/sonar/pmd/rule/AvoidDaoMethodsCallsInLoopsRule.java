/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.pmd.rule;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTDoStatement;
import net.sourceforge.pmd.lang.java.ast.ASTForInit;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;
import net.sourceforge.pmd.lang.java.rule.optimizations.AbstractOptimizationRule;

/**
 * Checks for DAO method calls in loops.
 * 
 * @author anorkus
 * @since 1.0
 */
public class AvoidDaoMethodsCallsInLoopsRule extends AbstractOptimizationRule {

	private static final String DAO_CLASS_POSTFIX = "Dao";
	
	@Override
	public Object visit(ASTPrimaryExpression node, Object data) {
		ASTName nameNode = node.getFirstDescendantOfType(ASTName.class);
		
		if (methodClassContainsDaoInName(nameNode) && insideLoop(node)) {
			addViolation(data, node);
		}
		
		return data;
	}

	private boolean insideLoop(ASTPrimaryExpression node) {
		Node firstBlockNode = node.getFirstParentOfType(ASTBlock.class);
		if (firstBlockNode == null) {
			return false;
		}
		
		if (firstBlockNode.jjtGetParent() instanceof ASTStatement) {
			Node n = firstBlockNode.jjtGetParent().jjtGetParent();
			if (n != null) {
				while (n != null) {
					if (n instanceof ASTDoStatement || n instanceof ASTWhileStatement || n instanceof ASTForStatement) {
						return true;
					} else if (n instanceof ASTForInit) {
						/*
						 * init part is not technically inside the loop. Skip parent
						 * ASTForStatement but continue higher up to detect nested
						 * loops
						 */
						n = n.jjtGetParent();
					} else if (n.jjtGetParent() instanceof ASTForStatement && n.jjtGetParent().jjtGetNumChildren() > 1
							&& n == n.jjtGetParent().jjtGetChild(1)) {
						// it is the second child of a ForStatement - which means
						// we are dealing with a for-each construct
						// In that case, we can ignore this allocation expression,
						// as the second child
						// is the expression, over which to iterate.
						// Skip this parent but continue higher up
						// to detect nested loops
						n = n.jjtGetParent();
					}
					n = n.jjtGetParent();
				}
			}
		}
		return false;
	}

	private boolean methodClassContainsDaoInName(ASTName node) {
		if (node == null || node.getType() == null) {
			return false;
		}

		String className = node.getType().toString();
		return StringUtils.endsWithIgnoreCase(className, DAO_CLASS_POSTFIX);
	}
}
