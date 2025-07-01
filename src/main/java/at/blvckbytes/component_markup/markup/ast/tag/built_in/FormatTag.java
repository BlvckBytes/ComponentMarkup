package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TransformerNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.interpreter.AnsiStyleColor;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FormatTag extends TagDefinition {

  private static final String TAG_NAME = "format";

  private static final ExpressionAttributeDefinition ATTR_OBFUSCATED = new ExpressionAttributeDefinition("obfuscated");
  private static final ExpressionAttributeDefinition ATTR_STRIKETHROUGH = new ExpressionAttributeDefinition("strikethrough");
  private static final ExpressionAttributeDefinition ATTR_UNDERLINED = new ExpressionAttributeDefinition("underlined");
  private static final ExpressionAttributeDefinition ATTR_ITALIC = new ExpressionAttributeDefinition("italic");
  private static final ExpressionAttributeDefinition ATTR_BOLD = new ExpressionAttributeDefinition("bold");
  private static final ExpressionAttributeDefinition ATTR_FONT = new ExpressionAttributeDefinition("font");
  private static final ExpressionAttributeDefinition ATTR_COLOR = new ExpressionAttributeDefinition("color");
  private static final ExpressionAttributeDefinition ATTR_SHADOW = new ExpressionAttributeDefinition("shadow");
  private static final ExpressionAttributeDefinition ATTR_SHADOW_OPACITY = new ExpressionAttributeDefinition("shadow-opacity");

  public FormatTag() {
    super(
      new String[] { TAG_NAME },
      TagClosing.OPEN_CLOSE,
      TagPriority.NORMAL,
      ATTR_OBFUSCATED,
      ATTR_STRIKETHROUGH,
      ATTR_UNDERLINED,
      ATTR_ITALIC,
      ATTR_BOLD,
      ATTR_FONT,
      ATTR_COLOR,
      ATTR_SHADOW,
      ATTR_SHADOW_OPACITY
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals(TAG_NAME);
  }

  @Override
  public @NotNull MarkupNode construct(
    String tagNameLower,
    CursorPosition position,
    AttributeMap attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    ContainerNode wrapper = new ContainerNode(position, children, letBindings);
    NodeStyle style = wrapper.getOrInstantiateStyle();

    ExpressionNode expression;

    if ((expression = ATTR_OBFUSCATED.singleOrNull(attributes)) != null)
      style.setFormat(Format.OBFUSCATED, expression);

    if ((expression = ATTR_BOLD.singleOrNull(attributes)) != null)
      style.setFormat(Format.BOLD, expression);

    if ((expression = ATTR_STRIKETHROUGH.singleOrNull(attributes)) != null)
      style.setFormat(Format.STRIKETHROUGH, expression);

    if ((expression = ATTR_UNDERLINED.singleOrNull(attributes)) != null)
      style.setFormat(Format.UNDERLINED, expression);

    if ((expression = ATTR_ITALIC.singleOrNull(attributes)) != null)
      style.setFormat(Format.ITALIC, expression);

    if ((expression = ATTR_FONT.singleOrNull(attributes)) != null)
      style.font = expression;

    if ((expression = ATTR_COLOR.singleOrNull(attributes)) != null)
      style.color = expression;

    if ((expression = ATTR_SHADOW.singleOrNull(attributes)) != null) {
      ExpressionNode shadowOpacity = ATTR_SHADOW_OPACITY.singleOrNull(attributes);

      style.shadowColor = new TransformerNode(
        expression,
        (input, environment, interpreter) -> {
          String colorValue = environment.getValueInterpreter().asString(input);

          if (shadowOpacity == null)
            return resolveShadowColor(colorValue, -1);

          Object opacityValue = interpreter.interpret(shadowOpacity, environment);

          if (opacityValue == null)
            return resolveShadowColor(colorValue, -1);

          return resolveShadowColor(colorValue, environment.getValueInterpreter().asDouble(opacityValue));
        }
      );
    }

    return wrapper;
  }

  private @Nullable String resolveShadowColor(String color, double opacity) {
    int colorLength = color.length();

    if (colorLength == 0)
      return null;

    int alpha = 255;

    if (opacity >= 0) {
      alpha = (int) Math.round(((double) alpha) * (opacity / 100.0));

      if (alpha > 255)
        alpha = 255;
    }

    if ((colorLength == 7 || colorLength == 9) && color.charAt(0) == '#') {
      for (int charIndex = 1; charIndex < colorLength; ++charIndex) {
        if (!isHexadecimalChar(color.charAt(charIndex)))
          return null;
      }

      if (colorLength == 7)
        return alpha == 255 ? color : color + String.format("%02X", alpha);

      return color.substring(0, 9 - 2) + String.format("%02X", alpha);
    }

    AnsiStyleColor ansiColor;

    if (color.charAt(0) == '&' && colorLength == 2)
      ansiColor = AnsiStyleColor.fromCharOrNull(color.charAt(1));

    else
      ansiColor = AnsiStyleColor.fromNameLowerOrNull(color.toLowerCase());

    if (ansiColor == null)
      return null;

    return ansiColor.name;
  }

  private boolean isHexadecimalChar(char c) {
    return (c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F') || (c >= '0' && c <= '9');
  }
}
