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

import java.nio.file.Path;
import java.util.List;

/**
 * コマンドライン引数の解析結果を保持するイミュータブルな値オブジェクト。
 *
 * @param templateArgs   テンプレート位置引数（1件以上）。各要素はファイルパス、または標準入力を表す{@code "-"}
 * @param dataArg        {@code --data}の値（nullable）。{@code null}または{@code "-"}は標準入力からの読込を意味する
 * @param format         {@code --format}の値（nullable）。{@code "json"}または{@code "yaml"}
 * @param outputPath     {@code --output}の値（nullable）。{@code null}は標準出力への出力を意味する
 * @param partialDir     {@code --partial-dir}の値（nullable）。{@code null}はテンプレートごとの既定ディレクトリ決定を意味する
 * @param helpRequested  {@code --help}/{@code -h}が指定されたか
 */
public record CliArguments(
        List<String> templateArgs,
        String dataArg,
        String format,
        Path outputPath,
        Path partialDir,
        boolean helpRequested
) {
}
