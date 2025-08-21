/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.util;

public class MessagePlaceholders {

  private final String[] placeholders;

  public MessagePlaceholders(String[] placeholders) {
    this.placeholders = placeholders;
  }

  public String get(int index) {
    if (index < 0 || index >= placeholders.length)
      return null;

    return placeholders[index];
  }
}
