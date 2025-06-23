package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PendingTextNode extends ContentNode {

  private final ThreadLocal<PendingTextNodeSubscriber> threadLocalSubscriber = ThreadLocal.withInitial(() -> null);

  public PendingTextNode(
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);
  }

  public void notify(String text, @Nullable NodeStyle styleOverride) {
    PendingTextNodeSubscriber subscriber = threadLocalSubscriber.get();

    if (subscriber == null)
      return;

    subscriber.accept(text, styleOverride);
  }

  public void subscribeOnce(PendingTextNodeSubscriber subscriber) {
    if (threadLocalSubscriber.get() != null)
      throw new IllegalStateException("There should only ever be one subscriber per thread at any point in time!");

    threadLocalSubscriber.set(subscriber);
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "PendingTextNode{\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
