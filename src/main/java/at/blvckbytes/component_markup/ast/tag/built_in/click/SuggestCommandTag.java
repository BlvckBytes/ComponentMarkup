package at.blvckbytes.component_markup.ast.tag.built_in.click;

public class SuggestCommandTag extends ClickTag {

  public SuggestCommandTag() {
    super(ClickAction.SUGGEST_COMMAND);
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("suggest-command");
  }
}
