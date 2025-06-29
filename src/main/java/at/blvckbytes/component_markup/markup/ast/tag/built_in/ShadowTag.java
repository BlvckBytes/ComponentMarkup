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

import java.awt.*;
import java.util.List;

public class ShadowTag extends TagDefinition {

  private static final Color BLACK        = new Color(  0,   0,   0);
  private static final Color DARK_BLUE    = new Color(  0,   0, 170);
  private static final Color DARK_GREEN   = new Color(  0, 170,   0);
  private static final Color DARK_AQUA    = new Color(  0, 170, 170);
  private static final Color DARK_RED     = new Color(170,   0,   0);
  private static final Color DARK_PURPLE  = new Color(170,   0, 170);
  private static final Color GOLD         = new Color(255, 170,   0);
  private static final Color GRAY         = new Color(170, 170, 170);
  private static final Color DARK_GRAY    = new Color( 85,  85,  85);
  private static final Color BLUE         = new Color( 85,  85, 255);
  private static final Color GREEN        = new Color( 85, 255,  85);
  private static final Color AQUA         = new Color( 85, 255, 255);
  private static final Color RED          = new Color(255,  85,  85);
  private static final Color LIGHT_PURPLE = new Color(255,  85, 255);
  private static final Color YELLOW       = new Color(255, 255,  85);
  private static final Color WHITE        = new Color(255, 255, 255);

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

    int a = 255;

    if (opacity >= 0) {
      a = (int) Math.round(((double) a) * (opacity / 100.0));

      if (a > 255)
        a = 255;
    }

    if ((colorLength == 7 || colorLength == 9) && color.charAt(0) == '#') {
      try {
        int r = Integer.parseInt(color.substring(1, 3), 16);
        int g = Integer.parseInt(color.substring(3, 5), 16);
        int b = Integer.parseInt(color.substring(5, 7), 16);

        if (opacity < 0 && colorLength == 9)
          a = Integer.parseInt(color.substring(7, 9), 16);

        return packRGBA(r, g, b, a);
      } catch (Throwable e) {
        return null;
      }
    }

    Color namedColor = resolveNamedColorOrZero(color.toLowerCase());

    if (namedColor == null)
      return null;

    return packRGBA(namedColor.getRed(), namedColor.getGreen(), namedColor.getBlue(), a);
  }

  private int packRGBA(int r, int g, int b, int a) {
    return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
  }

  private @Nullable Color resolveNamedColorOrZero(String name) {
    switch (name) {
      case "red":
        return RED;

      case "light_purple":
        return LIGHT_PURPLE;

      case "gold":
        return GOLD;

      case "dark_green":
        return DARK_GREEN;

      case "dark_aqua":
        return DARK_AQUA;

      case "yellow":
        return YELLOW;

      case "white":
        return WHITE;

      case "dark_red":
        return DARK_RED;

      case "dark_purple":
        return DARK_PURPLE;

      case "dark_gray":
        return DARK_GRAY;

      case "blue":
        return BLUE;

      case "gray":
        return GRAY;

      case "green":
        return GREEN;

      case "aqua":
        return AQUA;

      case "black":
        return BLACK;

      case "dark_blue":
        return DARK_BLUE;

      default:
        return null;
    }
  }
}
