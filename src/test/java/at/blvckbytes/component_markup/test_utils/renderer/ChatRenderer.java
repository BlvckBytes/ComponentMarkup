package at.blvckbytes.component_markup.test_utils.renderer;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.markup.interpreter.*;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

public class ChatRenderer {

  private static final BufferedImage DUMMY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
  private static final Graphics2D DUMMY_GRAPHICS = DUMMY_IMAGE.createGraphics();

  private static final float FONT_SIZE = 40;
  private static final int LINE_SPACING = 5;
  private static final int OUTER_PADDING = 20;
  private static final int UNDERLINE_MARGIN = 5;
  private static final int STRIKETHROUGH_MARGIN = -11;
  private static final int LINE_THICKNESS = 4;
  private static final int SHADOW_DELTA_X = 5;
  private static final int SHADOW_DELTA_Y = 5;

  private static @Nullable Font FONT_REGULAR, FONT_BOLD, FONT_ITALIC, FONT_BOLD_ITALIC;

  public static void main(String[] args) throws Exception {
    MarkupNode ast;

    try {
      TextWithAnchors input = new TextWithAnchors(
        "<rainbow>All of the fancy rainbow colors!</>",
        "<br/>",
        "<red><b>Hello, </b>world! :)</>",
        "<br/>",
        "<style shadow=\"red\"><aqua>One last <i>line</i>.</></>",
        "<br/>",
        "<gold *for=\"1..5\" for-separator={<br/>}>",
        "  <style",
        "    [italic]=\"loop.index == 0 || loop.index == 4\"",
        "    [underlined]=\"loop.index == 1 || loop.index == 4\"",
        "    [strikethrough]=\"loop.index == 2 || loop.index == 4\"",
        "    [bold]=\"loop.index == 3 || loop.index == 4\"",
        "  >just kidding!</>",
        "</>"
      );

      System.out.println("About to parse the following input:");
      System.out.println(input.text);

      ast = MarkupParser.parse(input.text, BuiltInTagRegistry.INSTANCE);
    } catch (MarkupParseException exception) {
      System.out.println("An error occurred while trying to parse the input:");
      for (String line : exception.makeErrorScreen())
        System.out.println(line);

      return;
    }

    ComponentConstructor componentConstructor = new JsonComponentConstructor() {
      @Override
      public SlotContext getSlotContext(SlotType slot) {
        SlotContext superResult = super.getSlotContext(slot);

        if (slot == SlotType.CHAT)
          return new SlotContext((char) 0, superResult.defaultStyle);

        return superResult;
      }
    };

    List<Object> components = MarkupInterpreter.interpret(
      componentConstructor,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      SlotType.CHAT,
      ast
    );

    BufferedImage image = render(components, componentConstructor.getSlotContext(SlotType.CHAT));
    ImageIO.write(image, "png", new java.io.File("chat_output.png"));
  }

  @SuppressWarnings("unchecked")
  public static BufferedImage render(List<?> components, SlotContext context) throws Exception {
    return render(translateJsonComponents((List<JsonObject>) components), context);
  }

