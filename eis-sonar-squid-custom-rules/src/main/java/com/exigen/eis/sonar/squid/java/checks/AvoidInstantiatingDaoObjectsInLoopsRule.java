package com.exigen.eis.sonar.squid.java.checks;

import java.util.List;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.Tree;

import com.exigen.eis.sonar.squid.java.Constant.Constants;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author tgriusys
 *
 */
@Rule(key = "AvoidInstantiatingDaoObjectsInLoopsRule", name = "EIS coding rules - Avoid instantiating DAO objects in loops", description = "DAO object should not be instantiated in loop.", priority = Priority.MAJOR)
public class AvoidInstantiatingDaoObjectsInLoopsRule extends IssuableSubscriptionVisitor {

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.NEW_CLASS, Tree.Kind.WHILE_STATEMENT, Tree.Kind.FOR_EACH_STATEMENT,
				Tree.Kind.FOR_STATEMENT, Tree.Kind.DO_STATEMENT);
	}

	@Override
	public void visitNode(Tree tree) {
		if (methodClassContainsDaoInName(tree) && insideLoop(tree)) {
			reportIssue(tree, "DAO object should not be instantiated in loop");
		}
	}

	public boolean methodClassContainsDaoInName(Tree tree) {
		if (tree.is(Tree.Kind.NEW_CLASS)) {
			return ((NewClassTree) tree).constructorSymbol().owner().name()
					.contains(Constants.DAO_CLASS_POSTFIX_CAMEL_CASE)
					|| ((NewClassTree) tree).constructorSymbol().owner().name()
							.contains(Constants.DAO_CLASS_POSTFIX_UPPER_CASE)
					|| ((NewClassTree) tree).constructorSymbol().owner().name()
							.contains(Constants.DAO_CLASS_POSTFIX_LOWER_CASE);
		} else {
			return false;
		}
	}

	public boolean insideLoop(Tree tree) {
		Tree highestParentTree = tree.parent();
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