/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.checkstyle.rule.javadoc;

/** 
 * Dummy interface for unit test. 
 */
@CanInvoke
@CanExtend
public interface EisPublicApiInterface_01 {

	/**
	 * Returns test text.
	 * 
	 * @param arg test argument
	 * @return test text
	 */
	public String doStuff1(String arg);
}
