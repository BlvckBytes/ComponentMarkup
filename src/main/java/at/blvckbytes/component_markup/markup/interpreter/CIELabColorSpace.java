package at.blvckbytes.component_markup.markup.interpreter;

import java.awt.color.ColorSpace;

/*
  Credits to: https://stackoverflow.com/a/77687937
 */
public class CIELabColorSpace extends ColorSpace {

  private static CIELabColorSpace cieLabColorSpace;
  private final float delta = 24.0f / 116.0f;

  // We use illuminant D50 "CIE 1931 2 Degree Standard Observer" as Photoshop does.
  // Values are normalized.
  private static final float Xn = 0.964212f;
  private static final float Yn = 1.0f;
  private static final float Zn = 0.825188f;

  private CIELabColorSpace() {
    super(ColorSpace.TYPE_Lab, 3);
  }

  public static CIELabColorSpace getInstance() {
    if (cieLabColorSpace == null) {
      cieLabColorSpace = new CIELabColorSpace();
    }

    return cieLabColorSpace;
  }

  @Override
  public float[] toRGB(float[] colorValue) {
    return SRGBColorSpace.getInstance().fromCIEXYZ(toCIEXYZ(colorValue));
  }

  @Override
  public float[] fromRGB(float[] rgbValue) {
    return fromCIEXYZ(SRGBColorSpace.getInstance().toCIEXYZ(rgbValue));
  }

  private float fTo(float t) {
    if (t > delta) {
      return (float) Math.pow(t, 3);
    } else {
      return (t - (16.0f / 116.0f)) / 7.787f;
    }
  }

  @Override
  public float[] toCIEXYZ(float[] colorValue) {
    float tY = (colorValue[0] + 16.0f) / 116.0f;
    float tX = (colorValue[1] / 500.0f) + tY;
    float tZ = tY - (colorValue[2] / 200.0f);

    float X = Xn * fTo(tX);
    float Y = Yn * fTo(tY);
    float Z = Zn * fTo(tZ);

    return new float[] {X, Y, Z};
  }

  private float fFrom(float t) {
    if (t > (float) Math.pow(delta, 3)) {
      return (float) Math.cbrt(t);
    } else {
      return (7.787f * t) + (16.0f / 116.0f);
    }
  }

  @Override
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