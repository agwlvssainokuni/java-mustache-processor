# Code Generation Plan - cli (Unit 2: CLI Tool)

このプランがCode Generationの唯一の実行根拠（single source of truth）である。各ステップ完了時に直ちに`[x]`へ更新する。

## Unit Context

- **ワークスペースルート**: `~/Documents/project/git/java-mustache-processor`
- **プロジェクト種別**: Greenfield / Gradleマルチプロジェクト（monolith構成、`{unit-name}/src/`パターン）
- **対応要件**（`unit-of-work-story-map.md`より）: FR-2, NFR-2（cli固有分）, NFR-3（CLI固有ロジックの単体テスト）, NFR-4（Security Baselineのうち CLI固有分）
- **依存Unit**: `Unit 1: Core Template Engine`（`core`）— `Mustache`, `Template`, `PartialResolver`, `FilePartialResolver`, `MustacheException`系を利用
- **参照設計成果物**:
  - `aidlc-docs/inception/application-design/components.md`, `component-methods.md`
  - `aidlc-docs/construction/cli/functional-design/business-logic-model.md`, `business-rules.md`, `domain-entities.md`
  - `aidlc-docs/construction/cli/nfr-requirements/nfr-requirements.md`, `tech-stack-decisions.md`
  - `aidlc-docs/construction/cli/nfr-design/nfr-design-patterns.md`, `logical-components.md`

## ステップ一覧

### Step 1: Project Structure Setup（`cli/build.gradle.kts`本実装）
- [x] `cli/build.gradle.kts`に以下を追加:
  - Jackson依存（`com.fasterxml.jackson.core:jackson-databind:2.22.1`, `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.22.1`、`implementation`スコープ、tech-stack-decisions.md）
  - Gradle Shadow plugin（`com.gradleup.shadow` version `9.6.0`）の適用（`application`プラグインの`mainClass`設定をそのまま`shadowJar`のManifestにも反映）
  - OWASP Dependency-Checkプラグイン適用（`org.owasp.dependencycheck` version `10.0.4`、`core/build.gradle.kts`と同じ設定方針）
  - SLF4J（`org.slf4j:slf4j-api:2.0.16`、`implementation`）、`slf4j-simple:2.0.16`（`testRuntimeOnly`）
  - `testRuntimeOnly("org.junit.platform:junit-platform-launcher")`
- [x] `cli/dependency-check-suppressions.xml`（`core`と同様の空の抑制ファイル）
- [x] `cli/src/main/java/cherry/mustache/cli/`, `cli/src/test/java/cherry/mustache/cli/` ディレクトリ作成
- [x] `.gitignore`に`cli/dependency-check-data/`の除外を追記
- [x] `./gradlew :cli:build`でビルド疎通確認済み（BUILD SUCCESSFUL、`shadowJar`/`shadowDistTar`/`shadowDistZip`タスクも正常動作）
- **是正記録**（fat jarでのスモークテストにより発見）: 当初`slf4j-simple`を`testRuntimeOnly`のみとしていたが、これはcoreと同じ「ライブラリはバインディングを持たず呼び出し側に委ねる」方針をcliにもそのまま適用したものだった。しかしcliはライブラリではなくエンドユーザー向け実行可能ツール（fat jar）であり、バインディングを同梱しないと実行のたびに`SLF4J(W): No SLF4J providers were found`という警告がstderrに出力されてしまう。`slf4j-simple`を`implementation`スコープに変更し、fat jarに同梱することで解消した

### Step 2: 例外・ドメインモデルの生成（domain-entities.md）
- [x] `cherry.mustache.cli.ArgumentException`（`RuntimeException`継承、`message`コンストラクタ）
- [x] `cherry.mustache.cli.ExitCode`（enum: `SUCCESS(0)`, `ARGUMENT_ERROR(1)`, `PARSE_ERROR(2)`, `RENDER_ERROR(3)`, `IO_ERROR(4)`、`int code()`）
- [x] `cherry.mustache.cli.CliArguments`（イミュータブル値オブジェクト、Recordとして実装。`templateArgs`, `dataArg`, `format`, `outputPath`, `partialDir`, `helpRequested`）
- 対応BR: BR-2, BR-8
- `./gradlew :cli:compileJava`でコンパイル確認済み（BUILD SUCCESSFUL）

### Step 3: ArgumentParserの生成（business-rules.md BR-2）
- [x] `cherry.mustache.cli.ArgumentParser`（`CliArguments parse(String[] args)`）
  - 位置引数（テンプレート、1つ以上必須）とオプション（`--data`, `--format`, `--output`, `--partial-dir`, `--help`/`-h`）の解析
  - 標準入力競合検出（テンプレート`-`と`--data`省略/`-`の同時使用をエラーとする、business-logic-model.md 3節）
  - 値の構文検証のみ実施し、ファイルパスの存在確認は行わない（NFR-SEC-3、実行時検出方針）
  - `--help`検出時は他の検証（位置引数必須・stdin競合等）をスキップして即座に返す（BR-7）
- 対応BR: BR-2, BR-7 / 対応NFR: NFR-SEC-3
- `./gradlew :cli:compileJava`でコンパイル確認済み（BUILD SUCCESSFUL）

### Step 4: DataLoaderの生成（business-rules.md BR-1, BR-3）
- [x] `cherry.mustache.cli.DataLoader`（`Map<String, Object> load(CliArguments args, InputStream stdin)`）
  - Jacksonの`ObjectMapper`（JSON用）と`YAMLMapper`（YAML用、`jackson-dataformat-yaml`提供）を用いて`Map<String, Object>`に変換（`TypeReference`で型安全に指定）
  - データ形式判定順序: `--format`優先 → ファイル拡張子（`.json`/`.yaml`/`.yml`） → 判定不能なら`ArgumentException`（標準入力かつ`--format`未指定の場合も`ArgumentException`）
  - Jackson例外（`JsonProcessingException`）はラップせずそのまま伝播（nfr-design-patterns.mdパターン2）
