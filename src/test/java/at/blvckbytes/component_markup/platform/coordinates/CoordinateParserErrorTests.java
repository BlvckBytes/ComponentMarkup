/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.coordinates;

import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.util.InputView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoordinateParserErrorTests {

  @Test
  public void shouldThrowOnMissingCoordinateNumbers() {
    TextWithSubViews text = new TextWithSubViews("");
    makeErrorCase(text, CoordinatesParseError.EXPECTED_X_COORDINATE, 0);

    text = new TextWithSubViews("`      ´");
    makeErrorCase(text, CoordinatesParseError.EXPECTED_X_COORDINATE, text.subView(0).endExclusive - 1);

    text = new TextWithSubViews("`12.5´");
    makeErrorCase(text, CoordinatesParseError.EXPECTED_Y_COORDINATE, text.subView(0).endExclusive - 1);

    text = new TextWithSubViews("`12.5  ´");
    makeErrorCase(text, CoordinatesParseError.EXPECTED_Y_COORDINATE, text.subView(0).endExclusive - 1);

    text = new TextWithSubViews("-5.5 `12.5´");
    makeErrorCase(text, CoordinatesParseError.EXPECTED_Z_COORDINATE, text.subView(0).endExclusive - 1);

    text = new TextWithSubViews("-5.5 `12.5  ´");
    makeErrorCase(text, CoordinatesParseError.EXPECTED_Z_COORDINATE, text.subView(0).endExclusive - 1);
  }

  @Test
  public void shouldThrowOnMalformedNumber() {
    TextWithSubViews text = new TextWithSubViews("`-´");
    makeErrorCase(text, CoordinatesParseError.MALFORMED_NUMBER, text.subView(0).startInclusive);

    text = new TextWithSubViews("`.´");
    makeErrorCase(text, CoordinatesParseError.MALFORMED_NUMBER, text.subView(0).startInclusive);

    text = new TextWithSubViews("`.a´");
    makeErrorCase(text, CoordinatesParseError.MALFORMED_NUMBER, text.subView(0).startInclusive);

    text = new TextWithSubViews("`5.a´");
    makeErrorCase(text, CoordinatesParseError.MALFORMED_NUMBER, text.subView(0).startInclusive);
  }

  @Test
  public void shouldThrowOnTrailingCharacters() {
    TextWithSubViews text = new TextWithSubViews("1 2 3 my_world `a´");
    makeErrorCase(text, CoordinatesParseError.EXPECTED_END_AFTER_WORLD_NAME, text.subView(0).startInclusive);
  }

  private void makeErrorCase(TextWithSubViews input, CoordinatesParseError expectedError, int expectedPosition) {
    CoordinatesParseException actualException = Assertions.assertThrows(
      CoordinatesParseException.class,
      () -> CoordinatesParser.parse(InputView.of(input.text))
    );

    Assertions.assertEquals(expectedError, actualException.error, input.text);
    Assertions.assertEquals(expectedPosition, actualException.position, input.text);
  }
}
