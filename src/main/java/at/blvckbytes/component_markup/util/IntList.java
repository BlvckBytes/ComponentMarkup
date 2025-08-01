/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.util;

public class IntList {

  public static final int DEFAULT_INITIAL_CAPACITY = 2;

  private int[] list;
  private int size;

  public IntList(int initialCapacity) {
    this.list = new int[initialCapacity];
  }

  public IntList() {
    this.list = new int[DEFAULT_INITIAL_CAPACITY];
  }

  public void add(int element) {
    int elementIndex = size++;

    if (elementIndex >= list.length) {
      int[] newList = new int[list.length * 2];
      System.arraycopy(this.list, 0, newList, 0, list.length);
      this.list = newList;
    }

    this.list[elementIndex] = element;
  }

  public int get(int index) {
    return list[index];
  }

  public int getSize() {
    return size;
  }
}
