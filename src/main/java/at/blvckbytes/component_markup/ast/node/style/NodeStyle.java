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

  public void enableFormatting(Formatting formatting) {
    formattingStates[formatting.ordinal()] = true;
  }

  public void disableFormatting(Formatting formatting) {
    formattingStates[formatting.ordinal()] = false;
  }

  public void clearFormatting(Formatting formatting) {
    formattingStates[formatting.ordinal()] = null;
  }

  public void reset() {
    Arrays.fill(formattingStates, null);
    this.color = null;
    this.font = null;
  }

  public @Nullable Boolean getFormatting(Formatting formatting) {
    return formattingStates[formatting.ordinal()];
  }
}
