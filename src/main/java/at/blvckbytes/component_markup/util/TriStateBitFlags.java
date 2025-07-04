package at.blvckbytes.component_markup.util;

public class TriStateBitFlags {

  /*
     value-index ...  7  6  5  4  3  2  1  0
     bit-index   ... .. .. .. 98 76 54 32 10
     input       ... 00 00 00 00 00 00 00 00

     NULL  00
     TRUE  01
     FALSE 10
   */

  public static boolean isAllNulls(int input) {
    return input == 0;
  }

  public static TriState read(int input, int index) {
    switch ((input >> (index << 2)) & 0b11) {
      case 0:
        return TriState.NULL;
      case 1:
        return TriState.TRUE;
      default:
        return TriState.FALSE;
    }
  }

  public static int write(int input, int index, TriState value) {
    int valueBits = 0;

    if (value == TriState.TRUE)
      valueBits = 1;
    else if (value == TriState.FALSE)
      valueBits = 2;

    return (input & (~(0b11 << (index << 2)))) | (valueBits << (index << 2));
  }
}
