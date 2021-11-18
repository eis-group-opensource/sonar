package com.exigen.eis.sonar.squid.java.checks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import com.exigen.eis.sonar.squid.java.Constant.Constants;
import com.exigen.eis.sonar.squid.java.helpers.Javadoc;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

/**
 * 
 * @author tgriusys
 *
 */

@Rule(key = "PublicApiJavadocStyleRule", name = "EIS coding rules - PublicApi Javadoc Style", description = "<html>"
		+ "Validates Javadoc comments to help ensure they are well formed. The following checks are performed:\n"
		+ "\n <ul>"
		+ "<li> Ensures the first sentence ends with proper punctuation (That is a period, question mark, or exclamation mark, by default). Javadoc automatically places the first sentence in the method summary table and index. With out proper punctuation the Javadoc may be malformed. All items eligible for the {@inheritDoc} tag are exempt from this requirement.</li> \n"
		+ "<li> Check text for Javadoc statements that do not have any description. This includes both completely empty Javadoc, and Javadoc with only tags such as @param and @return.</li>\n"
		+ "<li> Check text for incomplete HTML tags. Verifies that HTML tags have corresponding end tags and issues an \"Unclosed HTML tag found:\" error if not. An \"Extra HTML tag found:\" error is issued if an end tag is found without a previous open tag.</li>\n"
		+ "<li> Check that a package Javadoc comment is well-formed (as described above) and NOT missing from any package-info.java files.</li>\n"
		+ "<li> Check for allowed HTML tags. The list of allowed HTML tags is \"a\", \"abbr\", \"acronym\", \"address\", \"area\", \"b\", \"bdo\", \"big\", \"blockquote\", \"br\", \"caption\", \"cite\", \"code\", \"colgroup\", \"del\", \"div\", \"dfn\", \"dl\", \"em\", \"fieldset\", \"h1\" to \"h6\", \"hr\", \"i\", \"img\", \"ins\", \"kbd\", \"li\", \"ol\", \"p\", \"pre\", \"q\", \"samp\", \"small\", \"span\", \"strong\", \"sub\", \"sup\", \"table\", \"tbody\", \"td\", \"tfoot\", \"th\", \"thread\", \"tr\", \"tt\", \"ul\" </li>"
		+ "</ul>", priority = Priority.MAJOR)
public class PublicApiJavadocStyleRule extends IssuableSubscriptionVisitor {

	@RuleProperty(key = "checkOnlyInterfaces", description = "whether to check only interface definitions. Class, enumeration and annotation definitions will be skiped. Default is false", type = "BOOLEAN", defaultValue = "false")
	private boolean checkOnlyInterfaces;

	@RuleProperty(key = "checkFirstSentence", description = "Whether to check the first sentence for proper end of sentence. Default is true.", type = "BOOLEAN", defaultValue = "true")
	private boolean checkFirstSentence;

	@RuleProperty(key = "tokens", description = "definitions to check (Set of comma delimited values)", type = "STRING", defaultValue = "METHOD, VARIABLE, INTERFACE, CLASS, ANNOTATION_TYPE, NEW_CLASS, ENUM, PACKAGE")
	private String token;

	private ArrayList<String> tokensToCheck = new ArrayList<>();

	@RuleProperty(key = "scope", description = "visibility scope where Javadoc comments are checked (Set of comma delimited values)", type = "STRING", defaultValue = "private, public, protected, no_access_modifier, default")
	private String scope;

	private ArrayList<String> scopesToCheck = new ArrayList<>();

	@RuleProperty(key = "excludeScope", description = "visibility scope where Javadoc comments are not checked (Set of comma delimited values)", type = "STRING", defaultValue = "")
	private String excludeScope;

	private ArrayList<String> scopesToExclude = new ArrayList<>();

	@RuleProperty(key = "checkEmptyJavadoc", description = "Whether to check if the Javadoc is missing a describing text. Default is false.", type = "BOOLEAN", defaultValue = "false")
	private boolean checkEmptyJavadoc;

	@RuleProperty(key = "checkHtml", description = "Whether to check for incomplete html tags. Default is true.", type = "BOOLEAN", defaultValue = "true")
	private boolean checkHtml;

	private static final ArrayList<String> ALLOWED_HTML_TAGS = new ArrayList<String>(
			Arrays.asList("a", "abbr", "acronym", "address", "area", "b", "bdo", "big", "blockquote", "br", "caption",
					"cite", "code", "colgroup", "del", "div", "dfn", "dl", "em", "fieldset", "h1", "h2", "h3", "h4",
					"h5", "h6", "hr", "i", "img", "ins", "kbd", "li", "ol", "p", "pre", "q", "samp", "small", "span",
					"strong", "sub", "sup", "table", "tbody", "td", "tfoot", "th", "thread", "tr", "tt", "ul", "html"));

