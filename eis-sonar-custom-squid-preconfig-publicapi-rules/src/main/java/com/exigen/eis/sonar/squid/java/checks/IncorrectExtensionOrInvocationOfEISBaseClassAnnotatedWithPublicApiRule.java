package com.exigen.eis.sonar.squid.java.checks;

import java.util.List;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

import com.exigen.eis.sonar.squid.java.Constant.Constants;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author tgriusys
 *
 */
@Rule(key = "IncorrectExtensionOrInvocationOfEISBaseClassAnnotatedWithPublicApiRule", name = "EIS Coding Rules (Preconfig, Project) - PublicAPI violation is identified - incorrect extension or invocation of EIS base class annotated with <PublicAPI>", description = "<p>"
		+ "Preconfig and Project code should not contain:" + "</p>"
		+ "*Inheritance or implementation by Preconfig and Project code of EIS Base @Internal classes. <br/>"
		+ "*Method calls of @Internal EIS Base Classes annotated classes. <br/>"
		+ "*Inheritance or implementation of @CanInvoke annotated classes.   <br/>", priority = Priority.MAJOR)
public class IncorrectExtensionOrInvocationOfEISBaseClassAnnotatedWithPublicApiRule
		extends IssuableSubscriptionVisitor {

	private static final String INTERNAL_ANNOTATION_ISSUE_REPORT = " should not be used because it is annotated with @Internal.";
	private static final String CAN_INVOKE_ANNOTATION_ISSUE_REPORT = " should not be used because it is annotated with @CanInvoke.";

	ClassTree classTree;
	MethodInvocationTree methodInvocationTree;

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.CLASS, Tree.Kind.INTERFACE, Tree.Kind.METHOD_INVOCATION);
	}

	@Override
	public void visitNode(Tree tree) {
		if (tree.is(Tree.Kind.CLASS) || tree.is(Tree.Kind.INTERFACE)) {
			classTree = (ClassTree) tree;

			reportInheritanceOrImplementationOfInternalAnnotatedClasses();

			reportInheritanceOrImplementationOfCanInvokeAnnotatedClasses();

		} else if (tree.is(Tree.Kind.METHOD_INVOCATION)) {

			methodInvocationTree = (MethodInvocationTree) tree;

			reportMethodCallsOfInternalAnnotatedClasses();
		}
	}

	private void reportInheritanceOrImplementationOfInternalAnnotatedClasses() {

		if (!classTree.symbol().interfaces().isEmpty()) {
			for (int i = 0; i < classTree.symbol().interfaces().size(); i++) {
				Type interfaceType = classTree.symbol().interfaces().get(i);
				if (isInterfaceContainsInternalAnnotation(interfaceType)) {

					reportIssue(classTree, interfaceType.fullyQualifiedName() + INTERNAL_ANNOTATION_ISSUE_REPORT);
				}
			}
		}

		if (isSuperClassContainsInternalAnnotation()) {

			reportIssue(classTree.firstToken(),
					classTree.symbol().superClass().fullyQualifiedName() + INTERNAL_ANNOTATION_ISSUE_REPORT);
		}
	}

	private boolean isInterfaceContainsInternalAnnotation(Type interfaceType) {

		return interfaceType.symbol().metadata().isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH);
	}

	private boolean isSuperClassContainsInternalAnnotation() {
		if (classTree.symbol().superClass() != null) {
			return classTree.symbol().superClass().symbol().metadata()
					.isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH);
		} else {
			return false;
		}
	}

	private void reportInheritanceOrImplementationOfCanInvokeAnnotatedClasses() {
		if (!classTree.symbol().interfaces().isEmpty()) {
			for (int i = 0; i < classTree.symbol().interfaces().size(); i++) {
				Type interfaceType = classTree.symbol().interfaces().get(i);
				if (isInterfaceContainsCanInvokeAnnotation(interfaceType)
						&& !isInterfaceContainsCanExtendAnnotation(interfaceType)) {

					addIssue(classTree.firstToken().line(),
							interfaceType.fullyQualifiedName() + CAN_INVOKE_ANNOTATION_ISSUE_REPORT);
				}
			}
		}
		if (isSuperClassContainsCanInvokeAnnotation() && !isSuperClassContainsCanExtendAnnotation()) {
			addIssue(classTree.firstToken().line(),
					classTree.symbol().superClass().fullyQualifiedName() + CAN_INVOKE_ANNOTATION_ISSUE_REPORT);
		}
	}

	private boolean isInterfaceContainsCanInvokeAnnotation(Type interfaceType) {
		return interfaceType.symbol().metadata().isAnnotatedWith(Constants.CAN_INVOKE_ANNOTATION_PATH);
	}

	private boolean isInterfaceContainsCanExtendAnnotation(Type interfaceType) {
		return interfaceType.symbol().metadata().isAnnotatedWith(Constants.CAN_EXTEND_ANNOTATION_PATH);
	}

	private boolean isSuperClassContainsCanInvokeAnnotation() {
		if (classTree.symbol().superClass() != null) {
			return classTree.symbol().superClass().symbol().metadata()
					.isAnnotatedWith(Constants.CAN_INVOKE_ANNOTATION_PATH);
		} else {
			return false;
		}
	}

	private boolean isSuperClassContainsCanExtendAnnotation() {
		if (classTree.symbol().superClass() != null) {
			return classTree.symbol().superClass().symbol().metadata()
					.isAnnotatedWith(Constants.CAN_EXTEND_ANNOTATION_PATH);
		} else {
			return false;
		}
	}

	private void reportMethodCallsOfInternalAnnotatedClasses() {
		if (isMethodInvocationObjectClassAnnotatedWithInternalAnnotation()) {

			addIssue(methodInvocationTree.firstToken().line(),
					"Method calls of @Internal annotated EIS Base classes are not allowed.");
		}
	}

	private boolean isMethodInvocationObjectClassAnnotatedWithInternalAnnotation() {
		return methodInvocationTree.symbol().owner().metadata().isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH);
	}
}