package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.rainbow;

import at.blvckbytes.component_markup.markup.interpreter.PackedColor;

public class RainbowGenerator {

  public int getPackedColor(double progressionPercentage) {
    float h = (float) ((progressionPercentage / 100) * 6);

    int i = (int) Math.floor(h);

    float f = h - i;
    float q = 1 - f;

    if (i == 0)
      return PackedColor.of(1, f, 0);

    if (i == 1)
      return PackedColor.of(q, 1, 0);

    if (i == 2)
      return PackedColor.of(0, 1, f);

    if (i == 3)
      return PackedColor.of(0, q, 1);

    if (i == 4)
      return PackedColor.of(f, 0, 1);

    return PackedColor.of(1, 0, q);
  }
}
