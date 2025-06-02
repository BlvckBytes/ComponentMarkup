package at.blvckbytes.component_markup.ast.node.style;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class NodeStyle {

  private final Boolean[] formattingStates;

  public @Nullable String color;
  public @Nullable String font;

  public NodeStyle() {
    this.formattingStates = new Boolean[Formatting.VALUES.size()];
  }

  public NodeStyle enableFormatting(Formatting formatting) {
    formattingStates[formatting.ordinal()] = true;
    return this;
  }

  public NodeStyle disableFormatting(Formatting formatting) {
    formattingStates[formatting.ordinal()] = false;
    return this;
  }

  public NodeStyle clearFormatting(Formatting formatting) {
    formattingStates[formatting.ordinal()] = null;
    return this;
  }

  public NodeStyle reset() {
    Arrays.fill(formattingStates, null);
    this.color = null;
    this.font = null;
    return this;
  }

  public @Nullable Boolean getFormatting(Formatting formatting) {
    return formattingStates[formatting.ordinal()];
  }
}
