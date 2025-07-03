package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import org.jetbrains.annotations.Nullable;

public class ComputedStyle {

  public int packedColor = PackedColor.NULL_SENTINEL;
  public int packedShadowColor = PackedColor.NULL_SENTINEL;
  public @Nullable String font;
  public @Nullable Boolean @Nullable[] formats;

  public ComputedStyle() {}

  public ComputedStyle setFormat(Format format, @Nullable Boolean value) {
    if (this.formats == null)
      this.formats = new Boolean[Format.VALUES.size()];

    this.formats[format.ordinal()] = value;
    return this;
  }

  public ComputedStyle setFont(@Nullable String font) {
    this.font = font;
    return this;
  }

  public ComputedStyle setColor(int packedColor) {
    this.packedColor = packedColor;
    return this;
  }

  public ComputedStyle setShadowColor(int packedShadowColor) {
    this.packedShadowColor = packedShadowColor;
    return this;
  }

  public ComputedStyle copy() {
    ComputedStyle result = new ComputedStyle();
    result.packedColor = this.packedColor;
    result.packedShadowColor = this.packedShadowColor;
    result.font = this.font;

    if (this.formats != null) {
      result.formats = new Boolean[this.formats.length];
      System.arraycopy(this.formats, 0, result.formats, 0, this.formats.length);
    }

    return result;
  }

  public ComputedStyle(StyledNode styleHolder, Interpreter interpreter) {
    NodeStyle style = styleHolder.getStyle();

    if (style == null)
      return;

    if (style.color != null) {
      String colorString = interpreter.evaluateAsStringOrNull(style.color);

      if (colorString != null) {
        int packedColor = PackedColor.tryParse(colorString);

        if (packedColor != PackedColor.NULL_SENTINEL)
          this.packedColor = packedColor;
      }
    }

    if (style.shadowColor != null || style.shadowColorOpacity != null) {
      // Default Minecraft shadow-behaviour: color=#000000 opacity=25%
      int packedColor = AnsiStyleColor.BLACK.packedColor;
      int opacity = 64;

      if (style.shadowColor != null) {
        String colorString = interpreter.evaluateAsStringOrNull(style.shadowColor);

        if (colorString != null) {
          int parsedPackedColor = PackedColor.tryParse(colorString);

          if (parsedPackedColor != PackedColor.NULL_SENTINEL)
            packedColor = parsedPackedColor;
        }
      }

      if (style.shadowColorOpacity != null) {
        Double opacityValue = interpreter.evaluateAsDoubleOrNull(style.shadowColorOpacity);

        if (opacityValue != null)
          opacity = (int) Math.round((opacityValue / 100.0) * 255);
      }

      this.packedShadowColor = PackedColor.setClampedA(packedColor, opacity);
    }

    if (style.font != null)
      this.font = interpreter.evaluateAsStringOrNull(style.font);

    for (Format format : Format.VALUES) {
      ExpressionNode formatExpression = style.formatStates[format.ordinal()];

      if (formatExpression == null)
        continue;

      Boolean value = interpreter.evaluateAsBooleanOrNull(formatExpression);

      if (value == null)
        continue;

      if (this.formats == null)
        this.formats = new Boolean[style.formatStates.length];

      this.formats[format.ordinal()] = value;
    }
  }

  public void applyStyles(Object component, ComponentConstructor componentConstructor) {
    if (packedColor != PackedColor.NULL_SENTINEL)
      componentConstructor.setColor(component, packedColor);

    if (packedShadowColor != PackedColor.NULL_SENTINEL)
      componentConstructor.setShadowColor(component, packedShadowColor);

    if (font != null)
      componentConstructor.setFont(component, font);

    if (formats != null) {
      for (Format format : Format.VALUES) {
        Boolean value = formats[format.ordinal()];

        if (value == null)
          continue;

        switch (format) {
          case BOLD:
            componentConstructor.setBoldFormat(component, value);
            break;

          case ITALIC:
            componentConstructor.setItalicFormat(component, value);
            break;

          case OBFUSCATED:
            componentConstructor.setObfuscatedFormat(component, value);
            break;

          case UNDERLINED:
            componentConstructor.setUnderlinedFormat(component, value);
            break;

          case STRIKETHROUGH:
            componentConstructor.setStrikethroughFormat(component, value);
            break;

          // TODO: Rather log than crash
          default:
            throw new IllegalStateException("Unknown format: " + format.name());
        }
      }
    }
  }
}
