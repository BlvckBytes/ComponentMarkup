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

  public ExtendedBuilder(B builder) {
    this.builder = builder;
  }

  public void addChild(ExtendedBuilder<B> child) {
    if (this.children == null)
      this.children = new ArrayList<>();

    this.children.add(child);
  }

  public void addNonTerminalApplyingClosure(@NotNull Consumer<B> nonTerminalApplyingClosure) {
    if (this.nonTerminalApplyingClosures == null)
      this.nonTerminalApplyingClosures = new ArrayList<>();

    this.nonTerminalApplyingClosures.add(nonTerminalApplyingClosure);
  }

  public ExtendedBuilder<B> withStyle(ComputedStyle style) {
    // TODO: Set style vs add to style - this cannot possibly cover all cases... Or does it?
    //       Either way, I'd like this to represent intent better.
    this.style = style;
    return this;
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

    if (this.nonTerminalApplyingClosures != null)
      nonTerminalApplyingClosures.forEach(closure -> closure.accept(builder));

    return componentConstructor.finalizeComponent(builder);
  }
}
