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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * コマンドライン引数（{@code String[] args}）を解析し{@link CliArguments}を構築する。
 * ファイルパスの存在確認は行わず、値の構文検証のみを実施する（実行時検出方針）。
 */
public class ArgumentParser {

    private static final Logger log = LoggerFactory.getLogger(ArgumentParser.class);

    private static final String STDIN_MARKER = "-";

    public CliArguments parse(String[] args) {
        for (String arg : args) {
            if (arg.equals("--help") || arg.equals("-h")) {
                // BR-7: --helpが指定された場合、他の引数の妥当性検証は一切行わない
                return new CliArguments(List.of(), null, null, null, null, true);
            }
        }

        List<String> templateArgs = new ArrayList<>();
        String dataArg = null;
        String format = null;
        String outputPath = null;
        String partialDir = null;

        int i = 0;
        while (i < args.length) {
            String arg = args[i];
            switch (arg) {
                case "--data" -> {
                    dataArg = requireValue(args, i, "--data");
                    i++;
                }
                case "--format" -> {
                    format = requireValue(args, i, "--format");
                    i++;
                }
                case "--output" -> {
                    outputPath = requireValue(args, i, "--output");
                    i++;
                }
                case "--partial-dir" -> {
                    partialDir = requireValue(args, i, "--partial-dir");
                    i++;
                }
                default -> {
                    if (arg.startsWith("--")) {
                        fail("Unknown option: " + arg);
                    }
                    templateArgs.add(arg);
                }
            }
            i++;
        }

        if (templateArgs.isEmpty()) {
            fail("At least one template argument is required");
        }
        if (format != null && !format.equals("json") && !format.equals("yaml")) {
            fail("Invalid --format value: " + format);
        }
        validateStdinUsage(templateArgs, dataArg);

        return new CliArguments(
                templateArgs,
                dataArg,
                format,
                outputPath == null ? null : Path.of(outputPath),
                partialDir == null ? null : Path.of(partialDir),
                false
        );
    }

    private void validateStdinUsage(List<String> templateArgs, String dataArg) {
        long stdinTemplateCount = templateArgs.stream().filter(STDIN_MARKER::equals).count();
        boolean dataWantsStdin = dataArg == null || dataArg.equals(STDIN_MARKER);
        long stdinConsumers = stdinTemplateCount + (dataWantsStdin ? 1 : 0);
        if (stdinConsumers > 1) {
            fail("Standard input can only be used by one of: templates or --data");
        }
    }

    private String requireValue(String[] args, int optionIndex, String optionName) {
        if (optionIndex + 1 >= args.length) {
            fail("Missing value for option: " + optionName);
        }
        return args[optionIndex + 1];
    }

    private void fail(String message) {
        log.debug("Argument validation failed: {}", message);
        throw new ArgumentException(message);
    }
}
