package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Interpreter {

  TemporaryMemberEnvironment getEnvironment();

  @NotNull String evaluateAsString(ExpressionNode expression);

  @Nullable String evaluateAsStringOrNull(ExpressionNode expression);

  long evaluateAsLong(ExpressionNode expression);

  @Nullable Long evaluateAsLongOrNull(ExpressionNode expression);

  double evaluateAsDouble(ExpressionNode expression);

  @Nullable Double evaluateAsDoubleOrNull(ExpressionNode expression);

  boolean evaluateAsBoolean(ExpressionNode expression);

  TriState evaluateAsTriState(ExpressionNode expression);

  @Nullable Object evaluateAsPlainObject(ExpressionNode expression);

  List<Object> interpret(MarkupNode node, SlotContext slotContext);

  OutputBuilder getCurrentBuilder();

  ComponentConstructor getComponentConstructor();

  boolean isInSubtree();

}
