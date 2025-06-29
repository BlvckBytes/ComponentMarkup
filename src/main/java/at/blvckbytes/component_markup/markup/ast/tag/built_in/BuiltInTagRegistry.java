package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.tag.TagRegistry;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.click.*;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.hover.HoverAchievementTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.hover.HoverEntityTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.hover.HoverItemTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.hover.HoverTextTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt.BlockNbtTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt.EntityNbtTag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt.StorageNbtTag;

public class BuiltInTagRegistry extends TagRegistry {

  private static final BuiltInTagRegistry INSTANCE = new BuiltInTagRegistry();

  private BuiltInTagRegistry() {
    register(new ChangePageTag());
    register(new OpenFileTag());
    register(new OpenUrlTag());
    register(new RunCommandTag());
    register(new SuggestCommandTag());
    register(new ToClipboardTag());
    register(new HoverAchievementTag());
    register(new HoverEntityTag());
    register(new HoverItemTag());
    register(new HoverTextTag());
    register(new BlockNbtTag());
    register(new EntityNbtTag());
    register(new StorageNbtTag());
    register(new BreakTag());
    register(new ColorTag());
    register(new ShadowTag());
    register(ContainerTag.INSTANCE);
    register(new FontTag());
    register(new FormatTag());
    register(new ImmediateColorTag());
    register(new ImmediateFormatTag());
    register(new InsertTag());
    register(new KeyTag());
    register(new ScoreTag());
    register(new SelectorTag());
    register(new TranslateTag());
  }

  public static BuiltInTagRegistry get() {
    return INSTANCE;
  }
}
