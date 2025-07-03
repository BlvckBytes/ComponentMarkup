package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.gradient;

import at.blvckbytes.component_markup.markup.interpreter.AnsiStyleColor;
import at.blvckbytes.component_markup.markup.interpreter.PackedColor;

import java.util.Arrays;

public class GradientGenerator {

  public final long[] packedColors;
  public final double[] offsets;
  public final long[] zIndices;

  public GradientGenerator(long[] packedColors, double[] offsets, long[] zIndices) {
    this.packedColors = packedColors;
    this.zIndices = zIndices;
    this.offsets = clampAndPossiblyExtendOffsets(packedColors.length, offsets);
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

  public long getPackedColor(double progressionPercentage) {
    int colorCount = packedColors.length;

    if (colorCount == 0)
      return AnsiStyleColor.WHITE.packedColor;

    long firstColor = packedColors[0];

    if (colorCount == 1)
      return firstColor;

    double firstOffset = offsets[0];

    int lastIndex = packedColors.length - 1;
    long lastColor = packedColors[lastIndex];
    double lastOffset = offsets[lastIndex];

    long firstZIndex = zIndices.length == 0 ? 0 : zIndices[0];
    long lastZIndex = lastIndex >= zIndices.length ? 0 : zIndices[lastIndex];

    long aColor = firstColor, bColor = lastColor;
    double aOffset = firstOffset, bOffset = lastOffset;
    long aZIndex = firstZIndex, bZIndex = lastZIndex;

    for (int i = 1; i < colorCount - 1; i++) {
      long currentColor = packedColors[i];
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

    double resultRed   = PackedColor.getR(aColor);
    double resultGreen = PackedColor.getG(aColor);
    double resultBlue  = PackedColor.getB(aColor);

    resultRed   += progressionPercentage * (PackedColor.getR(bColor) - resultRed);
    resultGreen += progressionPercentage * (PackedColor.getG(bColor) - resultGreen);
    resultBlue  += progressionPercentage * (PackedColor.getB(bColor) - resultBlue);

    return PackedColor.of(
      (int) Math.floor(resultRed),
      (int) Math.floor(resultGreen),
      (int) Math.floor(resultBlue),
      255
    );
  }
}
