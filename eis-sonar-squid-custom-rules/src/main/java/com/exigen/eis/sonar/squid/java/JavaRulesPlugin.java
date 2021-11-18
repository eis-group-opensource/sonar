package com.exigen.eis.sonar.squid.java;

import org.sonar.api.Plugin;

/**
 * Entry point of your plugin containing your custom rules
 * 
 * @author tgriusys
 */
public class JavaRulesPlugin implements Plugin {

	@Override
	public void define(Context context) {

		// server extensions -> objects are instantiated during server startup
		context.addExtension(JavaRulesDefinition.class);

		// batch extensions -> objects are instantiated during code analysis
		context.addExtension(JavaFileCheckRegistrar.class);

	}

}
