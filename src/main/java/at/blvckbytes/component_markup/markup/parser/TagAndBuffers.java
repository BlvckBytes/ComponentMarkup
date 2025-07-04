package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TerminalNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ForLoopNode;
import at.blvckbytes.component_markup.markup.ast.node.control.IfElseIfElseNode;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TagAndBuffers implements ParserChildItem {

  public final TagDefinition tag;
  public final String tagNameLower;
  public final CursorPosition position;

  private final List<LetBinding> bindings;
  private final Set<String> bindingNames;

  private final AttributeMap attributes;
  private final Set<String> attributeNames;

  public final List<ParserChildItem> children;

  public @Nullable ExpressionNode ifCondition;
  public ConditionType ifConditionType = ConditionType.NONE;
  public @Nullable ExpressionNode useCondition;

  public @Nullable ExpressionNode forIterable;
  public @Nullable MarkupNode forSeparator;
  public @Nullable ExpressionNode forReversed;
  public @Nullable String forIterationVariable;

  public TagAndBuffers(TagDefinition tag, String tagNameLower, CursorPosition position) {
    this.tag = tag;
    this.tagNameLower = tagNameLower;
    this.position = position;

    this.bindings = new ArrayList<>();
    this.bindingNames = new HashSet<>();
    this.attributes = new AttributeMap();
    this.attributeNames = new HashSet<>();
    this.children = new ArrayList<>();
  }

  public boolean hasAttribute(String name) {
    return this.attributeNames.contains(name);
  }

  public void addAttribute(Attribute attribute) {
    this.attributes.add(attribute);
    this.attributeNames.add(attribute.name);
  }

  public boolean hasLetBinding(String name) {
    return this.bindingNames.contains(name);
  }

  public boolean addLetBinding(LetBinding letBinding) {
    if (!this.bindingNames.add(letBinding.name))
      return false;

    this.bindings.add(letBinding);
    return true;
  }

  private List<MarkupNode> getProcessedChildren() {
    List<MarkupNode> result = new ArrayList<>(children.size());

    ConditionType priorConditionType = ConditionType.NONE;
    List<MarkupNode> conditions = null;

    for (ParserChildItem child : children) {
      MarkupNode currentNode;

      ConditionType currentConditionType = ConditionType.NONE;

      if (child instanceof TagAndBuffers) {
        TagAndBuffers tagAndBuffers = (TagAndBuffers) child;

        currentNode = tagAndBuffers.createNode();
        currentConditionType = tagAndBuffers.ifConditionType;

        if (tagAndBuffers.ifCondition != null)
          currentNode.ifCondition = tagAndBuffers.ifCondition;

        if (tagAndBuffers.useCondition != null)
          currentNode.useCondition = tagAndBuffers.useCondition;

        if (tagAndBuffers.forIterable != null) {
          currentNode = new ForLoopNode(
            tagAndBuffers.forIterable,
            tagAndBuffers.forIterationVariable,
            currentNode,
            tagAndBuffers.forSeparator,
            tagAndBuffers.forReversed,
            tagAndBuffers.bindings
          );

          currentConditionType = ConditionType.NONE;
        }
      }

      else if (child instanceof TerminalNode)
        currentNode = (TerminalNode) child;
      else
        throw new IllegalStateException("Unknown child-type: " + child);

      switch (priorConditionType) {
        case NONE: {
          switch (currentConditionType) {
            case NONE:
              result.add(currentNode);
              break;

            case IF:
              conditions = new ArrayList<>();
              conditions.add(currentNode);
              break;

            case ELSE_IF:
            case ELSE:
              throw new MarkupParseException(currentNode.position, MarkupParseError.MISSING_PRECEDING_IF_SIBLING);
          }
          break;
        }

        case IF: {
          assert conditions != null;
          assert conditions.size() == 1;

          switch (currentConditionType) {
            case NONE:
              result.add(conditions.get(0));
              conditions = null;
              result.add(currentNode);
              break;

            case IF:
              result.add(conditions.get(0));
              conditions.set(0, currentNode);
              break;

            case ELSE_IF:
              conditions.add(currentNode);
              break;

            case ELSE:
              result.add(new IfElseIfElseNode(conditions, currentNode));
              conditions = null;
              break;
          }
          break;
        }

        case ELSE_IF: {
          assert conditions != null;

          switch (currentConditionType) {
            case NONE:
              result.add(new IfElseIfElseNode(conditions, null));
              conditions = null;
              result.add(currentNode);
              break;

            case IF:
              result.add(new IfElseIfElseNode(conditions, null));
              conditions = new ArrayList<>();
              conditions.add(currentNode);
              break;

            case ELSE_IF:
              conditions.add(currentNode);
              break;

            case ELSE:
              result.add(new IfElseIfElseNode(conditions, currentNode));
              conditions = null;
              break;
          }
          break;
        }

        case ELSE: {
          assert conditions == null;

          switch (currentConditionType) {
            case NONE:
              result.add(currentNode);
              break;

            case IF:
              conditions = new ArrayList<>();
              conditions.add(currentNode);
              break;

            case ELSE:
            case ELSE_IF:
              throw new MarkupParseException(currentNode.position, MarkupParseError.MISSING_PRECEDING_IF_SIBLING);
          }
          break;
        }
      }

      priorConditionType = currentConditionType;
    }

    if (conditions != null) {
      if (priorConditionType == ConditionType.IF) {
        assert conditions.size() == 1;
        result.add(conditions.get(0));
      }

      else {
        assert priorConditionType == ConditionType.ELSE_IF;
        result.add(new IfElseIfElseNode(conditions, null));
      }
    }

    return result;
  }

  private @Nullable MarkupNode createNodeOrNull(List<MarkupNode> processedChildren) {
    try {
      return tag.createNode(tagNameLower, position, attributes, bindings, processedChildren);
    } catch (Throwable thrownError) {
      String className = tag.getClass().getName();

      if (thrownError instanceof AbsentMandatoryAttributeException) {
        AttributeDefinition absentAttribute = ((AbsentMandatoryAttributeException) thrownError).attribute;

        if (!tag.attributes.contains(absentAttribute)) {
          LoggerProvider.get().log(Level.SEVERE, "Tag " + className + " (<" + tagNameLower + ">) tried to require an unregistered attribute called \"" + absentAttribute.name + "\"", thrownError);
          return null;
        }
      }

      LoggerProvider.get().log(Level.SEVERE, "An error occurred while trying to instantiate <" + tagNameLower + "> via " + className + "#createNode", thrownError);
      return null;
    }
  }

  public MarkupNode createNode() {
    List<String> missingNames = null;

    for (AttributeDefinition definition : tag.attributes) {
      if (!(definition instanceof MandatoryExpressionAttributeDefinition || definition instanceof MandatoryMarkupAttributeDefinition))
        continue;

      if (attributes.hasName(definition.name))
        continue;

      if (missingNames == null)
        missingNames = new ArrayList<>();

      missingNames.add(definition.name);
    }

    if (missingNames != null)
      throw new MarkupParseException(position, MarkupParseError.MISSING_MANDATORY_ATTRIBUTES, tagNameLower, String.join(", ", missingNames));

    MarkupNode result = createNodeOrNull(getProcessedChildren());

    if (result == null)
      return new TextNode(ImmediateExpression.of("<error>"), position, null);

    if (!(result instanceof ContainerNode))
      return result;

    ContainerNode containerNode = (ContainerNode) result;

    if (containerNode.children == null || containerNode.children.size() != 1)
      return result;

    MarkupNode onlyChild = containerNode.children.get(0);
    NodeStyle containerStyle = containerNode.getStyle();

    if (containerStyle == null || !containerStyle.hasEffect()) {
      onlyChild.doesResetStyle |= containerNode.doesResetStyle;
      return onlyChild;
    }

    if (onlyChild instanceof StyledNode) {
      onlyChild.doesResetStyle |= containerNode.doesResetStyle;
      StyledNode styledNode = (StyledNode) onlyChild;
      styledNode.getOrInstantiateStyle().inheritFrom(containerStyle, useCondition);
      return styledNode;
    }

    return result;
  }
}
