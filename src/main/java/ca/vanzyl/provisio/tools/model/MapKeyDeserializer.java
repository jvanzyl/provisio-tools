package ca.vanzyl.provisio.tools.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

// https://stackoverflow.com/questions/3403909/get-generic-type-of-class-at-runtime
public class MapKeyDeserializer extends JsonDeserializer<ToolProfileEntry> {

  @Override
  public ToolProfileEntry deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    String name = context.getParser().getCurrentName();
    ToolProfileEntry user = parser.readValueAs(ToolProfileEntry.class);
    return ImmutableToolProfileEntry.copyOf(user).withName(name);
  }
}