package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

public class MemberAndStyle {

  public final Object member;
  public final @Nullable ComputedStyle style;

  public MemberAndStyle(Object member, @Nullable ComputedStyle style) {
    this.member = member;
    this.style = style;
  }
}
