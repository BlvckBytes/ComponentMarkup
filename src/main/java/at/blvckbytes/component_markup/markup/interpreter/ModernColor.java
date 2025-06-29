package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ModernColor implements ComponentColor {

  public final Color color;
  private @Nullable AnsiStyleColor nearestAnsi;

  public ModernColor(Color color) {
    this.color = color;
  }

  public AnsiStyleColor getNearestAnsi() {
    if (nearestAnsi == null)
      nearestAnsi = AnsiStyleColor.getNearestColor(this.color);

    return nearestAnsi;
  }

  public String asNonAlphaHex() {
    return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
  }

  public String asAlphaHex() {
    return String.format("#%02X%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
  }
}