- 対応BR: BR-1, BR-3 / 対応NFR: NFR-SEC-2
- `./gradlew :cli:compileJava`でコンパイル確認済み（BUILD SUCCESSFUL）

### Step 5: OutputWriterの生成（business-rules.md BR-6）
- [x] `cherry.mustache.cli.OutputWriter`（`void write(String content, CliArguments args, PrintStream stdout)`）
  - `outputPath`指定時は常に上書きでファイル書き込み（UTF-8固定、`Files.writeString`の既定動作）、未指定時は`stdout`へ書き込み（余分な改行を付加しない）
  - `IOException`はラップせず伝播（nfr-design-patterns.mdパターン3、アトミック書き込みなし）
- 対応BR: BR-6
- `./gradlew :cli:compileJava`でコンパイル確認済み（BUILD SUCCESSFUL）

### Step 6: CliRunnerの生成（business-logic-model.md 1節・4節、nfr-design-patterns.mdパターン1）
- [x] `cherry.mustache.cli.CliRunner`（`ExitCode run(String[] args, InputStream in, PrintStream out, PrintStream err)`）
  - オーケストレーションフロー（引数解析→`--help`分岐→データ読込→テンプレートごとのソース読込・パーシャルディレクトリ決定・コンパイル・レンダリング→連結→出力）を実装
  - 単一try-catchチェーンによる例外→`ExitCode`マッピング（`ArgumentException`→`JsonProcessingException`→`MustacheParseException`→`MustacheRenderException`→`IOException`→`RuntimeException`の順）
  - `--help`時のUsage表示（`out`へ出力し`SUCCESS`を返す）
  - パーシャル解決ディレクトリ決定ロジック（`--partial-dir`優先、未指定時はテンプレートごとの親ディレクトリ、標準入力由来テンプレートは常にnullを返すPartialResolver固定、business-logic-model.md 5節）
  - エラーメッセージは内部詳細を含まない簡潔な文言を`err`へ、詳細はSLF4Jログへ（BR-9）。Jackson例外・分類不能なRuntimeExceptionは汎用メッセージ、それ以外（ArgumentException/MustacheParseException/MustacheRenderException/IOException）は`getMessage()`をそのまま出力（いずれも内部実装詳細を含まない設計済みのメッセージのため）
- 対応BR: BR-2〜BR-9 / 対応NFR: 全Resilience/Security Patterns
- `./gradlew :cli:compileJava`でコンパイル確認済み（BUILD SUCCESSFUL）

### Step 7: Mainの生成
- [x] `cherry.mustache.cli.Main`（`public static void main(String[] args)`。`CliRunner.run(args, System.in, System.out, System.err)`を呼び出し`System.exit(exitCode.code())`）
- [x] `cli/build.gradle.kts`の`application.mainClass`が`cherry.mustache.cli.Main`を指していることを確認（既存プレースホルダーの通り）
- [x] `./gradlew :cli:build`でビルド確認済み（BUILD SUCCESSFUL）
- [x] fat jar（`cli-0.1.0-SNAPSHOT-all.jar`）による手動スモークテストを実施し、正常系（変数展開、パーシャル解決、YAML自動判定、複数テンプレート連結、標準入力データ、`--output`、`--help`）・異常系（引数エラー、テンプレート構文エラー、データ構文エラー、存在しないファイル、標準入力競合）のすべてで期待通りの出力・終了コードを確認

### Step 8: 単体テストの生成（NFR-REL-1、cli-nfr-requirements-plan.md Q5=A）
- [ ] `ArgumentParserTest`（正常系・異常系: 位置引数0件、未知オプション、標準入力競合検出、`--help`等）
- [ ] `DataLoaderTest`（JSON/YAML読込、拡張子判定、`--format`優先、判定不能ケース、Jackson構文エラー）
- [ ] `OutputWriterTest`（標準出力/ファイル出力、上書き）
- [ ] `CliRunnerTest`（`ByteArrayInputStream`/`ByteArrayOutputStream`による標準入出力モック化。代表的な引数パターン・データ形式・エラーケース（引数エラー、データ構文エラー、テンプレート構文エラー、レンダリングエラー、I/Oエラー）ごとに`ExitCode`と出力内容を検証。特にJSON構文エラー時に`PARSE_ERROR`（`IO_ERROR`ではない）になることを明示的に検証するケースを含める（catch順序の正しさの回帰防止）
- 対応NFR: NFR-REL-1

### Step 9: ドキュメント生成
- [ ] `aidlc-docs/construction/cli/code/code-summary.md`（生成ファイル一覧、テスト構成、既知の制約事項）
- [ ] リポジトリルートまたは`cli/`にCLI使用方法の簡易ドキュメント（オプション一覧、使用例）を追加するか検討し、追加する場合は`README.md`を新規作成・更新する

### Step 10: OWASP Dependency-Check設定検証
- [ ] `./gradlew :cli:tasks`で`dependencyCheckAnalyze`等のタスクが登録されていることを確認
- [ ] 実際のスキャン実行はBuild and Testステージで実施（`core`と同じ方針）

## 対象外と判断した項目（理由付き）
- **Repository Layer**: cliはデータストアを持たないため対象外
- **Frontend Components**: UIを持たないCLIのため対象外
- **Database Migration Scripts**: データモデル（DB）を持たないため対象外
- **Deployment Artifacts Generation**: fat jarのビルド設定自体はStep1で行うが、実際のリリース・配布プロセスの構築は当面のスコープ外（requirements.md NFR-2「当面は非公開」）
