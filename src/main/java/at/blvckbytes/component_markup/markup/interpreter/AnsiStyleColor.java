package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public enum AnsiStyleColor implements ComponentColor {

  BLACK       ('0', new Color(  0,   0,   0)),
  DARK_BLUE   ('1', new Color(  0,   0, 170)),
  DARK_GREEN  ('2', new Color(  0, 170,   0)),
  DARK_AQUA   ('3', new Color(  0, 170, 170)),
  DARK_RED    ('4', new Color(170,   0,   0)),
  DARK_PURPLE ('5', new Color(170,   0, 170)),
  GOLD        ('6', new Color(255, 170,   0)),
  GRAY        ('7', new Color(170, 170, 170), "grey"),
  DARK_GRAY   ('8', new Color( 85,  85,  85), "dark_grey"),
  BLUE        ('9', new Color( 85,  85, 255)),
  GREEN       ('a', new Color( 85, 255,  85)),
  AQUA        ('b', new Color( 85, 255, 255)),
  RED         ('c', new Color(255,  85,  85)),
  LIGHT_PURPLE('d', new Color(255,  85, 255)),
  YELLOW      ('e', new Color(255, 255,  85)),
  WHITE       ('f', new Color(255, 255, 255)),
  ;

  public static final List<AnsiStyleColor> VALUES;
  public static final Set<String> NAMES;

  static {
    VALUES = Collections.unmodifiableList(Arrays.asList(values()));

    Set<String> buffer = new HashSet<>();

    for (AnsiStyleColor color : VALUES) {
      buffer.add(color.name);
      buffer.addAll(color.aliases);
    }

    NAMES = Collections.unmodifiableSet(buffer);
  }

  public final String name;
  public final char colorChar;
  public final Color color;
  public final List<String> aliases;

  private final float[] labColor;

  AnsiStyleColor(char colorChar, Color color, String... aliases) {
    this.name = name().toLowerCase();
    this.color = color;
    this.colorChar = colorChar;
    this.aliases = Collections.unmodifiableList(Arrays.asList(aliases));
    this.labColor = CIELabColorSpace.getInstance().fromRGB(color.getRGBComponents(null));
  }

  public static AnsiStyleColor getNearestColor(Color color) {
    float[] inputLab = CIELabColorSpace.getInstance().fromRGB(color.getRGBComponents(null));

    double minDistanceSquared = 0;
    AnsiStyleColor closestItem = null;

    for (AnsiStyleColor candidate : VALUES) {
      float[] candidateLab = candidate.labColor;

      double distanceSquared = (
        Math.pow(inputLab[0] - candidateLab[0], 2) +
        Math.pow(inputLab[1] - candidateLab[1], 2) +
        Math.pow(inputLab[2] - candidateLab[2], 2)
      );

      if (closestItem == null || distanceSquared < minDistanceSquared) {
        minDistanceSquared = distanceSquared;
        closestItem = candidate;
      }
    }

    return closestItem;
  }

  public static @Nullable AnsiStyleColor fromNameLowerOrNull(String nameLower) {
    switch (nameLower) {
      case "red":
        return RED;
      case "light_purple":
        return LIGHT_PURPLE;
      case "gold":
        return GOLD;
      case "dark_green":
        return DARK_GREEN;
      case "dark_aqua":
        return DARK_AQUA;
      case "yellow":
        return YELLOW;
      case "white":
        return WHITE;
      case "dark_red":
        return DARK_RED;
      case "dark_purple":
        return DARK_PURPLE;
      case "dark_gray":
      case "dark_grey":
        return DARK_GRAY;
      case "blue":
        return BLUE;
      case "gray":
      case "grey":
        return GRAY;
      case "green":
        return GREEN;
      case "aqua":
        return AQUA;
      case "black":
        return BLACK;
      case "dark_blue":
        return DARK_BLUE;
      default:
        return null;
    }
  }

  public static @Nullable AnsiStyleColor fromCharOrNull(char c) {
    switch (c) {
      case 'a':
      case 'A':
        return GREEN;
      case 'b':
      case 'B':
        return AQUA;
      case 'c':
      case 'C':
        return RED;
      case 'd':
      case 'D':
        return LIGHT_PURPLE;
      case 'e':
      case 'E':
        return YELLOW;
      case 'f':
      case 'F':
        return WHITE;
      case '0':
        return BLACK;
      case '1':
        return DARK_BLUE;
      case '2':
        return DARK_GREEN;
      case '3':
        return DARK_AQUA;
      case '4':
        return DARK_RED;
      case '5':
        return DARK_PURPLE;
      case '6':
        return GOLD;
      case '7':
        return GRAY;
      case '8':
        return DARK_GRAY;
      case '9':
        return BLUE;
      default:
        return null;
    }
  }

  @Override
  public Color getColor() {
    return this.color;
  }
}
