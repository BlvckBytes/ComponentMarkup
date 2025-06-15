package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.constructor.ComponentConstructor;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;

import java.util.List;

public class OutputBuilder {

  private final ComponentConstructor componentConstructor;
  public final IEvaluationEnvironment environment;
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
    throw new UnsupportedOperationException();
  }

  public void onNonTerminalBegin(AstNode node) {
    throw new UnsupportedOperationException();
  }

  public void onNonTerminalEnd() {
    throw new UnsupportedOperationException();
  }

  public void onContent(ContentNode node) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getResult() {
    throw new UnsupportedOperationException();
  }
}
