/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.selector;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.DeferredNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.markup.interpreter.MarkupInterpreter;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.platform.ComponentConstructor;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.platform.PlatformEntity;
import at.blvckbytes.component_markup.platform.SlotContext;
import at.blvckbytes.component_markup.platform.selector.SelectorParseException;
import at.blvckbytes.component_markup.platform.selector.SelectorParser;
import at.blvckbytes.component_markup.platform.selector.TargetSelector;
import at.blvckbytes.component_markup.platform.selector.TargetType;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;

public class SelectorNode extends DeferredNode<SelectorParameter> {

  public final ExpressionNode selector;
  public final @Nullable MarkupNode renderer;

  private static final MarkupNode DEFAULT_RENDERER;

  static {
    DEFAULT_RENDERER = MarkupParser.parse(
      InputView.of(
        "<container",
        "*for-entity=\"selector_result\"",
        "*for-separator={ <gray>,<space/> }",
        "*for-empty={ <red>The selector yielded no results! }",
        ">{entity.name}"
      ),
      BuiltInTagRegistry.INSTANCE
    );
  }

  public SelectorNode(
    ExpressionNode selector,
    @Nullable MarkupNode renderer,
    InputView positionProvider,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(positionProvider, letBindings);

    this.selector = selector;
    this.renderer = renderer;
  }

  @Override
  public @Nullable List<Object> renderComponent(
    SelectorParameter selectorParameter,
    ComponentConstructor componentConstructor,
    InterpretationEnvironment environment,
    SlotContext slotContext,
    @Nullable PlatformEntity recipient
  ) {
    if (recipient == null) {
      for (String line : ErrorScreen.make(selector.getFirstMemberPositionProvider(), "Cannot execute a selector without a provided recipient"))
        LoggerProvider.log(Level.WARNING, line, false);

      return null;
    }

    environment = environment
      .copy()
      .withVariable("selector_result", recipient.executeSelector(selectorParameter.selector))
      .withVariable("selector_origin", recipient);

    return MarkupInterpreter.interpret(
      componentConstructor, environment, recipient, slotContext,
      renderer == null ? DEFAULT_RENDERER : renderer
    ).unprocessedComponents;
  }

  @Override
  public SelectorParameter createParameter(Interpreter interpreter) {
    InputView selectorString = InputView.of(interpreter.evaluateAsString(this.selector));

    TargetSelector targetSelector;

    try {
      targetSelector = SelectorParser.parse(selectorString);
    } catch (SelectorParseException parseException) {
      for (String line : ErrorScreen.make(this.selector.getFirstMemberPositionProvider(), "Could not parse this target-selector"))
        LoggerProvider.log(Level.WARNING, line, false);

      LoggerProvider.log(Level.WARNING, "Falling back to \"@p\"; the following parse-error occurred:", false);

      for (String line : ErrorScreen.make(selectorString.contents, parseException.position, parseException.getErrorMessage()))
        LoggerProvider.log(Level.WARNING, line, false);

      targetSelector = new TargetSelector(TargetType.NEAREST_PLAYER, selectorString, Collections.emptyList());
    }

    return new SelectorParameter(targetSelector);
  }
}
