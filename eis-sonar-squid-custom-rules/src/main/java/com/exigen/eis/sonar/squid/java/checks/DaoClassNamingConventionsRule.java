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
@Rule(key = "DaoClassNamingConventionsRule", name = "EIS coding rules - DAO Class Naming Convention Check", description = "DAO implementation or DAO child class name should end up with Dao/DAO.", priority = Priority.INFO)
public class DaoClassNamingConventionsRule extends IssuableSubscriptionVisitor {

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.CLASS, Tree.Kind.INTERFACE);
	}

	@Override
	public void visitNode(Tree tree) {
		if (tree.is(Tree.Kind.CLASS)) {
			ClassTree classTree = (ClassTree) tree;
			if (!isClassNameEndsWithDao(classTree)) {
				if (isSuperClassContainsDao(classTree)) {
					reportIssue(classTree.simpleName(),
							"DAO implementation or DAO child class name should end up with Dao/DAO. PS superclass: "
									+ classTree.symbol().superClass().name() + " className: " + classTree.simpleName());
				} else if (!classTree.symbol().interfaces().isEmpty()) {
					for (int i = 0; i < classTree.symbol().interfaces().size(); i++) {
						if (isInterfaceContainsDao(classTree.symbol().interfaces().get(i).name())) {
							reportIssue(classTree.simpleName(),
									"DAO implementation or DAO child class name should end up with Dao/DAO. PS interface");
						}
					}
				} else {
					return;
				}
			} else {
				return;
			}
		}
	}

	public boolean isInterfaceContainsDao(String interfaceName) {
		return interfaceName.contains(Constants.DAO_CLASS_POSTFIX_CAMEL_CASE)
				|| interfaceName.contains(Constants.DAO_CLASS_POSTFIX_UPPER_CASE);
	}

	public boolean isSuperClassContainsDao(ClassTree tree) {
		if (tree.symbol().superClass() != null) {
			return tree.symbol().superClass().name().contains(Constants.DAO_CLASS_POSTFIX_CAMEL_CASE)
					|| tree.symbol().superClass().name().contains(Constants.DAO_CLASS_POSTFIX_UPPER_CASE);
		} else {
			return false;
		}
	}

	public boolean isClassNameEndsWithDao(ClassTree tree) {
		if (tree.simpleName() != null) {
			return tree.simpleName().name().endsWith(Constants.DAO_CLASS_POSTFIX_CAMEL_CASE)
					|| tree.simpleName().name().endsWith(Constants.DAO_CLASS_POSTFIX_UPPER_CASE);
		} else {
			return false;
		}
		/*
		 * for (int i = 0; i < tree.simpleName().name().length() - 2; i++) { try { if
		 * ((tree.symbol().owner().name().charAt(i) == 'D' &&
		 * tree.symbol().owner().name().charAt(i + 1) == 'A' &&
		 * tree.simpleName().name().charAt(i + 2) == 'O') ||
		 * (tree.symbol().owner().name().charAt(i) == 'D' &&
		 * tree.symbol().owner().name().charAt(i + 1) == 'a' &&
		 * tree.symbol().owner().name().charAt(i + 2) == 'o')) { return true;
		 * 
		 * } } catch (Exception e) { } } return false;
		 */
	}
}