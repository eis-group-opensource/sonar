/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.publicapi.sonar;

import org.sonar.api.SonarPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * @author agukov
 */
public class PublicApiPlugin extends SonarPlugin {

    @Override
    public List getExtensions() {
        return Arrays.asList(JavaRulesDefinition.class, JavaFileCheckRegistrar.class);
    }
}
