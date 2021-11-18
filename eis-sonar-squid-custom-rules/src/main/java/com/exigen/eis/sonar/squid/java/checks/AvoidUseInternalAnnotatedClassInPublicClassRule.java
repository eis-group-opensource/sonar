package com.exigen.eis.sonar.squid.java.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import com.exigen.eis.sonar.squid.java.Constant.Constants;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author tgriusys
 *
 */
@Rule(key = "AvoidUseInternalAnnotatedClassInPublicClassRule", name = "EIS coding rules - Avoid use internal annotated class in public class", description = "Checks if class annotated with @CanInvoke or @CanExtend does not have method which uses @Internal as return/argument type.", priority = Priority.MAJOR)
public class AvoidUseInternalAnnotatedClassInPublicClassRule extends IssuableSubscriptionVisitor {

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.METHOD, Tree.Kind.METHOD_INVOCATION, Tree.Kind.CLASS);
	}

	@Override
	public void visitNode(Tree tree) {
		if (tree.is(Tree.Kind.METHOD) && tree.parent().is(Tree.Kind.CLASS)) {
			MethodTree methodTree = ((MethodTree) tree);
			if (classContainsCanInvokeOrCanExtend(methodTree)) {
				// Check if method return type class is not primitive (String, void, ...) and
				// has @Internal annotation.
				if (!isReturnWrapperType(methodTree) && returnClassContainsInternalAnnotation(methodTree)) {
					reportIssue(methodTree.returnType(),
							methodTree.symbol().returnType().enclosingClass().owner().name() + "."
									+ methodTree.symbol().returnType().enclosingClass().name()
									+ " class must be annotated @CanInvoke or must be standart java class");
				}
				// Check if method argument class is not primitive (String, int, ...) and has
				// @Internal annotation.
				if (!methodTree.parameters().isEmpty()) {
					for (int i = 0; i < methodTree.parameters().size(); i++) {
						if (!isParameterWrapperType(methodTree, i)
								&& parameterClassContainsInternalAnnotation(methodTree, i)) {
							reportIssue(methodTree.parameters().get(i),
									methodTree.parameters().get(i).type().symbolType().fullyQualifiedName()
											+ " class must be annotated @CanInvoke or must be standart java class");
						}
					}

				}
			}
		}

	}

	public boolean classContainsCanInvokeOrCanExtend(MethodTree tree) {
		return tree.symbol().enclosingClass().metadata().isAnnotatedWith(Constants.CAN_INVOKE_ANNOTATION_PATH)
				|| tree.symbol().enclosingClass().metadata().isAnnotatedWith(Constants.CAN_EXTEND_ANNOTATION_PATH);
	}

	public boolean returnClassContainsInternalAnnotation(MethodTree tree) {
		return tree.symbol().returnType().enclosingClass().metadata()
				.isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH);
	}

	public boolean parameterClassContainsInternalAnnotation(MethodTree tree, int i) {
		return tree.parameters().get(i).type().symbolType().symbol().metadata()
				.isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH);
	}

	private static final ArrayList<String> WRAPPER_TYPES = new ArrayList(Arrays.asList("Boolean", "Character", "Byte",
			"Short", "Integer", "Long", "Float", "Double", "void", "String", "int"));

	public static boolean isParameterWrapperType(MethodTree tree, int i) {
		return WRAPPER_TYPES.contains(tree.parameters().get(i).type().symbolType().name());
	}

	public static boolean isReturnWrapperType(MethodTree tree) {
		return WRAPPER_TYPES.contains(tree.symbol().returnType().enclosingClass().name());
	}
}