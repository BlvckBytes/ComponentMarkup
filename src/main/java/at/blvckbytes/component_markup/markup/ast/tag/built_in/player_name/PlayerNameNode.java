/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.player_name;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.DeferredNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.*;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class PlayerNameNode extends DeferredNode<PlayerNameParameter> {

  public final @Nullable ExpressionNode displayName;
  public final @Nullable MarkupNode representation;

  public PlayerNameNode(
    @Nullable ExpressionNode displayName,
    @Nullable MarkupNode representation,
    StringView positionProvider,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(positionProvider, letBindings);

    this.displayName = displayName;
    this.representation = representation;
  }

  @Override
  public @Nullable List<Object> renderComponent(
    PlayerNameParameter parameter,
    ComponentConstructor componentConstructor,
    InterpretationEnvironment environment,
    SlotContext slotContext,
    @Nullable Object recipient
  ) {
    if (recipient == null)
      return null;

    DeferredDataProvider provider = componentConstructor.getDataProvider();
    String name = parameter.displayName ? provider.getDisplayName(recipient) : provider.getName(recipient);

    if (representation == null)
      return Collections.singletonList(componentConstructor.createTextComponent(name));

    environment = environment.copy().withVariable("player_name", name);

    return MarkupInterpreter.interpret(
      componentConstructor, environment, recipient, slotContext, representation
    ).unprocessedComponents;
  }

  @Override
  public PlayerNameParameter createParameter(Interpreter interpreter) {
    return new PlayerNameParameter(interpreter.evaluateAsBoolean(this.displayName));
  }
}
