package at.blvckbytes.component_markup.markup.interpreter;

@FunctionalInterface
public interface RawValueTransformer {

  Object transform(Object value);

}
