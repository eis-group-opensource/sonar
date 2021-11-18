/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.pmd.rule;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * <p> Rule which check if domain model List is correctly annotated. </p>
 *  
 * <p> Example of correctly annotated List: </p>
 * 
 * {@code @OneToMany} <br/>
 * {@code @JoinColumn} <br/>
 * {@code @IndexColumn} <br/>
 * {@code List ...} <br/>
 * 
 * <br/>
 * 
 * {@code @OneToMany} <br/>
 * {@code @JoinColumn} <br/>
 * {@code @OrderColumn} <br/>
 * {@code List ...}
 * 
 * @author anorkus
 * @since 1.0
 */
public class DomainModelListAnnotationRule extends AbstractJavaRule {
	
	private static final String ENTITY_ANNOTATION_NAME = "Entity";
	private static final String ONE_TO_MANY_ANNOTATION_NAME = "OneToMany";
	private static final String JOIN_COLUMN_ANNOTATION_NAME = "JoinColumn";
	private static final String INDEX_COLUMN_ANNOTATION_NAME = "IndexColumn";
	private static final String ORDER_COLUMN_ANNOTATION_NAME = "OrderColumn";
	
	private static final String DEFAULT_VIOLATION_MESSAGE = "Domain model List field should be correctly annotated.";

    @Override
    public Object visit(ASTTypeDeclaration node, Object data) {
    	if (isClassAnnotatedWithEntity(node)) {
    		super.visit(node, data);
    	}

    	return data;
    }
    
	/** Rule ignores entity interfaces. */
	@Override
	public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
		if (node.isInterface()) {
			return data;
		}

		return super.visit(node, data);
	}
	
    @Override
    public Object visit(ASTFieldDeclaration node, Object data) {
    	if (isList(node) && !isListContainsRequiredAnnotations(node)) {
    		addViolationWithMessage(data, node, DEFAULT_VIOLATION_MESSAGE);
    	}
    	
    	return data;
    }
    
    private boolean isClassAnnotatedWithEntity(ASTTypeDeclaration node) {
    	List<ASTAnnotation> annotations = node.findChildrenOfType(ASTAnnotation.class);
    	
    	return getAnnotationNames(annotations).contains(ENTITY_ANNOTATION_NAME);

    }
    
    private boolean isList(ASTFieldDeclaration node) {
    	if (node == null || node.getType() == null) {
    		return false;
    	}
    	
    	return List.class.isAssignableFrom(node.getType());
    }
    
    private boolean isListContainsRequiredAnnotations(ASTFieldDeclaration node) {
    	ASTClassOrInterfaceBodyDeclaration nodeDeclaration = node.getFirstParentOfType(ASTClassOrInterfaceBodyDeclaration.class);
    	List<ASTAnnotation> annotations = nodeDeclaration.findChildrenOfType(ASTAnnotation.class);
    	List<String> annotationNames = getAnnotationNames(annotations);
    	
    	if (!annotationNames.contains(ONE_TO_MANY_ANNOTATION_NAME)) {
    		return false;
    	}
    	
    	if (!annotationNames.contains(JOIN_COLUMN_ANNOTATION_NAME)) {
    		return false;
    	}
    	
    	if (!(annotationNames.contains(INDEX_COLUMN_ANNOTATION_NAME) || annotationNames.contains(ORDER_COLUMN_ANNOTATION_NAME))) {
    		return false;
    	}
    	
    	return true;
    }
    
    private List<String> getAnnotationNames(List<ASTAnnotation> annotations) {
    	List<String> annotationNames = new ArrayList<String>();
    	
    	for (ASTAnnotation annotation : annotations) {
    		ASTName name = annotation.getFirstDescendantOfType(ASTName.class);
			if (name != null) {
				annotationNames.add(name.getImage());
			}
    	}
    	
    	return annotationNames;
    }
}
