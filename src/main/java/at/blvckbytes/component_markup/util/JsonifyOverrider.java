package at.blvckbytes.component_markup.util;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

public interface JsonifyOverrider {

  @Nullable JsonElement overrideJsonRepresentation(String field);

}
