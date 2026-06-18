package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.constructor.ComponentConstructor;
import at.blvckbytes.component_markup.util.color.PackedColor;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ExtendedBuilder<B, C> {

  private @Nullable B builder;
  private @Nullable C wrappedComponent;

  private @Nullable String textOverride;

  private @Nullable List<ExtendedBuilder<B, C>> children;
  private @Nullable List<Consumer<B>> nonTerminalApplyingClosures;

  public @Nullable ComputedStyle style;

  public long explicitColor = PackedColor.NULL_SENTINEL;

  private ExtendedBuilder() {}

  public static <B, C> ExtendedBuilder<B, C> wrapPlatformComponent(@NotNull C wrappedComponent, @Nullable ComputedStyle style) {
    ExtendedBuilder<B, C> result = new ExtendedBuilder<>();

    result.wrappedComponent = wrappedComponent;
    result.style = style;

    return result;
  }

  public static <B, C> ExtendedBuilder<B, C> wrapPlatformBuilder(@NotNull B builder, @Nullable ComputedStyle style) {
    ExtendedBuilder<B, C> result = new ExtendedBuilder<>();

    result.builder = builder;
    result.style = style;

    return result;
  }

  public static <B, C> ExtendedBuilder<B, C> makeContainer(@NotNull List<ExtendedBuilder<B, C>> children, @Nullable ComputedStyle style) {
    ExtendedBuilder<B, C> result = new ExtendedBuilder<>();

    result.children = children;
    result.style = style;

    return result;
  }

  public static <B, C> ExtendedBuilder<B, C> makeEmpty() {
    return new ExtendedBuilder<>();
  }

  public void addChildren(List<B> addedChildren) {
    if (children == null)
      children = new ArrayList<>();

    for (B child : addedChildren)
      children.add(ExtendedBuilder.wrapPlatformBuilder(child, null));
  }

  public @NotNull ComputedStyle getOrInstantiateStyle() {
    if (this.style == null)
      this.style = new ComputedStyle();

    return style;
  }

  public void setText(String text) {
    textOverride = text;
  }

  public void addNonTerminalApplyingClosure(@NotNull Consumer<B> nonTerminalApplyingClosure) {
    if (this.nonTerminalApplyingClosures == null)
      this.nonTerminalApplyingClosures = new ArrayList<>();

    this.nonTerminalApplyingClosures.add(nonTerminalApplyingClosure);
  }

  public C toFinalizedComponent(ComponentConstructor<B, C> componentConstructor, InterpreterLogger logger) {
    if (wrappedComponent != null) {
      if (builder != null)
        throw new IllegalStateException("This case should be unreachable if constructor-parameter nullability was adhered to");

      // We were wrapping the component when encountering it during interpretation, as there may have been
      // a need to modify it - since it is immutable, we would have to wrap it in a builder to do so.
      // Seeing how no changes have been made, we can safely "unwrap" at this point, simplifying the output.
      if (style == null && children == null && nonTerminalApplyingClosures == null)
        return wrappedComponent;

      builder = componentConstructor.createTextComponent("");
      componentConstructor.addChildren(builder, Collections.singletonList(wrappedComponent));
    }

    else if (builder == null)
      builder = componentConstructor.createTextComponent("");

    if (textOverride != null)
      componentConstructor.setText(builder, textOverride);

    if (children != null && !children.isEmpty()) {
      List<C> childComponents = new ArrayList<>();

      for (ExtendedBuilder<B, C> childBuilder : children)
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
