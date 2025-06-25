package at.blvckbytes.component_markup.ast.node.style;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.util.Jsonifiable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class NodeStyle extends Jsonifiable {

  public final AExpression[] formatStates;
  public @Nullable AExpression color;
  public @Nullable AExpression font;

  public NodeStyle() {
    this.formatStates = new AExpression[Format.VALUES.size()];
    Arrays.fill(formatStates, ImmediateExpression.ofNull());
  }

  public void inheritFrom(NodeStyle other) {
    if (this.color == null)
      this.color = other.color;

    if (this.font == null)
      this.font = other.font;

    for (Format format : Format.VALUES) {
      AExpression thisFormatState = this.formatStates[format.ordinal()];

      if (thisFormatState != ImmediateExpression.ofNull())
        continue;

      this.formatStates[format.ordinal()] = other.formatStates[format.ordinal()];
    }
  }

  public boolean hasEffect() {
    if (this.font != null || this.color != null)
      return true;

    for (AExpression formatState : formatStates) {
      if (formatState != ImmediateExpression.ofNull())
        return true;
    }

    return false;
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
    StringBuilder result = new StringBuilder("{");

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

      if (formatIndex != Format.VALUES.size() - 1)
        result.append(';');
    }

    result.append('}');

    return result.toString();
  }

  @Override
  protected @Nullable JsonElement overrideJsonRepresentation(String field) {
    if (field.equals("formatStates"))
      return new JsonPrimitive(makeFormatExpression());

    return null;
  }
}
