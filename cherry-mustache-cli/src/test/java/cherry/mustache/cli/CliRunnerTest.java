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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliRunnerTest {

    private final CliRunner runner = new CliRunner();

    @TempDir
    Path tempDir;

    @Test
    void successfulRenderWritesToStdout() throws IOException {
        Path template = writeFile("t.mustache", "Hello, {{name}}!");
        Path data = writeFile("data.json", "{\"name\":\"World\"}");

        Result result = execute(new String[]{"--data", data.toString(), template.toString()}, "");

        assertEquals(ExitCode.SUCCESS, result.exitCode());
        assertEquals("Hello, World!", result.stdout());
        assertEquals("", result.stderr());
    }

    @Test
    void successfulRenderWithPartial() throws IOException {
        writeFile("partial.mustache", "{{name}}");
        Path template = writeFile("main.mustache", "Hi, {{>partial}}!");
        Path data = writeFile("data.json", "{\"name\":\"World\"}");

        Result result = execute(new String[]{"--data", data.toString(), template.toString()}, "");

        assertEquals(ExitCode.SUCCESS, result.exitCode());
        assertEquals("Hi, World!", result.stdout());
    }

    @Test
    void multipleTemplatesConcatenatedWithoutSeparator() throws IOException {
        Path t1 = writeFile("t1.mustache", "A-{{name}}");
        Path t2 = writeFile("t2.mustache", "B-{{name}}");
        Path data = writeFile("data.json", "{\"name\":\"X\"}");

        Result result = execute(new String[]{"--data", data.toString(), t1.toString(), t2.toString()}, "");

        assertEquals(ExitCode.SUCCESS, result.exitCode());
        assertEquals("A-XB-X", result.stdout());
    }

    @Test
    void outputToFile() throws IOException {
        Path template = writeFile("t.mustache", "Hello, {{name}}!");
        Path data = writeFile("data.json", "{\"name\":\"World\"}");
        Path output = tempDir.resolve("out.txt");

        Result result = execute(
                new String[]{"--data", data.toString(), "--output", output.toString(), template.toString()}, "");

        assertEquals(ExitCode.SUCCESS, result.exitCode());
        assertEquals("", result.stdout());
        assertEquals("Hello, World!", Files.readString(output, StandardCharsets.UTF_8));
    }

    @Test
    void yamlDataFormatIsAutoDetected() throws IOException {
        Path template = writeFile("t.mustache", "Hello, {{name}}!");
        Path data = writeFile("data.yaml", "name: World\n");

        Result result = execute(new String[]{"--data", data.toString(), template.toString()}, "");

        assertEquals(ExitCode.SUCCESS, result.exitCode());
        assertEquals("Hello, World!", result.stdout());
    }

    @Test
    void dataFromStdinByDefault() throws IOException {
        Path template = writeFile("t.mustache", "Hello, {{name}}!");

        Result result = execute(new String[]{"--format", "json", template.toString()}, "{\"name\":\"World\"}");

        assertEquals(ExitCode.SUCCESS, result.exitCode());
        assertEquals("Hello, World!", result.stdout());
    }

    @Test
    void helpFlagPrintsUsageAndReturnsSuccess() {
        Result result = execute(new String[]{"--help"}, "");

        assertEquals(ExitCode.SUCCESS, result.exitCode());
        assertTrue(result.stdout().contains("Usage:"));
        assertEquals("", result.stderr());
    }

    @Test
    void missingTemplateReturnsArgumentError() {
        Result result = execute(new String[]{}, "");

        assertEquals(ExitCode.ARGUMENT_ERROR, result.exitCode());
        assertTrue(result.stderr().startsWith("Error:"));
    }

    @Test
    void malformedDataReturnsParseErrorNotIoError() throws IOException {
        Path template = writeFile("t.mustache", "Hello, {{name}}!");
        Path data = writeFile("data.json", "{bad json");

        Result result = execute(new String[]{"--data", data.toString(), template.toString()}, "");

        assertEquals(ExitCode.PARSE_ERROR, result.exitCode());
    }

    @Test
    void templateSyntaxErrorReturnsParseError() throws IOException {
        Path template = writeFile("t.mustache", "{{#unclosed");
        Path data = writeFile("data.json", "{}");

        Result result = execute(new String[]{"--data", data.toString(), template.toString()}, "");

        assertEquals(ExitCode.PARSE_ERROR, result.exitCode());
    }

    @Test
    void missingTemplateFileReturnsIoError() throws IOException {
        Path data = writeFile("data.json", "{}");

        Result result = execute(new String[]{"--data", data.toString(), tempDir.resolve("nope.mustache").toString()}, "");

        assertEquals(ExitCode.IO_ERROR, result.exitCode());
    }

    @Test
    void circularPartialReturnsRenderError() throws IOException {
        writeFile("a.mustache", "{{>b}}");
        Path b = writeFile("b.mustache", "{{>a}}");
        Path data = writeFile("data.json", "{}");

        Result result = execute(new String[]{"--data", data.toString(), b.toString()}, "");

        assertEquals(ExitCode.RENDER_ERROR, result.exitCode());
    }

    private Path writeFile(String name, String content) throws IOException {
        Path path = tempDir.resolve(name);
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return path;
    }

    private Result execute(String[] args, String stdinContent) {
        InputStream in = new ByteArrayInputStream(stdinContent.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBuffer, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(errBuffer, true, StandardCharsets.UTF_8);

        ExitCode exitCode = runner.run(args, in, out, err);

        return new Result(exitCode, outBuffer.toString(StandardCharsets.UTF_8), errBuffer.toString(StandardCharsets.UTF_8));
    }

    private record Result(ExitCode exitCode, String stdout, String stderr) {
    }
}
