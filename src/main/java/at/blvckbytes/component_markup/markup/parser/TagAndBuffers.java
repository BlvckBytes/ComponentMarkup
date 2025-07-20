package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.WhenMatchingNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ForLoopNode;
import at.blvckbytes.component_markup.markup.ast.node.control.IfElseIfElseNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class TagAndBuffers implements ParserChildItem {

  public final TagDefinition tag;
  public final String tagNameLower;
  public final CursorPosition position;
  public final @Nullable TagAndBuffers parent;

  private @Nullable Set<LetBinding> bindings;
  private @Nullable Set<String> bindingNames;

  public final InternalAttributeMap attributeMap;

  private @Nullable List<ParserChildItem> children;

  public @Nullable ExpressionNode ifCondition;
  public ConditionType ifConditionType = ConditionType.NONE;
  public @Nullable ExpressionNode useCondition;

  public @Nullable ExpressionNode forIterable;
  public @Nullable MarkupNode forSeparator;
  public @Nullable MarkupNode forEmpty;
  public @Nullable ExpressionNode forReversed;
  public @Nullable String forIterationVariable;

  public @Nullable ExpressionNode whenInput;
  public @Nullable String whenIsValue;
  public boolean isWhenOther;

  public TagAndBuffers(
    TagDefinition tag,
    String tagNameLower,
    CursorPosition position,
    @Nullable TagAndBuffers parent
  ) {
    this.tag = tag;
    this.tagNameLower = tagNameLower;
    this.position = position;
    this.parent = parent;
    this.attributeMap = new InternalAttributeMap(tagNameLower, position);
  }

  public boolean hasLetBinding(String name) {
    if (this.bindingNames == null)
      return false;

    return this.bindingNames.contains(name);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean addLetBinding(LetBinding letBinding) {
    if (this.bindings == null) {
      this.bindings = new HashSet<>();
      this.bindings.add(letBinding);
      this.bindingNames = new HashSet<>();
      this.bindingNames.add(letBinding.name);
      return true;
    }

    assert this.bindingNames != null;

    if (!this.bindingNames.add(letBinding.name))
      return false;

    this.bindings.add(letBinding);
    return true;
  }

  public void addChild(ParserChildItem childItem) {
    if (this.children == null)
      this.children = new ArrayList<>();

    this.children.add(childItem);
  }

  private @Nullable List<MarkupNode> getProcessedChildren() {
    if (this.children == null) {
      if (whenInput != null)
        throw new MarkupParseException(position, MarkupParseError.WHEN_MATCHING_NO_CASES);

      return null;
    }

    List<MarkupNode> result = new ArrayList<>(children.size());

    ConditionType priorConditionType = ConditionType.NONE;
    List<MarkupNode> conditions = null;
    Map<String, MarkupNode> whenCases = null;
    MarkupNode whenOther = null;

    for (ParserChildItem child : children) {
      MarkupNode currentNode;

      ConditionType currentConditionType = ConditionType.NONE;

      if (child instanceof TagAndBuffers) {
        TagAndBuffers childTag = (TagAndBuffers) child;

        currentNode = childTag.createNode();
        currentConditionType = childTag.ifConditionType;

        if (childTag.forIterable != null) {
          currentNode = new ForLoopNode(
            childTag.forIterable,
            childTag.forIterationVariable,
            currentNode,
            childTag.forSeparator,
            childTag.forEmpty,
            childTag.forReversed,
            childTag.bindings
          );

          currentConditionType = ConditionType.NONE;
        }

        if (whenInput != null) {
          if (childTag.isWhenOther) {
            if (whenOther != null)
              throw new MarkupParseException(childTag.position, MarkupParseError.WHEN_MATCHING_DUPLICATE_FALLBACK);

            whenOther = currentNode;
          }

          else {
            if (childTag.whenIsValue == null)
              throw new MarkupParseException(childTag.position, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

            if (whenCases == null)
              whenCases = new HashMap<>();

            if (whenCases.put(childTag.whenIsValue.toLowerCase(), currentNode) != null)
              throw new MarkupParseException(childTag.position, MarkupParseError.WHEN_MATCHING_DUPLICATE_CASE, whenIsValue);
          }

          continue;
        }
      }

      else if (child instanceof MarkupNode) {
        MarkupNode node = (MarkupNode) child;

        if (whenInput != null)
          throw new MarkupParseException(node.position, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

        currentNode = node;
      } else
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

    if (whenInput != null && (whenCases == null || whenCases.isEmpty()))
      throw new MarkupParseException(position, MarkupParseError.WHEN_MATCHING_NO_CASES);

    if (whenCases != null) {
      assert whenInput != null;
      result.add(new WhenMatchingNode(position, whenInput, whenCases, whenOther));
    }

    if (conditions != null) {
      if (whenInput != null)
        throw new MarkupParseException(conditions.get(0).position, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

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

  private @Nullable MarkupNode createNodeOrNull(@Nullable List<MarkupNode> processedChildren) {
    MarkupNode result;

    try {
      result = tag.createNode(tagNameLower, position, attributeMap, bindings, processedChildren);
    } catch (Throwable thrownError) {
      if (thrownError instanceof MarkupParseException)
        throw thrownError;

      LoggerProvider.log(Level.SEVERE, "An error occurred while trying to instantiate <" + tagNameLower + "> via " + tag.getClass() + "#createNode", thrownError);
      return null;
    }

    attributeMap.validateNoUnusedAttributes();

    return result;
  }

  public MarkupNode createNode() {
    MarkupNode result = createNodeOrNull(getProcessedChildren());

    if (result == null)
      return new TextNode("<error>", position);

    if (ifCondition != null)
      result.ifCondition = ifCondition;

    if (useCondition != null)
      result.useCondition = useCondition;

    if (!(result instanceof ContainerNode))
      return result;

    ContainerNode containerNode = (ContainerNode) result;

    if (containerNode.children == null || containerNode.children.size() != 1)
      return result;

    MarkupNode onlyChild = containerNode.children.get(0);

    if (!onlyChild.canBeUnpackedFromAndIfSoInherit(containerNode))
      return result;

    return onlyChild;
  }
}
