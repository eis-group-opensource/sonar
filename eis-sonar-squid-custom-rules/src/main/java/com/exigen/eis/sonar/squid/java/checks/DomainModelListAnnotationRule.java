package com.exigen.eis.sonar.squid.java.checks;

import java.util.List;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import com.exigen.eis.sonar.squid.java.Constant.Constants;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author tgriusys
 * 
 */

@Rule(key = "DomainModelListAnnotationRule", name = "EIS coding rules - Domain model List annotation rule", description = "Check if domain model List has correct annotation set."
		+ "<p>" + "Example of correctly annotated List:" + "</p>" + "@OneToMany <br/>" + "@JoinColumn <br/>"
		+ "@IndexColumn <br/>" + "List ... <br/>" + "<br/>" + "or <br/>" + "<br/>" + "@OneToMany <br/>"
		+ "@JoinColumn <br/>" + "@OrderColumn <br/>" + "List ...<br/>" + "<br/>"
		+ "Violation registered if at least one annotation is missing.", priority = Priority.MAJOR)
public class DomainModelListAnnotationRule extends IssuableSubscriptionVisitor {

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.ANNOTATION, Tree.Kind.CLASS, Tree.Kind.VARIABLE, Tree.Kind.METHOD,
				Tree.Kind.METHOD_INVOCATION);
	}

	@Override
	public void visitNode(Tree tree) {
		if (tree.is(Tree.Kind.VARIABLE) && isClassAnnotatedWithEntity(tree) && isList((VariableTree) tree)
				&& !isListContainsRequiredAnnotations((VariableTree) tree) && isNotInMethod(tree)) {
			/*
			 * isListContainsAtLeastOneRequiredAnnotation((VariableTree) tree) &&
			 */
			reportIssue(((VariableTree) tree).simpleName(), "Domain model List field should be correctly annotated.");
		}
	}

	private boolean isList(VariableTree tree) {
		return tree.symbol().type().fullyQualifiedName().contains(Constants.LIST_VARIABLE_PATH);
	}

	private boolean isListContainsRequiredAnnotations(VariableTree tree) {
		if (tree.symbol().metadata().isAnnotatedWith(Constants.ONE_TO_MANY_ANNOTATION_PATH)
				&& tree.symbol().metadata().isAnnotatedWith(Constants.JOIN_COLUMN_ANNOTATION_PATH)
				&& (tree.symbol().metadata().isAnnotatedWith(Constants.INDEX_COLUMN_ANNOTATION_PATH)
						|| tree.symbol().metadata().isAnnotatedWith(Constants.ORDER_COLUMN_ANNOTATION_PATH))) {
			return true;
		}
		return false;

	}

	/*
	 * private boolean isListContainsAtLeastOneRequiredAnnotation(VariableTree tree)
	 * { return tree.symbol().metadata().isAnnotatedWith(Constants.
	 * ONE_TO_MANY_ANNOTATION_PATH) || JOIN_COLUMN_ANNOTATION_PATH) ||
	 * tree.symbol().metadata().isAnnotatedWith(Constants.
	 * tree.symbol().metadata().isAnnotatedWith(Constants.
	 * INDEX_COLUMN_ANNOTATION_PATH) ||
	 * tree.symbol().metadata().isAnnotatedWith(Constants.
	 * ORDER_COLUMN_ANNOTATION_PATH); }
	 */

	private boolean isClassAnnotatedWithEntity(Tree tree) {
		return ((VariableTree) tree).symbol().enclosingClass().metadata()
				.isAnnotatedWith(Constants.ENTITY_ANNOTATION_PATH);

	}

	private boolean isNotInMethod(Tree tree) {
		if (tree.parent().is(Tree.Kind.CLASS)) {
			return true;
		} else {
			return false;
		}
	}

}