  private static BufferedImage render(ChatComponent[] components, SlotContext context) throws Exception {
    int requiredWidth = 0;
    int requiredHeight = 0;

    for (int i = 0; i < components.length; ++i) {
      ChatComponent component = components[i];

      measureComponentAndAttachDimensions(component);

      if (i != 0)
        requiredHeight += LINE_SPACING;

      assert component.totalDimension != null;
      requiredWidth = Math.max(requiredWidth, component.totalDimension.width);
      requiredHeight += component.totalDimension.height;
    }

    requiredWidth += 2 * OUTER_PADDING;
    requiredHeight += 2 * OUTER_PADDING;

    BufferedImage image = new BufferedImage(requiredWidth, requiredHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();

    graphics.setComposite(AlphaComposite.Clear);
    graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    graphics.setComposite(AlphaComposite.SrcOver);

    Dimension offset = new Dimension(OUTER_PADDING, OUTER_PADDING);

    for (int i = 0; i < components.length; ++i) {
      ChatComponent component = components[i];

      if (i != 0)
        offset.height += LINE_SPACING;

      assert component.totalDimension != null;

      offset.height += component.totalDimension.height;

      renderComponent(graphics, offset, component, context);
    }

    graphics.dispose();

    return image;
  }

  private static void measureComponentAndAttachDimensions(ChatComponent component) throws Exception {
    int height = measureFontHeight(component);
    int width = measureTextWidth(component.text, component);

    component.selfDimension = new Dimension(width, height);

    for (ChatComponent child : component.extra) {
      measureComponentAndAttachDimensions(child);

      assert child.totalDimension != null;
      width += child.totalDimension.width;
    }

    component.totalDimension = new Dimension(width, height);
  }

  private static ChatComponent[] translateJsonComponents(List<JsonObject> components) {
    ChatComponent[] result = new ChatComponent[components.size()];

    for (int i = 0; i < result.length; ++i)
      result[i] = translateJsonComponent(components.get(i), null);

    return result;
  }

  private static ChatComponent translateJsonComponent(JsonObject component, @Nullable ChatComponent parent) {
    JsonElement textElement = component.get("text");

    if (!(textElement instanceof JsonPrimitive))
      throw new IllegalStateException("Expected \"text\" to be a string on: " + component);

    JsonElement colorElement = component.get("color");
    Color color = null;

    if (colorElement != null) {
      if (!(colorElement instanceof JsonPrimitive))
        throw new IllegalStateException("Expected \"color\" to be a string on: " + component);

      long packedColor = PackedColor.tryParse(colorElement.getAsString());

      if (packedColor == PackedColor.NULL_SENTINEL)
        throw new IllegalStateException("Encountered unparsable \"color\" on: " + component);

      color = colorFromPacked(packedColor);
    }

    JsonElement shadowColorElement = component.get("shadow_color");
    Color shadowColor = null;

    if (shadowColorElement != null) {
      if (!(shadowColorElement instanceof JsonPrimitive))
        throw new IllegalStateException("Expected \"shadow_color\" to be a string on: " + component);

      long packedColor = PackedColor.tryParse(shadowColorElement.getAsString());

      if (packedColor == PackedColor.NULL_SENTINEL)
        throw new IllegalStateException("Encountered unparsable \"shadow_color\" on: " + component);

      shadowColor = colorFromPacked(packedColor);
    }

    ChatComponent result = new ChatComponent(textElement.getAsString(), color, shadowColor, parent);

    for (Format format : Format.VALUES) {
      String formatName = format.name().toLowerCase();
      JsonElement formatElement = component.get(formatName);

      if (formatElement == null)
        continue;

      if (!(formatElement instanceof JsonPrimitive))
        throw new IllegalStateException("Encountered unparsable format \"" + formatName + "\" on: " + component);

      if (!formatElement.getAsBoolean())
        continue;

      result.enableFormat(format);
    }

    JsonElement extra = component.get("extra");

    if (extra == null)
      return result;

    if (!(extra instanceof JsonArray))
      throw new IllegalStateException("Expected \"extra\" to be an array: " + component);

    for (JsonElement child : (JsonArray) extra) {
      if (!(child instanceof JsonObject))
        throw new IllegalStateException("Expected all \"extra\"-entries to be objects: " + component);

      result.extra.add(translateJsonComponent((JsonObject) child, result));
    }

    return result;
  }

  private static int measureFontHeight(ChatComponent component) throws Exception {
    Font font = selectFontVariant(component.hasFormat(Format.BOLD), component.hasFormat(Format.ITALIC));
    FontMetrics fontMetrics = DUMMY_GRAPHICS.getFontMetrics(font);
    return fontMetrics.getHeight();
  }

  private static int measureTextWidth(String input, ChatComponent component) throws Exception {
    Font font = selectFontVariant(component.hasFormat(Format.BOLD), component.hasFormat(Format.ITALIC));
    FontMetrics fontMetrics = DUMMY_GRAPHICS.getFontMetrics(font);

    int width = 0;

    for (int index = 0; index < input.length(); ++index)
      width += fontMetrics.charWidth(input.charAt(index));

    return width;
  }

  private static Color colorFromPacked(long packedColor) {
    return new Color(
      (int) PackedColor.getR(packedColor),
      (int) PackedColor.getG(packedColor),
      (int) PackedColor.getB(packedColor),
      (int) PackedColor.getA(packedColor)
    );
  }

  private static void renderComponent(Graphics2D graphics, Dimension offset, ChatComponent component, SlotContext context) throws Exception {
    Font font = selectFontVariant(component.hasFormat(Format.BOLD), component.hasFormat(Format.ITALIC));

    graphics.setFont(font);

    Color color = component.getColor();

    Color shadowColor = component.getShadowColor();

    if (shadowColor == null) {
      if (color != null)
        shadowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), context.defaultStyle.packedShadowColorOpacity);
      else
        shadowColor = colorFromPacked(context.defaultStyle.packedShadowColor);
    }

