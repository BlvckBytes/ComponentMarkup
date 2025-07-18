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

  // TODO: Write proper test-cases

  private static final ComponentConstructor componentConstructor = new JsonComponentConstructor();
  private static final SlotContext chatContextNewLine = componentConstructor.getSlotContext(SlotType.CHAT);
  private static final SlotContext chatContextNewComponent = new SlotContext((char) 0, chatContextNewLine.defaultStyle);

  @Test
  public void shouldNotInitializeTreeIfNoDeferredComponentsPresent() {
    makeCase(
      new TextWithAnchors(
        "<red>",
        "  Hello, world! :)",
        "  <bold>test!"
      ),
      chatContextNewLine,
      null
    );
  }

  private void makeCase(TextWithAnchors input, SlotContext slotContext, @Nullable AddressTreeBuilder expectedDeferredAddresses) {
    MarkupNode ast = MarkupParser.parse(input.text, BuiltInTagRegistry.INSTANCE);

    ComponentOutput output = MarkupInterpreter.interpret(
      componentConstructor,
      new InterpretationEnvironment(),
      null,
      slotContext,
      ast
    );

    if (expectedDeferredAddresses == null) {
      Assertions.assertNull(output.deferredAddresses, "Expected there to not be an address-tree present");
      return;
    }

    Assertions.assertEquals(Jsonifier.jsonify(expectedDeferredAddresses.build()), Jsonifier.jsonify(output.deferredAddresses));
  }
}