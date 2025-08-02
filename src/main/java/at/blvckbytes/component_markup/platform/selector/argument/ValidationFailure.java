/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

public enum ValidationFailure {
  IS_NEGATIVE,
  IS_FRACTIONAL,
  IS_NEGATED,
  IS_RANGE,
  IS_RANGE_START_NEGATIVE,
  IS_RANGE_END_NEGATIVE,
  IS_RANGE_START_FRACTIONAL,
  IS_RANGE_END_FRACTIONAL,
  IS_NON_NUMERIC,
  IS_NON_NUMERIC_OR_RANGE,
  IS_NON_STRING,
  IS_NON_SORT_CRITERION
}
