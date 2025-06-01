package at.blvckbytes.component_markup.ast.tag.built_in.click;

public class OpenFileTag extends ClickTag {

  public OpenFileTag() {
    super(ClickAction.OPEN_FILE);
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("open-file");
  }
}
