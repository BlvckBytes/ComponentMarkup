package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.ErrorMessage;

public enum MarkupParseError implements ErrorMessage {
  UNKNOWN_TAG("The tag %s does not exist"),
  UNKNOWN_ATTRIBUTE("The tag %s does not have %s as an attribute"),
  MISSING_MANDATORY_ATTRIBUTES("The tag %s misses these mandatory attribute(s): %s"),
  UNKNOWN_STRUCTURAL_ATTRIBUTE("The structural attribute %s does not exist"),
  UNNAMED_LET_BINDING("Let-bindings require a variable-name: let-<variable_name>"),
  UNNAMED_FOR_LOOP("For-loops with a dash require an iteration-variable name: *for-<iteration_variable_name>"),
  BINDING_IN_USE("The name %s is already used in another binding"),
  MALFORMED_IDENTIFIER("The name %s is malformed: cannot start with digits or underscores, may only contain a-z, 0-9 and underscores"),
  UNBALANCED_ATTRIBUTE_BRACKETS("The brackets used to enable binding on this attribute are unbalanced: missing closing-bracket"),
  UNBALANCED_CLOSING_TAG("There was no still-opened tag with the name %s that could be closed"),
  UNBALANCED_CLOSING_TAG_BLANK("There was no still-opened tag that could be closed"),
  NON_STRING_STRUCTURAL_ATTRIBUTE("Structural attributes may only be strings, containing expressions"),
  NON_STRING_EXPRESSION_ATTRIBUTE("Attributes bound to expressions may only be strings, containing expressions"),
  NON_STRING_LET_ATTRIBUTE("Let-bindings may only be strings, containing expressions"),
  EXPECTED_MARKUP_VALUE("The attribute %s expected a markup-value: { ... }"),
  EXPECTED_SCALAR_VALUE("The attribute %s expected a scalar-value: string, boolean or number"),
  EXPECTED_SELF_CLOSING_TAG("This tag is self-closing, <%s />, and does not support a separate closing-tag"),
  EXPECTED_OPEN_CLOSE_TAG("This tag requires a separate closing-tag </%1$s>, as it expects content and does not support self-closing <%1$s />"),
  EXPECTED_STRUCTURAL_ATTRIBUTE_FLAG("The structural attribute %1$s must not be assigned to a value, as it's just a flag: %1$s=... -> %1$s"),
  MULTIPLE_NON_MULTI_ATTRIBUTE("The attribute %s does not support being specified more than once"),
  MULTIPLE_IF_ELSE_CONDITIONS("There has already been a prior *if/*else-if/*else on this tag, and said conditions may only occur once"),
  MULTIPLE_USE_CONDITIONS("There has already been a prior *use on this tag, and said condition may only occur once"),
  MULTIPLE_LOOPS("There has already been a prior loop on this tag, and loops may only occur once"),
  MISSING_PRECEDING_IF_SIBLING("Cannot use branching (else-if, else) without having specified a preceding if-sibling"),
  SPREAD_DISALLOWED_ON_NON_EXPRESSION("The spread-operator ... is only allowed on expression-attributes: [%s]=\"<expression>\""),
  XML_PARSE_ERROR("Encountered malformed XML"),
  EXPRESSION_PARSE_ERROR("Encountered a malformed expression"),
  EXPRESSION_TOKENIZE_ERROR("Encountered a malformed expression"),
  ;

  private final String message;

  MarkupParseError(String message) {
    this.message = message;
  }

  @Override
  public String getErrorMessage() {
    return this.message;
  }
}
