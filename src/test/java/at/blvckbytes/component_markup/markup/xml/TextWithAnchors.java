package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TextWithAnchors {

  private static class ViewIndices {
    final int startInclusive;
    int endInclusive;

    ViewIndices(int startInclusive) {
      this.startInclusive = startInclusive;
    }
  }

  public final String text;
  private final StringView rootView;
  private final List<StringView> subViews;
  private final List<Integer> anchors;

  public TextWithAnchors(String... lines) {
    this.subViews = new ArrayList<>();
    this.anchors = new ArrayList<>();

    StringBuilder result = new StringBuilder();
    int charIndex = 0;

    List<ViewIndices> indicesInOrder = new ArrayList<>();
    Stack<ViewIndices> indicesStack = new Stack<>();

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
                throw new IllegalStateException("Unbalanced closing-backtick at " + charIndex + "(line " + (linesIndex + 1) + ")");

              indicesStack.pop().endInclusive = charIndex - 1;
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
            anchors.add(charIndex);
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

    this.rootView = StringView.of(text);

    for (ViewIndices indices : indicesInOrder) {
      rootView.setSubViewStart(indices.startInclusive);
      this.subViews.add(rootView.buildSubViewInclusive(indices.endInclusive));
    }
  }

  public void addViewIndexToBeRemoved(int position) {
    this.rootView.addIndexToBeRemoved(position);
  }

  public @NotNull StringView subView(int index) {
    if (index < 0)
      throw new IllegalStateException("Cannot request negative indices");

    if (index >= subViews.size())
      throw new IllegalStateException("Requested index " + index + "; only got " + subViews.size() + " sub-views");

    return subViews.get(index);
  }

  public int anchor(int index) {
    if (index < 0)
      throw new IllegalStateException("Cannot request negative indices");

    if (index >= anchors.size())
      throw new IllegalStateException("Requested index " + index + "; only got " + anchors.size() + " anchors");

    return anchors.get(index);
  }

  public static String escape(Object input) {
    return String.valueOf(input)
      .replace("@", "\\@")
      .replace("`", "\\`")
      .replace("´", "\\´");
  }
}
