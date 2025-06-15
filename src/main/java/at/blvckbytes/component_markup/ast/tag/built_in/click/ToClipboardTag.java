package at.blvckbytes.component_markup.ast.tag.built_in.click;

public class ToClipboardTag extends ClickTag {

  public ToClipboardTag() {
    super(ClickAction.COPY_TO_CLIPBOARD);
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equals("to-clipboard");
  }
}
