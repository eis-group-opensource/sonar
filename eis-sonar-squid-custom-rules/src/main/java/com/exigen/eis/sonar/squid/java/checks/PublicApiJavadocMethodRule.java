package com.exigen.eis.sonar.squid.java.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TypeTree;

import com.exigen.eis.sonar.squid.java.Constant.Constants;
import com.exigen.eis.sonar.squid.java.Constant.ExceptionsLists;
import com.exigen.eis.sonar.squid.java.helpers.Javadoc;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author tgriusys
 *
 */

@Rule(key = "PublicApiJavadocMethodRule", name = "EIS coding rules - PublicApi Javadoc Method", description = "Checks the Javadoc of a method or constructor. "
		+ "To allow documented java.lang.RuntimeExceptions that are not declared, set property allowUndeclaredRTE to true. "
		+ "<p>"
		+ "Error messages about parameters and type parameters for which no param tags are present can be suppressed by defining property allowMissingParamTags. "
		+ "Error messages about exceptions which are declared to be thrown, but for which no throws tag is present can be suppressed by defining property allowMissingThrowsTags. "
		+ "Error messages about methods which return non-void but for which no return tag is present can be suppressed by defining property allowMissingReturnTag."
		+ "</p>" + "<p>" + "Javadoc is not required on a method that is tagged with the @Override annotation. "
		+ "It is recommended using a single {@inheritDoc} tag instead of all the other tags." + "</p>" + "<p>"
		+ "Note that only inheritable items will allow the {@inheritDoc} tag to be used in place of comments. "
		+ "Static methods at all visibilities, private non-static methods and constructors are not inheritable.", priority = Priority.MAJOR)
public class PublicApiJavadocMethodRule extends IssuableSubscriptionVisitor {

	@RuleProperty(key = "allowMissingParamTags", description = "whether to ignore errors when a method has parameters but does not have matching param tags in the javadoc. Default is false.", type = "BOOLEAN", defaultValue = "false")
	private boolean allowMissingParamTags;

	@RuleProperty(key = "allowMissingPropertyJavadoc", description = "Whether to allow missing Javadoc on accessor methods for properties (setters and getters)."
			+ "The setter and getter methods must match exactly the structures below. <code> public void"
			+ "setNumber(final int number) { mNumber number; } public int getNumber() { return mNumber; }"
			+ "public boolean isSomething() { return false; } </code>. Default is false.", type = "BOOLEAN", defaultValue = "false")
	private boolean allowMissingPropertyJavadoc;

	@RuleProperty(key = "allowThrowsTagsForSubclasses", description = "whether to allow documented exceptions that are subclass of one of declared exception. Default is false.", type = "BOOLEAN", defaultValue = "false")
	private boolean allowThrowsTagsForSubclasses;

	@RuleProperty(key = "allowUndeclaredRTE", description = "whether to allow documented exceptions that are not declared if they are a subclass of java.lang.RuntimeException. Default is false.", type = "BOOLEAN", defaultValue = "false")
	private boolean allowUndeclaredRTE;

	@RuleProperty(key = "allowMissingReturnTag", description = "whether to ignore errors when a method returns non-void type does have a return tag in the javadoc. Default is false.", type = "BOOLEAN", defaultValue = "false")
	private boolean allowMissingReturnTag;

	@RuleProperty(key = "suppressLoadErrors", description = "When set to false all problems with loading classes would be reported as violations. Default is true.", type = "BOOLEAN", defaultValue = "true")
	private boolean suppressLoadErrors;

	@RuleProperty(key = "allowMissingThrowsTags", description = "whether to ignore errors when a method declares that it throws exceptions but does have matching throws tags in the javadoc. Default is false.", type = "BOOLEAN", defaultValue = "false")
	private boolean allowMissingThrowsTags;

	@RuleProperty(key = "checkOnlyInterfaces", description = "whether to check only interface definitions. Class, enumeration and annotation definitions will be skiped. Default is false", type = "BOOLEAN", defaultValue = "false")
	private boolean checkOnlyInterfaces;

