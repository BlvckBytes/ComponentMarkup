package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface PendingTextNodeSubscriber {

  void accept(String text, @Nullable NodeStyle styleOverride);

}
