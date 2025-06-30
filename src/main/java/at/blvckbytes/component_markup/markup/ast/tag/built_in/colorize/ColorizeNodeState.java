package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.markup.interpreter.ComponentColor;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Stack;

public abstract class ColorizeNodeState {

  public final String tagNameLower;
  public final double phase;
  public final EnumSet<ColorizeFlag> flags;

  private final Stack<List<Object>> injectedComponentsStack;

  public ColorizeNodeState(String tagNameLower, double phase, EnumSet<ColorizeFlag> flags) {
    this.tagNameLower = tagNameLower;
    this.phase = phase;
    this.flags = flags;

    this.injectedComponentsStack = new Stack<>();
  }

  protected @Nullable ComponentColor getColor(int index, int length) {
    double progressionPercentage = (index / (double) length) * 100;

    if (progressionPercentage > 100)
      progressionPercentage = 100;

    progressionPercentage += phase;

    while (progressionPercentage > 100)
      progressionPercentage -= 100;

    return getColor(progressionPercentage);
  }

  protected abstract @Nullable ComponentColor getColor(double progressionPercentage);

  public boolean doesTargetNode(ColorizeNode node) {
    return node.tagNameLower.equals(this.tagNameLower);
  }

  public void begin() {
    if (flags.contains(ColorizeFlag.MERGE_INNER) && !injectedComponentsStack.empty()) {
      injectedComponentsStack.push(null);
      return;
    }

    injectedComponentsStack.push(new ArrayList<>());
  }

  public void discard() {
    injectedComponentsStack.pop();
  }

  public boolean endAndGetIfStackIsEmpty(Interpreter interpreter) {
    List<Object> injectedComponents = injectedComponentsStack.pop();

    if (injectedComponents == null)
      return injectedComponentsStack.empty();

    int length = injectedComponents.size();

    for (int index = 0; index < length; ++index) {
      Object injectedComponent = injectedComponents.get(index);

      ComponentColor color = getColor(index, length);

      if (color != null)
        interpreter.getComponentConstructor().setColor(injectedComponent, color);
    }

    return injectedComponentsStack.empty();
  }

  public void addInjected(Object component) {
    if (flags.contains(ColorizeFlag.MERGE_INNER)) {
      injectedComponentsStack.get(0).add(component);
      return;
    }

    injectedComponentsStack.peek().add(component);
  }
}
