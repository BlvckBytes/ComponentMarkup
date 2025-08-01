/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.markup.ast.node.control.*;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.ExpressionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TranslateNode;
import at.blvckbytes.component_markup.markup.ast.tag.MarkupList;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.test_utils.Tuple;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public abstract class MarkupParserTestsBase {

  protected static NodeWrapper<ForLoopNode> forLoop(
    ExpressionNode iterable,
    @Nullable StringView iterationVariable,
    NodeWrapper<?> wrappedBody,
    @Nullable NodeWrapper<?> wrappedSeparator,
    @Nullable ExpressionNode reversed,
    @Nullable NodeWrapper<?> wrappedEmpty
  ) {
    return new NodeWrapper<>(new ForLoopNode(
      iterable,
      iterationVariable,
      wrappedBody.get(),
      wrappedSeparator == null ? null : wrappedSeparator.get(),
      wrappedEmpty == null ? null : wrappedEmpty.get(),
      reversed,
      new LinkedHashSet<>()
    ));
  }

  protected static List<Tuple<StringView, NodeWrapper<? extends MarkupNode>>> whenMap(Object... items) {
    List<Tuple<StringView, NodeWrapper<? extends MarkupNode>>> result = new ArrayList<>();

    if (items.length % 2 != 0)
      throw new IllegalStateException("Expected an even number of items");

    for (int i = 0; i < items.length; i += 2) {
      StringView key = (StringView) items[i];
      NodeWrapper<?> value = (NodeWrapper<?>) items[i + 1];
      result.add(new Tuple<>(key, value));
    }

    return result;
  }

  protected static NodeWrapper<WhenMatchingNode> when(
    StringView positionProvider,
    ExpressionNode input,
    @Nullable NodeWrapper<?> wrappedFallback,
    List<Tuple<StringView, NodeWrapper<? extends MarkupNode>>> wrappedCases
  ) {
    WhenMatchingMap cases = new WhenMatchingMap();

    for (Tuple<StringView, NodeWrapper<? extends MarkupNode>> entry : wrappedCases)
      cases.put(entry.first, entry.second.get());

    return new NodeWrapper<>(new WhenMatchingNode(positionProvider, input, cases, wrappedFallback == null ? null : wrappedFallback.get()));
  }

  @SafeVarargs
  protected static NodeWrapper<IfElseIfElseNode> ifElseIfElse(@Nullable NodeWrapper<?> wrappedFallback, NodeWrapper<? extends MarkupNode>... wrappedConditions) {
    List<MarkupNode> conditions = new ArrayList<>();

    for (NodeWrapper<? extends MarkupNode> wrappedCondition : wrappedConditions)
      conditions.add(wrappedCondition.get());

    return new NodeWrapper<>(new IfElseIfElseNode(conditions, wrappedFallback == null ? null : wrappedFallback.get()));
  }

  protected static ExpressionNode bool(StringView raw, boolean value) {
    return ImmediateExpression.ofBoolean(raw, value);
  }

  protected static ExpressionNode string(StringView value) {
    return ImmediateExpression.ofString(value, value.buildString());
  }

  protected static ExpressionNode expr(StringView expression) {
    return ExpressionParser.parse(expression, null);
  }

  protected static NodeWrapper<ContainerNode> container(StringView positionProvider) {
    return new NodeWrapper<>(new ContainerNode(positionProvider, new ArrayList<>(), new LinkedHashSet<>()));
  }

  protected static NodeWrapper<ExpressionDrivenNode> exprDriven(StringView positionProvider, StringView expression) {
    return new NodeWrapper<>(new ExpressionDrivenNode(positionProvider, expr(expression)));
  }

  protected static NodeWrapper<TranslateNode> translate(ExpressionNode key, StringView positionProvider, @Nullable ExpressionNode fallback, NodeWrapper<?>... wrappedWiths) {
    List<MarkupNode> withs = new ArrayList<>();

    for (NodeWrapper<?> wrappedWith : wrappedWiths)
      withs.add(wrappedWith.get());

    return new NodeWrapper<>(new TranslateNode(key, toMarkupList(withs), fallback, positionProvider, new LinkedHashSet<>()));
  }

  private static MarkupList toMarkupList(List<MarkupNode> markupNodes) {
    return new MarkupList(Collections.emptyList()) {
      @Override
      public List<MarkupNode> get(Interpreter interpreter) {
        return markupNodes;
      }
    };
  }

  protected static NodeWrapper<InterpolationNode> interpolation(StringView expression) {
    return new NodeWrapper<>(new InterpolationNode(expression, expr(expression)));
  }

  protected static NodeWrapper<TextNode> text(StringView value) {
    return new NodeWrapper<>(new TextNode(value, value.buildString()));
  }

  protected static void makeCase(TextWithAnchors input, NodeWrapper<?> wrappedExpectedNode) {
    MarkupNode actualNode = MarkupParser.parse(StringView.of(input.text), BuiltInTagRegistry.INSTANCE);
    Assertions.assertEquals(Jsonifier.jsonify(wrappedExpectedNode.get()), Jsonifier.jsonify(actualNode));
  }
}
