package com.exigen.eis.sonar.squid.java.checks;

import java.util.List;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Tree;

import com.exigen.eis.sonar.squid.java.Constant.Constants;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author tgriusys
 *
 */
@Rule(key = "AvoidUsePublicApiAnnotationsInPreconfigAndProjectCodeRule", name = "EIS coding rules - Don't use @PublicAPI annotations in preconfig or project code", description = "PublicApi annotations should not be used in Preconfig and Project code.", priority = Priority.MAJOR)
public class AvoidUsePublicApiAnnotationsInPreconfigAndProjectCodeRule extends IssuableSubscriptionVisitor {

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.CLASS, Tree.Kind.INTERFACE, Tree.Kind.ENUM);
	}

	@Override
	public void visitNode(Tree tree) {
		ClassTree classTree = (ClassTree) tree;
		if (classTree.symbol().metadata().isAnnotatedWith(Constants.CAN_EXTEND_ANNOTATION_PATH)
				|| classTree.symbol().metadata().isAnnotatedWith(Constants.CAN_INVOKE_ANNOTATION_PATH)
				|| classTree.symbol().metadata().isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH)
				|| classTree.symbol().metadata().isAnnotatedWith(Constants.SPI_ANNOTATION_PATH)) {
			reportIssue(classTree.simpleName(),
					"PublicApi annotations should not be used in Preconfig and Project code.");
		}
	}

}