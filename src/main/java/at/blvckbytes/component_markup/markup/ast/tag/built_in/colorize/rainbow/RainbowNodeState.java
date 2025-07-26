package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.rainbow;

import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeFlag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeNodeState;
import at.blvckbytes.component_markup.util.StringView;

import java.util.EnumSet;

public class RainbowNodeState extends ColorizeNodeState {

  private static final RainbowGenerator rainbowGenerator = new RainbowGenerator();

  public RainbowNodeState(StringView tagName, double phase, EnumSet<ColorizeFlag> flags) {
    super(tagName, phase, flags);
  }

  @Override
  protected long getPackedColor(double progressionPercentage) {
    return rainbowGenerator.getPackedColor(progressionPercentage);
  }
}
