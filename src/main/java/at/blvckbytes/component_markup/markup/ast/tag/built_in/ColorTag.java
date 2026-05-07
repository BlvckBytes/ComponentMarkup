package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TransformerNode;
import at.blvckbytes.component_markup.expression.interpreter.ExpressionInterpreter;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.parser.token.TokenEmitter;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.color.PackedColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ColorTag extends TagDefinition {

  public ColorTag() {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("color", true);
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
    ContainerNode wrapper = new ContainerNode(tagName, children, letBindings);

    ExpressionNode flagValue = attributes.getOptionalBoundFlagExpressionNode();
    ExpressionNode color = flagValue == null ? attributes.getMandatoryExpressionNode("value") : flagValue;

    ExpressionNode lighten = attributes.getOptionalExpressionNode("lighten");
    ExpressionNode darken = attributes.getOptionalExpressionNode("darken");

    if (lighten == null && darken == null) {
      wrapper.getOrInstantiateStyle().color = color;
      return wrapper;
    }

    wrapper.getOrInstantiateStyle().color = new TransformerNode(color, (value, environment, logger) -> {
      if (value == null)
        return null;

      String colorString = environment.getValueInterpreter().asString(value);
      long packedColor = PackedColor.tryParse(colorString);

      if (packedColor == PackedColor.NULL_SENTINEL)
        return null;

      if (lighten != null) {
        Object lightenValue = ExpressionInterpreter.interpret(lighten, environment, logger);

        if (lightenValue != null) {
          float factor = (float) (environment.getValueInterpreter().asDouble(lightenValue) / 100);
          packedColor = PackedColor.lighten(packedColor, factor);
        }
      }

      if (darken != null) {
        Object darkenValue = ExpressionInterpreter.interpret(darken, environment, logger);

        if (darkenValue != null) {
          float factor = (float) (environment.getValueInterpreter().asDouble(darkenValue) / 100);
          packedColor = PackedColor.darken(packedColor, factor);
        }
      }

      return PackedColor.asShortestHex(packedColor);
    });

    return wrapper;
  }
}
