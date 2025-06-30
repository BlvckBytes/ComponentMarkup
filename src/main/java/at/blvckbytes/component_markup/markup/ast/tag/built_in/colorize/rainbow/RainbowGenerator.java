package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.rainbow;

import java.awt.*;

public class RainbowGenerator {

  public Color getColor(double progressionPercentage) {
    float s = 1;
    float v = 1;
    float h = (float) ((progressionPercentage / 100) * 6);

    int i = (int) Math.floor(h);

    float f = h - i;
    float p = v * (1 - s);
    float q = v * (1 - s * f);
    float t = v * (1 - s * (1 - f));

    if (i == 0)
      return new Color(v, t, p);

    if (i == 1)
      return new Color(q, v, p);

    if (i == 2)
      return new Color(p, v, t);

    if (i == 3)
      return new Color(p, q, v);

    if (i == 4)
      return new Color(t, p, v);

    return new Color(v, p, q);
  }
}
