/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.TagRegistry;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.click.*;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.gradient.GradientTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.rainbow.RainbowTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.transition.TransitionTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.duration.DurationTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.hover.HoverEntityTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.hover.HoverItemTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.hover.HoverTextTag;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.util.InputView;

public class BuiltInTagRegistry extends TagRegistry {

  public static final TagRegistry INSTANCE = new BuiltInTagRegistry();
  public static final MarkupNode DEFAULT_SELECTOR_RENDERER;

  static {
    DEFAULT_SELECTOR_RENDERER = MarkupParser.parse(
      InputView.of(
        "<container",
        "  *for-entity=\"selector_result\"",
        "  *for-separator={ <gray>,<space/> }",
        "  *for-empty={ <red>The selector yielded no results! }",
        ">{entity.name}"
      ),
      BuiltInTagRegistry.INSTANCE
    );
  }

  protected BuiltInTagRegistry() {
    super();

    register(new ChangePageTag());
    register(new OpenFileTag());
    register(new OpenUrlTag());
    register(new RunCommandTag());
    register(new SuggestCommandTag());
    register(new ToClipboardTag());
    register(new HoverEntityTag());
    register(new HoverItemTag());
    register(new HoverTextTag());
    register(new BreakTag());
    register(new ContainerTag());
    register(new StyleTag());
    register(new ImmediateColorTag());
    register(new ImmediateFormatTag());
    register(new InsertTag());
    register(new KeyTag());
    register(new TranslateTag());
    register(new GradientTag());
    register(new RainbowTag());
    register(new TransitionTag());
    register(new ResetTag());
    register(new SpaceTag());
    register(new PlayerNameTag());
    register(new NumberTag());
    register(new DateTag());
    register(new DurationTag());
  }
}
