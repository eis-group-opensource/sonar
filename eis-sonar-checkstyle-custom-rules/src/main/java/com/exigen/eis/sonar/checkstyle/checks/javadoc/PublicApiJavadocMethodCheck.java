/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.checkstyle.checks.javadoc;

import com.exigen.eis.sonar.checkstyle.util.PublicApiUtil;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocMethodCheck;

/**
 * Re-use original checkstyle JavadocMethodCheck check for EIS public API classes.
 * Only classes with CanInvoke and CanExtend are considered as Public API.
 * Additional checkOnlyInterfaces parameter is supported. 
 * 
 * @Jira EISDEV-39059, EISDEV-114321
 * @author anorkus
 * @since 1.0
 */
public class PublicApiJavadocMethodCheck extends JavadocMethodCheck {

	/** Indicates if only interfaces should be checked. **/
	private boolean checkOnlyInterfaces = false;

	/** Indicates if target class is annotated with public API annotations. */
	private boolean publicApiAnnotated = false;

	/** Indicates if target class is interface. */
	private boolean isInterface = false;

	@SuppressWarnings("deprecation")
	@Override
	public void beginTree(DetailAST rootAST) {
		if (!getFileContents().inPackageInfo()) {
			publicApiAnnotated = PublicApiUtil.isPublicApiAnnotated(rootAST);
			isInterface = PublicApiUtil.isInterface(rootAST);
		}
		super.beginTree(rootAST);
	}

	/*@Override*/
	protected boolean isMissingJavadocAllowed(final DetailAST ast) {
		if (publicApiAnnotated == false) {
			return true;
		}

		if (checkOnlyInterfaces && isInterface == false) {
			return true;
		}

		return false;
	}

	/**
	 * Sets the flag that determines that only interfaces should be checked.
	 * 
	 * @param flag <code>true</code> if only interfaces should be checked
	 */
	public void setCheckOnlyInterfaces(boolean flag) {
		this.checkOnlyInterfaces = flag;
	}
}
