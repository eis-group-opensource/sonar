/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
/**
 * 
 */
package com.exigen.eis.sonar.pmd.rule;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTResultType;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaTypeNode;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * AvoidUseInternalAnnotatedClassInPublicClassRule.java violation rule if class annotated with CanExtend or CanInvoke method input, output types must be not annotated with internal annotation. 
 *
 * @author avasmanas
 * @since 1.0
 *
 */
public class AvoidUseInternalAnnotatedClassInPublicClassRule extends AbstractJavaRule {

	private static final String INTERNAL_ANNOTATION_NAME = "Internal";
	private static final String CAN_EXTEND_ANNOTATION_NAME = "CanExtend";
	private static final String CAN_INVOKE_ANNOTATION_NAME = "CanInvoke";

	@Override
	public Object visit(ASTTypeDeclaration node, Object data) {
		List<ASTAnnotation> astAnnotations = node.findChildrenOfType(ASTAnnotation.class);

		for (ASTAnnotation astAnnotation : astAnnotations) {
			AbstractJavaTypeNode image = (AbstractJavaTypeNode) astAnnotation.jjtGetChild(0);
			ASTName name = (ASTName) image.jjtGetChild(0);
			if (CAN_EXTEND_ANNOTATION_NAME.equals(name.getImage()) || CAN_INVOKE_ANNOTATION_NAME.equals(name.getImage())) {
				return super.visit(node, data);
			}
		}
		
		return data;
	}

	@Override
	public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
		if (node.isInterface()) {
			return data;
		}

		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTMethodDeclaration method, Object data) {
		List<ASTResultType> resultTypes = method.findDescendantsOfType(ASTResultType.class);
		if (resultTypes.isEmpty() == false) {
			ASTResultType astResultType = resultTypes.get(0);
			int childrenNumber = astResultType.jjtGetNumChildren();
			if (childrenNumber > 0) {
				ASTType returnType = (ASTType) resultTypes.get(0).jjtGetChild(0);
				Class<?> returnClass = returnType.getType();
				if (returnClass == null){
					return data;
				}

				if (isClassAnnotated(returnClass, INTERNAL_ANNOTATION_NAME)) {
					addViolationWithMessage(data, method, returnClass.getCanonicalName() + " class must be annotated @CanInvoke or must be standart java class");
					return data;
				}
				
			}
		}

		List<ASTFormalParameter> fields = method.findDescendantsOfType(ASTFormalParameter.class);
		if (fields.isEmpty() == false) {
			for (ASTFormalParameter astFormalParameter : fields) {
				List<ASTType> fieldType = astFormalParameter.findChildrenOfType(ASTType.class);
				Class<?> type = fieldType.get(0).getType();
				if (type == null){
					return data;
				}
				
				if (isClassAnnotated(type, INTERNAL_ANNOTATION_NAME)) {
					addViolationWithMessage(data, method, type.getCanonicalName() + " class must be annotated @CanInvoke or must be standart java class");
					return data;
				}
			}

		}

		return visit(((JavaNode) (method)), data);
	}

	private boolean isClassAnnotated(Class<?> type, String annotationName) {
		Annotation[] annotations = type.getDeclaredAnnotations();
		List<Annotation> annotationsList = Arrays.asList(annotations);
		for (Annotation annotation : annotationsList) {
			if (annotation.annotationType().getName().endsWith(annotationName)) {
				return true;
			}
		}

		return false;
	}

}
