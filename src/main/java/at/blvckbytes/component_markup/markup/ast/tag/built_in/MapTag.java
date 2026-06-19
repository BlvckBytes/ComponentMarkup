package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.MarkupAttribute;
import at.blvckbytes.component_markup.markup.interpreter.LoopVariable;
import at.blvckbytes.component_markup.markup.interpreter.MarkupInterpreter;
import at.blvckbytes.component_markup.markup.interpreter.TemporaryMemberEnvironment;
import at.blvckbytes.component_markup.markup.parser.MarkupParseError;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.parser.token.TokenEmitter;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class MapTag extends TagDefinition {

  public MapTag() {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("map", true) || tagName.startsWith("map-", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @Nullable TokenEmitter tokenEmitter,
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    String builtName = tagName.buildString();
    int firstDashIndex = builtName.indexOf('-');

    if (firstDashIndex < 0 || firstDashIndex == builtName.length() - 1)
      throw new MarkupParseException(tagName, MarkupParseError.EMPTY_BINDING_NAME);

    InputView mappingName = tagName.buildSubViewRelative(firstDashIndex + 1);

    if (tokenEmitter != null) {
      tokenEmitter.emitToken(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, tagName.buildSubViewRelative(3, 4));
      tokenEmitter.emitToken(TokenType.MARKUP__IDENTIFIER__BINDING, mappingName);
    }

    if (MarkupParser.isInvalidIdentifier(mappingName, true))
      throw new MarkupParseException(mappingName, MarkupParseError.MALFORMED_IDENTIFIER, mappingName.buildString());

    ExpressionNode flagValue = attributes.getOptionalBoundFlagExpressionNode();
    ExpressionNode iterable = flagValue != null ? flagValue : attributes.getMandatoryExpressionNode("iterable");

    Attribute mapper = attributes.getMandatoryAttribute("mapper");

    return new FunctionDrivenNode(tagName, letBindings, interpreter -> {
      if (children == null)
        return null;

      TemporaryMemberEnvironment environment = interpreter.getEnvironment();

      List<?> items = environment.getValueInterpreter().asList(interpreter.evaluateAsPlainObject(iterable));
      List<Object> result = new ArrayList<>();

      environment.beginScope();

      LoopVariable loopVariable = new LoopVariable(items.size());
      environment.setScopeVariable("loop", loopVariable);

      for (int index = 0; index < items.size(); ++index) {
        loopVariable.setIndex(index);
        environment.setScopeVariable("item", items.get(index));

        if (mapper instanceof ExpressionAttribute) {
          ExpressionNode expressionNode = ((ExpressionAttribute) mapper).value;
          result.add(interpreter.evaluateAsPlainObject(expressionNode));
          continue;
        }

        if (mapper instanceof MarkupAttribute) {
          MarkupNode markupNode = ((MarkupAttribute) mapper).value;

          Object component = MarkupInterpreter.interpret(
            markupNode, SlotType.CHAT, environment,
            interpreter.getComponentConstructor(), interpreter.getLogger()
          ).get(0);

          result.add(component);
        }
      }

      environment.endScope();

      environment.beginScope();
      environment.setScopeVariable(mappingName.buildString(), result);

      for (MarkupNode child : children)
        interpreter.interpret(child);

      environment.endScope();

      return null;
    });
  }
}
