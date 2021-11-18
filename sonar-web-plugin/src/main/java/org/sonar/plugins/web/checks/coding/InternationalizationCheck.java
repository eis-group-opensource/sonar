/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
/*
 * SonarQube Web Plugin
 * Copyright (C) 2010 SonarSource and Matthijs Galesloot
 * dev@sonar.codehaus.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonar.plugins.web.checks.coding;

import org.apache.commons.lang.StringUtils;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.web.checks.AbstractPageCheck;
import org.sonar.plugins.web.checks.RuleTags;
import org.sonar.plugins.web.checks.WebRule;
import org.sonar.plugins.web.node.Node;
import org.sonar.plugins.web.node.TagNode;
import org.sonar.plugins.web.node.TextNode;

import java.util.Arrays;
import java.util.List;

/**
 * In addition to default solar-web-plugin internationalization rule this
 * supports some symbol codes such as &#160;, &amp;, &nbsp;. Also
 * additional symbols such as brackets, question mark, plus sign, etc. and few
 * extended ASCII characters such as « and ». The violations are not ignored if
 * any of allowed characters are added to violating value.
 * 
 * @author enorkus
 *
 */
@Rule(
  key = "InternationalizationCheck",
  priority = Priority.MAJOR)
@WebRule(activeByDefault = false)
@RuleTags({
  RuleTags.JSP_JSF,
  RuleTags.USER_EXPERIENCE
})
public class InternationalizationCheck extends AbstractPageCheck {

  private static final String PUNCTUATIONS_AND_SPACE_STRING = " \t\n\r|-%:,.?!/,'\"/;)([]+*&#160&amp&nbsp&#x2191&#x2193&gt&#215&ltbr><";
  private static final String[] PUNCTUATIONS_AND_SPACE = { " ", "\t", "\n", "\r", "|", "-", "%", ":", ",", ".", "?", "!", "/", "'", "\"", ";", ")", "(", "[", "]", "+", "*", "&#160", "#160", "&amp",
			"&nbsp", "nbsp", "&#x2191", "&#x2193", "&gt", "&#215", "&lt", "br", "<", ">" };
  private static final String SPECIAL_CHARACTERS = "«»";
  private static final String REGEXP = ".*[a-zA-Z0-9]+.*";
  private static final String DEFAULT_ATTRIBUTES = "outputLabel.value, outputText.value";
  private static final List<String> SKIPPED_TAGS = Arrays.asList("script", "h:outputScript", "style", "e:showFields", "h:outputStylesheet", "ui:remove");

  @RuleProperty(
    key = "attributes",
    defaultValue = DEFAULT_ATTRIBUTES)
  public String attributes = DEFAULT_ATTRIBUTES;

  private QualifiedAttribute[] attributesArray;
  private TagNode element;

  @Override
  public void startDocument(List<Node> nodes) {
    this.attributesArray = parseAttributes(attributes);
  }

  @Override
  public void characters(TextNode textNode) {
    if (!isUnifiedExpression(textNode.getCode()) && !isPunctuationOrSpace(textNode.getCode()) && !SKIPPED_TAGS.contains(element.getNodeName())) {
      createViolation(textNode.getStartLinePosition(), "Define this label in the resource bundle.");
    }
  }

@Override
  public void startElement(TagNode element) {
    if (attributesArray.length > 0) {
      for (QualifiedAttribute attribute : attributesArray) {
        if (notValid(element, attribute)) {
          return;
        }
      }
    }
  }

  private boolean notValid(TagNode element, QualifiedAttribute attribute) {
	this.element = element;
    if (element.equalsElementName(attribute.getNodeName())) {
      String value = element.getAttribute(attribute.getAttributeName());
      if (value != null) {
        value = value.trim();
        if (value.length() > 0 && !isUnifiedExpression(value) && !isPunctuationOrSpace(value)) {
          createViolation(element.getStartLinePosition(), "Define this label in the resource bundle.");
          return true;
        }
      }
    }
    return false;
  }

	private static boolean isPunctuationOrSpace(String value) {
		// checking if the value contains any of allowed punctuation or spaces
		if (StringUtils.containsAny(value, PUNCTUATIONS_AND_SPACE_STRING)) {
			for (int i = 0; i < PUNCTUATIONS_AND_SPACE.length; i++) {
				// removing allowed punctuation or spaces
				if (StringUtils.contains(value, PUNCTUATIONS_AND_SPACE[i])) {
					value = StringUtils.remove(value, PUNCTUATIONS_AND_SPACE[i]);
				}
			}
		}
		if ("".equals(value)) {
			// return true if value was all punctuation or spaces
			return true;
		} else {
			return isSpecialCharacter(value);
		}
	}

	private static boolean isSpecialCharacter(String value) {
		// check if value has any special characters but no letters of alphabet and numbers
		if (StringUtils.containsAny(value, SPECIAL_CHARACTERS) && !value.matches(REGEXP)) {
			return true;
		} else {
			return false;
		}
	}
}