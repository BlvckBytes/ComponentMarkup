package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

import java.awt.*;

public interface ComponentColor {

  static @Nullable ComponentColor tryParse(String input) {
    int inputLength = input.length();

    if (inputLength < 2)
      return null;

    char firstChar = input.charAt(0);

    if (inputLength == 2 && firstChar == '&')
      return AnsiStyleColor.fromCharOrNull(input.charAt(1));

    if (firstChar == '#' && (inputLength == 7 || inputLength == 9)) {
      try {
        int r = Integer.parseInt(input.substring(1, 3), 16);
        int g = Integer.parseInt(input.substring(3, 5), 16);
        int b = Integer.parseInt(input.substring(5, 7), 16);
        int a = inputLength == 7 ? 255 : Integer.parseInt(input.substring(7, 9), 16);

        return new ModernColor(new Color(r, g, b, a));
      } catch (Throwable error) {
        return null;
      }
    }

    return AnsiStyleColor.fromNameLowerOrNull(input.toLowerCase());
  }

  default int getPackedARGB() {
    if (this instanceof AnsiStyleColor)
      return ((AnsiStyleColor) this).color.getRGB();

    if (this instanceof ModernColor)
      return ((ModernColor) this).color.getRGB();

    return 0;
  }
}
