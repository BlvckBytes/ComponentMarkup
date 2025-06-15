package at.blvckbytes.component_markup.ast.node.style;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class NodeStyle {

  public final AExpression[] formatStates;
  public @Nullable AExpression color;
  public @Nullable AExpression font;

  public NodeStyle() {
    this.formatStates = new AExpression[Format.VALUES.size()];
    Arrays.fill(formatStates, ImmediateExpression.ofNull());
  }

  public void setFormat(Format formatting, AExpression value) {
    formatStates[formatting.ordinal()] = value;
  }

  public void reset() {
    Arrays.fill(formatStates, ImmediateExpression.ofNull());
    this.color = null;
    this.font = null;
  }

  private String makeFormatExpression() {
    StringBuilder result = new StringBuilder();

    for (int formatIndex = 0; formatIndex < Format.VALUES.size(); ++formatIndex) {
      Format format = Format.VALUES.get(formatIndex);
      AExpression state = formatStates[format.ordinal()];

      result.append('&');

      switch (format) {
        case MAGIC:
          result.append('k');
          break;

        case BOLD:
          result.append('l');
          break;

        case STRIKETHROUGH:
          result.append('m');
          break;

        case UNDERLINED:
          result.append('n');
          break;

        case ITALIC:
          result.append('o');
          break;
      }

      result.append('=').append(state.expressionify());

      if (formatIndex != 0)
        result.append(';');
    }

    return result.toString();
  }

  public String stringify(int indentLevel) {
    return(
      AstNode.indent(indentLevel) + "NodeStyle{\n" +
      AstNode.indent(indentLevel + 1) + "color=" + (color == null ? "null" : color.expressionify()) + ",\n" +
      AstNode.indent(indentLevel + 1) + "font=" + (font == null ? "null" : font.expressionify()) + ",\n" +
      AstNode.indent(indentLevel + 1) + "format=" + makeFormatExpression() + "\n" +
      AstNode.indent(indentLevel) + "}"
    );
  }

  public void copyFrom(NodeStyle other) {
    this.font = other.font;
    this.color = other.color;
    System.arraycopy(other.formatStates, 0, this.formatStates, 0, formatStates.length);
  }
}
