/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.coordinates;

import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.util.InputView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoordinateParserTests {

  @Test
  public void shouldParseCoordinatesWithoutWorld() {
    TextWithSubViews text = new TextWithSubViews("12 .4 -551.2");
    makeCase(text, new Coordinates(12, .4, -551.2, null));
  }

  @Test
  public void shouldParseCoordinatesWithWorld() {
    TextWithSubViews text = new TextWithSubViews("12 .4 -551.2 `my_world´");
    makeCase(text, new Coordinates(12, .4, -551.2, text.subView(0)));

    text = new TextWithSubViews("12 .4 -551.2 `my_world´   ");
    makeCase(text, new Coordinates(12, .4, -551.2, text.subView(0)));
  }

  private void makeCase(TextWithSubViews input, Coordinates expectedCoordinates) {
    Coordinates actualCoordinates = CoordinatesParser.parse(InputView.of(input.text));
    Assertions.assertEquals(Jsonifier.jsonify(expectedCoordinates), Jsonifier.jsonify(actualCoordinates));
  }
}
