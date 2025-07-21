package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeferredAddressesTests {

  // TODO: Add more complex cases

  private static final ComponentConstructor componentConstructor = new JsonComponentConstructor();
  private static final SlotContext chatContextNewLine = componentConstructor.getSlotContext(SlotType.CHAT);
  private static final SlotContext chatContextNewComponent = new SlotContext((char) 0, chatContextNewLine.defaultStyle);

  @Test
  public void shouldNotInitializeTreeIfNoDeferredComponentsPresent() {
    makeCase(
      new TextWithAnchors(
        "<red>",
        "  Hello, world! :)",
        "  <bold>test!</>",
        "  <br/>",
        "  <italic>test2"
      ),
      null
    );
  }

  @Test
  public void shouldGenerateTreeForRatherSimpleCase() {
    makeCase(
      new TextWithAnchors(
        "outer 0",
        "<br/>",
        "outer 1",
        "<br/>",
        "outer 2",
        "<player-name/>",
        "<player-name/>",
        "last text"
      ),
      new AddressTreeBuilder()
        .put(2, slotMap -> (
          slotMap
            .slot(MembersSlot.CHILDREN, addressTree -> (
              addressTree
                .terminal(1)
                .terminal(2)
            ))
        ))
    );
  }

  private void makeCase(TextWithAnchors input, @Nullable AddressTreeBuilder expectedDeferredAddresses) {
    MarkupNode ast = MarkupParser.parse(input.text, BuiltInTagRegistry.INSTANCE);

    ComponentOutput output = MarkupInterpreter.interpret(
      componentConstructor,
      new InterpretationEnvironment(),
      null,
      chatContextNewComponent,
      ast
    );

    if (expectedDeferredAddresses == null) {
      Assertions.assertNull(output.deferredAddresses, "Expected there to not be an address-tree present");
      return;
    }

    Assertions.assertEquals(Jsonifier.jsonify(expectedDeferredAddresses.result), Jsonifier.jsonify(output.deferredAddresses));
  }
}