package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ComponentSequence {

  public final @Nullable MarkupNode nonTerminal;
  private @Nullable List<Object> members;
  public final @Nullable ComputedStyle computedStyle;
  public final @Nullable ComputedStyle effectiveStyle;
  public final @Nullable ComputedStyle styleToApply;

  public void addMember(Object member) {
    if (this.members == null)
      this.members = new ArrayList<>();

    this.members.add(member);
  }

  public Object combine(ComponentConstructor componentConstructor) {
    if (this.members == null || this.members.isEmpty())
     return componentConstructor.createTextNode("");

    if (members.size() == 1)
      return members.get(0);

    Object sequenceComponent = componentConstructor.createTextNode("");
    componentConstructor.setChildren(sequenceComponent, members);
    return sequenceComponent;
  }

  public static ComponentSequence initial(ComputedStyle defaultStyle) {
    return new ComponentSequence(null, null, defaultStyle, null);
  }

  public static ComponentSequence next(MarkupNode styleProvider, Interpreter interpreter, ComponentSequence parentSequence) {
    ComputedStyle computedStyle = null;

    if (styleProvider instanceof StyledNode)
      computedStyle = new ComputedStyle((StyledNode) styleProvider, interpreter);

    ComputedStyle styleToApply = computedStyle;
    ComputedStyle effectiveStyle = computedStyle;
    ComputedStyle inheritedStyle = parentSequence.effectiveStyle;

    if (inheritedStyle != null) {
      // Do not specify styles explicitly which are already active due to inheritance
      if (styleToApply != null)
        styleToApply = styleToApply.copy().subtractCommonalities(inheritedStyle);

      // Add the inherited style to what's currently effective
      effectiveStyle = effectiveStyle == null ? inheritedStyle : effectiveStyle.copy().addMissing(inheritedStyle);
    }

    return new ComponentSequence(styleProvider, computedStyle, effectiveStyle, styleToApply);
  }

  public ComponentSequence(
    @Nullable MarkupNode nonTerminal,
    @Nullable ComputedStyle computedStyle,
    @Nullable ComputedStyle effectiveStyle,
    @Nullable ComputedStyle styleToApply
  ) {
    this.nonTerminal = nonTerminal;
    this.members = new ArrayList<>();
    this.computedStyle = computedStyle;
    this.effectiveStyle = effectiveStyle;
    this.styleToApply = styleToApply;
  }
}
