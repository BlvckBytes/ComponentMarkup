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
    char[] result = new char[7];

    result[0] = '#';

    toHex(result, 1, color.getRed());
    toHex(result, 3, color.getGreen());
    toHex(result, 5, color.getBlue());

    return new String(result);
  }

  private void toHex(char[] output, int offset, int input) {
    int firstDigit = (input >> 4) & 0xF;
    int secondDigit = input & 0xF;

    output[offset] = firstDigit < 10 ? (char) ('0' + firstDigit) : (char) ('A' + (firstDigit - 10));
    output[offset + 1] = secondDigit < 10 ? (char) ('0' + secondDigit) : (char) ('A' + (secondDigit - 10));
  }

  public String asAlphaHex() {
    char[] result = new char[9];

    result[0] = '#';

    toHex(result, 1, color.getRed());
    toHex(result, 3, color.getGreen());
    toHex(result, 5, color.getBlue());
    toHex(result, 7, color.getAlpha());

    return new String(result);
  }

  @Override
  public Color getColor() {
    return this.color;
  }
}
