package com.exigen.eis.sonar.squid.java.checks;

import java.util.List;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Tree;

import com.exigen.eis.sonar.squid.java.Constant.Constants;
import com.google.common.collect.ImmutableList;

@Rule(key = "EntityMustBeAnnotatedWithDiscriminatorColumnRule", name = "EIS coding rules - Entity must be annotated with discriminator column", description = "Class annotated with @Entity and @CanExtend must be annotated with @DiscriminatorColumn. It does not need @DiscriminatorColumn if any of class parents has it", priority = Priority.MAJOR)
public class EntityMustBeAnnotatedWithDiscriminatorColumnRule extends IssuableSubscriptionVisitor {

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.CLASS);
	}

	@Override
	public void visitNode(Tree tree) {
		if (tree.is(Tree.Kind.CLASS) && containsEntityAndCanExtend((ClassTree) tree)) {
			ClassTree classTree = (ClassTree) tree;
			if (classTree.symbol().superClass() != null && !classContainsDiscriminatorColumn(classTree)) {
				if (classTree.symbol().superClass().name().equals("Object")) {
					reportIssue(classTree.simpleName(), classTree.symbol().name()
							+ " class annotated with @Entity and @CanExtend must have @DiscriminatorColumn annotation");
				} else if (!parentClassContainsDiscriminatorColumnAnnotation(classTree)) {
					reportIssue(classTree.superClass(),
							classTree.symbol().name() + " class annotated with @Entity and @CanExtend parent class "
									+ classTree.symbol().superClass().name()
									+ " must be annotated with @DiscriminatorColumn");
				}
			}
		}
	}

	private boolean containsEntityAndCanExtend(ClassTree tree) {
		return tree.symbol().metadata().isAnnotatedWith(Constants.ENTITY_ANNOTATION_PATH)
				&& tree.symbol().metadata().isAnnotatedWith(Constants.CAN_EXTEND_ANNOTATION_PATH);
	}

	private boolean classContainsDiscriminatorColumn(ClassTree tree) {
		if (tree.symbol().metadata().isAnnotatedWith(Constants.DISCRIMINATOR_COLUMN_ANNOTATION_PATH)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean parentClassContainsDiscriminatorColumnAnnotation(ClassTree tree) {
		Type highestSuperClass = tree.symbol().superClass();
		// going through all superclasses searching for @DiscriminatorAnnotation
		while (!highestSuperClass.symbol().name().equals("Object")) {
			if (highestSuperClass.symbol().metadata().isAnnotatedWith(Constants.DISCRIMINATOR_COLUMN_ANNOTATION_PATH)) {
				return true;
			} else {
				highestSuperClass = highestSuperClass.symbol().superClass();
			}
		}
		return false;
	}
}