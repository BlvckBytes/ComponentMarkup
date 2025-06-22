package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.node.control.ConditionalNode;
import at.blvckbytes.component_markup.ast.node.control.ForLoopNode;
import at.blvckbytes.component_markup.ast.node.control.IfThenElseNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.ast.tag.TagDefinition;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TagAndBuffers implements ParserChildItem {

  public final TagDefinition tag;
  public final String tagNameLower;
  public final CursorPosition position;

  public final List<LetBinding> bindings;
  public final List<Attribute> attributes;
  public final List<ParserChildItem> children;

  public @Nullable AExpression condition;
  public ConditionType conditionType = ConditionType.NONE;

  public @Nullable AExpression iterable;
  public String iterationVariable = "";

  public TagAndBuffers(TagDefinition tag, String tagNameLower, CursorPosition position) {
    this.tag = tag;
    this.tagNameLower = tagNameLower;
    this.position = position;

    this.bindings = new ArrayList<>();
    this.attributes = new ArrayList<>();
    this.children = new ArrayList<>();
  }

  private List<AstNode> getProcessedChildren() {
    List<AstNode> result = new ArrayList<>(children.size());

    ConditionType priorConditionType = ConditionType.NONE;
    List<ConditionalNode> conditions = null;

    for (ParserChildItem child : children) {
      AstNode currentNode;

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

        if (tagAndBuffers.iterable != null) {
          currentNode = new ForLoopNode(
            tagAndBuffers.iterable,
            tagAndBuffers.iterationVariable,
            currentNode,
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
              throw new IllegalStateException("Missing preceding *if sibling");
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
              result.add(new IfThenElseNode(conditions, currentNode));
              conditions = null;
              break;
          }
          break;
        }

        case ELSE_IF: {
          assert conditions != null;

          switch (currentConditionType) {
            case NONE:
              result.add(new IfThenElseNode(conditions, null));
              conditions = null;
              result.add(currentNode);
              break;

            case IF:
              result.add(new IfThenElseNode(conditions, null));
              conditions = new ArrayList<>();
              conditions.add((ConditionalNode) currentNode);
              break;

            case ELSE_IF:
              conditions.add((ConditionalNode) currentNode);
              break;

            case ELSE:
              result.add(new IfThenElseNode(conditions, currentNode));
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
              throw new IllegalStateException("Missing preceding *if sibling");
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
        result.add(new IfThenElseNode(conditions, null));
      }
    }

    return result;
  }

  public AstNode construct() {
    return tag.construct(
      tagNameLower,
      // TODO: Implement
      false,
      position,
      attributes, bindings, getProcessedChildren()
    );
  }
}
