package at.blvckbytes.component_markup.ast.tag.built_in.click;

public class OpenUrlTag extends ClickTag {

  public OpenUrlTag() {
    super(ClickAction.OPEN_URL);
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("open-url");
  }
}
