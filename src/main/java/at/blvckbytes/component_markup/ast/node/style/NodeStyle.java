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

  public NodeStyle enableFormat(Format formatting) {
    formatStates[formatting.ordinal()] = ImmediateExpression.of(true);
    return this;
  }

  public NodeStyle disableFormat(Format formatting) {
    formatStates[formatting.ordinal()] = ImmediateExpression.of(false);
    return this;
  }

  public NodeStyle clearFormat(Format formatting) {
    formatStates[formatting.ordinal()] = ImmediateExpression.ofNull();
    return this;
  }

  public NodeStyle reset() {
    Arrays.fill(formatStates, ImmediateExpression.ofNull());
    this.color = null;
    this.font = null;
    return this;
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
}
