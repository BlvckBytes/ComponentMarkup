/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public class TagCloseEvent implements XmlEvent {

  public final @Nullable StringView tagName;
  public final @Nullable String tagNameBuildResult;
  public final int pointyPosition;

  public TagCloseEvent(@Nullable StringView tagName, int pointyPosition, @Nullable String tagNameBuildResult) {
    this.tagName = tagName;
    this.tagNameBuildResult = tagNameBuildResult;
    this.pointyPosition = pointyPosition;
  }
}
