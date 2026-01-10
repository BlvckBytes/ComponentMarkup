package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.constructor.ComponentConstructor;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ExtendedBuilder<B> {

  public final B builder;

  private @Nullable List<ExtendedBuilder<B>> children;
  private @Nullable List<Consumer<B>> nonTerminalApplyingClosures;

  public @Nullable ComputedStyle style;

  public ExtendedBuilder(B builder, @Nullable ComputedStyle style) {
    this.builder = builder;
    this.style = style;
  }

  public ExtendedBuilder(B builder, @Nullable ComputedStyle style, @Nullable List<ExtendedBuilder<B>> children) {
    this.builder = builder;
    this.style = style;
    this.children = children;
  }

  public ExtendedBuilder(B builder) {
    this.builder = builder;
  }

  public void addNonTerminalApplyingClosure(@NotNull Consumer<B> nonTerminalApplyingClosure) {
    if (this.nonTerminalApplyingClosures == null)
      this.nonTerminalApplyingClosures = new ArrayList<>();

    this.nonTerminalApplyingClosures.add(nonTerminalApplyingClosure);
  }

  public <C> C toFinalizedComponent(ComponentConstructor<B, C> componentConstructor, InterpreterLogger logger) {
    if (children != null && !children.isEmpty()) {
      List<C> childComponents = new ArrayList<>();

      for (ExtendedBuilder<B> childBuilder : children)
        childComponents.add(childBuilder.toFinalizedComponent(componentConstructor, logger));

      componentConstructor.addChildren(builder, childComponents);
    }

    if (style != null)
      style.applyStyles(builder, componentConstructor, logger);

    if (this.nonTerminalApplyingClosures != null) {
      // Non-terminals are appended from the inside out, so we need apply them in reverse
      // order, such that the closest of same type wins out in the end.
      for (int i = nonTerminalApplyingClosures.size() - 1; i >= 0; --i)
        nonTerminalApplyingClosures.get(i).accept(builder);
    }

    return componentConstructor.finalizeComponent(builder);
  }
}
