package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.TriState;
import at.blvckbytes.component_markup.util.TriStateBitFlags;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class ComputedStyle extends Jsonifiable {

  public long packedColor = PackedColor.NULL_SENTINEL;
  public long packedShadowColor = PackedColor.NULL_SENTINEL;
  public @Nullable String font;
  public int formats;

  public ComputedStyle() {}

  public boolean hasEffect() {
    if (packedColor != PackedColor.NULL_SENTINEL)
      return true;

    if (packedShadowColor != PackedColor.NULL_SENTINEL)
      return true;

    if(font != null)
      return true;

    return !TriStateBitFlags.isAllNulls(formats);
  }

  public ComputedStyle setFormat(Format format, TriState value) {
    this.formats = TriStateBitFlags.write(formats, format.ordinal(), value);
    return this;
  }

  public ComputedStyle setFont(@Nullable String font) {
    this.font = font;
    return this;
  }

  public ComputedStyle setColor(long packedColor) {
    this.packedColor = packedColor;
    return this;
  }

  public ComputedStyle setShadowColor(long packedShadowColor) {
    this.packedShadowColor = packedShadowColor;
    return this;
  }

  public ComputedStyle addMissing(@Nullable ComputedStyle other) {
    if (other == null)
      return this;

    if (this.packedColor == PackedColor.NULL_SENTINEL)
      this.packedColor = other.packedColor;

    if (this.packedShadowColor == PackedColor.NULL_SENTINEL)
      this.packedShadowColor = other.packedShadowColor;

    if (this.font == null)
      this.font = other.font;

    if (TriStateBitFlags.isAllNulls(this.formats)) {
      this.formats = other.formats;
      return this;
    }

    if (TriStateBitFlags.isAllNulls(other.formats))
      return this;

    for (int index = 0; index < Format.COUNT; ++index) {
      if (TriStateBitFlags.read(this.formats, index) != TriState.NULL)
        continue;

      TriState otherState = TriStateBitFlags.read(other.formats, index);

      this.formats = TriStateBitFlags.write(this.formats, index, otherState);
    }

    return this;
  }

  public void subtractUncommonProperties(@Nullable ComputedStyle other) {
    if (other == null)
      return;

    if (this.font != null && other.font == null)
      this.font = null;

    if (this.packedColor != PackedColor.NULL_SENTINEL && other.packedColor == PackedColor.NULL_SENTINEL)
      this.packedColor = PackedColor.NULL_SENTINEL;

    if (this.packedShadowColor != PackedColor.NULL_SENTINEL && other.packedShadowColor == PackedColor.NULL_SENTINEL)
      this.packedShadowColor = PackedColor.NULL_SENTINEL;

    if (TriStateBitFlags.isAllNulls(this.formats))
      return;

    for (int index = 0; index < Format.COUNT; ++index) {
      TriState thisFormat = TriStateBitFlags.read(this.formats, index);

      if (thisFormat == TriState.NULL)
        continue;

      TriState otherFormat = TriStateBitFlags.read(other.formats, index);

      if (otherFormat == TriState.NULL)
        this.formats = TriStateBitFlags.write(this.formats, index, TriState.NULL);
    }
  }

  public void subtractCommonStyles(@Nullable ComputedStyle other) {
    if (other == null)
      return;

    if (this.font != null && other.font != null)
      this.font = null;

    if (this.packedColor != PackedColor.NULL_SENTINEL && other.packedColor != PackedColor.NULL_SENTINEL)
      this.packedColor = PackedColor.NULL_SENTINEL;

    if (this.packedShadowColor != PackedColor.NULL_SENTINEL && other.packedShadowColor != PackedColor.NULL_SENTINEL)
      this.packedShadowColor = PackedColor.NULL_SENTINEL;

    if (TriStateBitFlags.isAllNulls(this.formats) || TriStateBitFlags.isAllNulls(other.formats))
      return;

    for (int index = 0; index < Format.COUNT; ++index) {
      TriState thisFormat = TriStateBitFlags.read(this.formats, index);

      if (thisFormat != TriState.NULL) {
        TriState otherFormat = TriStateBitFlags.read(other.formats, index);

        if (otherFormat != TriState.NULL)
          this.formats = TriStateBitFlags.write(this.formats, index, TriState.NULL);
      }
    }
  }

  public ComputedStyle subtractEqualStyles(@Nullable ComputedStyle other) {
    if (other == null)
      return this;

    if (this.font != null && this.font.equals(other.font))
      this.font = null;

    if (this.packedColor != PackedColor.NULL_SENTINEL && this.packedColor == other.packedColor)
      this.packedColor = PackedColor.NULL_SENTINEL;

    if (this.packedShadowColor != PackedColor.NULL_SENTINEL && this.packedShadowColor == other.packedShadowColor)
      this.packedShadowColor = PackedColor.NULL_SENTINEL;

    if (TriStateBitFlags.isAllNulls(this.formats) || TriStateBitFlags.isAllNulls(other.formats))
      return this;

    for (int index = 0; index < Format.COUNT; ++index) {
      TriState thisFormat = TriStateBitFlags.read(this.formats, index);

      if (thisFormat != TriState.NULL) {
        TriState otherFormat = TriStateBitFlags.read(other.formats, index);

        if (thisFormat == otherFormat)
          this.formats = TriStateBitFlags.write(this.formats, index, TriState.NULL);
      }
    }

    return this;
  }

  public ComputedStyle applyDefaults(@Nullable ComputedStyle mask, SlotContext slotContext) {
    if (mask == null)
      return this;

    ComputedStyle defaultStyle = slotContext.defaultStyle;

    if (this.packedColor == PackedColor.NULL_SENTINEL) {
      if (mask.packedColor != PackedColor.NULL_SENTINEL && mask.packedColor != defaultStyle.packedColor) {
        this.packedColor = defaultStyle.packedColor;
      }
    }

    if (this.packedShadowColor == PackedColor.NULL_SENTINEL) {
      if (mask.packedShadowColor != PackedColor.NULL_SENTINEL && mask.packedShadowColor != defaultStyle.packedShadowColor)
        this.packedShadowColor = defaultStyle.packedShadowColor;
    }

    if (this.font == null) {
      if (mask.font != null && !mask.font.equals(defaultStyle.font))
        this.font = defaultStyle.font;
    }

    // Nothing to set to default or no default value provided
    if (TriStateBitFlags.isAllNulls(mask.formats) || TriStateBitFlags.isAllNulls(defaultStyle.formats))
      return this;

    for (int index = 0; index < Format.COUNT; ++index) {
      TriState thisFormat = TriStateBitFlags.read(this.formats, index);

      if (thisFormat != TriState.NULL)
        continue;

      TriState maskFormat = TriStateBitFlags.read(mask.formats, index);

      if (maskFormat == TriState.NULL)
        continue;

      TriState defaultFormat = TriStateBitFlags.read(defaultStyle.formats, index);

      if (maskFormat == defaultFormat)
        continue;

      this.formats = TriStateBitFlags.write(this.formats, index, defaultFormat);
    }

    return this;
  }

  public ComputedStyle copy() {
    ComputedStyle result = new ComputedStyle();
    result.packedColor = this.packedColor;
    result.packedShadowColor = this.packedShadowColor;
    result.font = this.font;
    result.formats = this.formats;
    return result;
  }

  public ComputedStyle(StyledNode styleHolder, Interpreter interpreter) {
    NodeStyle style = styleHolder.getStyle();

    if (style == null)
      return;

    if (style.color != null) {
      String colorString = interpreter.evaluateAsStringOrNull(style.color);

      if (colorString != null) {
        long packedColor = PackedColor.tryParse(colorString);

        if (packedColor != PackedColor.NULL_SENTINEL)
          this.packedColor = packedColor;
      }
    }

    if (style.shadowColor != null || style.shadowColorOpacity != null) {
      // Default Minecraft shadow-behaviour: color=#000000 opacity=25%
      long packedColor = AnsiStyleColor.BLACK.packedColor;
      int opacity = 64;

      if (style.shadowColor != null) {
        String colorString = interpreter.evaluateAsStringOrNull(style.shadowColor);

        if (colorString != null) {
          long parsedPackedColor = PackedColor.tryParse(colorString);

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

    for (int index = 0; index < Format.COUNT; ++index) {
      ExpressionNode formatExpression = style.formatStates[index];

      if (formatExpression == null)
        continue;

      TriState value = interpreter.evaluateAsTriState(formatExpression);

      if (value == TriState.NULL)
        continue;

      this.formats = TriStateBitFlags.write(this.formats, index, value);
    }
  }

  public void applyStyles(Object component, ComponentConstructor componentConstructor) {
    if (packedColor != PackedColor.NULL_SENTINEL)
      componentConstructor.setColor(component, packedColor);

    if (packedShadowColor != PackedColor.NULL_SENTINEL)
      componentConstructor.setShadowColor(component, packedShadowColor);

    if (font != null)
      componentConstructor.setFont(component, font);

    if (TriStateBitFlags.isAllNulls(formats))
      return;

    for (Format format : Format.VALUES) {
      TriState state = TriStateBitFlags.read(formats, format.ordinal());

      // As of now, there's no need to ever remove formats again, so don't call into the constructor
      if (state == TriState.NULL)
        continue;

      switch (format) {
        case BOLD:
          componentConstructor.setBoldFormat(component, state);
          break;

        case ITALIC:
          componentConstructor.setItalicFormat(component, state);
          break;

        case OBFUSCATED:
          componentConstructor.setObfuscatedFormat(component, state);
          break;

        case UNDERLINED:
          componentConstructor.setUnderlinedFormat(component, state);
          break;

        case STRIKETHROUGH:
          componentConstructor.setStrikethroughFormat(component, state);
          break;

        default:
          LoggerProvider.get().log(Level.WARNING, "Encountered unknown format: " + format.name());
      }
    }
  }
}
