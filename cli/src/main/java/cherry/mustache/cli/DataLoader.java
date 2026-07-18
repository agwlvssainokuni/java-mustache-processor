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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * JSON/YAML形式の入力データ（ファイルまたは標準入力）を{@code Map<String, Object>}へ変換する。
 */
public class DataLoader {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_MAPPER = new YAMLMapper();

    public Map<String, Object> load(CliArguments args, InputStream stdin) throws IOException {
        boolean useStdin = args.dataArg() == null || args.dataArg().equals("-");
        ObjectMapper mapper = resolveFormat(args, useStdin).equals("yaml") ? YAML_MAPPER : JSON_MAPPER;

        if (useStdin) {
            return mapper.readValue(stdin, MAP_TYPE);
        }
        try (InputStream in = Files.newInputStream(Path.of(args.dataArg()))) {
            return mapper.readValue(in, MAP_TYPE);
        }
    }

    private String resolveFormat(CliArguments args, boolean useStdin) {
        if (args.format() != null) {
            return args.format();
        }
        if (useStdin) {
            throw new ArgumentException("--format is required when reading data from standard input");
        }
        String path = args.dataArg();
        if (path.endsWith(".json")) {
            return "json";
        }
        if (path.endsWith(".yaml") || path.endsWith(".yml")) {
            return "yaml";
        }
        throw new ArgumentException("Cannot determine data format from file extension: " + path);
    }
}
