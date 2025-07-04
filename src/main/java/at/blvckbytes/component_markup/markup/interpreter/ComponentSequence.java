package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ComponentSequence {

  private final ComponentConstructor componentConstructor;
  public final @Nullable MarkupNode nonTerminal;
  private @Nullable List<Object> members;
  private @Nullable List<String> unstyledTexts;
  public final @Nullable ComputedStyle computedStyle;
  public final @Nullable ComputedStyle effectiveStyle;
  public final @Nullable ComputedStyle styleToApply;

  private @Nullable ComputedStyle commonStyle;

  public void possiblyUpdateCommonStyleToOnlyElement() {
    if (members == null || members.size() != 1)
      return;

    if (this.commonStyle == null) {
      this.commonStyle = this.styleToApply;
      return;
    }

    this.commonStyle.addMissing(this.styleToApply);
  }

  public @Nullable ComputedStyle getCommonStyle() {
    return commonStyle;
  }

  public void addUnstyledText(String text) {
    if (this.unstyledTexts == null)
      this.unstyledTexts = new ArrayList<>();

    this.unstyledTexts.add(text);

    if (commonStyle == null)
      commonStyle = new ComputedStyle();
  }

  private void concatAndInstantiateUnstyledTexts() {
    if (unstyledTexts == null || unstyledTexts.isEmpty())
      return;

    int unstyledCount = unstyledTexts.size();

    if (this.members == null)
      this.members = new ArrayList<>();

    if (unstyledCount == 1)
      members.add(componentConstructor.createTextNode(unstyledTexts.get(0)));

    else {
      StringBuilder accumulator = new StringBuilder();

      for (String unstyledText : unstyledTexts)
        accumulator.append(unstyledText);

      members.add(componentConstructor.createTextNode(accumulator.toString()));
    }

    unstyledTexts.clear();
  }

  public void addMember(Object member, @Nullable ComputedStyle memberCommonStyle) {
    concatAndInstantiateUnstyledTexts();

    if (this.members == null)
      this.members = new ArrayList<>();

    if (memberCommonStyle != null) {
      if (this.commonStyle == null)
        this.commonStyle = memberCommonStyle.copy();
      else
        this.commonStyle.subtractUncommonProperties(memberCommonStyle);;
    }

    this.members.add(member);
  }

  public Object combine(ComponentConstructor componentConstructor) {
    concatAndInstantiateUnstyledTexts();

    if (this.members == null || this.members.isEmpty())
     return componentConstructor.createTextNode("");

    if (members.size() == 1)
      return members.get(0);

    Object sequenceComponent = componentConstructor.createTextNode("");
    componentConstructor.setChildren(sequenceComponent, members);
    return sequenceComponent;
  }

  public static ComponentSequence initial(ComputedStyle defaultStyle, ComponentConstructor componentConstructor) {
    return new ComponentSequence(null, null, defaultStyle, null, componentConstructor);
  }

  public static ComponentSequence next(
    MarkupNode styleProvider,
    Interpreter interpreter,
    ComponentSequence parentSequence,
    SlotContext chatContext,
    ComponentConstructor componentConstructor
  ) {
    ComputedStyle computedStyle = null;

    if (styleProvider instanceof StyledNode)
      computedStyle = new ComputedStyle((StyledNode) styleProvider, interpreter);

    ComputedStyle styleToApply = computedStyle;
    ComputedStyle effectiveStyle = computedStyle;
    ComputedStyle inheritedStyle = parentSequence.effectiveStyle;

    if (inheritedStyle != null) {
      // Do not specify styles explicitly which are already active due to inheritance
      if (styleToApply != null)
        styleToApply = styleToApply.copy().subtractEqualStyles(inheritedStyle);

      // Add the inherited style to what's currently effective
      effectiveStyle = effectiveStyle == null ? inheritedStyle : effectiveStyle.copy().addMissing(inheritedStyle);

      // Add explicit properties to invert unwanted inherited style
      // By definition, a reset means resetting to chat-state; thus,
      // that's the context to get defaults from
      if (styleProvider != null && styleProvider.doesResetStyle) {
        ComputedStyle mask = inheritedStyle.copy().subtractEqualStyles(computedStyle);

        if (styleToApply == null)
          styleToApply = new ComputedStyle();

        styleToApply = styleToApply.applyDefaults(mask, chatContext);
      }
    }

    return new ComponentSequence(styleProvider, computedStyle, effectiveStyle, styleToApply, componentConstructor);
  }

  public ComponentSequence(
    @Nullable MarkupNode nonTerminal,
    @Nullable ComputedStyle computedStyle,
    @Nullable ComputedStyle effectiveStyle,
    @Nullable ComputedStyle styleToApply,
    ComponentConstructor componentConstructor
  ) {
    this.nonTerminal = nonTerminal;
    this.members = new ArrayList<>();
    this.computedStyle = computedStyle;
    this.effectiveStyle = effectiveStyle;
    this.styleToApply = styleToApply;
    this.componentConstructor = componentConstructor;
  }
}
