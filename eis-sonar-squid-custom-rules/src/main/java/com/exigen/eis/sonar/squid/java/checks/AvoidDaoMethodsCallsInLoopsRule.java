package com.exigen.eis.sonar.squid.java.checks;

import java.util.List;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.ReturnStatementTree;
import org.sonar.plugins.java.api.tree.Tree;

import com.exigen.eis.sonar.squid.java.Constant.Constants;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author tgriusys
 *
 */
@Rule(key = "AvoidDaoMethodsCallsInLoopsRule", name = "EIS coding rules - Avoid DAO methods calls in loops", description = "DAO methods should not be called inside loop", priority = Priority.MAJOR)
public class AvoidDaoMethodsCallsInLoopsRule extends IssuableSubscriptionVisitor {

	private String lastSectionFromPackage = "No package name found. Report this issue with examples";

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.CLASS, Tree.Kind.METHOD_INVOCATION, Tree.Kind.RETURN_STATEMENT);
	}

	@Override
	public void visitNode(Tree tree) {
		lastSectionFromPackage = getLastSectionOfPackage(tree, lastSectionFromPackage);

		if (!isCurrentClassDao() && methodClassContainsDaoInName(tree) && insideLoop(tree)) {
			reportIssue(tree.firstToken(), "DAO methods should not be called inside loop ");
		}

	}

	private String getLastSectionOfPackage(Tree tree, String currentClassName) {
		boolean breakScanning = false;
		Tree highestParentTree = tree.parent();
		if (!breakScanning) {
			while (highestParentTree != null) {
				// rule is checking parents until reaches package. Then rule reads package name
				// and gets last folder name, which usually is 'dao' for Dao classes.
				if (highestParentTree.is(Tree.Kind.COMPILATION_UNIT)) {
					breakScanning = true;
					return ((CompilationUnitTree) highestParentTree).packageDeclaration().packageName().lastToken()
							.text();
				} else {
					highestParentTree = highestParentTree.parent();
				}
			}
		} else if (tree.is(Tree.Kind.CLASS))

		{
			breakScanning = false;
		} else {
			return currentClassName;
		}
		return currentClassName;
	}

	/**
	 * If lastSectionFromPackage contains "dao", rule returns, that this class is
	 * dao class.
	 * 
	 * @return
	 */
	private boolean isCurrentClassDao() {
		return lastSectionFromPackage.contains(Constants.DAO_CLASS_POSTFIX_LOWER_CASE)
				|| lastSectionFromPackage.contains(Constants.DAO_CLASS_POSTFIX_CAMEL_CASE)
				|| lastSectionFromPackage.contains(Constants.DAO_CLASS_POSTFIX_UPPER_CASE);
	}

	/**
	 * 
	 * @param tree
	 * @return
	 */

	private boolean methodClassContainsDaoInName(Tree tree) {
		if (tree.is(Tree.Kind.METHOD_INVOCATION)) {

			// try to add .parent() after tree).
			return ((MethodInvocationTree) tree).symbol().owner().name()
					.contains(Constants.DAO_CLASS_POSTFIX_LOWER_CASE)
					|| ((MethodInvocationTree) tree).symbol().owner().name()
							.contains(Constants.DAO_CLASS_POSTFIX_CAMEL_CASE)
					|| ((MethodInvocationTree) tree).symbol().owner().name()
							.contains(Constants.DAO_CLASS_POSTFIX_UPPER_CASE);
		} else if (tree.is(Tree.Kind.RETURN_STATEMENT)) {
			try {
				// maybe .parent() after tree) can help? to check if method has object before it
				return ((ReturnStatementTree) tree).expression().symbolType().symbol().owner().name()
						.contains(Constants.DAO_CLASS_POSTFIX_LOWER_CASE)
						|| ((ReturnStatementTree) tree).expression().symbolType().symbol().owner().name()
								.contains(Constants.DAO_CLASS_POSTFIX_CAMEL_CASE)
						|| ((ReturnStatementTree) tree).expression().symbolType().symbol().owner().name()
								.contains(Constants.DAO_CLASS_POSTFIX_UPPER_CASE);
			} catch (Exception e) {
			}
			return false;
		} else {
			return false;
		}
	}

	private boolean insideLoop(Tree tree) {
		Tree highestParentTree = tree.parent();
		if (highestParentTree.firstToken().text().equals("(")
				&& highestParentTree.parent().parent().firstToken().text().equals("for")) {
			return false;
		}
		while (highestParentTree.parent() != null) {
			if (highestParentTree.parent().firstToken().text().equals("for")
					|| highestParentTree.parent().firstToken().text().equals("while")
					|| highestParentTree.parent().firstToken().text().equals("do")) {
				return true;
			} else {
				highestParentTree = highestParentTree.parent();
			}
		}
		return false;

	}
}