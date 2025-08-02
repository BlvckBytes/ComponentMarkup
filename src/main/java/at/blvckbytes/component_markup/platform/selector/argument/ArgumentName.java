/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

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
    false,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_RANGE, NumericFlag.NON_NEGATED))
  ),
  START_Y(
    "y",
    MultiAllowance.NEVER,
    false,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_RANGE, NumericFlag.NON_NEGATED))
  ),
  START_Z(
    "z",
    MultiAllowance.NEVER,
    false,
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
    false,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_NEGATIVE, NumericFlag.NON_NEGATED))
  ),
  /*
  Filter target selection based on their x-difference, y-difference, and z-difference from
  some point (analogous to DISTANCE). Cannot duplicate any one of these three arguments.
   */
  DELTA_X(
    "dx",
    MultiAllowance.NEVER,
    false,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_NEGATIVE, NumericFlag.NON_NEGATED))
  ),
  DELTA_Y(
    "dy",
    MultiAllowance.NEVER,
    false,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_NEGATIVE, NumericFlag.NON_NEGATED))
  ),
  DELTA_Z(
    "dz",
    MultiAllowance.NEVER,
    false,
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
    false,
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
    false,
    makeNumericValidator(EnumSet.of(NumericFlag.NON_NEGATED))
  ),
  /*
  Filter target selection based on the entity's scoreboard tags. Multiple tag arguments
  are allowed, and all arguments must be fulfilled for an entity to be selected.
  */
  TAG(
    "tag",
    MultiAllowance.MULTI_IF_EITHER,
    true,
    makeStringValidator()
  ),
  /*
  Filter target selection based on teams. Arguments testing for equality cannot be
  duplicated, while arguments testing for inequality can.
  */
  TEAM(
    "team",
    MultiAllowance.MULTI_IF_NEGATED,
    true,
    makeStringValidator()
  ),
  /*
  Filter target selection by name. Values are strings, so spaces are allowed only if quotes
  are applied. This cannot be a JSON text compound. Arguments testing for equality cannot
  be duplicated, while arguments testing for inequality can.
   */
  NAME(
    "name",
    MultiAllowance.MULTI_IF_NEGATED,
    true,
    makeStringValidator()
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
    true,
    makeStringValidator()
  ),
  /*
  Filter target selection based on the entity's experience levels. This naturally filters out
  all non-player targets. Cannot duplicate these arguments.

  Integer ranges are supported to select a range of values.
   */
  LEVEL(
    "level",
    MultiAllowance.NEVER,
    false,
    makeNumericValidator(EnumSet.allOf(NumericFlag.class))
  ),
  /*
  Filter target selection by game mode. This naturally filters out all non-player targets.
  Arguments testing for equality cannot be duplicated, while arguments testing for
  inequality can.
   */
  GAME_MODE(
    "gamemode",
    MultiAllowance.MULTI_IF_NEGATED,
    true,
    makeStringValidator()
  ),
  /*
  Limit the number of selectable targets for a target selector.
  */
  LIMIT(
    "limit",
    MultiAllowance.NEVER,
    false,
    makeNumericValidator(EnumSet.allOf(NumericFlag.class))
  ),
  /*
  Specifies selection-priority when limiting and dictates the order of results in general.
   */
  SORT(
    "sort",
    MultiAllowance.NEVER,
    false,
    t -> {
      if (t instanceof SortCriterion)
        return null;

      return ValidationFailure.IS_NON_SORT_CRITERION;
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
  public final boolean supportsStrings;
  public final Function<ArgumentValue, @Nullable ValidationFailure> typeErrorProvider;

  ArgumentName(
    String name,
    MultiAllowance multiAllowance,
    boolean supportsStrings,
    Function<ArgumentValue, @Nullable ValidationFailure> typeErrorProvider
  ) {
    this.name = name;
    this.multiAllowance = multiAllowance;
    this.supportsStrings = supportsStrings;
    this.typeErrorProvider = typeErrorProvider;
  }

  private static Function<ArgumentValue, @Nullable ValidationFailure> makeNumericValidator(EnumSet<NumericFlag> flags) {
    boolean allowsRanges = !flags.contains(NumericFlag.NON_RANGE);

    return t -> {
      if (t instanceof NumericValue) {
        NumericValue numericValue = (NumericValue) t;

        if (numericValue.isNegative && flags.contains(NumericFlag.NON_NEGATIVE))
          return ValidationFailure.IS_NEGATIVE;

        if (numericValue.isDouble && flags.contains(NumericFlag.NON_FRACTIONAL))
          return ValidationFailure.IS_FRACTIONAL;

        if (numericValue.isNegated && flags.contains(NumericFlag.NON_NEGATED))
          return ValidationFailure.IS_NEGATED;

        return null;
      }

      if (t instanceof NumericRangeValue) {
        if (!allowsRanges)
          return ValidationFailure.IS_RANGE;

        NumericRangeValue rangeValue = (NumericRangeValue) t;

        if (rangeValue.startInclusive.isNegative && flags.contains(NumericFlag.NON_NEGATIVE))
          return ValidationFailure.IS_RANGE_START_NEGATIVE;

        if (rangeValue.endInclusive.isNegative && flags.contains(NumericFlag.NON_NEGATIVE))
          return ValidationFailure.IS_RANGE_END_NEGATIVE;

        if (rangeValue.startInclusive.isDouble && flags.contains(NumericFlag.NON_FRACTIONAL))
          return ValidationFailure.IS_RANGE_START_FRACTIONAL;

        if (rangeValue.endInclusive.isDouble && flags.contains(NumericFlag.NON_FRACTIONAL))
          return ValidationFailure.IS_RANGE_END_FRACTIONAL;

        return null;
      }

      if (!allowsRanges)
        return ValidationFailure.IS_NON_NUMERIC;

      return ValidationFailure.IS_NON_NUMERIC_OR_RANGE;
    };
  }

  private static Function<ArgumentValue, @Nullable ValidationFailure> makeStringValidator() {
    return t -> {
      if (!(t instanceof StringValue))
        return ValidationFailure.IS_NON_STRING;

      return null;
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
