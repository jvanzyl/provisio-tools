package ca.vanzyl.provisio.tools.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class YamlMapper<T> {

  private final ObjectMapper mapper;

  public YamlMapper() {
    mapper = new ObjectMapper(new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.setSerializationInclusion(Include.NON_EMPTY);
    mapper.setSerializationInclusion(Include.NON_ABSENT);
    mapper.setSerializationInclusion(Include.NON_DEFAULT);
  }

  public T read(Path input, Class<T> clazz) throws IOException {
    return mapper.readValue(input.toFile(), clazz);
  }

  public List<T> read(Path input, TypeReference<List<T>> clazz) throws IOException {
    return mapper.readValue(input.toFile(), clazz);
  }

  public String write(T object) throws IOException {
    return mapper.writeValueAsString(object);
  }
}
