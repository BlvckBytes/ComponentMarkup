package at.blvckbytes.component_markup.markup.interpreter;

import java.awt.color.ColorSpace;

/*
  Credits to: https://stackoverflow.com/a/77687937
 */
public class SRGBColorSpace extends ColorSpace {
  private static SRGBColorSpace sRGBColorSpace;

  private SRGBColorSpace() {
    super(ColorSpace.TYPE_RGB, 3);
  }

  public static SRGBColorSpace getInstance() {
    if (sRGBColorSpace == null) {
      sRGBColorSpace = new SRGBColorSpace();
    }

    return sRGBColorSpace;
  }

  @Override
  public float[] toRGB(float[] colorValue) {
    return colorValue;
  }

  @Override
  public float[] fromRGB(float[] rgbValue) {
    return rgbValue;
  }

  private float fTo(float t) {
    if (t <= 0.04045f) {
      return t / 12.92f;
    } else {
      return (float) Math.pow((t + 0.055) / 1.055, 2.4);
    }
  }

  @Override
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

  private float fFrom(float t) {
    if (t > 0.0031308f) {
      return (1.055f * ((float) Math.pow(t, 1 / 2.4))) - 0.055f;
    } else {
      return t * 12.92f;
    }
  }

  @Override
  public float[] fromCIEXYZ(float[] colorValue) {
    float X = colorValue[0];
    float Y = colorValue[1];
    float Z = colorValue[2];

    //  Use D50 chromatically adapted matrix as Photoshop does.
    float tR = (3.1338561f * X) + (-1.6168667f * Y) + (-0.4906146f * Z);
    float tG = (-0.9787684f * X) + (1.9161415f * Y) + (0.0334540f * Z);
    float tB = (0.0719453f * X) + (-0.2289914f * Y) + (1.4052427f * Z);

    float r = fFrom(tR) * 255.0f;
    float g = fFrom(tG) * 255.0f;
    float b = fFrom(tB) * 255.0f;

    return new float[] {r, g, b};
  }
}