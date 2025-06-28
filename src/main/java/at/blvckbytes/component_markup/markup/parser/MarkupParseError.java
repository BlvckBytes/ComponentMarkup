package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.ErrorMessage;

public enum MarkupParseError implements ErrorMessage {
  UNKNOWN_TAG("This tag does not exist"),
  UNKNOWN_ATTRIBUTE("This attribute is not a member of its tag"),
  UNKNOWN_STRUCTURAL_ATTRIBUTE("This structural attribute does not exist"),
  UNNAMED_LET_BINDING("Let-bindings require a variable-name: let-<variable_name>"),
  UNNAMED_FOR_LOOP("Fot-loops require an iteration-variable name: *for-<iteration_name>"),
  BINDING_IN_USE("The name of this binding is already in use"),
  MALFORMED_IDENTIFIER("This name is malformed: cannot start with digits or underscores, may only contain a-z, 0-9 and underscores"),
  UNBALANCED_ATTRIBUTE_BRACKETS("The brackets used to enable binding on this attribute are unbalanced: missing closing-bracket"),
  UNBALANCED_CLOSING_TAG("There was no tag with this name that could be closed"),
  NON_STRING_STRUCTURAL_ATTRIBUTE("Structural attributes may only be strings, containing expressions"),
  NON_STRING_EXPRESSION_ATTRIBUTE("Attributes bound to expressions may only be strings, containing expressions"),
  NON_STRING_LET_ATTRIBUTE("Let-bindings may only be strings, containing expressions"),
  EXPECTED_SUBTREE_VALUE("This attribute expected a markup-value: { ... }"),
  EXPECTED_SCALAR_VALUE("This attribute expected a scalar-value: string, boolean or number"),
  EXPECTED_SELF_CLOSING_TAG("This tag is self-closing, <name />, and does not support a separate closing-tag"),
  EXPECTED_OPEN_CLOSE_TAG("This tag requires a separate closing-tag as it expects content, and does not support self-closing <name />"),
  EXPECTED_STRUCTURAL_ATTRIBUTE_FLAG("This structural attribute must not be assigned to a value, as it's just a flag (delete: name=...)"),
  MULTIPLE_NON_MULTI_ATTRIBUTE("This attribute does not support being specified more than once"),
  MULTIPLE_CONDITIONS("There has already been a prior condition on this tag, and conditions may only occur once"),
  MULTIPLE_LOOPS("There has already been a prior loop on this tag, and loops may only occur once"),
  MISSING_PRECEDING_IF_SIBLING("Cannot use branching (else-if, else) without having specified a preceding if-sibling"),
  MISSING_ATTRIBUTE_VALUE("This attribute requires a value: name=..."),
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
