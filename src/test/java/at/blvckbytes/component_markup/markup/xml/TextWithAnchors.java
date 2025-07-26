package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.util.Jsonifier;
import at.blvckbytes.component_markup.util.StringPosition;
import at.blvckbytes.component_markup.util.StringView;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Stack;

public class TextWithAnchors {

  private static class ViewIndices {
    final int startCharIndex;
    int endCharIndex;

    private ViewIndices(int startCharIndex) {
      this.startCharIndex = startCharIndex;
    }
  }

  public final String text;
  private final StringView rootView;
  private final List<StringView> subViews;
  private final List<StringPosition> anchors;

  public TextWithAnchors(String... lines) {
    this.subViews = new ArrayList<>();
    this.anchors = new ArrayList<>();

    StringBuilder result = new StringBuilder();
    int charIndex = 0;

    Stack<ViewIndices> indicesStack = new Stack<>();
    List<ViewIndices> indicesInOrder = new ArrayList<>();

    for (int linesIndex = 0; linesIndex < lines.length; ++linesIndex) {
      String line = lines[linesIndex];

      for (int lineCharIndex = 0; lineCharIndex < line.length(); ++lineCharIndex) {
        char currentChar = line.charAt(lineCharIndex);

        boolean isEscaped = lineCharIndex != 0 && line.charAt(lineCharIndex - 1) == '\\';

        boolean isOpening;

        if ((isOpening = currentChar == '`') || currentChar == '´') {
          if (isEscaped) {
            result.deleteCharAt(result.length() - 1);
            --charIndex;
          }

          else {
            if (isOpening) {
              ViewIndices indices = new ViewIndices(charIndex);
              indicesStack.push(indices);
              indicesInOrder.add(indices);
            }
            else {
              if (indicesStack.isEmpty())
                throw new IllegalStateException("Unbalanced closing-backtick at " + charIndex);

              ViewIndices indices = indicesStack.pop();
              indices.endCharIndex = charIndex - 1;
            }

            continue;
          }
        }

        if (currentChar == '@') {
          if (isEscaped) {
            result.deleteCharAt(result.length() - 1);
            --charIndex;
          }

          else {
            anchors.add(new StringPosition(null, charIndex));
            continue;
          }
        }

        ++charIndex;

        result.append(currentChar);
      }

      if (linesIndex == lines.length - 1)
        break;

      result.append('\n');
      ++charIndex;
    }

    if (!indicesStack.isEmpty()) {
      ViewIndices firstItem = indicesStack.get(0);
      throw new IllegalStateException("Unbalanced stack: " + Jsonifier.jsonify(firstItem));
    }

    this.text = result.toString();
    this.rootView = StringView.of(text);

    for (ViewIndices indices : indicesInOrder) {
      StringPosition start = new StringPosition(rootView, indices.startCharIndex);
      rootView.setSubViewStart(start);
      StringPosition end = new StringPosition(rootView, indices.endCharIndex);
      this.subViews.add(rootView.buildSubViewUntilPosition(end));
    }
  }

  public void addViewIndexToBeRemoved(StringPosition position) {
    this.rootView.addIndexToBeRemoved(position.charIndex);
  }

  public @NotNull StringView subView(int index, EnumSet<SubstringFlag> flags) {
    StringView view = subView(index);
    view.setBuildFlags(flags);
    return view;
  }

  public @NotNull StringView subView(int index) {
    if (index < 0)
      throw new IllegalStateException("Cannot request negative indices");

    if (index >= subViews.size())
      throw new IllegalStateException("Requested index " + index + "; only got " + subViews.size() + " sub-views");

    return subViews.get(index);
  }

  public @NotNull StringPosition anchor(int index) {
    if (index < 0)
      throw new IllegalStateException("Cannot request negative indices");

    if (index >= anchors.size())
      throw new IllegalStateException("Requested index " + index + "; only got " + subViews.size() + " anchors");

    return anchors.get(index);
  }
}
