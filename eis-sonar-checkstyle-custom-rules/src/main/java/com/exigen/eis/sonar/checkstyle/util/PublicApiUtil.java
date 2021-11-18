/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.checkstyle.util;

import com.puppycrawl.tools.checkstyle.utils.AnnotationUtility;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Utility class for Public API related custom checks.
 *  
 * @author anorkus
 * @since 1.0
 */
public class PublicApiUtil {

	public static final String CAN_INVOCE_ANNOTATION = "CanInvoke";
	public static final String CAN_EXTEND_ANNOTATION = "CanExtend";

	/**
	 * Determines if class is EIS Public API class or not.
	 * 
	 * @param rootAST root Abstract Syntax Tree element.
	 * @return return {@code true} if class is contains public API annotations, {@code false} otherwise.
	 */
	public static boolean isPublicApiAnnotated(DetailAST rootAST) {
		DetailAST ast = rootAST;

		while (ast != null && ast.getType() != TokenTypes.CLASS_DEF && ast.getType() != TokenTypes.INTERFACE_DEF
				&& ast.getType() != TokenTypes.ENUM_DEF && ast.getType() != TokenTypes.ANNOTATION_DEF) {
			ast = ast.getNextSibling();
		}
		
		return containsAnnotation(ast, CAN_INVOCE_ANNOTATION) || containsAnnotation(ast, CAN_EXTEND_ANNOTATION);

	}
	
	/**
	 * Determines if class is interface.
	 * 
	 * @param rootAST root Abstract Syntax Tree element.
	 * @return return {@code true} if class is interface, {@code false} otherwise.
	 */
	public static boolean isInterface(DetailAST rootAST) {
		DetailAST ast = rootAST;

		while (ast != null && ast.getType() != TokenTypes.CLASS_DEF && ast.getType() != TokenTypes.INTERFACE_DEF
				&& ast.getType() != TokenTypes.ENUM_DEF && ast.getType() != TokenTypes.ANNOTATION_DEF) {
			ast = ast.getNextSibling();
		}
		
		if (ast != null && ast.getType() == TokenTypes.INTERFACE_DEF) {
			return true;
		}
		
		return false;

	}
	
	private static boolean containsAnnotation(final DetailAST ast, String annotation) {
		return getAnnotation(ast, annotation) != null;
	}
	
	/**
	 * Checks to see if the AST is annotated with the passed in annotation and
	 * return the AST representing that annotation.
	 * 
	 * Original {@link AnnotationUtility#getAnnotation(DetailAST, String)}
	 * method in some cases 'AT' element misinterpreted with other (e.g.
	 * javadoc) element.
	 * 
	 * @param ast the current node
	 * @param annotation the annotation name to check for
	 * @return the AST representing that annotation
	 * @see {@link AnnotationUtility#getAnnotation(DetailAST, String)}
	 */
	private static DetailAST getAnnotation(final DetailAST ast, String annotation) {
		if (ast == null) {
			throw new NullPointerException("the ast is null");
		}

		if (annotation == null) {
			throw new NullPointerException("the annotation is null");
		}

		if (annotation.trim().length() == 0) {
			throw new IllegalArgumentException("the annotation is empty or spaces");
		}

		final DetailAST holder = AnnotationUtility.getAnnotationHolder(ast);

		for (DetailAST child = holder.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ANNOTATION) {
				final DetailAST at = child.findFirstToken(TokenTypes.AT);
				final String name = FullIdent.createFullIdent(at.getNextSibling()).getText();
				if (annotation.equals(name)) {
					return child;
				}
			}
		}

		return null;
	}
}
