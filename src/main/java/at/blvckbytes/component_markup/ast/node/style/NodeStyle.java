package at.blvckbytes.component_markup.ast.node.style;

import at.blvckbytes.component_markup.ast.node.AstNode;
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

  private String makeFormattingExpression() {
    StringBuilder result = new StringBuilder();

    for (Formatting formatting : Formatting.VALUES) {
      Boolean state = formattingStates[formatting.ordinal()];

      if (state == null)
        continue;

      result.append('&');

      if (!state)
        result.append('!');

      switch (formatting) {
        case MAGIC:
          result.append('k');
          break;

        case BOLD:
          result.append('l');
          break;

        case STRIKETHROUGH:
          result.append('m');
          break;

        case UNDERLINE:
          result.append('n');
          break;

        case ITALIC:
          result.append('o');
          break;
      }
    }

    return result.toString();
  }

  public String stringify(int indentLevel) {
    return(
      AstNode.indent(indentLevel) + "NodeStyle{\n" +
      AstNode.indent(indentLevel + 1) + "color=" + (color == null ? "null" : "'" + color + "'") + ",\n" +
      AstNode.indent(indentLevel + 1) + "font=" + (font == null ? "null" : "'" + font + "'") + ",\n" +
      AstNode.indent(indentLevel + 1) + "formatting=" + makeFormattingExpression() + "\n" +
      AstNode.indent(indentLevel) + "}"
    );
  }
}
