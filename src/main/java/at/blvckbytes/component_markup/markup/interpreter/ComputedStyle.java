/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.platform.*;
import at.blvckbytes.component_markup.util.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.logging.Level;

public class ComputedStyle {

  public long packedColor = PackedColor.NULL_SENTINEL;
  public long packedShadowColor = PackedColor.NULL_SENTINEL;
  public int packedShadowColorOpacity;
  public @Nullable String font;
  public @Nullable InputView fontPosition;
  public int formats;
  public boolean reset;

  public boolean doStylesEqual(@Nullable ComputedStyle other) {
    if (other == null)
      return hasNoEffect();

    if (packedColor != other.packedColor)
      return false;

    if (packedShadowColor != other.packedShadowColor)
      return false;

    if (formats != other.formats)
      return false;

    return Objects.equals(font, other.font);
  }

  public static @Nullable ComputedStyle addMissing(@Nullable ComputedStyle styleToExtend, @Nullable ComputedStyle addition) {
    if (styleToExtend == null && addition == null)
      return null;

    if (styleToExtend == null)
      return addition.copy();

    styleToExtend.addMissing(addition);

    return styleToExtend;
  }

  public boolean hasNoEffect() {
    if (this.packedColor != PackedColor.NULL_SENTINEL)
      return false;

    if (this.packedShadowColor != PackedColor.NULL_SENTINEL)
      return false;

    if (this.font != null)
      return false;

    if (!TriStateBitFlags.isAllNulls(formats))
      return false;

    return !this.reset;
  }

  public void addMissing(@Nullable ComputedStyle other) {
    if (other == null)
      return;

    if (this.packedColor == PackedColor.NULL_SENTINEL)
      this.packedColor = other.packedColor;

    if (this.packedShadowColor == PackedColor.NULL_SENTINEL)
      this.packedShadowColor = other.packedShadowColor;

    if (this.font == null) {
      this.font = other.font;
      this.fontPosition = other.fontPosition;
    }

    if (TriStateBitFlags.isAllNulls(this.formats)) {
      this.formats = other.formats;
      return;
    }

    if (TriStateBitFlags.isAllNulls(other.formats))
      return;

    for (int index = 0; index < Format.COUNT; ++index) {
      if (TriStateBitFlags.read(this.formats, index) != TriState.NULL)
        continue;

      TriState otherState = TriStateBitFlags.read(other.formats, index);

      this.formats = TriStateBitFlags.write(this.formats, index, otherState);
    }
  }

  public void subtractStylesOnEquality(@Nullable ComputedStyle other, boolean equal) {
    if (other == null) {
      if (!equal)
        clearStylesButReset();

      return;
    }

    if (this.font != null && (this.font.equals(other.font) ^ (!equal)))
      this.font = null;

    if (this.packedColor != PackedColor.NULL_SENTINEL && ((this.packedColor == other.packedColor) ^ (!equal)))
      this.packedColor = PackedColor.NULL_SENTINEL;

    if (this.packedShadowColor != PackedColor.NULL_SENTINEL && ((this.packedShadowColor == other.packedShadowColor) ^ (!equal)))
      this.packedShadowColor = PackedColor.NULL_SENTINEL;

    if (TriStateBitFlags.isAllNulls(this.formats))
      return;

    for (int index = 0; index < Format.COUNT; ++index) {
      TriState thisFormat = TriStateBitFlags.read(this.formats, index);

      if (thisFormat == TriState.NULL)
        continue;

      TriState otherFormat = TriStateBitFlags.read(other.formats, index);

      if ((thisFormat == otherFormat) ^ (!equal))
        this.formats = TriStateBitFlags.write(this.formats, index, TriState.NULL);
    }
  }

