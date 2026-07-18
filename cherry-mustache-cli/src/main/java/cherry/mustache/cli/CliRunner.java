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

import cherry.mustache.FilePartialResolver;
import cherry.mustache.Mustache;
import cherry.mustache.MustacheParseException;
import cherry.mustache.MustacheRenderException;
import cherry.mustache.PartialResolver;
import cherry.mustache.Template;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * CLI全体のオーケストレーション。引数解析・データ読込・テンプレートのコンパイル/連結レンダリング・
 * 出力書き込みまでの一連の流れを制御し、{@link ExitCode}を決定する。
 */
public class CliRunner {

    private static final Logger log = LoggerFactory.getLogger(CliRunner.class);

    private static final String STDIN_MARKER = "-";
    private static final PartialResolver UNRESOLVED_PARTIAL_RESOLVER = partialName -> null;

    private static final String USAGE = """
            Usage: mustache-cli [OPTIONS] TEMPLATE...

            Renders one or more Mustache templates against a single data source and
            concatenates the results.

            Positional arguments:
              TEMPLATE                Template file path, or "-" to read from standard input

            Options:
              --data <path|->         Input data file (JSON or YAML). Defaults to standard input
              --format <json|yaml>    Explicit data format. Required when data is read from standard input
              --output <path>         Output file path. Defaults to standard output
              --partial-dir <path>    Base directory for partial resolution. Defaults to each template's own directory
              --help, -h               Show this usage information and exit
            """;

    private final ArgumentParser argumentParser = new ArgumentParser();
    private final DataLoader dataLoader = new DataLoader();
    private final OutputWriter outputWriter = new OutputWriter();

    public ExitCode run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        try {
            CliArguments cliArguments = argumentParser.parse(args);
            if (cliArguments.helpRequested()) {
                out.print(USAGE);
                return ExitCode.SUCCESS;
            }

            Map<String, Object> data = dataLoader.load(cliArguments, in);

            StringBuilder result = new StringBuilder();
            for (String templateArg : cliArguments.templateArgs()) {
                result.append(renderTemplate(templateArg, cliArguments, data, in));
            }

            outputWriter.write(result.toString(), cliArguments, out);
            return ExitCode.SUCCESS;
        } catch (ArgumentException e) {
            err.println("Error: " + e.getMessage());
            return ExitCode.ARGUMENT_ERROR;
        } catch (JsonProcessingException e) {
            // JsonProcessingExceptionはIOExceptionのサブタイプのため、後続の汎用IOExceptionより先にcatchする
            log.debug("Failed to parse input data", e);
            err.println("Error: invalid data format");
            return ExitCode.PARSE_ERROR;
        } catch (MustacheParseException e) {
            log.debug("Failed to parse template", e);
            err.println("Error: " + e.getMessage());
            return ExitCode.PARSE_ERROR;
        } catch (MustacheRenderException e) {
            log.debug("Failed to render template", e);
            err.println("Error: " + e.getMessage());
            return ExitCode.RENDER_ERROR;
        } catch (IOException e) {
            log.debug("I/O failure", e);
            err.println("Error: " + e.getMessage());
            return ExitCode.IO_ERROR;
        } catch (RuntimeException e) {
            log.error("Unexpected error", e);
            err.println("Error: unexpected failure");
            return ExitCode.RENDER_ERROR;
        }
    }

    private String renderTemplate(String templateArg, CliArguments cliArguments, Map<String, Object> data, InputStream in) throws IOException {
        boolean stdinTemplate = templateArg.equals(STDIN_MARKER);
        String templateSource = stdinTemplate
                ? new String(in.readAllBytes(), StandardCharsets.UTF_8)
                : Files.readString(Path.of(templateArg), StandardCharsets.UTF_8);
        PartialResolver partialResolver = resolvePartialResolver(cliArguments, templateArg, stdinTemplate);
        Template template = Mustache.compile(templateSource, partialResolver);
        return template.render(data);
    }

    private PartialResolver resolvePartialResolver(CliArguments cliArguments, String templateArg, boolean stdinTemplate) {
        if (cliArguments.partialDir() != null) {
            return new FilePartialResolver(cliArguments.partialDir());
        }
        if (stdinTemplate) {
            return UNRESOLVED_PARTIAL_RESOLVER;
        }
        Path parent = Path.of(templateArg).toAbsolutePath().getParent();
        return new FilePartialResolver(parent);
    }
}
