package at.blvckbytes.component_markup.xml.event;

public class BeforeEventCursorEvent implements XmlEvent {

  public final int charIndex;
  public final int line;
  public final int column;

  public BeforeEventCursorEvent(int charIndex, int line, int column) {
    this.charIndex = charIndex;
    this.line = line;
    this.column = column;
  }

  @Override
  public String toString() {
    return "BeforeEventCursorEvent{charIndex=" + charIndex + ", line=" + line + ", column=" + column + "}";
  }
}