  public void subtractStylesOnCommonality(@Nullable ComputedStyle other, boolean common) {
    if (other == null) {
      if (!common)
        clearStylesButReset();

      return;
    }

    if (this.font != null && ((other.font == null) ^ common))
      this.font = null;

    if (this.packedColor != PackedColor.NULL_SENTINEL && ((other.packedColor == PackedColor.NULL_SENTINEL) ^ common))
      this.packedColor = PackedColor.NULL_SENTINEL;

    if (this.packedShadowColor != PackedColor.NULL_SENTINEL && ((other.packedShadowColor == PackedColor.NULL_SENTINEL) ^ common))
      this.packedShadowColor = PackedColor.NULL_SENTINEL;

    if (TriStateBitFlags.isAllNulls(this.formats))
      return;

    if (common && TriStateBitFlags.isAllNulls(other.formats))
      return;

    for (int index = 0; index < Format.COUNT; ++index) {
      TriState thisFormat = TriStateBitFlags.read(this.formats, index);

      if (thisFormat == TriState.NULL)
        continue;

      TriState otherFormat = TriStateBitFlags.read(other.formats, index);

      if ((otherFormat == TriState.NULL) ^ common)
        this.formats = TriStateBitFlags.write(this.formats, index, TriState.NULL);
    }
  }

  public void addMissingDefaults(@Nullable ComputedStyle mask, SlotContext slotContext) {
    if (mask == null)
      return;

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
      if (mask.font != null && !mask.font.equals(defaultStyle.font)) {
        this.font = defaultStyle.font;
        this.fontPosition = null;
      }
    }

