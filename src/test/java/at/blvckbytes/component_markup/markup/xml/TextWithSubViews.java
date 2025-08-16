/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TextWithSubViews {

  private static class ViewIndices {
    final int startInclusive;
    int endExclusive;

    ViewIndices(int startInclusive) {
      this.startInclusive = startInclusive;
    }
  }

  public final String text;
  private final InputView rootView;
  private final InputView initialView;
  private final List<InputView> subViews;

  public TextWithSubViews(String... lines) {
    this.subViews = new ArrayList<>();

    StringBuilder result = new StringBuilder();
    int charIndex = 0;

    List<ViewIndices> indicesInOrder = new ArrayList<>();
    Stack<ViewIndices> indicesStack = new Stack<>();

    for (int linesIndex = 0; linesIndex < lines.length; ++linesIndex) {
      String line = lines[linesIndex];

      for (int lineCharIndex = 0; lineCharIndex < line.length(); ++lineCharIndex) {
        char currentChar = line.charAt(lineCharIndex);

        boolean isOpening;

        if ((isOpening = currentChar == '`') || currentChar == '´') {
          // This escape-sequence is a bit awkward, yes, but there are no other options, because
          // `` is perfectly valid (two nested sub-views), and sometimes, backslashes are in need
          // of being captured into a sub-view, i.e. `\´, which is why that also falls flat.
          if (lineCharIndex != 0 && line.charAt(lineCharIndex - 1) == '×') {
            result.setCharAt(result.length() - 1, currentChar);
            continue;
          }

          if (isOpening) {
            ViewIndices indices = new ViewIndices(charIndex);
            indicesStack.push(indices);
            indicesInOrder.add(indices);
          }
          else {
            if (indicesStack.isEmpty())
              throw new IllegalStateException("Unbalanced closing-backtick at " + charIndex + "(line " + (linesIndex + 1) + ")");

            indicesStack.pop().endExclusive = charIndex;
          }

          continue;
        }

        ++charIndex;

        result.append(currentChar);
      }

      if (linesIndex == lines.length - 1)
        break;

      result.append('\n');
      ++charIndex;
    }

    this.text = result.toString();

    if (!indicesStack.isEmpty()) {
      int index = indicesStack.pop().startInclusive;
      throw new IllegalStateException(
        "Unbalanced subview-stack: "
          + (index == 0 ? "" : this.text.charAt(index - 1))
          + this.text.charAt(index)
          + (index == this.text.length() - 1 ? "" : this.text.charAt(index + 1))
      );
    }

    this.rootView = InputView.of(text);
    this.initialView = rootView.endExclusive == 0 ? rootView : rootView.buildSubViewAbsolute(0, 0);

    for (ViewIndices indices : indicesInOrder)
      this.subViews.add(rootView.buildSubViewAbsolute(indices.startInclusive, indices.endExclusive));
  }

  public void addViewIndexToBeRemoved(int position) {
    this.rootView.addIndexToBeRemoved(position);
  }

  public InputView initialView() {
    return initialView;
  }

  public @NotNull InputView subView(int index) {
    if (index < 0)
      throw new IllegalStateException("Cannot request negative indices");

    if (index >= subViews.size())
      throw new IllegalStateException("Requested index " + index + "; only got " + subViews.size() + " sub-views");

    return subViews.get(index);
  }
}
