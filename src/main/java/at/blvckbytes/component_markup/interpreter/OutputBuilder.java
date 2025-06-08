package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.ast.node.click.InsertNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.node.tooltip.TooltipNode;
import at.blvckbytes.component_markup.constructor.ComponentConstructor;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;

import java.util.List;

public class OutputBuilder {

  private final ComponentConstructor componentConstructor;
  private final IEvaluationEnvironment environment;
  private final BreakMode breakMode;

  public OutputBuilder(
    ComponentConstructor componentConstructor,
    IEvaluationEnvironment environment,
    BreakMode breakMode
  ) {
    this.componentConstructor = componentConstructor;
    this.environment = environment;
    this.breakMode = breakMode;
  }

  public void onBreak() {

  }

  public void onContainerBegin(ContainerNode containerNode) {
    ContentNode head = new TextNode(ImmediateExpression.of(""), containerNode.position);
    head.style.copyFrom(containerNode.style);
  }

  public void onContent(ContentNode contentNode) {
    componentConstructor.createContentNode(contentNode, environment);
  }

  public void onInsertBegin(InsertNode insertNode) {

  }

  public void onTooltipBegin(TooltipNode tooltipNode) {

  }

  public void onClickBegin(ClickNode clickNode) {

  }

  public void onAnyEnd() {

  }

  public List<Object> getResult() {
    throw new UnsupportedOperationException();
  }
}
