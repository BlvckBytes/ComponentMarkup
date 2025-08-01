/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

import at.blvckbytes.component_markup.platform.selector.SortCriterion;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// https://minecraft.fandom.com/wiki/Target_selectors
public enum ArgumentName {
  /*
  Define a position in the world the selector starts at, for use with the distance
  argument, the volume arguments, or the limit argument. Using these arguments alone
  will not restrict the entities found, and will only affect the sorting of targets.
  Cannot duplicate any one of these three arguments.
   */
  START_X("x", IntegerValue.class, FloatValue.class),
  START_Y("y", IntegerValue.class, FloatValue.class),
  START_Z("z", IntegerValue.class, FloatValue.class),
  /*
  Filter target selection based on their Euclidean distances from some point, searching
  for the target's feet (a point at the bottom of the center of their hitbox). If the
  positional arguments are left undefined, radius is calculated relative to the position
  of the command's execution. This argument limits the search of entities to the current
  dimension. Cannot duplicate these arguments.

  Float ranges are supported to select a specific region. Only unsigned values are allowed.
   */
  DISTANCE("distance", IntegerValue.class, FloatValue.class, IntegerRangeValue.class, FloatRangeValue.class),
  /*
  Filter target selection based on their x-difference, y-difference, and z-difference from
  some point (analogous to DISTANCE). Cannot duplicate any one of these three arguments.
   */
  DELTA_X("dx", IntegerValue.class, FloatValue.class),
  DELTA_Y("dy", IntegerValue.class, FloatValue.class),
  DELTA_Z("dz", IntegerValue.class, FloatValue.class),
  /*
  Filter target selection based on the entity's rotation along the pitch axis, measured
  in degrees. Values range from -90 (straight up) to 0 (at the horizon) to +90 (straight
  down). Cannot duplicate these arguments.

  Float Ranges are supported to select a specific range of angles.
   */
  X_ROTATION("x_rotation", IntegerValue.class, FloatValue.class),
  /*
  Filter target selection based on the entity's rotation along the yaw axis, measured
  clockwise in degrees from due south (or the positive Z direction). Values vary from
  -180 (facing due north) to -90 (facing due east) to 0 (facing due south) to +90
  (facing due west) to +180 (facing due north again). Cannot duplicate these arguments.

  Float Ranges are supported to select a specific range of angles.
   */
  Y_ROTATION("y_rotation", IntegerValue.class, FloatValue.class),
  /*
  Filter target selection based on the entity's scoreboard tags. Multiple tag arguments
  are allowed, and all arguments must be fulfilled for an entity to be selected.
  */
  TAG("tag", StringValue.class),
  /*
  Filter target selection based on teams. Arguments testing for equality cannot be
  duplicated, while arguments testing for inequality can.
  */
  TEAM("team", StringValue.class),
  /*
  Filter target selection by name. Values are strings, so spaces are allowed only if quotes
  are applied. This cannot be a JSON text compound. Arguments testing for equality cannot
  be duplicated, while arguments testing for inequality can.
   */
  NAME("name", StringValue.class),
  /*
  Filter target selection based on the entity's identifier. The given entity type must be a
  valid entity ID or entity type tag used to identify different types of entities internally.
  The namespace can be left out if the ID is within the minecraft namespace. Entity IDs or
  tags are case-sensitive. Arguments testing for equality cannot be duplicated, while
  arguments testing for inequality can.
   */
  TYPE("type", StringValue.class),
  /*
  Filter target selection based on the entity's experience levels. This naturally filters out
  all non-player targets. Cannot duplicate these arguments.

  Integer ranges are supported to select a range of values.
   */
  LEVEL("level", IntegerValue.class, FloatValue.class, IntegerRangeValue.class, FloatRangeValue.class),
  /*
  Filter target selection by game mode. This naturally filters out all non-player targets.
  Arguments testing for equality cannot be duplicated, while arguments testing for
  inequality can.
   */
  GAME_MODE("gamemode", StringValue.class),
  /*
  Limit the number of selectable targets for a target selector.
  */
  LIMIT("limit", IntegerValue.class),
  /*
  Specifies selection-priority when limiting and dictates the order of results in general.
   */
  SORT("sort", SortCriterion.class),
  ;

  public static final String NAMES_STRING = Arrays.stream(values())
    .map(x -> String.valueOf(x.name))
    .collect(Collectors.joining(", "));

  public final String name;
  public final Set<Class<? extends ArgumentValue>> supportedArguments;

  @SafeVarargs
  ArgumentName(String name, Class<? extends ArgumentValue>... supportedArguments) {
    this.name = name;

    Set<Class<? extends ArgumentValue>> buffer = new HashSet<>();
    Collections.addAll(buffer, supportedArguments);
    this.supportedArguments = Collections.unmodifiableSet(buffer);
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