	private static final ArrayList<String> SINGLETON_HTML_TAGS = new ArrayList<>(
			Arrays.asList("area", "br", "hr", "img", "li"));

	private ArrayList<String> badHtmlTags = new ArrayList<>();

	Set<File> directoriesWithoutPackageFile = Sets.newHashSet();

	private Javadoc javadoc;

	private String javadocContent;

	private EnumSet<Tree.Kind> nodesToVisit;

	private boolean isDescriptionExists = true;

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.METHOD, Tree.Kind.CLASS, Tree.Kind.INTERFACE, Tree.Kind.ENUM,
				Tree.Kind.ANNOTATION_TYPE, Tree.Kind.VARIABLE, Tree.Kind.NEW_CLASS, Tree.Kind.PACKAGE,
				Tree.Kind.CONSTRUCTOR);
	}

	@Override
	public void visitNode(Tree tree) {
		if (((tree.is(Tree.Kind.CLASS) || tree.is(Tree.Kind.INTERFACE) || tree.is(Tree.Kind.ANNOTATION_TYPE)
				|| tree.is(Tree.Kind.ENUM)) && classContainsPublicApiAnnotations(tree))
				|| (tree.parent() != null && tree.parent().is(Tree.Kind.CLASS)
						&& classContainsPublicApiAnnotations(tree.parent()))
				|| (tree.parent() != null && tree.parent().is(Tree.Kind.INTERFACE)
						&& classContainsPublicApiAnnotations(tree.parent()))) {

			getScopes();

			getExcludeScopes();

			getTokens();

			getJavadocContent(tree);

			ArrayList<String> modifiers = new ArrayList<>();

			// getting modifiers (string) in arrayList so it would be easier to check if it
			// contains specific strings
			if (tree.is(Tree.Kind.ANNOTATION_TYPE) || tree.is(Tree.Kind.CLASS) || tree.is(Tree.Kind.ENUM)
					|| tree.is(Tree.Kind.INTERFACE)) {
				ClassTree classTree = (ClassTree) tree;
				for (int i = 0; i < classTree.modifiers().size(); i++) {
					modifiers.add(classTree.modifiers().get(i).firstToken().text());
				}
			} else if (tree.is(Tree.Kind.CONSTRUCTOR) || tree.is(Tree.Kind.METHOD)) {
				MethodTree methodTree = (MethodTree) tree;
				for (int i = 0; i < methodTree.modifiers().size(); i++) {
					modifiers.add(methodTree.modifiers().get(i).firstToken().text());
				}
			} else if (tree.is(Tree.Kind.VARIABLE)) {
				VariableTree variableTree = (VariableTree) tree;
				for (int i = 0; i < variableTree.modifiers().size(); i++) {
					modifiers.add(variableTree.modifiers().get(i).firstToken().text());
				}
			} else if (tree.is(Tree.Kind.NEW_CLASS)) {
				NewClassTree newClassTree = (NewClassTree) tree;
				modifiers.add("public"); // TODO: check it
			}
			// package kind is not checked because it does not have any modifiers

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
							if ((tree.is(Tree.Kind.INTERFACE) || (tree.is(Tree.Kind.METHOD)
									&& ((MethodTree) tree).parent().is(Tree.Kind.INTERFACE)))) {
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

			// if no scopes to exclude exists
			else {
				// going through allowed scopes
				for (int i = 0; i < modifiers.size(); i++) {
					if (scopesToCheck.contains(modifiers.get(i)) && tokensToCheck.contains(tree.kind().name())) {
						if (checkOnlyInterfaces) {
							if ((tree.is(Tree.Kind.INTERFACE) || (tree.is(Tree.Kind.METHOD)
									&& ((MethodTree) tree).parent().is(Tree.Kind.INTERFACE)))) {
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
	}

	public boolean classContainsPublicApiAnnotations(Tree tree) {
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

	public void reportHandler(Tree tree) {

		if (javadocContent.length() > 0) {

			// Javadoc description
			if (checkEmptyJavadoc) {
				reportEmptyDescription(tree);
			}

			// First sentence check
			if (checkFirstSentence && javadocContent.length() > 2 && isDescriptionExists) {
				reportFirstSentence(tree);
			}

			// HTML check
			if (checkHtml) {

				// Incomplete HTML tags
				reportIncompleteHtmlTags(tree);

				// Allowed HTML check
				reportNotAllowedHtmlTags(tree);
			}

			// package-info.java
			reportPackageInfo(tree);
		}
	}

	public void getScopes() {
		if (scope != null && scope.contains(",")) {
			String[] scopes = scope.split(",");

			for (int i = 0; i < scopes.length; i++) {
				scopesToCheck.add(scopes[i].trim().toLowerCase());
			}
		} else if (scope != null && scope.trim().length() > 0) {
			scopesToCheck.add(scope.trim().toLowerCase());
		}
	}

	public void getExcludeScopes() {
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

	public boolean firstSentenceEndsProperly() {
		String firstSentence = javadocContent.substring(2).split(",,")[0];
		if (!firstSentence.endsWith(".") || !firstSentence.endsWith("!") || !firstSentence.endsWith("?")) {
			firstSentence = javadocContent.substring(2).split(",,,,")[0];
			return firstSentence.endsWith(".") || firstSentence.endsWith("!") || firstSentence.endsWith("?");
		} else {
			return true;
		}
	}

	public boolean isDescriptionEmpty() {
		return javadoc.noMainDescription();
	}

	public boolean isReturnDescriptionEmpty() {
		return javadoc.noReturnDescription();
	}

	public boolean isParametersDescriptionEmpty() {
		if (javadoc.undocumentedParameters().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public boolean containsExtraHtmlTag() {
		return !javadocContent.contains("<html>") && javadocContent.contains("</html>");
	}

	public boolean isUsingAllowedHtmlTags() {
		for (int i = 0; i < javadocContent.length(); i++) {
			if ((javadocContent.charAt(i) == '<' && javadocContent.charAt(i + 1) == '/')
					|| javadocContent.charAt(i) == '<') {
				for (int j = i; j < javadocContent.length(); j++) {
					if (javadocContent.charAt(j) == '>' || javadocContent.charAt(j) == ' ') {
						String htmlTag;
						if (javadocContent.charAt(i + 1) != '/') {
							htmlTag = javadocContent.substring(i + 1, j);
						} else {
							htmlTag = javadocContent.substring(i + 2, j);
						}
						if (!ALLOWED_HTML_TAGS.contains(htmlTag)) {
							badHtmlTags.add(htmlTag);
							break;
						}
						htmlTag = "";
						break;
					}
				}
			}
		}
		if (badHtmlTags.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public void reportFirstSentence(Tree tree) {
		if (!javadocContent.contains("{@inheritDoc}") && !firstSentenceEndsProperly()) {
			addIssue(tree.firstToken().line() - 1, "First sentence of Javadoc is missing an ending period.");
		}
	}

	public void reportEmptyDescription(Tree tree) {
		if (isDescriptionEmpty()) {
			isDescriptionExists = false;
			addIssue(tree.firstToken().line() - 1, "Javadoc has empty description section");
		}
		if (tree.is(Tree.Kind.METHOD) && ((MethodTree) tree).symbol().returnType().toString() != "void"
				&& isReturnDescriptionEmpty()) {
			addIssue(tree.firstToken().line() - 1, "Javadoc has empty return description");
		}
		if (tree.is(Tree.Kind.METHOD) && !((MethodTree) tree).parameters().isEmpty()
				&& isParametersDescriptionEmpty()) {
			addIssue(tree.firstToken().line() - 1, "Javadoc has empty parameters description");
		}
	}

	public void reportNotAllowedHtmlTags(Tree tree) {
		if (!isUsingAllowedHtmlTags()) {
			addIssue(tree.firstToken().line() - 1, "Unknown tags: " + String.join(", ", badHtmlTags));
			badHtmlTags.clear();
		}
	}

	public void reportIncompleteHtmlTags(Tree tree) {
		for (int i = 0; i < ALLOWED_HTML_TAGS.size(); i++) {

			int beginCount = 0;
			int endCount = 0;
			Pattern beginPattern = Pattern.compile("<" + ALLOWED_HTML_TAGS.get(i) + " ");
			Pattern alternativeBeginPattern = Pattern.compile("<" + ALLOWED_HTML_TAGS.get(i) + ">");
			Pattern endPattern = Pattern.compile("</" + ALLOWED_HTML_TAGS.get(i) + ">");
			Matcher beginMatcher = beginPattern.matcher(javadocContent);
			Matcher alternativeBeginMatcher = alternativeBeginPattern.matcher(javadocContent);
			Matcher endMatcher = endPattern.matcher(javadocContent);

			while (beginMatcher.find()) {
				beginCount++;
			}
			while (alternativeBeginMatcher.find()) {
				beginCount++;
			}
			while (endMatcher.find()) {
				endCount++;
			}
			if (beginCount > endCount && !SINGLETON_HTML_TAGS.contains(ALLOWED_HTML_TAGS.get(i))) {
				addIssue(tree.firstToken().line() - 1, "Unclosed HTML tag found: <" + ALLOWED_HTML_TAGS.get(i) + ">");
			} else if (beginCount < endCount && !SINGLETON_HTML_TAGS.contains(ALLOWED_HTML_TAGS.get(i))) {
				addIssue(tree.firstToken().line() - 1, "Extra HTML tag found: </" + ALLOWED_HTML_TAGS.get(i) + ">");
			}
		}
	}

	public void reportPackageInfo(Tree tree) {

	}
}