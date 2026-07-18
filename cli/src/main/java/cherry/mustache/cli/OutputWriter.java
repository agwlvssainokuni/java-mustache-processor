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

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * レンダリング結果を標準出力またはファイルへ書き込む。
 */
public class OutputWriter {

    public void write(String content, CliArguments args, PrintStream stdout) throws IOException {
        if (args.outputPath() != null) {
            Files.writeString(args.outputPath(), content, StandardCharsets.UTF_8);
        } else {
            stdout.print(content);
        }
    }
}