    // Nothing to set to default or no default value provided
    if (TriStateBitFlags.isAllNulls(mask.formats) || TriStateBitFlags.isAllNulls(defaultStyle.formats))
      return;

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
  }

  public ComputedStyle copy() {
    ComputedStyle result = new ComputedStyle();
    result.packedColor = this.packedColor;
    result.packedShadowColor = this.packedShadowColor;
    result.font = this.font;
    result.fontPosition = this.fontPosition;
    result.formats = this.formats;
    result.reset = this.reset;
    return result;
  }

  public static @Nullable ComputedStyle computeFor(MarkupNode node, Interpreter interpreter) {
    if (!(node instanceof StyledNode))
      return null;

    NodeStyle style = ((StyledNode) node).getStyle();

    if (style == null)
      return null;

    ComponentConstructor componentConstructor = interpreter.getComponentConstructor();

    ComputedStyle result = null;

    if (style.color != null) {
      String colorString = interpreter.evaluateAsStringOrNull(style.color);

      if (colorString != null) {
        if (!componentConstructor.doesSupport(PlatformFeature.COLOR)) {
          for (String line : ErrorScreen.make(style.color.getFirstMemberPositionProvider(), "Custom colors are not supported on this platform"))
            LoggerProvider.log(Level.WARNING, line, false);
        }

        long packedColor = PackedColor.tryParse(colorString);

        if (packedColor != PackedColor.NULL_SENTINEL) {
          result = new ComputedStyle();
          result.packedColor = packedColor;
        }
      }
    }

    if (style.shadowColor != null || style.shadowColorOpacity != null) {
      // Default Minecraft shadow-behaviour: color=(foreground || #000000) opacity=25%
      long packedColor = AnsiStyleColor.BLACK.packedColor;

      //noinspection ConstantValue
      if (result != null && result.packedColor != PackedColor.NULL_SENTINEL)
        packedColor = result.packedColor;

      ExpressionNode firstNonNullValue = null;
      int opacity = 63;

      if (style.shadowColor != null) {
        String colorString = interpreter.evaluateAsStringOrNull(style.shadowColor);

        if (colorString != null) {
          firstNonNullValue = style.shadowColor;

          long parsedPackedColor = PackedColor.tryParse(colorString);

          if (parsedPackedColor != PackedColor.NULL_SENTINEL)
            packedColor = parsedPackedColor;
        }
      }

      if (style.shadowColorOpacity != null) {
        Double opacityValue = interpreter.evaluateAsDoubleOrNull(style.shadowColorOpacity);

        if (opacityValue != null) {
          if (firstNonNullValue == null)
            firstNonNullValue = style.shadowColorOpacity;

          opacity = (int) Math.round((opacityValue / 100.0) * 255);
        }
      }

      if (firstNonNullValue != null) {
        if (!componentConstructor.doesSupport(PlatformFeature.SHADOW_COLOR)) {
          for (String line : ErrorScreen.make(firstNonNullValue.getFirstMemberPositionProvider(), "Custom shadow-colors are not supported on this platform"))
            LoggerProvider.log(Level.WARNING, line, false);
        }

        if (result == null)
          result = new ComputedStyle();

        result.packedShadowColor = PackedColor.setClampedA(packedColor, opacity);
        result.packedShadowColorOpacity = opacity;
      }
    }

    if (style.font != null) {
      String font = interpreter.evaluateAsStringOrNull(style.font);

      if (font != null) {
        if (!componentConstructor.doesSupport(PlatformFeature.FONT)) {
          for (String line : ErrorScreen.make(style.font.getFirstMemberPositionProvider(), "Custom fonts are not supported on this platform"))
            LoggerProvider.log(Level.WARNING, line, false);
        }

        if (result == null)
          result = new ComputedStyle();

        result.font = font;
        result.fontPosition = style.font.getFirstMemberPositionProvider();
      }
    }

    if (style.reset != null) {
      if (result == null)
        result = new ComputedStyle();

      result.reset = interpreter.evaluateAsBoolean(style.reset);
    }

    for (Format format : Format.VALUES) {
      ExpressionNode formatExpression = style.getFormat(format);

      if (formatExpression == null)
        continue;

      TriState value = interpreter.evaluateAsTriState(formatExpression);

      if (value == TriState.NULL)
        continue;

      if (!componentConstructor.doesSupport(format.feature)) {
        for (String line : ErrorScreen.make(formatExpression.getFirstMemberPositionProvider(), "The " + format.name().toLowerCase() + "-format is not supported on this platform"))
          LoggerProvider.log(Level.WARNING, line, false);
      }

      if (result == null)
        result = new ComputedStyle();

      result.formats = TriStateBitFlags.write(result.formats, format.ordinal(), value);
    }

    return result;
  }

  public void applyStyles(Object component, ComponentConstructor componentConstructor) {
    PlatformWarning.clear();

    if (packedColor != PackedColor.NULL_SENTINEL && componentConstructor.doesSupport(PlatformFeature.COLOR))
      componentConstructor.setColor(component, packedColor);

    if (packedShadowColor != PackedColor.NULL_SENTINEL && componentConstructor.doesSupport(PlatformFeature.SHADOW_COLOR))
      componentConstructor.setShadowColor(component, packedShadowColor);

    if (font != null && componentConstructor.doesSupport(PlatformFeature.FONT)) {
      componentConstructor.setFont(component, font);

      if (fontPosition != null)
        PlatformWarning.logIfEmitted(PlatformWarning.MALFORMED_FONT_NAME, fontPosition, font);
      else {
        PlatformWarning.callIfEmitted(
          PlatformWarning.MALFORMED_FONT_NAME,
          () -> LoggerProvider.log(Level.WARNING, "Encountered an invalid default font-value: \"" + font + "\"")
        );
      }
    }

    if (TriStateBitFlags.isAllNulls(formats))
      return;

    for (Format format : Format.VALUES) {
      TriState state = TriStateBitFlags.read(formats, format.ordinal());

      // As of now, there's no need to ever remove formats again, so don't call into the constructor
      if (state == TriState.NULL)
        continue;

      if (!componentConstructor.doesSupport(format.feature))
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
          LoggerProvider.log(Level.WARNING, "Encountered unknown format: " + format.name());
      }
    }
  }

  @JsonifyGetter
  @SuppressWarnings("unused")
  public String readableFormats() {
    StringJoiner result = new StringJoiner(", ");

    for (Format format : Format.VALUES) {
      TriState value = TriStateBitFlags.read(this.formats, format.ordinal());
      result.add(format.name() + ": " + value.name());
    }

    return result.toString();
  }

  @JsonifyGetter
  @SuppressWarnings("unused")
  public String readableColor() {
    return readablePackedColor(this.packedColor);
  }

  @JsonifyGetter
  @SuppressWarnings("unused")
  public String readableShadowColor() {
    return readablePackedColor(this.packedShadowColor);
  }

  private String readablePackedColor(long packedColor) {
    if (packedColor == PackedColor.NULL_SENTINEL)
      return "null";

    AnsiStyleColor ansiColor = AnsiStyleColor.fromColor(packedColor);

    if (ansiColor != null)
      return ansiColor.name;

    return PackedColor.asAlphaHex(packedColor);
  }

  private void clearStylesButReset() {
    this.packedColor = PackedColor.NULL_SENTINEL;
    this.packedShadowColor = PackedColor.NULL_SENTINEL;
    this.packedShadowColorOpacity = 0;
    this.font = null;
    this.formats = 0;
  }
}
