package at.blvckbytes.component_markup.xml;

import at.blvckbytes.component_markup.xml.event.BeforeEventCursorEvent;

import java.util.ArrayList;
import java.util.List;

public class TextWithAnchors {

  private final List<BeforeEventCursorEvent> anchors;
  public final String text;

  public TextWithAnchors(String... lines) {
    this.anchors = new ArrayList<>();

    StringBuilder result = new StringBuilder();
    int lineNumber = 1, columnNumber = 1, charIndex = 0;

    for (int linesIndex = 0; linesIndex < lines.length; ++linesIndex) {
      String line = lines[linesIndex];

      for (int lineCharIndex = 0; lineCharIndex < line.length(); ++lineCharIndex) {
        char currentChar = line.charAt(lineCharIndex);

        if (currentChar == '@') {
          this.anchors.add(new BeforeEventCursorEvent(charIndex, lineNumber, columnNumber));
          continue;
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
  }

  public BeforeEventCursorEvent getAnchor(int index) {
    if (index < 0 || index >= anchors.size())
      throw new IllegalStateException("Anchor-index " + index + " out of range for size " + anchors.size());

    return anchors.get(index);
  }
}
