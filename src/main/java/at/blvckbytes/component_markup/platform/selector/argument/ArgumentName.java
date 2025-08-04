/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

import at.blvckbytes.component_markup.platform.selector.SelectorParseError;
import at.blvckbytes.component_markup.platform.selector.SortCriterion;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.stream.Collectors;

// https://minecraft.fandom.com/wiki/Target_selectors
public enum ArgumentName {
  /*
  Define a position in the world the selector starts at, for use with the distance
  argument, the volume arguments, or the limit argument. Using these arguments alone
  will not restrict the entities found, and will only affect the sorting of targets.
  Cannot duplicate any one of these three arguments.
   */
  START_X(
    "x",
    MultiAllowance.NEVER,
    null,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_RANGE, NumericFlag.NON_NEGATED))
  ),
  START_Y(
    "y",
    MultiAllowance.NEVER,
    null,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_RANGE, NumericFlag.NON_NEGATED))
  ),
  START_Z(
    "z",
    MultiAllowance.NEVER,
    null,
    makeNumericValidator( EnumSet.of(NumericFlag.NON_RANGE, NumericFlag.NON_NEGATED))
  ),
  /*
  Filter target selection based on their Euclidean distances from some point, searching
  for the target's feet (a point at the bottom of the center of their hitbox). If the
  positional arguments are left undefined, radius is calculated relative to the position
  of the command's execution. This argument limits the search of entities to the current
  dimension. Cannot duplicate these arguments.

  Float ranges are supported to select a specific region. Only unsigned values are allowed.
   */
  DISTANCE(
    "distance",
    MultiAllowance.NEVER,
    AcceptedValue.RANGE,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_NEGATIVE, NumericFlag.NON_NEGATED))
  ),
  /*
  Filter target selection based on their x-difference, y-difference, and z-difference from
  some point (analogous to DISTANCE). Cannot duplicate any one of these three arguments.
   */
  DELTA_X(
    "dx",
    MultiAllowance.NEVER,
    AcceptedValue.RANGE,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_NEGATIVE, NumericFlag.NON_NEGATED))
  ),
  DELTA_Y(
    "dy",
    MultiAllowance.NEVER,
    AcceptedValue.RANGE,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_NEGATIVE, NumericFlag.NON_NEGATED))
  ),
  DELTA_Z(
    "dz",
    MultiAllowance.NEVER,
    AcceptedValue.RANGE,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_NEGATIVE, NumericFlag.NON_NEGATED))
  ),
  /*
  Filter target selection based on the entity's rotation along the pitch axis, measured
  in degrees. Values range from -90 (straight up) to 0 (at the horizon) to +90 (straight
  down). Cannot duplicate these arguments.

  Float Ranges are supported to select a specific range of angles.
   */
  X_ROTATION(
    "x_rotation",
    MultiAllowance.NEVER,
    AcceptedValue.RANGE,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_NEGATED))
  ),
  /*
  Filter target selection based on the entity's rotation along the yaw axis, measured
  clockwise in degrees from due south (or the positive Z direction). Values vary from
  -180 (facing due north) to -90 (facing due east) to 0 (facing due south) to +90
  (facing due west) to +180 (facing due north again). Cannot duplicate these arguments.

  Float Ranges are supported to select a specific range of angles.
   */
  Y_ROTATION(
    "y_rotation",
    MultiAllowance.NEVER,
    AcceptedValue.RANGE,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_NEGATED))
  ),
  /*
  Filter target selection based on the entity's scoreboard tags. Multiple tag arguments
  are allowed, and all arguments must be fulfilled for an entity to be selected.
  */
  TAG(
    "tag",
    MultiAllowance.MULTI_IF_EITHER,
    AcceptedValue.STRING,
    null
  ),
  /*
  Filter target selection based on teams. Arguments testing for equality cannot be
  duplicated, while arguments testing for inequality can.
  */
  TEAM(
    "team",
    MultiAllowance.MULTI_IF_NEGATED,
    AcceptedValue.STRING,
    null
  ),
  /*
  Filter target selection by name. Values are strings, so spaces are allowed only if quotes
  are applied. This cannot be a JSON text compound. Arguments testing for equality cannot
  be duplicated, while arguments testing for inequality can.
   */
  NAME(
    "name",
    MultiAllowance.MULTI_IF_NEGATED,
    AcceptedValue.STRING,
    makeNonBlankStringValidator()
  ),
  /*
  Filter target selection based on the entity's identifier. The given entity type must be a
  valid entity ID or entity type tag used to identify different types of entities internally.
  The namespace can be left out if the ID is within the minecraft namespace. Entity IDs or
  tags are case-sensitive. Arguments testing for equality cannot be duplicated, while
  arguments testing for inequality can.
   */
  TYPE(
    "type",
    MultiAllowance.MULTI_IF_NEGATED,
    AcceptedValue.STRING,
    makeNonBlankStringValidator()
  ),
  /*
  Filter target selection based on the entity's experience levels. This naturally filters out
  all non-player targets. Cannot duplicate these arguments.

  Integer ranges are supported to select a range of values.
   */
  LEVEL(
    "level",
    MultiAllowance.NEVER,
    null,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_NEGATIVE, NumericFlag.NON_FRACTIONAL, NumericFlag.NON_NEGATED))
  ),
  /*
  Filter target selection by game mode. This naturally filters out all non-player targets.
  Arguments testing for equality cannot be duplicated, while arguments testing for
  inequality can.
   */
  GAME_MODE(
    "gamemode",
    MultiAllowance.MULTI_IF_NEGATED,
    AcceptedValue.STRING,
    makeNonBlankStringValidator()
  ),
  /*
  Limit the number of selectable targets for a target selector.
  */
  LIMIT(
    "limit",
    MultiAllowance.NEVER,
    null,
    makeNumericValidator(EnumSet.allOf(NumericFlag.class))
  ),
  /*
  Specifies selection-priority when limiting and dictates the order of results in general.
   */
  SORT(
    "sort",
    MultiAllowance.NEVER,
    AcceptedValue.SORT_CRITERION,
    t -> {
      if (t instanceof SortCriterion)
        return null;

      return SelectorParseError.VALIDATION_FAILED_IS_NON_SORT_CRITERION;
    }
  ),
  ;

  private enum NumericFlag {
    NON_RANGE,
    NON_NEGATIVE,
    NON_FRACTIONAL,
    NON_NEGATED,
  }

  public static final String NAMES_STRING = Arrays.stream(values())
    .map(x -> String.valueOf(x.name))
    .collect(Collectors.joining(", "));

  public final String name;
  public final MultiAllowance multiAllowance;
  public final @Nullable AcceptedValue acceptedValue;
  public final @Nullable Function<ArgumentValue, @Nullable SelectorParseError> typeErrorProvider;

  ArgumentName(
    String name,
    MultiAllowance multiAllowance,
    @Nullable AcceptedValue acceptedValue,
    @Nullable Function<ArgumentValue, @Nullable SelectorParseError> typeErrorProvider
  ) {
    this.name = name;
    this.multiAllowance = multiAllowance;
    this.acceptedValue = acceptedValue;
    this.typeErrorProvider = typeErrorProvider;
  }

  private static Function<ArgumentValue, @Nullable SelectorParseError> makeNonBlankStringValidator() {
    return t -> {
      // Unreachable if AcceptedValue#STRING is set
      if (!(t instanceof StringValue))
        return null;

      String value = ((StringValue) t).value;

      for (int charIndex = 0; charIndex < value.length(); ++charIndex) {
        if (Character.isWhitespace(value.charAt(charIndex)))
          continue;

        return null;
      }

      return SelectorParseError.VALIDATION_FAILED_IS_BLANK_STRING;
    };
  }

  private static Function<ArgumentValue, @Nullable SelectorParseError> makeNumericValidator(EnumSet<NumericFlag> flags) {
    boolean allowsRanges = !flags.contains(NumericFlag.NON_RANGE);

    return t -> {
      if (t instanceof NumericValue) {
        NumericValue numericValue = (NumericValue) t;

        if (numericValue.isNegative && flags.contains(NumericFlag.NON_NEGATIVE))
          return SelectorParseError.VALIDATION_FAILED_IS_NEGATIVE;

        if (numericValue.isDouble && flags.contains(NumericFlag.NON_FRACTIONAL))
          return SelectorParseError.VALIDATION_FAILED_IS_FRACTIONAL;

        if (numericValue.isNegated && flags.contains(NumericFlag.NON_NEGATED))
          return SelectorParseError.VALIDATION_FAILED_IS_NEGATED;

        return null;
      }

      if (t instanceof NumericRangeValue) {
        if (!allowsRanges)
          return SelectorParseError.VALIDATION_FAILED_IS_RANGE;

        NumericRangeValue rangeValue = (NumericRangeValue) t;

        if (rangeValue.startInclusive.isNegative && flags.contains(NumericFlag.NON_NEGATIVE))
          return SelectorParseError.VALIDATION_FAILED_IS_RANGE_START_NEGATIVE;

        if (rangeValue.endInclusive.isNegative && flags.contains(NumericFlag.NON_NEGATIVE))
          return SelectorParseError.VALIDATION_FAILED_IS_RANGE_END_NEGATIVE;

        if (rangeValue.startInclusive.isDouble && flags.contains(NumericFlag.NON_FRACTIONAL))
          return SelectorParseError.VALIDATION_FAILED_IS_RANGE_START_FRACTIONAL;

        if (rangeValue.endInclusive.isDouble && flags.contains(NumericFlag.NON_FRACTIONAL))
          return SelectorParseError.VALIDATION_FAILED_IS_RANGE_END_FRACTIONAL;

        return null;
      }

      if (!allowsRanges)
        return SelectorParseError.VALIDATION_FAILED_IS_NON_NUMERIC;

      return SelectorParseError.VALIDATION_FAILED_IS_NON_NUMERIC_OR_RANGE;
    };
  }

  public static @Nullable ArgumentName ofName(StringView name) {
    switch (name.buildString()) {
      case "x":
        return START_X;
      case "y":
        return START_Y;
      case "z":
        return START_Z;
      case "distance":
        return DISTANCE;
      case "dx":
        return DELTA_X;
      case "dy":
        return DELTA_Y;
      case "dz":
        return DELTA_Z;
      case "x_rotation":
        return X_ROTATION;
      case "y_rotation":
        return Y_ROTATION;
      case "tag":
        return TAG;
      case "team":
        return TEAM;
      case "name":
        return NAME;
      case "type":
        return TYPE;
      case "level":
        return LEVEL;
      case "gamemode":
        return GAME_MODE;
      case "limit":
        return LIMIT;
      case "sort":
        return SORT;
      default:
        return null;
    }
  }
}
