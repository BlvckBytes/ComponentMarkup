package at.blvckbytes.component_markup.xml;

import at.blvckbytes.component_markup.xml.event.BeforeEventCursorEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TextWithAnchors {

  private final List<BeforeEventCursorEvent> anchors;
  private final List<BeforeEventCursorEvent> auxAnchors;

  public final String text;

  public TextWithAnchors(String... lines) {
    this.anchors = new ArrayList<>();
    this.auxAnchors = new ArrayList<>();

    StringBuilder result = new StringBuilder();
    int lineNumber = 1, columnNumber = 1, charIndex = 0;

    for (int linesIndex = 0; linesIndex < lines.length; ++linesIndex) {
      String line = lines[linesIndex];

      for (int lineCharIndex = 0; lineCharIndex < line.length(); ++lineCharIndex) {
        char currentChar = line.charAt(lineCharIndex);

        BeforeEventCursorEvent event = new BeforeEventCursorEvent(charIndex, lineNumber, columnNumber);

        if (currentChar == '@') {
          this.anchors.add(event);
          continue;
        }

        if (currentChar == '#') {
          this.auxAnchors.add(event);
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

  public int getAnchorCount() {
    return anchors.size();
  }

  public @Nullable BeforeEventCursorEvent getAnchor(int index) {
    if (index < 0 || index >= anchors.size())
      return null;

    return anchors.get(index);
  }

  public @Nullable BeforeEventCursorEvent getAuxAnchor(int index) {
    if (index < 0 || index >= auxAnchors.size())
      return null;

    return auxAnchors.get(index);
  }
}
