package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.gradient;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class GradientGenerator {

  public final List<Color> colors;
  public final double[] offsets;
  public final long[] zIndices;

  public GradientGenerator(List<Color> colors, double[] offsets, long[] zIndices) {
    this.colors = colors;
    this.zIndices = zIndices;
    this.offsets = clampAndPossiblyExtendOffsets(colors.size(), offsets);
  }

  private double[] clampAndPossiblyExtendOffsets(int colorCount, double[] offsets) {
    int providedOffsets = offsets.length;
    int missingOffsets = colorCount - providedOffsets;

    Arrays.sort(offsets);

    if (missingOffsets > 0) {
      double[] newOffsets = new double[colorCount];
      System.arraycopy(offsets, 0, newOffsets, 0, offsets.length);
      offsets = newOffsets;
    }

    for (int index = 0; index < providedOffsets; ++index) {
      double currentOffset = offsets[index];

      if (currentOffset < 0)
        offsets[index] = 0;

      else if (currentOffset > 100)
        offsets[index] = 100;
    }

    if (missingOffsets == 0)
      return offsets;

    if (missingOffsets == 1) {
      offsets[providedOffsets] = 100;
      return offsets;
    }

    double highestOffset = offsets[Math.max(0, providedOffsets - 1)];

    if (highestOffset == 100) {
      Arrays.fill(offsets, providedOffsets, offsets.length, 100);
      return offsets;
    }

    double nextValue = highestOffset;
    double stepSize;

    if (providedOffsets == 0)
      stepSize = (100 - highestOffset) / (missingOffsets - 1);
    else {
      stepSize = (100 - highestOffset) / missingOffsets;
      nextValue += stepSize;
    }

    for (int index = providedOffsets; index < offsets.length; ++index) {
      double currentValue = nextValue;

      nextValue += stepSize;

      if (currentValue > 100)
        currentValue = 100;

      offsets[index] = currentValue;
    }

    return offsets;
  }

  public Color getColor(double progressionPercentage) {
    int colorCount = colors.size();

    if (colorCount == 0)
      return Color.BLACK;

    Color firstColor = colors.get(0);

    if (colorCount == 1)
      return firstColor;

    double firstOffset = offsets[0];

    int lastIndex = colors.size() - 1;
    Color lastColor = colors.get(lastIndex);
    double lastOffset = offsets[lastIndex];

    long firstZIndex = zIndices.length == 0 ? 0 : zIndices[0];
    long lastZIndex = lastIndex >= zIndices.length ? 0 : zIndices[lastIndex];

    Color aColor = firstColor, bColor = lastColor;
    double aOffset = firstOffset, bOffset = lastOffset;
    long aZIndex = firstZIndex, bZIndex = lastZIndex;

    for (int i = 1; i < colorCount - 1; i++) {
      Color currentColor = colors.get(i);
      double currentOffset = offsets[i];
      long currentZIndex = i >= zIndices.length ? 0 : zIndices[i];

      if (currentOffset < progressionPercentage && currentOffset >= aOffset) {
        if (currentZIndex >= aZIndex) {
          aColor = currentColor;
          aOffset = currentOffset;
          aZIndex = currentZIndex;
        }
      }

      if (currentOffset >= progressionPercentage && currentOffset <= bOffset) {
        if (currentZIndex >= bZIndex) {
          bColor = currentColor;
          bOffset = currentOffset;
          bZIndex = currentZIndex;
        }
      }
    }

    if (bOffset == aOffset)
      return bZIndex > aZIndex ? bColor : aColor;

    // Relativize the percentage to that smaller gradient section
    // How far into the sub-gradient is that point, from 0 to 1,
    // which is the ratio from the length travelled on the whole gradient
    // to get from A to percentage, divided by the span of A and B.
    progressionPercentage = (progressionPercentage - aOffset) / (bOffset - aOffset);
    progressionPercentage = Math.max(0, Math.min(1, progressionPercentage));

    double resultRed   = aColor.getRed()   + progressionPercentage * (bColor.getRed()   - aColor.getRed());
    double resultGreen = aColor.getGreen() + progressionPercentage * (bColor.getGreen() - aColor.getGreen());
    double resultBlue  = aColor.getBlue()  + progressionPercentage * (bColor.getBlue()  - aColor.getBlue());

    return new Color(
      (int) Math.floor(resultRed),
      (int) Math.floor(resultGreen),
      (int) Math.floor(resultBlue)
    );
  }
}