	@RuleProperty(key = "tokens", description = "definitions to check (Set of comma delimited values)", type = "STRING", defaultValue = "METHOD, NEW_CLASS, CONSTRUCTOR")
	private String token;

	private ArrayList<String> tokensToCheck = new ArrayList<>();

	@RuleProperty(key = "scope", description = "visibility scope where Javadoc comments are checked (Set of comma delimited values)", type = "STRING", defaultValue = "private, public, protected, default, no_access_modifier")
	private String scope;

	private ArrayList<String> scopesToCheck = new ArrayList<>();

	@RuleProperty(key = "excludeScope", description = "visibility scope where Javadoc comments are not checked (Set of comma delimited values)", type = "STRING", defaultValue = "")
	private String excludeScope;

	private ArrayList<String> scopesToExclude = new ArrayList<>();

	private String javadocContent;

	private Javadoc javadoc;

	private ArrayList<String> documentedExceptions = new ArrayList<String>();

	private ArrayList<TypeTree> declaredExceptionsAsTypeTree = new ArrayList<>();

	private ArrayList<String> declaredExceptionsAsText = new ArrayList<>();

	private static final String THROWS_TAG = "@throws";

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.METHOD, Tree.Kind.CONSTRUCTOR);
	}

	@Override
	public void visitNode(Tree tree) {

		try {
			if ((tree.parent().is(Tree.Kind.CLASS) || tree.parent().is(Tree.Kind.INTERFACE))
					&& classContainsPublicApiAnnotations(tree.parent())) {

				getJavadocContent(tree);

				getDocumentedExceptions(tree);

				getDeclaredExceptions(tree);

				getScopes();

				getExcludeScopes();

				getTokens();

				MethodTree methodTree = (MethodTree) tree;

				ArrayList<String> modifiers = new ArrayList<>();
				// getting modifiers (string) in arrayList so it would be easier to check if it
				// contains specific strings
				for (int i = 0; i < methodTree.modifiers().size(); i++) {
					modifiers.add(methodTree.modifiers().get(i).firstToken().text());
				}
				if (!modifiers.contains("public") && !modifiers.contains("private") && !modifiers.contains("protected")
						&& !modifiers.contains("default")) {
					modifiers.add("no_access_modifier");
				}

				// checking if there is scopes to exclude
				if (!scopesToExclude.isEmpty()) {
					// going through allowed scopes
					for (int i = 0; i < modifiers.size(); i++) {
						if (scopesToCheck.contains(modifiers.get(i)) && !scopesToExclude.contains(modifiers.get(i))
								&& tokensToCheck.contains(tree.kind().name())) {
							if (checkOnlyInterfaces) {
								if (methodTree.parent().is(Tree.Kind.INTERFACE)) {
									reportHandler(tree);
								} else {
									return;
								}
							} else {
								reportHandler(tree);
							}
						}
					}
				}
				if (javadocContent.contains("@inheritDoc") && (!modifiers.contains("private")
						|| !modifiers.contains("static") || !tree.is(Tree.Kind.CONSTRUCTOR))) {
					return;
				}
				// if no scopes to exclude exists
				else {
					// going through allowed scopes
					for (int i = 0; i < modifiers.size(); i++) {
						if (scopesToCheck.contains(modifiers.get(i)) && tokensToCheck.contains(tree.kind().name())) {
							if (checkOnlyInterfaces) {

								if (methodTree.parent().is(Tree.Kind.INTERFACE)) {
									reportHandler(tree);
								} else {
									return;
								}

							} else {
								reportHandler(tree);
							}
						}
					}
				}
			} else {
				return;
			}

		} catch (

		Exception e) {
			reportLoadErrors(tree);
		}
	}

	private boolean classContainsPublicApiAnnotations(Tree tree) {
		ClassTree classTree = (ClassTree) tree;
		return classTree.symbol().metadata().isAnnotatedWith(Constants.CAN_INVOKE_ANNOTATION_PATH)
				|| classTree.symbol().metadata().isAnnotatedWith(Constants.CAN_EXTEND_ANNOTATION_PATH)
				|| classTree.symbol().metadata().isAnnotatedWith(Constants.INTERNAL_ANNOTATION_PATH)
				|| classTree.symbol().metadata().isAnnotatedWith(Constants.SPI_ANNOTATION_PATH);
	}

	private void getJavadocContent(Tree tree) {
		javadoc = new Javadoc(tree);
		javadocContent = "";
		if (javadoc.javadocLines.size() > 0) {
			javadocContent = ",," + String.join(",,", javadoc.javadocLines);
		}
	}

	private void reportHandler(Tree tree) {
		if (!allowMissingPropertyJavadoc) {
			if (isMethodSetterOrGetter(tree) && javadocContent.isEmpty()
					&& !((MethodTree) tree).symbol().metadata().isAnnotatedWith(Constants.OVERRIDE_ANNOTATION_PATH)) {
				reportIssue(((MethodTree) tree).simpleName(), "Setters and getter are not allowed to miss javadoc");
				return;
			}
		} else {
			if (isMethodSetterOrGetter(tree) && javadocContent.isEmpty()) {
				return;
			}
		}
		// if javadoc is empty and does not have @Override annotation rule should report
		// it as issue
		if (javadocContent.isEmpty()
				&& !((MethodTree) tree).symbol().metadata().isAnnotatedWith(Constants.OVERRIDE_ANNOTATION_PATH)) {
			reportIssue(((MethodTree) tree).simpleName(), "Javadoc expected");
			return;
		}
		// if javadoc is empty and does have @Override annotation rule should not report
		// issues on this method
		if (javadocContent.isEmpty()
				&& ((MethodTree) tree).symbol().metadata().isAnnotatedWith(Constants.OVERRIDE_ANNOTATION_PATH)) {
			return;
		}
		if (!allowUndeclaredRTE) {
			reportUndeclaredRTE(tree);
		}
		if (!allowMissingParamTags) {
			reportMissingParamTags(tree);
		}
		if (!allowThrowsTagsForSubclasses) {
			reportThrowsTagsForSubclasses(tree);
		}
		if (!allowMissingReturnTag) {
			reportMissingReturnTag(tree);
		}
		if (!allowMissingThrowsTags) {
			reportMissingThrowsTags(tree);
		}
	}

	private void getScopes() {
		if (scope != null && scope.contains(",")) {
			String[] scopes = scope.split(",");

			for (int i = 0; i < scopes.length; i++) {
				scopesToCheck.add(scopes[i].trim().toLowerCase());
			}
		} else if (scope != null && scope.trim().length() > 0) {
			scopesToCheck.add(scope.trim().toLowerCase());
		}
	}

	private void getExcludeScopes() {
		if (excludeScope != null && excludeScope.contains(",")) {
			String[] excludeScopes = excludeScope.split(",");
			for (int i = 0; i < excludeScopes.length; i++) {
				scopesToExclude.add(excludeScopes[i].trim().toLowerCase());
			}
		} else if (excludeScope != null && excludeScope.trim().length() > 0) {
			scopesToExclude.add(excludeScope.trim().toLowerCase());
		}

	}

	public void getTokens() {
		if (token != null && token.contains(",")) {
			String[] tokens = token.split(",");

			// for multiple tokens (comma delimited values)
			for (int i = 0; i < tokens.length; i++) {
				tokensToCheck.add(tokens[i].trim().toUpperCase());
			}
			// if only one token exists
		} else if (token != null && token.trim().length() > 0) {
			tokensToCheck.add(token.trim().toUpperCase());
		}
	}

	private void reportMissingParamTags(Tree tree) {
		if (tree.is(Tree.Kind.METHOD) && !((MethodTree) tree).parameters().isEmpty()
				&& !isParametersTagsMatchesParametersCount(((MethodTree) tree).parameters().size())) {
			addIssue(tree.firstToken().line() - 1, "Missing an @param tag.");
		}
	}

	private boolean isParametersTagsMatchesParametersCount(int ownedParametersCount) {
		Pattern pattern = Pattern.compile("@param");
		Matcher matcher = pattern.matcher(javadocContent);
		int javadocParametersCounter = 0;
		while (matcher.find()) {
			javadocParametersCounter++;
		}
		if (javadocParametersCounter == ownedParametersCount) {
			return true;
		} else {
			return false;
		}

	}

	private void reportMissingReturnTag(Tree tree) {
		if (tree.is(Tree.Kind.METHOD) && ((MethodTree) tree).symbol().returnType().toString() != "void"
				&& !isReturnTagExist()) {
			addIssue(tree.firstToken().line() - 1, "Expected an @return tag.");
		}
	}

	private boolean isReturnTagExist() {
		return javadocContent.contains("@return");
	}

	private void reportMissingThrowsTags(Tree tree) {
		if (isThrowingException(tree) && !isDeclaredThrowsDocumented(tree)) {
			addIssue(tree.firstToken().line() - 1,
					"Method declares that it throws exceptions but does not have matching throws tags in the javadoc.");
		}
	}

	private boolean isThrowingException(Tree tree) {
		return !((MethodTree) tree).throwsClauses().isEmpty();
	}

	private boolean isDeclaredThrowsDocumented(Tree tree) {
		int matchesCounter = 0;
		for (int i = 0; i < ((MethodTree) tree).throwsClauses().size(); i++) {
			if (documentedExceptions.contains(((MethodTree) tree).throwsClauses().get(i).firstToken().text())) {
				matchesCounter++;
			}
		}
		if (((MethodTree) tree).throwsClauses().size() == matchesCounter) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isMethodSetterOrGetter(Tree tree) {
		MethodTree methodTree = (MethodTree) tree;
		Symbol.MethodSymbol methodSymbol = methodTree.symbol();
		if (isGetterLike(methodSymbol) || isSetterLike(methodSymbol)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isGetterLike(Symbol.MethodSymbol methodSymbol) {
		if (!methodSymbol.parameterTypes().isEmpty() || isPrivateStaticOrAbstract(methodSymbol)) {
			return false;
		}
		String methodName = methodSymbol.name();
		if (methodName.length() > 3 && methodName.startsWith("get")) {
			return true;
		}
		if (methodName.length() > 2 && methodName.startsWith("is")) {
			return true;
		}
		return false;
	}

	private boolean isSetterLike(Symbol.MethodSymbol methodSymbol) {
		if (methodSymbol.parameterTypes().size() != 1 || isPrivateStaticOrAbstract(methodSymbol)) {

			return false;
		}
		String methodName = methodSymbol.name();
		if (methodName.length() > 3 && methodName.startsWith("set") && methodSymbol.returnType().type().isVoid()) {
			return true;
		}
		return false;
	}

	private static boolean isPrivateStaticOrAbstract(Symbol.MethodSymbol methodSymbol) {

		return methodSymbol.isPrivate() || methodSymbol.isStatic() || methodSymbol.isAbstract();
	}

	private void reportThrowsTagsForSubclasses(Tree tree) {
		for (int i = 0; i < documentedExceptions.size(); i++) {
			for (int j = 0; j < declaredExceptionsAsTypeTree.size(); j++) {
				for (int k = 0; k < ExceptionsLists.exceptionsPaths.size(); k++) {
					try {
						if (Class.forName(ExceptionsLists.exceptionsPaths.get(k) + "." + documentedExceptions.get(i))
								.getSimpleName() != "java.lang.Throwable"
								&& (Class
										.forName(ExceptionsLists.exceptionsPaths.get(k) + "."
												+ documentedExceptions.get(i))
										.getSuperclass().getName()
										.equals(declaredExceptionsAsTypeTree.get(j).symbolType().fullyQualifiedName())
										|| Class.forName(ExceptionsLists.exceptionsPaths.get(k)
												+ "." + documentedExceptions.get(i)).getSuperclass().getSuperclass()
												.getName()
												.equals(declaredExceptionsAsTypeTree.get(j).symbolType()
														.fullyQualifiedName())
										|| Class.forName(ExceptionsLists.exceptionsPaths.get(k) + "."
												+ documentedExceptions.get(i)).getSuperclass().getSuperclass()
												.getSuperclass().getName().equals(declaredExceptionsAsTypeTree.get(j)
														.symbolType().fullyQualifiedName()))) {

							addIssue(tree.firstToken().line() - 1,
									"Documented exceptions that are subclass of one of declared exceptions are not allowed");
						}

					} catch (ClassNotFoundException e) {
					}
				}
			}
		}

	}

	private void reportUndeclaredRTE(Tree tree) {
		if (isJavadocContainsExceptionDocumentation() && !isDocumentedExceptionsDeclared(tree)
				&& isFoundExceptionRuntimeExceptionSubclass(tree)) {
			addIssue(tree.firstToken().line() - 1, "Documented but undeclared RTE subclass found");
		}

	}

	private boolean isJavadocContainsExceptionDocumentation() {
		if (javadocContent.contains(THROWS_TAG)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isDocumentedExceptionsDeclared(Tree tree) {
		if (declaredExceptionsAsText.isEmpty()) {
			return false;
		} else {

			int counter = 0;

			for (int i = 0; i < documentedExceptions.size(); i++) {
				if (declaredExceptionsAsText.contains(documentedExceptions.get(i))) {
					counter++;
				}
			}

			if (counter == documentedExceptions.size()) {
				return true;
			} else {
				return false;
			}
		}
	}

	private boolean isFoundExceptionRuntimeExceptionSubclass(Tree tree) {
		for (int i = 0; i < documentedExceptions.size(); i++) {
			for (int k = 0; k < ExceptionsLists.exceptionsPaths.size(); k++) {
				try {
					if (Class.forName(ExceptionsLists.exceptionsPaths.get(k) + "." + documentedExceptions.get(i))
							.getSimpleName() != "java.lang.Throwable"
							&& (Class
									.forName(ExceptionsLists.exceptionsPaths.get(k) + "." + documentedExceptions.get(i))
									.getSuperclass().getName().equals("java.lang.RuntimeException")
									|| Class.forName(
											ExceptionsLists.exceptionsPaths.get(k) + "." + documentedExceptions.get(i))
											.getSuperclass().getSuperclass().getName()
											.equals("java.lang.RuntimeException")
									|| Class.forName(
											ExceptionsLists.exceptionsPaths.get(k) + "." + documentedExceptions.get(i))
											.getSuperclass().getSuperclass().getSuperclass().getName()
											.equals("java.lang.RuntimeException"))) {
						return true;
					}
				} catch (ClassNotFoundException e) {

				}
			}
		}
		return false;

	}

	private void getDocumentedExceptions(Tree tree) {

		documentedExceptions.clear();

		String[] javadocLines;

		javadocLines = javadocContent.split(",,");

		for (int i = 0; i < javadocLines.length; i++) {

			if (javadocLines[i].contains(THROWS_TAG)) {
				documentedExceptions.add(javadocLines[i].substring((javadocLines[i].indexOf(THROWS_TAG) + 8),
						(javadocLines[i].indexOf("Exception") + 9)));
			}
		}
	}

	private void getDeclaredExceptions(Tree tree) {

		declaredExceptionsAsText.clear();
		declaredExceptionsAsTypeTree.clear();

		if (!((MethodTree) tree).throwsClauses().isEmpty()) {
			for (int i = 0; i < ((MethodTree) tree).throwsClauses().size(); i++) {
				declaredExceptionsAsTypeTree.add(((MethodTree) tree).throwsClauses().get(i));
				declaredExceptionsAsText.add(((MethodTree) tree).throwsClauses().get(i).firstToken().text());

			}
		}
	}

	private void reportLoadErrors(Tree tree) {

		if (suppressLoadErrors) {
			reportIssue(((MethodTree) tree).simpleName(), "Load errors occured.");
		}
	}

}
