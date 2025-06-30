package at.blvckbytes.component_markup.markup.ast.tag.built_in.gradient;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class GradientGenerator {

  public final List<Color> colors;
  public final double[] offsets;

  public GradientGenerator(List<Color> colors, double[] offsets) {
    this.colors = colors;
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

    double highestOffset = offsets[Math.max(0, providedOffsets - 1)];

    if (highestOffset == 100) {
      Arrays.fill(offsets, providedOffsets, offsets.length, 100);
      return offsets;
    }

    double stepSize = missingOffsets == 1 ? 100 : (100 - highestOffset) / (missingOffsets - 1);
    double nextValue = highestOffset;

    if (providedOffsets != 0)
      nextValue += stepSize;

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

    // Quick exit: If the first color has a higher value than 0,
    // the first n percent are that color statically.
    if (progressionPercentage <= firstOffset)
      return firstColor;

    int lastIndex = colors.size() - 1;
    Color lastColor = colors.get(lastIndex);
    double lastOffset = offsets[lastIndex];

    // Quick exit: If the last color has a lower value than 1,
    // the last (1 - n) percent are that color statically.
    if (progressionPercentage >= lastOffset)
      return lastColor;

    // Find the two nearest colors around the current percentage point which
    // will make up the smaller in-between-gradient the caller is interested in

    // Start out assuming that A will be the first and B the last color
    Color aColor = firstColor, bColor = lastColor;
    double aOffset = firstOffset, bOffset = lastOffset;

    // Only iterate from 1 until n-1, as first and last are already active
    for (int i = 1; i < colorCount - 1; i++) {
      Color currentColor = colors.get(i);
      double currentOffset = offsets[i];

      // Set A if the color is below the percentage but higher
      // up than the previous A color
      if (currentOffset < progressionPercentage && currentOffset > aOffset) {
        aColor = currentColor;
        aOffset = currentOffset;
      }

      // Set B if the color is above the percentage but lower
      // down than the previous B color
      // It is important to also allow an exact percentage match here, to not
      // make hitting colors impossible when at their exact percentage
      if (currentOffset >= progressionPercentage && currentOffset < bOffset) {
        bColor = currentColor;
        bOffset = currentOffset;
      }
    }

    // Relativize the percentage to that smaller gradient section
    // How far into the sub-gradient is that point, from 0 to 1,
    // which is the ratio from the length travelled on the whole gradient
    // to get from A to percentage, divided by the span of A and B.
    progressionPercentage = (progressionPercentage - aOffset) / (bOffset - aOffset);

    // Linearly interpolate
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
