package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TransformerNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.interpreter.AnsiStyleColor;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShadowTag extends TagDefinition {

  private static final String TAG_NAME = "shadow";

  private static final MandatoryExpressionAttributeDefinition ATTR_VALUE = new MandatoryExpressionAttributeDefinition("value");
  private static final ExpressionAttributeDefinition ATTR_OPACITY = new ExpressionAttributeDefinition("opacity");

  public ShadowTag() {
    super(
      new String[] { TAG_NAME },
      TagClosing.OPEN_CLOSE,
      TagPriority.NORMAL,
      ATTR_VALUE,
      ATTR_OPACITY
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
    ExpressionNode opacity = ATTR_OPACITY.singleOrNull(attributes);

    wrapper.getOrInstantiateStyle().shadowColor = new TransformerNode(
      ATTR_VALUE.single(attributes),
      (input, environment, interpreter) -> {
        String colorValue = environment.getValueInterpreter().asString(input);

        if (opacity == null)
          return resolveColor(colorValue, -1);

        Object opacityValue = interpreter.interpret(opacity, environment);

        if (opacityValue == null)
          return resolveColor(colorValue, -1);

        return resolveColor(colorValue, environment.getValueInterpreter().asDouble(opacityValue));
      }
    );

    return wrapper;
  }

  private @Nullable String resolveColor(String color, double opacity) {
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
