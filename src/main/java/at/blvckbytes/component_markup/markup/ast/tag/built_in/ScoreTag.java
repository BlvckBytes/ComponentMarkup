/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.platform.PlatformWarning;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ScoreTag extends TagDefinition {

  public ScoreTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("score", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionNode name = attributes.getMandatoryExpressionNode("name");
    ExpressionNode objective = attributes.getMandatoryExpressionNode("objective");
    ExpressionNode type = attributes.getOptionalExpressionNode("type");
    ExpressionNode fallback = attributes.getOptionalExpressionNode("fallback");
    ExpressionNode override = attributes.getOptionalExpressionNode("override");
    MarkupNode renderer = attributes.getOptionalMarkupNode("renderer");

    return new FunctionDrivenNode(tagName, interpreter -> {
      PlatformWarning.clear();

      String objectiveValue = interpreter.evaluateAsString(objective);
      String typeValue = interpreter.evaluateAsStringOrNull(type);

      Object scoreValue = interpreter.getDataProvider().resolveScore(
        interpreter.evaluateAsString(name),
        objectiveValue, typeValue
      );

      if (type != null)
        PlatformWarning.logIfEmitted(PlatformWarning.MALFORMED_SCORE_TYPE, type.getFirstMemberPositionProvider(), typeValue);

      PlatformWarning.logIfEmitted(PlatformWarning.MISSING_SCORE_TYPE, objective.getFirstMemberPositionProvider(), objectiveValue);

      if (scoreValue == null) {
        if (fallback != null)
          scoreValue = interpreter.evaluateAsStringOrNull(fallback);
        else
          PlatformWarning.logIfEmitted(PlatformWarning.UNKNOWN_OBJECTIVE, objective.getFirstMemberPositionProvider(), objectiveValue);
      }

      if (override != null)
        scoreValue = interpreter.evaluateAsStringOrNull(override);

      if (scoreValue == null)
        scoreValue = 0;

      if (renderer != null) {
        interpreter.getEnvironment().setScopeVariable("score_value", scoreValue);
        return renderer;
      }

      return scoreValue;
    });
  }
}
