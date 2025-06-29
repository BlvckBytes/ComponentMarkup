package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TransformerNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShadowTag extends TagDefinition {

  private static final String TAG_NAME = "shadow";

  public ShadowTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("value", AttributeType.EXPRESSION, false, true),
        new AttributeDefinition("opacity", AttributeType.EXPRESSION, false, false)
      },
      new String[] { TAG_NAME },
      TagClosing.OPEN_CLOSE,
      TagPriority.NORMAL
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
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    ContainerNode wrapper = new ContainerNode(position, children, letBindings);
    ExpressionNode opacity = tryFindExpressionAttribute("opacity", attributes);

    wrapper.getOrInstantiateStyle().shadowColor = new TransformerNode(
      findExpressionAttribute("value", attributes),
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

  private @Nullable Integer resolveColor(String color, double opacity) {
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
      try {
        int r = Integer.parseInt(color.substring(1, 3), 16);
        int g = Integer.parseInt(color.substring(3, 5), 16);
        int b = Integer.parseInt(color.substring(5, 7), 16);

        if (opacity < 0 && colorLength == 9)
          alpha = Integer.parseInt(color.substring(7, 9), 16);

        return packRGBA(r, g, b, alpha);
      } catch (Throwable e) {
        return null;
      }
    }

    color = color.toLowerCase();

    AnsiStyleColor ansiColor;

    if (color.charAt(0) == '&' && colorLength == 2)
      ansiColor = AnsiStyleColor.fromChar(color.charAt(1));

    else
      ansiColor = AnsiStyleColor.fromName(color);

    if (ansiColor == null)
      return null;

    return packRGBA(
      ansiColor.color.getRed(),
      ansiColor.color.getGreen(),
      ansiColor.color.getBlue(),
      alpha
    );
  }

  private int packRGBA(int r, int g, int b, int a) {
    return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
  }
}
