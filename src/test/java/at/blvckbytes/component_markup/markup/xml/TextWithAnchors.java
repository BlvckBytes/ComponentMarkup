package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.markup.xml.event.CursorPositionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TextWithAnchors {

  private final List<CursorPositionEvent> anchors;
  private final List<CursorPositionEvent> auxAnchors;

  public final String text;

  public TextWithAnchors(String... lines) {
    this.anchors = new ArrayList<>();
    this.auxAnchors = new ArrayList<>();

    List<CursorPosition> anchorPositions = new ArrayList<>();
    List<CursorPosition> auxAnchorPositions = new ArrayList<>();

    StringBuilder result = new StringBuilder();
    int lineNumber = 1, columnNumber = 1, charIndex = 0;

    for (int linesIndex = 0; linesIndex < lines.length; ++linesIndex) {
      String line = lines[linesIndex];

      for (int lineCharIndex = 0; lineCharIndex < line.length(); ++lineCharIndex) {
        char currentChar = line.charAt(lineCharIndex);
        boolean isEscaped = lineCharIndex != 0 && line.charAt(lineCharIndex - 1) == '\\';

        CursorPosition position = new CursorPosition(charIndex + 1, lineNumber, columnNumber, null);

        if (currentChar == '@') {
          if (isEscaped) {
            result.deleteCharAt(result.length() - 1);
            --charIndex;
            --columnNumber;
          }

          else {
            anchorPositions.add(position);
            continue;
          }
        }

        if (currentChar == '#') {
          if (isEscaped) {
            result.deleteCharAt(result.length() - 1);
            --charIndex;
            --columnNumber;
          }

          else {
            auxAnchorPositions.add(position);
            continue;
          }
        }

        if (currentChar == '\n') {
          ++lineNumber;
          columnNumber = 1;
        }
        else
          ++columnNumber;

        ++charIndex;

        result.append(currentChar);
      }

      if (linesIndex == lines.length - 1)
        break;

      result.append('\n');

      ++lineNumber;
      columnNumber = 1;
      ++charIndex;
    }

    this.text = result.toString();

    for (CursorPosition anchorPosition : anchorPositions) {
      this.anchors.add(new CursorPositionEvent(
        new CursorPosition(
          anchorPosition.nextCharIndex,
          anchorPosition.lineNumber,
          anchorPosition.columnNumber,
          this.text
        )
      ));
    }

    for (CursorPosition auxAnchorPosition : auxAnchorPositions) {
      this.auxAnchors.add(new CursorPositionEvent(
        new CursorPosition(
          auxAnchorPosition.nextCharIndex,
          auxAnchorPosition.lineNumber,
          auxAnchorPosition.columnNumber,
          this.text
        )
      ));
    }
  }

  public int getAnchorCount() {
    return anchors.size();
  }

  public @NotNull CursorPosition anchor(int index) {
    CursorPositionEvent positionEvent = anchorEvent(index);

    if (positionEvent == null)
      throw new IllegalStateException("Required anchor at index " + index);

    return positionEvent.position;
  }

  public int anchorIndex(int index) {
    if (index < 0 || index >= anchors.size())
      throw new IllegalStateException("Required anchor at index " + index);

    int nextCharIndex = anchors.get(index).position.nextCharIndex;

    if (nextCharIndex > 0)
      --nextCharIndex;

    return nextCharIndex;
  }

  public int auxAnchorIndex(int index) {
    if (index < 0 || index >= auxAnchors.size())
      throw new IllegalStateException("Required aux-anchor at index " + index);

    int nextCharIndex = auxAnchors.get(index).position.nextCharIndex;

    if (nextCharIndex > 0)
      --nextCharIndex;

    return nextCharIndex;
  }

  public @Nullable CursorPositionEvent anchorEvent(int index) {
    if (index < 0 || index >= anchors.size())
      return null;

    return anchors.get(index);
  }

  public @Nullable CursorPositionEvent auxAnchorEvent(int index) {
    if (index < 0 || index >= auxAnchors.size())
      return null;

    return auxAnchors.get(index);
  }

  public @NotNull CursorPosition auxAnchor(int index) {
    CursorPositionEvent positionEvent = auxAnchorEvent(index);

    if (positionEvent == null)
      throw new IllegalStateException("Required aux-anchor at index " + index);

    return positionEvent.position;
  }

  public static String escape(Object input) {
    return String.valueOf(input)
      .replace("@", "\\@")
      .replace("#", "\\#");
  }
}
