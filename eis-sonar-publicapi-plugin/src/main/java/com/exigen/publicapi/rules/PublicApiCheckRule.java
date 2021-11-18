/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.publicapi.rules;

import java.util.List;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.SymbolMetadata.AnnotationInstance;
import org.sonar.plugins.java.api.tree.Arguments;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import com.exigen.publicapi.utils.PublicApiUtils;
import com.google.common.collect.ImmutableList;

/**
 * @author agukov
 * @author tgriusys
 */

@Rule(key = "EIS coding rules - <PublicAPI> rule violation.", name = "EIS Coding rules - PublicAPI violation is identified - incorrect extension or invocation of EIS base class annotated with <PublicAPI>", description = "Each subsystem should be able to call other subsystems Public API methods only marked as @CanInvoke and extend other subsystem classes marked as @CanExtend. ", priority = Priority.MAJOR)

@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("10min")

public class PublicApiCheckRule extends IssuableSubscriptionVisitor {

	private String contextPackage;

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.IMPORT, Tree.Kind.CLASS, Tree.Kind.METHOD_INVOCATION);

	}

	@Override
	public void visitNode(Tree tree) {
		if (!context.getFile().getPath().contains("generated")) {
			if (tree instanceof MethodInvocationTree) {
				visitMethodInvocation((MethodInvocationTree) tree);
				visitMethodArguments((MethodInvocationTree) tree);
			} else if (tree instanceof ClassTree) {
				visitClass((ClassTree) tree);
			} else if (tree instanceof ImportTree) {
				visitImport((ImportTree) tree);
			}
		}
	}

	private void visitImport(ImportTree tree) {
		// TODO Auto-generated method stub VariableSymbol

	}

	private void visitClass(ClassTree tree) {

		this.contextPackage = PublicApiUtils.getContextPackage(context);

		if (tree.superClass() != null) {
			String superClassPackage = PublicApiUtils.getSuperClassPackage(tree);

			if (PublicApiUtils.publicAPIruleApplies(contextPackage, superClassPackage)) {
				List<AnnotationInstance> superAnnotationList = PublicApiUtils.getAnnotations(tree.superClass());

				if (!PublicApiUtils.canBeExtended(superAnnotationList)) {
					String supperClassName = PublicApiUtils.getSuperClassName(tree);

					reportIssue(tree, "Trying to extend class " + superClassPackage + "." + supperClassName
							+ " which is from different subsystem and doesnt have @CanExtend annotation");
				}
			}
		}
	}

	private void visitMethodInvocation(MethodInvocationTree tree) {
		if (tree.symbol().name() != null && !tree.symbol().name().equals("<init>")) {

			String methodPackage = PublicApiUtils.getMethodPackage(tree);

			if (PublicApiUtils.publicAPIruleApplies(contextPackage, methodPackage)) {
				if (!PublicApiUtils.isMethodFromSupperClass(context, tree)) {
					List<AnnotationInstance> methodAnnotations = PublicApiUtils.getAnnotations(tree);

					if (methodAnnotations == null || !PublicApiUtils.canBeInvoked(methodAnnotations)) {
						addIssue(tree.arguments().closeParenToken().line(),
								"trying to invoke method " + methodPackage + "."
										+ PublicApiUtils.getMethodClassName(tree) + "$" + tree.symbol().name()
										+ " which is from different subsystem and doesnt have @CanInvoke annotation");
					}
				}
			}
		}
	}

	private void visitMethodArguments(MethodInvocationTree tree) {
		if (tree.symbol().name() != null && !tree.symbol().name().equals("<init>")) {
			Arguments arguments = tree.arguments();

			for (ExpressionTree arg : arguments) {
				if (arg instanceof MemberSelectExpressionTree) { // if variable, else method
					ExpressionTree expressionTree = ((MemberSelectExpressionTree) arg).expression();

					if (expressionTree instanceof IdentifierTree) {
						String variablePackage = ((IdentifierTree) expressionTree).symbolType().symbol().owner().name();

						if (PublicApiUtils.publicAPIruleApplies(contextPackage, variablePackage)) {
							List<AnnotationInstance> methodAnnotations = ((IdentifierTree) expressionTree).symbolType()
									.symbol().owner().metadata().annotations();

                            if (methodAnnotations == null || !PublicApiUtils.canBeInvokedByArgument(expressionTree)) {
								if (!((MemberSelectExpressionTree) arg).identifier().symbol().name()
										.equals("!unknownSymbol!")) {
									addIssue(tree.arguments().closeParenToken().line(),
											"trying to use internal variable "
													+ ((MemberSelectExpressionTree) arg).identifier().symbol().name()
													+ " from " + variablePackage + "."
													+ ((IdentifierTree) expressionTree).symbolType().symbol().name());
								}
							}
						}
					}
				}
			}
		}

	}

}
