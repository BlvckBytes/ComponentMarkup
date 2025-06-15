package at.blvckbytes.component_markup.constructor;

import at.blvckbytes.component_markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.ast.node.click.InsertNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.node.hover.HoverNode;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;

import java.util.List;

public interface ComponentConstructor {

  Object createContentNode(ContentNode node, IEvaluationEnvironment environment);

  void setStyle(Object component, NodeStyle style, IEvaluationEnvironment environment);

  void setChildren(Object component, List<Object> children);

  void setClickAction(Object component, ClickNode node, IEvaluationEnvironment environment);

  void setHoverAction(Object component, HoverNode node, IEvaluationEnvironment environment);

  void setInsertAction(Object component, InsertNode node, IEvaluationEnvironment environment);

}
