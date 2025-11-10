/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.util.color;

/*
  Credits to: https://stackoverflow.com/a/77687937
 */
public class CIELabColorSpace {

  private static CIELabColorSpace cieLabColorSpace;

  // We use illuminant D50 "CIE 1931 2 Degree Standard Observer" as Photoshop does.
  // Values are normalized.
  private static final float Xn = 0.964212f;
  private static final float Yn = 1.0f;
  private static final float Zn = 0.825188f;

  public static CIELabColorSpace getInstance() {
    if (cieLabColorSpace == null) {
      cieLabColorSpace = new CIELabColorSpace();
    }

    return cieLabColorSpace;
  }

  public float[] fromRGB(float[] rgbValue) {
    return fromCIEXYZ(SRGBColorSpace.getInstance().toCIEXYZ(rgbValue));
  }

  private float fFrom(float t) {
    float delta = 24.0f / 116.0f;
    if (t > (float) Math.pow(delta, 3)) {
      return (float) Math.cbrt(t);
    } else {
      return (7.787f * t) + (16.0f / 116.0f);
    }
  }

  public float[] fromCIEXYZ(float[] colorValue) {
    float X = colorValue[0] / Xn;
    float Y = colorValue[1] / Yn;
    float Z = colorValue[2] / Zn;

    float L = ((116.0f * fFrom(Y)) - 16.0f);
    float a = (500.0f * (fFrom(X) - fFrom(Y)));
    float b = (200.0f * (fFrom(Y) - fFrom(Z)));

    return new float[] {L, a, b};
  }
}