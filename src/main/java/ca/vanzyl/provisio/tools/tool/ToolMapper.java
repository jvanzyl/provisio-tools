package ca.vanzyl.provisio.tools.tool;

import static ca.vanzyl.provisio.tools.model.ToolDescriptor.DESCRIPTOR;
import static com.pivovarit.function.ThrowingFunction.unchecked;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.walk;

import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.util.YamlMapper;
import com.pivovarit.function.ThrowingFunction;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToolMapper {

  private final YamlMapper<ToolDescriptor> mapper;

  public ToolMapper() {
    this.mapper = new YamlMapper<>();
  }

  public ToolDescriptor toolDescriptor(Path input) throws IOException {
    return mapper.read(input, ToolDescriptor.class);
  }

  public final static ThrowingFunction<Path, ToolDescriptor, IOException> toolDescriptorFrom =
      path -> new YamlMapper<ToolDescriptor>().read(path, ToolDescriptor.class);

  public static Map<String, ToolDescriptor> collectToolDescriptorsMap(ProvisioningRequest request) throws Exception {
    try (Stream<Path> stream = Stream.concat(
        scan(request.toolDescriptorsDirectory(), 3),
        scan(request.localToolDescriptorsDirectory(), 3))) {
      return stream
          .filter(p -> p.toString().endsWith(DESCRIPTOR))
          .map(unchecked(toolDescriptorFrom))
          .collect(Collectors.toMap(ToolDescriptor::id, Function.identity(), (i, j) -> j, TreeMap::new));
    }
  }

  public static Stream<Path> scan(Path directory, int depth) throws Exception {
    if(exists(directory)) {
      return walk(directory, depth);
    } else {
      return Stream.of();
    }
  }
}
