/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.publicapi.rules;

import java.util.List;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.SymbolMetadata.AnnotationInstance;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import com.exigen.publicapi.utils.PublicApiUtils;
import com.google.common.collect.ImmutableList;

/**
 * @author tkracka
 * @author tgriusys
 */

@Rule(key = "EIS coding rules - <PublicAPI> annotations rule violation.", name = "EIS Coding rules - PublicAPI annotations rule violation", description = "PublicAPI annotation check rule. Each outer class should contain relevant annotation. If there is no annotation, or annotations contradict each other - violation is thrown.", priority = Priority.MAJOR)

@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("10min")

public class PublicApiAnnotationCheckRule extends IssuableSubscriptionVisitor {

	private String contextPackage;

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.CLASS);
	}

	@Override
	public void visitNode(Tree tree) {
		if (!context.getFile().getPath().contains("generated")) {
			if (tree instanceof ClassTree) {
				visitClass((ClassTree) tree);
			}
		}
	}

	public void visitClass(ClassTree tree) {

		if (!tree.symbol().name().isEmpty()) {
			this.contextPackage = PublicApiUtils.getContextPackage(context);
			if (PublicApiUtils.isEisClass(contextPackage)) {
				Symbol outermostClassSymbol = PublicApiUtils.getOutermostClassAsSymbol(tree.symbol());
				List<AnnotationInstance> annotationList = outermostClassSymbol.metadata().annotations();

				if (outermostClassSymbol == tree.symbol()) {
					if (!PublicApiUtils.isInternal(annotationList) && !PublicApiUtils.canBeInvoked(annotationList)) {
						reportIssue(tree.simpleName(),
								"Class " + tree.symbol().name() + " doesn't have PublicAPI annotation.");
					} else if (PublicApiUtils.isInternal(annotationList)
							&& PublicApiUtils.canBeInvoked(annotationList)) {
						reportIssue(tree.simpleName(), "Class " + tree.symbol().name()
								+ " annotated with @Internal annotation can't have @CanExtend or @CanInvoke annotation.");
					}
				}
			}
		}
	}
}
