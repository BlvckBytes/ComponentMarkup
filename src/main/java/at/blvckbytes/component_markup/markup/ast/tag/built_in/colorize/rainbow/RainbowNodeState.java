package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.rainbow;

import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeFlag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeNodeState;

import java.util.EnumSet;

public class RainbowNodeState extends ColorizeNodeState {

  private static final RainbowGenerator rainbowGenerator = new RainbowGenerator();

  public RainbowNodeState(String tagNameLower, double phase, EnumSet<ColorizeFlag> flags) {
    super(tagNameLower, phase, flags);
  }

  @Override
  protected long getPackedColor(double progressionPercentage) {
    return rainbowGenerator.getPackedColor(progressionPercentage);
  }
}
