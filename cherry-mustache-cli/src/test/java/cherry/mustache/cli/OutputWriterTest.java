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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutputWriterTest {

    private final OutputWriter outputWriter = new OutputWriter();

    @TempDir
    Path tempDir;

    @Test
    void writesToStdoutWhenNoOutputPath() throws IOException {
        CliArguments args = new CliArguments(List.of("t.mustache"), null, null, null, null, false);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        outputWriter.write("Hello, World!", args, new PrintStream(buffer, true, StandardCharsets.UTF_8));

        assertEquals("Hello, World!", buffer.toString(StandardCharsets.UTF_8));
    }

    @Test
    void writesToFileWhenOutputPathSpecified() throws IOException {
        Path outputPath = tempDir.resolve("out.txt");
        CliArguments args = new CliArguments(List.of("t.mustache"), null, null, outputPath, null, false);

        outputWriter.write("Hello, File!", args, new PrintStream(new ByteArrayOutputStream()));

        assertEquals("Hello, File!", Files.readString(outputPath, StandardCharsets.UTF_8));
    }

    @Test
    void overwritesExistingFile() throws IOException {
        Path outputPath = tempDir.resolve("out.txt");
        Files.writeString(outputPath, "old content that is longer", StandardCharsets.UTF_8);
        CliArguments args = new CliArguments(List.of("t.mustache"), null, null, outputPath, null, false);

        outputWriter.write("new", args, new PrintStream(new ByteArrayOutputStream()));

        assertEquals("new", Files.readString(outputPath, StandardCharsets.UTF_8));
    }
}
