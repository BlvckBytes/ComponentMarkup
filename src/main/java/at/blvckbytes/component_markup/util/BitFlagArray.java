/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.util;

import java.util.ArrayList;
import java.util.List;

public class BitFlagArray {

  @JsonifyIgnore
  private final long[] longs;

  @JsonifyIgnore
  private final int size;

  public BitFlagArray(int size) {
    int requiredLongs = (size + (Long.SIZE - 1)) / Long.SIZE;
    this.longs = new long[requiredLongs];
    this.size = size;
  }

  public void set(int index) {
    if (index < 0 || index >= size)
      throw new IllegalStateException("Index " + index + " out of range [0;" + size + "]");

    long targetLong = longs[index / Long.SIZE];
    long targetBit = 1L << (index % Long.SIZE);

    if ((targetLong & targetBit) != 0)
      throw new IllegalStateException("Bit at index " + index + " was already set");

    longs[index / Long.SIZE] = targetLong | targetBit;
  }

  public boolean get(int index) {
    return (longs[index / Long.SIZE] & (1L << (index % Long.SIZE))) != 0;
  }

  public void clearRange(int startInclusive, int endInclusive) {
    if (startInclusive < 0 || startInclusive >= size)
      throw new IllegalStateException("Start " + startInclusive + " out of range [0;" + size + "]");

    if (endInclusive < 0 || endInclusive >= size)
      throw new IllegalStateException("End " + startInclusive + " out of range [0;" + size + "]");

    if (endInclusive < startInclusive)
      throw new IllegalStateException("Negative range provided");

    int nextStart = startInclusive;

    while (nextStart < endInclusive) {
      int bitStart = nextStart % Long.SIZE;
      int remainingBits = Long.SIZE - bitStart;
      int clearCount = Math.min(remainingBits, endInclusive - nextStart + 1);

      longs[nextStart / Long.SIZE] &= ~setBitsInRange(bitStart, bitStart + clearCount - 1);

      nextStart += clearCount;
    }
  }

  private static long setBitsInRange(int startInclusive, int endInclusive) {
    if (startInclusive < 0 || endInclusive > 63 || startInclusive > endInclusive)
      throw new IllegalArgumentException("Invalid bit-range: [" + startInclusive + ";" + endInclusive + "]");

    if (startInclusive == 0 && endInclusive == 63)
      return -1L;

    return ((1L << (endInclusive - startInclusive + 1)) - 1) << startInclusive;
  }

  @JsonifyGetter
  public List<Integer> getRemovedIndices() {
    List<Integer> result = new ArrayList<>();

    for (int index = 0; index < size; ++index) {
      if (!get(index))
        continue;

      result.add(index);
    }

    return result;
  }
}
