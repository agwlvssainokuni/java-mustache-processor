/*
 * Copyright 2026 agwlvssainokuni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cherry.mustache.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataLoaderTest {

    private final DataLoader dataLoader = new DataLoader();

    @TempDir
    Path tempDir;

    @Test
    void loadsJsonFromFileByExtension() throws IOException {
        Path file = tempDir.resolve("data.json");
        Files.writeString(file, "{\"name\":\"World\"}", StandardCharsets.UTF_8);
        CliArguments args = argsWithData(file.toString(), null);

        Map<String, Object> data = dataLoader.load(args, emptyStdin());

        assertEquals("World", data.get("name"));
    }

    @Test
    void loadsYamlFromFileByExtension() throws IOException {
        Path file = tempDir.resolve("data.yaml");
        Files.writeString(file, "name: World\n", StandardCharsets.UTF_8);
        CliArguments args = argsWithData(file.toString(), null);

        Map<String, Object> data = dataLoader.load(args, emptyStdin());

        assertEquals("World", data.get("name"));
    }

    @Test
    void formatOptionOverridesExtension() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file, "{\"name\":\"World\"}", StandardCharsets.UTF_8);
        CliArguments args = argsWithData(file.toString(), "json");

        Map<String, Object> data = dataLoader.load(args, emptyStdin());

        assertEquals("World", data.get("name"));
    }

    @Test
    void unknownExtensionWithoutFormatThrowsArgumentException() {
        Path file = tempDir.resolve("data.txt");
        CliArguments args = argsWithData(file.toString(), null);

        assertThrows(ArgumentException.class, () -> dataLoader.load(args, emptyStdin()));
    }

    @Test
    void stdinWithoutFormatThrowsArgumentException() {
        CliArguments args = argsWithData(null, null);

        assertThrows(ArgumentException.class, () -> dataLoader.load(args, emptyStdin()));
    }

    @Test
    void stdinWithFormatReadsJson() throws IOException {
        CliArguments args = argsWithData(null, "json");
        InputStream stdin = new ByteArrayInputStream("{\"name\":\"World\"}".getBytes(StandardCharsets.UTF_8));

        Map<String, Object> data = dataLoader.load(args, stdin);

        assertEquals("World", data.get("name"));
    }

    @Test
    void malformedJsonThrowsJsonProcessingException() {
        CliArguments args = argsWithData(null, "json");
        InputStream stdin = new ByteArrayInputStream("{bad json".getBytes(StandardCharsets.UTF_8));

        assertThrows(JsonProcessingException.class, () -> dataLoader.load(args, stdin));
    }

    private static CliArguments argsWithData(String dataArg, String format) {
        return new CliArguments(List.of("template.mustache"), dataArg, format, null, null, false);
    }

    private static InputStream emptyStdin() {
        return new ByteArrayInputStream(new byte[0]);
    }
}
