package at.blvckbytes.component_markup.xml.event;

public class TextEvent implements XmlEvent {

  public final String text;

  public TextEvent(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return "TextEvent{text='" + text + "'}";
  }
}
