package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.ErrorMessage;

public enum MarkupParseError implements ErrorMessage {
  UNKNOWN_TAG("The tag %s does not exist"),
  UNSUPPORTED_ATTRIBUTE("The tag %s does not support %s as an attribute (at least in this current constellation)"),
  MISSING_MANDATORY_ATTRIBUTE("The tag %s misses the mandatory attribute: %s"),
  UNKNOWN_INTRINSIC_ATTRIBUTE("The intrinsic attribute %s does not exist"),
  UNNAMED_LET_BINDING("Let-bindings require a variable-name: let-<variable_name>"),
  BRACKETED_INTRINSIC_ATTRIBUTE("Do not use [brackets] on intrinsic attributes; either use * as a prefix for expressions or + for literals"),
  UNNAMED_FOR_LOOP("For-loops with a dash require an iteration-variable name: *for-<iteration_variable_name>"),
  EMPTY_EXPRESSION("Expressions cannot be empty"),
  LITERAL_INTRINSIC_MARKUP_ATTRIBUTE("Intrinsic attribute with markup-values { ... } always use the * prefix"),
  AUXILIARY_FOR_INTRINSIC_ATTRIBUTE("The intrinsic attribute %s may only be used after a *for loop"),
  BINDING_IN_USE("The name %s is already used in another binding"),
  EMPTY_BINDING("Let-bindings always require a value to be bound"),
  NON_MARKUP_OR_EXPRESSION_CAPTURE("The let-binding %s is capturing and can thus only be assigned with a markup-value { ... } or an expression"),
  EMPTY_ATTRIBUTE_NAME("This attribute-name does not contain anything but operators"),
  MALFORMED_ATTRIBUTE_NAME("The attribute-name %s is malformed: cannot start with digits or hyphens, may only contain a-z, 0-9 and hyphens"),
  MALFORMED_IDENTIFIER("The variable-name %s is malformed: cannot start with digits or underscores, may only contain a-z, 0-9 and underscores"),
  UNBALANCED_ATTRIBUTE_BRACKETS("The brackets used to enable binding on this attribute are unbalanced: missing opening- or closing-bracket"),
  UNBALANCED_CAPTURE_PARENTHESES("The parentheses used to enable capturing on this binding are unbalanced: missing opening- or closing-parentheses"),
  MULTIPLE_ATTRIBUTE_BRACKETS("The brackets used to enable binding on this attribute may only be used once!"),
  MULTIPLE_ATTRIBUTE_SPREADS("The three dots used to enable spreading on this attribute may only be used once!"),
  MULTIPLE_CAPTURE_PARENTHESES("The parentheses used to enable capturing on this binding may only be used once!"),
  MALFORMED_SPREAD_OPERATOR("Use three leading dots to denote the spread-operator: ..."),
  UNBALANCED_CLOSING_TAG("There was no still-opened tag with the name %s that could be closed"),
  UNBALANCED_CLOSING_TAG_BLANK("There was no still-opened tag that could be closed"),
  IS_CASE_OUTSIDE_OF_WHEN_PARENT("An +is case may only occur within a parent containing the *when expression"),
  OTHER_CASE_OUTSIDE_OF_WHEN_PARENT("An *other case may only occur within a parent containing the *when expression"),
  WHEN_MATCHING_COLLIDING_CASES("Only specify one of either +is or *other per member of *when"),
  WHEN_MATCHING_DISALLOWED_MEMBER("Within *when matching, only +is or *other members and nested *when containers are allowed; static content without such attributes is also disallowed"),
  WHEN_MATCHING_DUPLICATE_INPUT("There can only be one *when input"),
  WHEN_MATCHING_DUPLICATE_CASE("The case %s occurred more than once"),
  WHEN_MATCHING_DUPLICATE_FALLBACK("The *other case occurred more than once"),
  WHEN_MATCHING_NO_CASES("Specify at least one +is case to be used with *when"),
  NON_EXPRESSION_INTRINSIC_ATTRIBUTE("The intrinsic attribute %s only works with expressions or literals"),
  NON_LITERAL_INTRINSIC_ATTRIBUTE("The intrinsic attribute %s only works with literal values"),
  NON_STRING_EXPRESSION_ATTRIBUTE("Attributes bound to expressions may only be strings, containing expressions"),
  EXPECTED_MARKUP_ATTRIBUTE_VALUE("The attribute %s expected a markup-value: { ... }"),
  EXPECTED_EXPRESSION_ATTRIBUTE_VALUE("The attribute %s of tag %s expected a literal (string, boolean or number) or a bound expression"),
  EXPECTED_SELF_CLOSING_TAG("This tag is self-closing, <%s />, and does not support a separate closing-tag"),
  EXPECTED_OPEN_CLOSE_TAG("This tag requires a separate closing-tag </%1$s>, as it expects content and does not support self-closing <%1$s />"),
  EXPECTED_INTRINSIC_ATTRIBUTE_FLAG("The intrinsic attribute %1$s must not be assigned to a value, as it's just a flag: %1$s=... -> %1$s"),
  MULTIPLE_NON_MULTI_ATTRIBUTE("The attribute %s of tag %s does not support being specified more than once"),
  SPREAD_ON_NON_MULTI_ATTRIBUTE("The attribute %s of tag %s does not support multiple values, and thus the spread-operator"),
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
