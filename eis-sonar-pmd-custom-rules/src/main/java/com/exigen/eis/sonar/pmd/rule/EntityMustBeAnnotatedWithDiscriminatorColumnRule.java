/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.pmd.rule;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTExtendsList;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaTypeNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * EntityMustBeAnnotatedWithDiscriminatorColumnRule add violation if class annotated with @Entity and @CanExtend is not annotated with @DiscriminatorColumn
 *
 * @author avasmanas
 * @since 1.0
 *
 */
public class EntityMustBeAnnotatedWithDiscriminatorColumnRule extends AbstractJavaRule {
	
	private static final String ENTITY_NAME = "Entity";
	private static final String CAN_EXTEND_NAME = "CanExtend";
	private static final String DISCRIMINTATOR_COLUMN_NAME = "DiscriminatorColumn";


	public Object visit(ASTCompilationUnit node, Object data) {
		List<String> annotationNamesHolder = createAnnotationNameHolder();
		List<ASTTypeDeclaration> astTypeDeclaration = node.findChildrenOfType(ASTTypeDeclaration.class);
		if (astTypeDeclaration.isEmpty()) {
			return data;
		}
		ASTTypeDeclaration declaration = astTypeDeclaration.get(0);

		List<ASTAnnotation> annotations = declaration.findChildrenOfType(ASTAnnotation.class);
		for (ASTAnnotation astAnnotation : annotations) {
			AbstractJavaTypeNode image = (AbstractJavaTypeNode)astAnnotation.jjtGetChild(0);
			ASTName name = (ASTName) image.jjtGetChild(0);
			removeAnnotation(name, annotationNamesHolder);
			if (annotationNamesHolder.isEmpty()) {
				return data;
			}
		}

		if (annotationNamesHolder.size() != 0 && !annotationNamesHolder.contains(ENTITY_NAME) && !annotationNamesHolder.contains(CAN_EXTEND_NAME)) {
			List<ASTClassOrInterfaceDeclaration> astClassOrInterfaceDeclaration = declaration.findChildrenOfType(ASTClassOrInterfaceDeclaration.class);

			ASTClassOrInterfaceDeclaration extend = astClassOrInterfaceDeclaration.get(0);
			String currentClassName = extend.getImage();
			List<ASTExtendsList> extendsList = extend.findChildrenOfType(ASTExtendsList.class);
			
			if (extendsList.isEmpty()) {
				addViolationWithMessage(data, node, currentClassName +" class annotated with @Entity and @CanExtend must have @DiscriminatorColumn annotation");
				return data;
			}
			ASTExtendsList extendsValue = extendsList.get(0);
			ASTClassOrInterfaceType classOrInterfaceType = (ASTClassOrInterfaceType) extendsValue.jjtGetChild(0);
			
			Class<?> extendClass = classOrInterfaceType.getType();
			if (extendClass == null) {
				return data;
			}
			while ("java.lang.Object".equalsIgnoreCase(extendClass.getCanonicalName()) == false) {
				if (containsDiscriminatorColumnAnnotation(extendClass)) {
					return data;
				}
				extendClass = extendClass.getSuperclass();
			} 
			addViolationWithMessage(data, node, currentClassName +" class annotated with @Entity and @CanExtend parent class "+ classOrInterfaceType.getImage() + " must be annotated with @DiscriminatorColumn");
		}

		return data;
	}

	private boolean containsDiscriminatorColumnAnnotation(Class<?> extendClass) {
		Annotation[] annotations = extendClass.getDeclaredAnnotations();
		List<Annotation> annotationsList = Arrays.asList(annotations);
		for (Annotation annotation : annotationsList) {
			if (annotation.annotationType().getName().endsWith(DISCRIMINTATOR_COLUMN_NAME)) {
				return true;
			}
		}

		return false;
	}

	private void removeAnnotation(ASTName name, List<String> annotationNamesHolder) {
		if (name != null && annotationNamesHolder.contains(name.getImage())) {
			annotationNamesHolder.remove(name.getImage());
		}
	}
	
	private List<String> createAnnotationNameHolder(){
		List<String> annotationNamesHolder = new ArrayList<String>();
		annotationNamesHolder.add(ENTITY_NAME);
		annotationNamesHolder.add(CAN_EXTEND_NAME);
		annotationNamesHolder.add(DISCRIMINTATOR_COLUMN_NAME);
		return annotationNamesHolder;
	}

}