    if (color == null)
      color = colorFromPacked(context.defaultStyle.packedColor);

    graphics.setColor(shadowColor);
    graphics.drawString(component.text, offset.width + SHADOW_DELTA_X, offset.height + SHADOW_DELTA_Y);

    int underlineX = offset.width;
    int underlineY = offset.height + UNDERLINE_MARGIN;

    int strikethroughX = offset.width;
    int strikethroughY = offset.height + STRIKETHROUGH_MARGIN;

    Stroke oldStroke = graphics.getStroke();
    Stroke newStroke = new BasicStroke(LINE_THICKNESS);

    graphics.setStroke(newStroke);

    assert component.selfDimension != null;

    if (component.hasFormat(Format.UNDERLINED)) {
      underlineX += SHADOW_DELTA_X;
      underlineY += SHADOW_DELTA_Y;

      graphics.setColor(shadowColor);
      graphics.drawLine(underlineX, underlineY, underlineX + component.selfDimension.width, underlineY);

      underlineX -= SHADOW_DELTA_X;
      underlineY -= SHADOW_DELTA_Y;
    }

    if (component.hasFormat(Format.STRIKETHROUGH)) {
      strikethroughX += SHADOW_DELTA_X;
      strikethroughY += SHADOW_DELTA_Y;

      graphics.setColor(shadowColor);
      graphics.drawLine(strikethroughX, strikethroughY, strikethroughX + component.selfDimension.width, strikethroughY);

      strikethroughX -= SHADOW_DELTA_X;
      strikethroughY -= SHADOW_DELTA_Y;
    }

    graphics.setStroke(oldStroke);

    graphics.setColor(color);
    graphics.drawString(component.text, offset.width, offset.height);

    graphics.setStroke(newStroke);

    if (component.hasFormat(Format.UNDERLINED)) {
      graphics.setColor(color);
      graphics.drawLine(underlineX, underlineY, underlineX + component.selfDimension.width, underlineY);
    }

    if (component.hasFormat(Format.STRIKETHROUGH)) {
      graphics.setColor(color);
      graphics.drawLine(strikethroughX, strikethroughY, strikethroughX + component.selfDimension.width, strikethroughY);
    }

    if (component.hasFormat(Format.OBFUSCATED))
      throw new IllegalStateException("Obfuscation cannot be (or is not yet) an implemented format");

    graphics.setStroke(oldStroke);

    Dimension childOffset = new Dimension(offset.width + component.selfDimension.width, offset.height);

    for (ChatComponent child : component.extra) {
      renderComponent(graphics, childOffset, child, context);
      assert child.totalDimension != null;
      childOffset.width += child.totalDimension.width;
    }
  }

  private static Font selectFontVariant(boolean bold, boolean italic) throws Exception {
    if (bold && italic)
      return getFontBoldItalic();

    if (bold)
      return getFontBold();

    if (italic)
      return getFontItalic();

    return getFontRegular();
  }

  private static Font getFontRegular() throws Exception {
    if (FONT_REGULAR == null)
      FONT_REGULAR = loadFont("/font/MinecraftRegular.otf");

    return FONT_REGULAR;
  }

  private static Font getFontBold() throws Exception {
    if (FONT_BOLD == null)
      FONT_BOLD = loadFont("/font/MinecraftBold.otf");

    return FONT_BOLD;
  }

  private static Font getFontItalic() throws Exception {
    if (FONT_ITALIC == null)
      FONT_ITALIC = loadFont("/font/MinecraftItalic.otf");

    return FONT_ITALIC;
  }

  private static Font getFontBoldItalic() throws Exception {
    if (FONT_BOLD_ITALIC == null)
      FONT_BOLD_ITALIC = loadFont("/font/MinecraftBoldItalic.otf");

    return FONT_BOLD_ITALIC;
  }

  private static Font loadFont(String resourcePath) throws Exception {
    try (
      InputStream is = ChatRenderer.class.getResourceAsStream(resourcePath)
    ) {
      if (is == null)
        throw new RuntimeException("Font not found: " + resourcePath);

      return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(FONT_SIZE);
    }
  }
}