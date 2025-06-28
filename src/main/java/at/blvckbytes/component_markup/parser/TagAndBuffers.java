package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.node.control.ConditionalNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.node.control.ForLoopNode;
import at.blvckbytes.component_markup.ast.node.control.IfElseIfElseNode;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.ast.tag.TagDefinition;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TagAndBuffers implements ParserChildItem {

  public final TagDefinition tag;
  public final String tagNameLower;
  public final CursorPosition position;

  private final List<LetBinding> bindings;
  private final Set<String> bindingNames;

  private final List<Attribute> attributes;
  private final Set<String> attributeNames;

  public final List<ParserChildItem> children;

  public @Nullable ExpressionNode condition;
  public ConditionType conditionType = ConditionType.NONE;

  public @Nullable ExpressionNode forIterable;
  public @Nullable MarkupNode forSeparator;
  public @Nullable ExpressionNode forReversed;
  public String forIterationVariable = "";

  public TagAndBuffers(TagDefinition tag, String tagNameLower, CursorPosition position) {
    this.tag = tag;
    this.tagNameLower = tagNameLower;
    this.position = position;

    this.bindings = new ArrayList<>();
    this.bindingNames = new HashSet<>();
    this.attributes = new ArrayList<>();
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
    List<ConditionalNode> conditions = null;

    for (ParserChildItem child : children) {
      MarkupNode currentNode;

      ConditionType currentConditionType = ConditionType.NONE;

      if (child instanceof TagAndBuffers) {
        TagAndBuffers tagAndBuffers = (TagAndBuffers) child;

        currentNode = tagAndBuffers.construct();
        currentConditionType = tagAndBuffers.conditionType;

        if (tagAndBuffers.condition != null) {
          currentNode = new ConditionalNode(
            tagAndBuffers.condition,
            currentNode,
            tagAndBuffers.bindings
          );
        }

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

      else if (child instanceof ContentNode)
        currentNode = (ContentNode) child;
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
              conditions.add((ConditionalNode) currentNode);
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
              conditions.set(0, (ConditionalNode) currentNode);
              break;

            case ELSE_IF:
              conditions.add((ConditionalNode) currentNode);
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
              conditions.add((ConditionalNode) currentNode);
              break;

            case ELSE_IF:
              conditions.add((ConditionalNode) currentNode);
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
              conditions.add((ConditionalNode) currentNode);
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

  public MarkupNode construct() {
    MarkupNode result = tag.construct(
      tagNameLower,
      position,
      attributes, bindings, getProcessedChildren()
    );

    if (!(result instanceof ContainerNode))
      return result;

    ContainerNode containerNode = (ContainerNode) result;

    if (containerNode.children == null || containerNode.children.size() != 1)
      return result;

    MarkupNode onlyChild = containerNode.children.get(0);
    NodeStyle containerStyle = containerNode.getStyle();

    if (containerStyle == null || !containerStyle.hasEffect())
      return onlyChild;

    if (onlyChild instanceof StyledNode) {
      StyledNode styledNode = (StyledNode) onlyChild;
      NodeStyle childStyle = styledNode.getStyle();

      if (childStyle == null)
        styledNode.setStyle(containerStyle);
      else
        childStyle.inheritFrom(containerStyle);

      return styledNode;
    }

    return result;
  }
}
