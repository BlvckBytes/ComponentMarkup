/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform;

/*
  Credits to: https://stackoverflow.com/a/77687937
 */
public class SRGBColorSpace {
  private static SRGBColorSpace sRGBColorSpace;

  public static SRGBColorSpace getInstance() {
    if (sRGBColorSpace == null) {
      sRGBColorSpace = new SRGBColorSpace();
    }

    return sRGBColorSpace;
  }

  private float fTo(float t) {
    if (t <= 0.04045f) {
      return t / 12.92f;
    } else {
      return (float) Math.pow((t + 0.055) / 1.055, 2.4);
    }
  }

  public float[] toCIEXYZ(float[] colorValue) {
    // Also normalize RGB values here.
    float r = fTo(colorValue[0] / 255.0f);
    float g = fTo(colorValue[1] / 255.0f);
    float b = fTo(colorValue[2] / 255.0f);

    //  Use D50 chromatically adapted matrix here as Photoshop does.
    float X = (0.4360747f * r) + (0.3850649f * g) + (0.1430804f * b);
    float Y = (0.2225045f * r) + (0.7168786f * g) + (0.0606169f * b);
    float Z = (0.0139322f * r) + (0.0971045f * g) + (0.7141733f * b);

    return new float[] {X, Y, Z};
  }
}