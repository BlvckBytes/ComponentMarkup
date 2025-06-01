package at.blvckbytes.component_markup.ast.tag.built_in.click;

public class RunCommandTag extends ClickTag {

  public RunCommandTag() {
    super(ClickAction.RUN_COMMAND);
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("run-command");
  }
}
