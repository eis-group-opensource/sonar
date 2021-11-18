/* Copyright © 2018 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.publicapi.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Symbol.TypeSymbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import com.exigen.publicapi.utils.Constants;
import com.google.common.collect.ImmutableList;

@Rule(key = "AvoidUseInternalAnnotatedClassInPublicClassRule", name = "EIS Coding Rules (Base) - Illegal usage of @Internal annotated class as a method argument or return type in PublicAPI classes", description = "Checks if class annotated with @CanInvoke or @CanExtend does not have method which uses @Internal as return/argument type.", priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("10min")
/**
 * 
 * @author tgriusys
 *
 */
public class AvoidUseInternalAnnotatedClassInPublicClassRule extends IssuableSubscriptionVisitor {

	private static final ArrayList<String> WRAPPER_TYPES = new ArrayList<String>(Arrays.asList("Boolean", "Character",
			"Byte", "Short", "Integer", "Long", "Float", "Double", "void", "String", "int"));

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.METHOD);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visitNode(Tree tree) {

		MethodTree methodTree = ((MethodTree) tree);
		Symbol methodOwnerSymbol = methodTree.symbol().owner();

		if (classContainsCanInvokeOrCanExtend(methodOwnerSymbol)) {

			// Check if method return type class is not primitive (String, void, ...) and
			// has @Internal annotation.
			reportMethodReturnClassContainingInternalAnnotation(methodTree);

			// Check if method argument class is not wrapper type and has
			// @Internal annotation.
			reportMethodParametersClassContainingInternalAnnotation(methodTree);
		}

	}

	private boolean classContainsCanInvokeOrCanExtend(Symbol classSymbol) {
		return classSymbol.metadata().isAnnotatedWith(Constants.CAN_INVOKE_ANNOTATION_PATH)
				|| classSymbol.metadata().isAnnotatedWith(Constants.CAN_EXTEND_ANNOTATION_PATH);
	}

	private void reportMethodReturnClassContainingInternalAnnotation(MethodTree methodTree) {
		TypeSymbol methodReturnTypeSymbol = methodTree.symbol().returnType();
		Type methodReturnType = methodTree.returnType().symbolType();
		// for array type parameters
		if (methodReturnType.isArray()) {
			Type.ArrayType arrayType = (Type.ArrayType) methodReturnType;
			Type arrayElementType = arrayType.elementType();
			if (!isElementWrapperType(arrayElementType) && elementClassContainsInternalAnnotation(arrayElementType)) {
				reportIssue(methodTree.returnType(), arrayElementType.fullyQualifiedName()
						+ " class must be annotated @CanInvoke or must be standart java class");
			}

		} else if (!isReturnWrapperType(methodReturnTypeSymbol)
				&& returnClassContainsInternalAnnotation(methodReturnTypeSymbol)) {
			reportIssue(methodTree.returnType(),
					methodReturnTypeSymbol.enclosingClass().owner().name() + "."
							+ methodTree.symbol().returnType().enclosingClass().name()
							+ " class must be annotated @CanInvoke or must be standart java class");
		}

	}

	private static boolean isReturnWrapperType(TypeSymbol methodReturnType) {
		return WRAPPER_TYPES.contains(methodReturnType.enclosingClass().name());
	}

	private boolean returnClassContainsInternalAnnotation(TypeSymbol methodReturnTypeSymbol) {
		return methodReturnTypeSymbol.enclosingClass().metadata().isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH);
	}

	private void reportMethodParametersClassContainingInternalAnnotation(MethodTree methodTree) {
		if (!methodTree.parameters().isEmpty()) {
			for (int i = 0; i < methodTree.parameters().size(); i++) {
				Type parameterType = methodTree.parameters().get(i).type().symbolType();
				// for array type parameters
				if (parameterType.isArray()) {
					Type.ArrayType arrayType = (Type.ArrayType) parameterType;
					Type arrayElementType = arrayType.elementType();
					if (!isElementWrapperType(arrayElementType)
							&& elementClassContainsInternalAnnotation(arrayElementType)) {
						reportIssue(methodTree.parameters().get(i), arrayElementType.fullyQualifiedName()
								+ " class must be annotated @CanInvoke or must be standart java class");
					}
				} else if (!isElementWrapperType(parameterType)
						&& elementClassContainsInternalAnnotation(parameterType)) {
					reportIssue(methodTree.parameters().get(i), parameterType.fullyQualifiedName()
							+ " class must be annotated @CanInvoke or must be standart java class");
				}
			}
		}
	}

	private static boolean isElementWrapperType(Type parameterType) {
		return WRAPPER_TYPES.contains(parameterType.name());
	}

	private boolean elementClassContainsInternalAnnotation(Type type) {
		return type.symbol().metadata().isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH);
	}

}