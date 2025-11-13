/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

public class MemberAndStyle<B> {

  public final B member;
  public final @Nullable ComputedStyle style;

  public MemberAndStyle(B member, @Nullable ComputedStyle style) {
    this.member = member;
    this.style = style;
  }
}
