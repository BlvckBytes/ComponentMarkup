/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.interpreter.ExtendedBuilder;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.constructor.ComponentConstructor;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.color.PackedColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class ColorizeNodeState {

  private static class Candidate<B> {
    final ExtendedBuilder<B> extendedBuilder;
    final MarkupNode node;
    final @Nullable String text;

    Candidate(ExtendedBuilder<B>extendedBuilder, MarkupNode node) {
      this.extendedBuilder = extendedBuilder;
      this.node = node;
      this.text = node instanceof TextNode ? ((TextNode) node).textValue : null;
    }
  }

  public final InputView tagName;
  public final double phase;
  public final EnumSet<ColorizeFlag> flags;
  public final int initialSubtreeDepth;

  private final Stack<List<Candidate<?>>> candidateStack;

  public ColorizeNodeState(InputView tagName, int initialSubtreeDepth, double phase, EnumSet<ColorizeFlag> flags) {
    this.tagName = tagName;
    this.initialSubtreeDepth = initialSubtreeDepth;
    this.phase = phase;
    this.flags = flags;

    this.candidateStack = new Stack<>();
  }

  protected long getPackedColor(int index, int length) {
    double progressionPercentage = (index / (double) length) * 100;

    if (progressionPercentage > 100)
      progressionPercentage = 100;

    progressionPercentage += phase;

    while (progressionPercentage > 100)
      progressionPercentage -= 100;

    return getPackedColor(progressionPercentage);
  }

  protected abstract long getPackedColor(double progressionPercentage);

  public boolean doesTargetNode(ColorizeNode node) {
    return node.tagName.contentEquals(this.tagName.buildString(), true);
  }

  public void begin() {
    if (flags.contains(ColorizeFlag.MERGE_INNER) && !candidateStack.empty()) {
      candidateStack.push(null);
      return;
    }

    candidateStack.push(new ArrayList<>());
  }

  public void discard() {
    candidateStack.pop();
  }

  public <B, C> boolean endAndGetIfStackIsEmpty(Interpreter<B, C> interpreter) {
    // Sorry, but I'm not carrying generics all the way through the project just to avoid
    // this little unchecked cast. The main reason as to why I added them to the ComponentConstructor
    // is that they make public APIs friendlier and catch missing calls to finalize within the
    // interpreter and output-builder; noting more, nothing less. They will never be perfect.
    //noinspection unchecked
    List<Candidate<B>> candidates = (List<Candidate<B>>) (List<?>) candidateStack.pop();

    if (candidates == null)
      return candidateStack.empty();

    ComponentConstructor<B, C> componentConstructor = interpreter.getComponentConstructor();

    List<Candidate<B>> targets = new ArrayList<>(candidates.size());
    int requiredColorCount = 0;

    for (Candidate<B> candidate : candidates) {
      if (!flags.contains(ColorizeFlag.OVERRIDE_COLORS) && candidate.extendedBuilder.explicitColor != PackedColor.NULL_SENTINEL)
        continue;

      if (flags.contains(ColorizeFlag.SKIP_NON_TEXT) && candidate.text == null)
        continue;

      if (candidate.text == null) {
        if (flags.contains(ColorizeFlag.SKIP_NON_TEXT))
          continue;

        targets.add(candidate);
        ++requiredColorCount;
        continue;
      }

      targets.add(candidate);

      for (int charIndex = 0; charIndex < candidate.text.length(); ++charIndex) {
        char currentChar = candidate.text.charAt(charIndex);

        if (flags.contains(ColorizeFlag.SKIP_WHITESPACE) && Character.isWhitespace(currentChar))
          continue;

        ++requiredColorCount;
      }
    }

    int nextColorIndex = 0;

    for (Candidate<B> target : targets) {
      if (target.text == null) {
        long color = getPackedColor(nextColorIndex++, requiredColorCount);
        componentConstructor.setColor(target.extendedBuilder.builder, color, true);
        continue;
      }

      if (target.text.length() == 1) {
        if (!flags.contains(ColorizeFlag.SKIP_WHITESPACE) || !Character.isWhitespace(target.text.charAt(0))) {
          long color = getPackedColor(nextColorIndex++, requiredColorCount);
          componentConstructor.setColor(target.extendedBuilder.builder, color, true);
        }

        continue;
      }

      List<C> children = new ArrayList<>();

      for (int charIndex = 0; charIndex < target.text.length(); ++charIndex) {
        boolean isFirst = charIndex == 0;
        char currentChar = target.text.charAt(charIndex);
        StringBuilder text = new StringBuilder();

        text.append(currentChar);

        long color = getPackedColor(nextColorIndex, requiredColorCount);

        if (!flags.contains(ColorizeFlag.SKIP_WHITESPACE) || !Character.isWhitespace(currentChar))
          ++nextColorIndex;

        char nextChar;

        while (charIndex < target.text.length() - 1) {
          nextChar = target.text.charAt(charIndex + 1);

          if (Character.isWhitespace(nextChar)) {
            if (!flags.contains(ColorizeFlag.SKIP_WHITESPACE))
              ++nextColorIndex;

            text.append(nextChar);
            ++charIndex;
            continue;
          }

          if (getPackedColor(nextColorIndex, requiredColorCount) == color) {
            ++nextColorIndex;
            text.append(nextChar);
            ++charIndex;
            continue;
          }

          break;
        }

        // Use the target-component to put the first char- and color into; only then make use
        // of its children as to inject the remaining parts, each with once again their own color.
        if (isFirst) {
          if (!componentConstructor.setText(target.extendedBuilder.builder, text.toString()))
            interpreter.getLogger().logErrorScreen(target.node.positionProvider, "Could not set the text of a component-builder during colorization");

          componentConstructor.setColor(target.extendedBuilder.builder, color, true);
          continue;
        }

        B charComponent = componentConstructor.createTextComponent(text.toString());

        componentConstructor.setColor(charComponent, color, true);

        children.add(componentConstructor.finalizeComponent(charComponent));
      }

      if (!children.isEmpty())
        componentConstructor.addChildren(target.extendedBuilder.builder, children);
    }

    return candidateStack.empty();
  }

  public void addCandidate(ExtendedBuilder<?> extendedBuilder, MarkupNode node) {
    if (flags.contains(ColorizeFlag.MERGE_INNER)) {
      candidateStack.get(0).add(new Candidate<>(extendedBuilder, node));
      return;
    }

    candidateStack.peek().add(new Candidate<>(extendedBuilder, node));
  }
}
