package at.blvckbytes.component_markup.ast.tag.built_in.click;

public class ChangePageTag extends ClickTag {

  public ChangePageTag() {
    super(ClickAction.CHANGE_PAGE);
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equals("change-page");
  }
}
