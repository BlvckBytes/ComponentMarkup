/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser;

import java.util.function.Function;

public enum MarkupParseError {
  UNKNOWN_TAG(args -> "The tag " + args[0] + " does not exist"),
  UNSUPPORTED_ATTRIBUTE(args -> "The tag " + args[0] + " does not support " + args[1] + " as an attribute (at least in this current constellation)"),
  MISSING_MANDATORY_ATTRIBUTE(args -> "The tag " + args[0] + " misses the mandatory attribute: " + args[1]),
  UNKNOWN_INTRINSIC_ATTRIBUTE(args -> "The intrinsic attribute " + args[0] + " does not exist"),
  UNNAMED_LET_BINDING(args -> "Let-bindings require a variable-name: let-<variable_name>"),
  BRACKETED_INTRINSIC_ATTRIBUTE(args -> "Do not use [brackets] on intrinsic attributes; either use * as a prefix for expressions or + for literals"),
  UNNAMED_FOR_LOOP(args -> "For-loops with a dash require an iteration-variable name: *for-<iteration_variable_name>"),
  EMPTY_EXPRESSION(args -> "Expressions cannot be empty"),
  LITERAL_INTRINSIC_MARKUP_ATTRIBUTE(args -> "Intrinsic attributes with markup-values { ... } always use the * prefix"),
  LITERAL_INTRINSIC_TEMPLATE_LITERAL_ATTRIBUTE(args -> "Intrinsic attributes with template-literal `...` always use the * prefix"),
  AUXILIARY_FOR_INTRINSIC_ATTRIBUTE(args -> "The intrinsic attribute " + args[0] + " may only be used after a *for loop"),
  BINDING_IN_USE(args -> "The name " + args[0] + " is already used in another binding"),
  VALUELESS_BINDING(args -> "Let-bindings always require a value to be bound"),
  NON_MARKUP_OR_EXPRESSION_CAPTURE(args -> "The let-binding " + args[0] + " is capturing and can thus only be assigned with a markup-value { ... } or an expression"),
  EMPTY_ATTRIBUTE_NAME(args -> "This attribute-name is empty"),
  EMPTY_BINDING_NAME(args -> "This binding-name is empty"),
  MALFORMED_ATTRIBUTE_NAME(args -> "The attribute-name " + args[0] + " is malformed: cannot start with digits or hyphens, may only contain a-z, 0-9 and hyphens"),
  MALFORMED_IDENTIFIER(args -> "The variable-name " + args[0] + " is malformed: cannot start with digits or underscores, may only contain a-z, 0-9 and underscores"),
  RESERVED_IDENTIFIER(args -> "The variable-name " + args[0] + " is reserved by an operator or a literal and thus inaccessible within expressions"),
  UNBALANCED_ATTRIBUTE_BRACKETS(args -> "The brackets used to enable binding on this attribute are unbalanced: missing opening- or closing-bracket"),
  UNBALANCED_CAPTURE_PARENTHESES(args -> "The parentheses used to enable capturing on this binding are unbalanced: missing opening- or closing-parentheses"),
  MULTIPLE_ATTRIBUTE_BRACKETS(args -> "The brackets used to enable binding on this attribute may only be used once!"),
  MULTIPLE_ATTRIBUTE_INTRINSIC_MARKERS(args -> "The asterisk-/plus-symbol used to mark intrinsic attributes may only be used once!"),
  LATE_ATTRIBUTE_BRACKETS(args -> "The brackets used to enable binding on this attribute may only appear at the very outside of the name!"),
  MULTIPLE_ATTRIBUTE_SPREADS(args -> "The three dots used to enable spreading on this attribute may only be used once!"),
  MULTIPLE_CAPTURE_PARENTHESES(args -> "The parentheses used to enable capturing on this binding may only be used once!"),
  MALFORMED_SPREAD_OPERATOR(args -> "Use three leading dots to denote the spread-operator: ..."),
  UNBALANCED_CLOSING_TAG(args -> "There was no still-opened tag with the name " + args[0] + " that could be closed"),
  UNBALANCED_CLOSING_TAG_BLANK(args -> "There was no still-opened tag that could be closed"),
  IS_CASE_OUTSIDE_OF_WHEN_PARENT(args -> "An +is case may only occur within a parent containing the *when expression"),
  OTHER_CASE_OUTSIDE_OF_WHEN_PARENT(args -> "An *other case may only occur within a parent containing the *when expression"),
  WHEN_MATCHING_COLLIDING_CASES(args -> "Only specify one of either +is or *other per member of *when"),
  WHEN_MATCHING_DISALLOWED_MEMBER(args -> "Within *when matching, only +is or *other members and nested *when containers are allowed; static content without such attributes is also disallowed"),
  WHEN_MATCHING_DUPLICATE_INPUT(args -> "There can only be one *when input"),
  WHEN_MATCHING_DUPLICATE_CASE(args -> "The case " + args[0] + " occurred more than once"),
  WHEN_MATCHING_DUPLICATE_FALLBACK(args -> "The *other case occurred more than once"),
  WHEN_MATCHING_NO_CASES(args -> "Specify at least one +is case to be used with *when"),
  NON_EXPRESSION_INTRINSIC_ATTRIBUTE(args -> "The intrinsic attribute " + args[0] + " only works with expressions or literals"),
  NON_LITERAL_INTRINSIC_ATTRIBUTE(args -> "The intrinsic attribute " + args[0] + " only works with literal values"),
  NON_STRING_EXPRESSION_ATTRIBUTE(args -> "Attributes bound to expressions may only be strings, containing expressions"),
  EXPECTED_MARKUP_ATTRIBUTE_VALUE(args -> "The attribute " + args[0] + " expected a markup-value: { ... }"),
  EXPECTED_EXPRESSION_ATTRIBUTE_VALUE(args -> "The attribute " + args[0] + " of tag " + args[1] + " expected a literal (string or number) or a bound expression"),
  EXPECTED_SELF_CLOSING_TAG(args -> "This tag is self-closing, <" + args[0] + " />, and does not support a separate closing-tag"),
  EXPECTED_OPEN_CLOSE_TAG(args -> "This tag requires a separate closing-tag </" + args[0] + ">, as it expects content and does not support self-closing <" + args[0] + " />"),
  EXPECTED_INTRINSIC_ATTRIBUTE_FLAG(args -> "The intrinsic attribute " + args[0] + " must not be assigned to a value, as it's just a flag: " + args[0] + " -> " + args[0]),
  MULTIPLE_NON_MULTI_ATTRIBUTE(args -> "The attribute " + args[0] + " of tag " + args[1] + " does not support being specified more than once"),
  SPREAD_ON_NON_MULTI_ATTRIBUTE(args -> "The attribute " + args[0] + " of tag " + args[1] + " does not support multiple values, and thus the spread-operator"),
  MULTIPLE_IF_ELSE_CONDITIONS(args -> "There has already been a prior *if/*else-if/*else on this tag, and said conditions may only occur once"),
  MULTIPLE_USE_CONDITIONS(args -> "There has already been a prior *use on this tag, and said condition may only occur once"),
  MULTIPLE_LOOPS(args -> "There has already been a prior loop on this tag, and loops may only occur once"),
  MISSING_PRECEDING_IF_SIBLING(args -> "Cannot use branching (else-if, else) without having specified a preceding if-sibling"),
  SPREAD_DISALLOWED_ON_NON_BINDING(args -> "The spread-operator ... is only allowed on bound attributes: [" + args[0] + "]=\"<expression>\""),
  XML_PARSE_ERROR(args -> "Encountered malformed XML"),
  EXPRESSION_PARSE_ERROR(args -> "Encountered a malformed expression"),
  EXPRESSION_TOKENIZE_ERROR(args -> "Encountered a malformed expression"),
  ;

  public final Function<String[], String> messageBuilder;

  MarkupParseError(Function<String[], String> messageBuilder) {
    this.messageBuilder = messageBuilder;
  }
}
