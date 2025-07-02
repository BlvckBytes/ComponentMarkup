package at.blvckbytes.component_markup.markup.interpreter;

public class PackedColor {

  public static final int NULL_SENTINEL = -1;

  public static int of(int r, int g, int b, int a) {
    return (b & 0xFF) | ((g & 0xFF) << 8) | ((r & 0xFF) << 16) | ((a & 0xFF) << 24);
  }

  public static int of(float r, float g, float b) {
    return of((int) (r * 255 + .5), (int) (g * 255 + .5), (int) (b * 255 + .5), 255);
  }

  public static int getA(int packedColor) {
    return (packedColor >> 24) & 0xFF;
  }

  public static int getR(int packedColor) {
    return (packedColor >> 16) & 0xFF;
  }

  public static int getG(int packedColor) {
    return (packedColor >> 8) & 0xFF;
  }

  public static int getB(int packedColor) {
    return packedColor & 0xFF;
  }

  public static int setClampedA(int packedColor, int a) {
    if (a < 0)
      a = 0;

    return (packedColor & 0xFFFFFF) | ((a & 0xFF) << 24);
  }

  public static String asNonAlphaHex(int packedColor) {
    char[] result = new char[7];

    result[0] = '#';

    toHex(result, 1, getR(packedColor));
    toHex(result, 3, getG(packedColor));
    toHex(result, 5, getB(packedColor));

    return new String(result);
  }

  public static String asAlphaHex(int packedColor) {
    char[] result = new char[9];

    result[0] = '#';

    toHex(result, 1, getR(packedColor));
    toHex(result, 3, getG(packedColor));
    toHex(result, 5, getB(packedColor));
    toHex(result, 7, getA(packedColor));

    return new String(result);
  }

  public static int tryParse(String input) {
    int inputLength = input.length();

    if (inputLength < 2)
      return NULL_SENTINEL;

    char firstChar = input.charAt(0);

    AnsiStyleColor ansiColor;

    if (inputLength == 2 && firstChar == '&') {
      ansiColor = AnsiStyleColor.fromCharOrNull(input.charAt(1));
      return ansiColor == null ? NULL_SENTINEL : ansiColor.packedColor;
    }

    if (firstChar == '#' && (inputLength == 7 || inputLength == 9)) {
      int r = fromHexOrMinusOne(input, 1);

      if (r < 0)
        return NULL_SENTINEL;

      int g = fromHexOrMinusOne(input, 3);

      if (g < 0)
        return NULL_SENTINEL;

      int b = fromHexOrMinusOne(input, 5);

      if (b < 0)
        return NULL_SENTINEL;

      int a = inputLength == 7 ? 255 : fromHexOrMinusOne(input, 7);

      if (a < 0)
        return NULL_SENTINEL;

      return PackedColor.of(r, g, b, a);
    }

    ansiColor = AnsiStyleColor.fromNameLowerOrNull(input.toLowerCase());
    return ansiColor == null ? NULL_SENTINEL : ansiColor.packedColor;
  }

  private static int hexCharToIntOrMinusOne(char c) {
    if (c >= 'a' && c <= 'f')
      return 10 + (c - 'a');

    if (c >= 'A' && c <= 'F')
      return 10 + (c - 'A');

    if (c >= '0' && c <= '9')
      return (c - '0');

    return -1;
  }

  private static int fromHexOrMinusOne(String input, int offset) {
    int result;
    int charValue;

    if ((charValue = hexCharToIntOrMinusOne(input.charAt(offset))) < 0)
      return -1;

    result = charValue;

    if ((charValue = hexCharToIntOrMinusOne(input.charAt(offset + 1))) < 0)
      return -1;

    result += charValue << 4;

    return result;
  }

  private static void toHex(char[] output, int offset, int input) {
    int firstDigit = (input >> 4) & 0xF;
    int secondDigit = input & 0xF;

    output[offset] = firstDigit < 10 ? (char) ('0' + firstDigit) : (char) ('A' + (firstDigit - 10));
    output[offset + 1] = secondDigit < 10 ? (char) ('0' + secondDigit) : (char) ('A' + (secondDigit - 10));
  }
}
