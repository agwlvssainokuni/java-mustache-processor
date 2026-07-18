# Code Generation Summary - cli (Unit 2: CLI Tool)

`cli-code-generation-plan.md`（全10ステップ）に基づき生成した成果物のサマリー。

## プロジェクト構成
- `cli/build.gradle.kts`（`application`プラグイン、Gradle Shadow plugin `com.gradleup.shadow:9.6.0`、OWASP Dependency-Check、Jackson、SLF4J）
- `cli/dependency-check-suppressions.xml`（`core`と同様の空の抑制ファイル）
- `.gitignore`に`cli/dependency-check-data/`を追記

## アプリケーションコード（`cli/src/main/java/cherry/mustache/cli/`）

| 分類 | クラス |
|---|---|
| 例外・ドメインモデル | `ArgumentException`, `ExitCode`（enum）, `CliArguments`（Record） |
| 引数解析 | `ArgumentParser` |
| データ読込 | `DataLoader`（Jackson: JSON/YAML統一） |
| 出力 | `OutputWriter` |
| オーケストレーション | `CliRunner`（例外→ExitCode一元マッピング、パーシャル解決ディレクトリ決定、Usage表示） |
| エントリポイント | `Main` |

全8ファイルにApache License 2.0のヘッダーコメントを付与（著作権者: agwlvssainokuni、2026年）。`cli`は他モジュールから利用される公開APIを持たないため（NFR Requirements Maintainability=N/A）、Javadocは主要な公開クラス・メソッドに簡潔なもののみを付与している。

## ロギング
`core`と同じくSLF4J（`slf4j-api`）を採用し`System.out`/`System.err`は使用しない。`cli`は（ライブラリではなく）エンドユーザー向け実行可能ツールであるため、`core`と異なり`slf4j-simple`を`implementation`スコープでfat jarに同梱している（詳細は「Code Generation中に発見・是正した不具合」参照）。ユーザー向けCLI出力（レンダリング結果・エラーメッセージ・Usage）は`CliRunner.run`に注入された`PrintStream`経由で行い、SLF4Jログ（診断用、`debug`/`error`）とは明確に区別している。

## テストコード（`cli/src/test/java/cherry/mustache/cli/`）

| 種別 | ファイル | 件数 |
|---|---|---|
| 引数解析 | `ArgumentParserTest` | 12 |
| データ読込 | `DataLoaderTest` | 7 |
| 出力 | `OutputWriterTest` | 3 |
| オーケストレーション（標準入出力モック化） | `CliRunnerTest` | 12 |

**合計34件、全件成功**（`./gradlew :cli:test`）。`./gradlew test`でcore（197件）との回帰がないことも確認済み。Property-Based Testing・公式Mustache specテストスイートは`unit-of-work-story-map.md`の通りUnit 1（core）の責務であり、本Unitでは対象外。

## ドキュメント
- リポジトリルートに`README.md`を新規作成し、`core`（ライブラリ）の基本的な使い方と`cli`のインストール・使用方法（オプション一覧・実行例・ラムダ非対応の制約）を記載

## Code Generation中に発見・是正した不具合（1件）
fat jarによる手動スモークテストの結果、以下の不具合を発見し是正した。

- **SLF4Jバインディング未同梱**: 当初`slf4j-simple`を`core`と同じ方針（`testRuntimeOnly`のみ）にしていたため、fat jarを実行するたびに`SLF4J(W): No SLF4J providers were found`という警告がstderrに出力されていた。`core`はライブラリでありバインディング選定を呼び出し側に委ねるのが正しい設計だが、`cli`はエンドユーザー向け実行可能ツールであるため、`slf4j-simple`を`implementation`スコープに変更しfat jarに同梱することで解消した

## 手動スモークテスト（fat jar）
`cli-0.1.0-SNAPSHOT-all.jar`を実際に`java -jar`で起動し、以下を確認した（NFR Requirements Q5で確定した方針通り自動テストの対象外だが、手動での動作確認を実施）:
- 正常系: 変数展開、パーシャル解決（テンプレートと同ディレクトリ）、YAML自動判定、複数テンプレート連結、標準入力データ（`--format`必須含む）、`--output`ファイル出力、`--help`
- 異常系: 引数エラー（テンプレート未指定）、テンプレート構文エラー（`PARSE_ERROR`）、データ構文エラー（`PARSE_ERROR`、`IO_ERROR`ではないことを確認）、存在しないテンプレートファイル（`IO_ERROR`）、標準入力競合（`ARGUMENT_ERROR`）
- いずれも期待通りの出力・終了コードであることを確認

## 既知の制約事項
- CLIはラムダを含むテンプレートのレンダリングをサポートしない（FR-2、README.mdに明記）
- fat jarをプロセス起動する自動E2Eテストは設けていない（NFR Requirements Q5=A、`CliRunner`への単体テストで代替）
- OWASP Dependency-Checkの実際のスキャン実行（NVD初回同期）はBuild and Testステージで実施（`core`と同じ方針）
