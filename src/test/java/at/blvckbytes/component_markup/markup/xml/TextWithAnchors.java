package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.util.StringPosition;
import at.blvckbytes.component_markup.util.StringView;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TextWithAnchors {

  private static class ViewIndices {
    final int startInclusive;
    final int endInclusive;

    ViewIndices(int startInclusive, int endInclusive) {
      this.startInclusive = startInclusive;
      this.endInclusive = endInclusive;
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

    List<ViewIndices> indicesInOrder = new ArrayList<>();
    int startInclusive = -1;

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
              if (startInclusive != -1)
                throw new IllegalStateException("Encountered unbalanced prior subview-marker");

              startInclusive = charIndex;
            }
            else {
              if (startInclusive == -1)
                throw new IllegalStateException("Unbalanced closing-backtick at " + charIndex);

              indicesInOrder.add(new ViewIndices(startInclusive, charIndex - 1));
              startInclusive = -1;
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
            anchors.add(new StringPosition(charIndex));
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

    if (startInclusive != -1) {
      throw new IllegalStateException("Unbalanced subview-stack");
    }

    this.text = result.toString();
    this.rootView = StringView.of(text);

    for (ViewIndices indices : indicesInOrder) {
      StringPosition start = new StringPosition(indices.startInclusive);
      rootView.setSubViewStart(start);
      StringPosition end = new StringPosition(indices.endInclusive);
      this.subViews.add(rootView.buildSubViewInclusive(end));
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

  public static String escape(Object input) {
    return String.valueOf(input)
      .replace("@", "\\@")
      .replace("`", "\\`")
      .replace("´", "\\´");
  }
}
