package ca.vanzyl.provisio.tools;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import java.io.IOException;
import java.nio.file.Path;

public class YamlMapper<T> {

  private final ObjectMapper mapper;

  public YamlMapper() {
    mapper = new ObjectMapper(new YAMLFactory());
    mapper.registerModule(new GuavaModule());
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
  }

  public T read(Path input, Class<T> clazz) throws IOException {
    return mapper.readValue(input.toFile(), clazz);
  }
}
