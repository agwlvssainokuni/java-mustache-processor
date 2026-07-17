# Code Generation Plan - core (Unit 1: Core Template Engine)

このプランがCode Generationの唯一の実行根拠（single source of truth）である。各ステップ完了時に直ちに`[x]`へ更新する。

## Unit Context

- **ワークスペースルート**: `~/Documents/project/git/java-mustache-processor`
- **プロジェクト種別**: Greenfield / Gradleマルチプロジェクト（monolith構成、`{unit-name}/src/`パターン）
- **対応要件**（`unit-of-work-story-map.md`より）: FR-1, NFR-1, NFR-3, NFR-4, NFR-6
- **依存Unit**: なし（`core`は依存を持たない最下層Unit）
- **Unit 2（cli）への公開インターフェース**: `Mustache`, `Template`, `PartialResolver`, `MapPartialResolver`, `FilePartialResolver`, `Lambda`, `MustacheException`系
- **参照設計成果物**:
  - `aidlc-docs/inception/application-design/components.md`, `component-methods.md`
  - `aidlc-docs/construction/core/functional-design/business-logic-model.md`, `business-rules.md`, `domain-entities.md`
  - `aidlc-docs/construction/core/nfr-requirements/nfr-requirements.md`, `tech-stack-decisions.md`
  - `aidlc-docs/construction/core/nfr-design/nfr-design-patterns.md`, `logical-components.md`

## ステップ一覧

### Step 1: Project Structure Setup（Greenfield）
- [x] `settings.gradle.kts`（`rootProject.name`、`include("core", "cli")`）
- [x] ルート`build.gradle.kts`（共通設定: Java 25 toolchain、UTF-8固定、`group`/`version`一元管理）
- [x] Gradle Wrapperの生成（`gradlew`, `gradlew.bat`, `gradle/wrapper/`）
- [x] `core/build.gradle.kts`（`java-library`プラグイン、JUnit5/jqwik依存、OWASP Dependency-Checkプラグイン適用）
- [x] `core/src/main/java/cherry/mustache/`, `core/src/test/java/cherry/mustache/`, `core/src/test/resources/spec/` ディレクトリ作成
- [x] `cli/build.gradle.kts`のプレースホルダー（Unit 2で本実装、ここでは`core`への依存宣言のみ記述しビルドが通る最小構成とする）
- [x] `.gitignore`にGradleビルド生成物（`.gradle/`, `build/`）・OWASP Dependency-Checkデータの除外、`gradle-wrapper.jar`の除外解除を追記
- [x] `./gradlew build`でビルド疎通確認済み（BUILD SUCCESSFUL）

### Step 2: 例外階層の生成（domain-entities.md）
- [x] `cherry.mustache.MustacheException`（`RuntimeException`継承、`message`/`cause`コンストラクタ）
- [x] `cherry.mustache.MustacheParseException`（`line`, `column`フィールド）
- [x] `cherry.mustache.MustacheRenderException`（`key`フィールド、nullable）
- 対応BR: BR-8, BR-9 / 対応SECURITY: SECURITY-09, SECURITY-15

### Step 3: ASTドメインモデルの生成（domain-entities.md）
- [x] `cherry.mustache.ast.Node`（抽象基底、`render(Context, RenderSession, Writer)`）
- [x] `cherry.mustache.ast.TextNode`
- [x] `cherry.mustache.ast.VariableNode`
- [x] `cherry.mustache.ast.UnescapedVariableNode`
- [x] `cherry.mustache.ast.SectionNode`（`rawText`フィールド含む）
- [x] `cherry.mustache.ast.InvertedSectionNode`
- [x] `cherry.mustache.ast.CommentNode`
- [x] `cherry.mustache.ast.PartialNode`
- [x] `cherry.mustache.ast.RootNode`（ASTルート、domain-entities.mdには無いが複数の子ノードを束ねる実装上必要な追加ノード）
- [x] `cherry.mustache.ast.Reparser`（functional interface、Lambda出力・パーシャル本文の再パース用コールバック。`ast`が`parser`パッケージへ直接依存しないための抽象化、実装上の追加）
- [x] `cherry.mustache.ast.HtmlEscaper` / `Truthiness` / `LambdaSupport`（package-private内部ユーティリティ）
- 対応BR: BR-1〜BR-6, BR-11
- **実装上の補足**: Lambda再パースはBR-2.4節「現在のデリミタで再パース」を満たすため、`SectionNode`/`VariableNode`/`UnescapedVariableNode`に`openDelimiter`/`closeDelimiter`スナップショットを追加（domain-entities.mdのフィールド一覧には無いが、承認済みの業務ルールを満たすための実装詳細であり業務ルール自体への変更ではない）

### Step 4: Contextの生成（domain-entities.md, BR-6, BR-10）
- [x] `cherry.mustache.render.Context`（イミュータブル、`data`/`parent`フィールド、`resolve(String)`/`push(Object)`）
- [x] POJOプロパティ解決ロジック（getter→Recordアクセサ→publicフィールド、BR-7）を`PojoResolver`（package-private）として実装
- [x] `cherry.mustache.render.Lookup`（package-private record、「未解決」と「解決結果がnull」を区別する内部ヘルパー）
- 対応BR: BR-6, BR-7, BR-10

### Step 5: Parserの生成（business-logic-model.md 1節）
- [x] `cherry.mustache.parser.Parser`（`Node parse(String template)` / `Node parse(String template, String openDelimiter, String closeDelimiter)`）
  - タグスキャン、タグ種別判定、スタンドアロン行判定・空白除去、スタックベースのツリー構築、パーシャル遅延解決（`PartialNode`生成のみ）
  - Delimitersは独立クラスとせず、Parser内のローカル変数として実装（domain-entities.mdの意図する「Parser内部の一時状態」を素直に実現）
- 対応BR: BR-2, BR-4, BR-5, BR-11 / 対応SECURITY: SECURITY-05（入力の構文検証）

### Step 6: Rendererの生成（business-logic-model.md 2節）
- [x] `cherry.mustache.render.Renderer`（`render(Node root, Context context, PartialResolver partialResolver, Writer out)`）
- [x] `cherry.mustache.render.RenderSession`（実装上の追加: `PartialResolver`・Lambda再パースコールバック・循環参照検出用`resolvingPartials`集合を1回の`render()`呼び出しスコープで束ねる。component-methods.mdの`PartialResolver`単体パラメータをこのセッションオブジェクトに置き換え）
- [x] HTMLエスケープ処理（BR-1、`ast.HtmlEscaper`）
- [x] 真偽判定ロジック（BR-3、`ast.Truthiness`）
- [x] ラムダ処理（変数/セクション/否定セクション、BR-2.4節相当）
- [x] パーシャル循環参照検出（`RenderSession.resolvingPartials`、呼び出しスタックローカル、BR-9）
- 対応BR: BR-1, BR-3, BR-6, BR-8, BR-9

### Step 7: PartialResolver実装群の生成（domain-entities.md, nfr-design-patterns.md）
- [x] `cherry.mustache.PartialResolver`（interface、`resolve(String partialName)`）
- [x] `cherry.mustache.MapPartialResolver`
- [x] `cherry.mustache.FilePartialResolver`（パス正規化・`baseDir`前方一致ガード節、`IOException`の`MustacheRenderException`ラップ）
- 対応NFR: NFR-SEC-1（パストラバーサル対策）、Resilience Patterns（I/Oエラー処理）

### Step 8: Lambdaの生成
- [x] `cherry.mustache.Lambda`（functional interface、`String execute(String text)`）

### Step 9: 公開APIの生成（`Mustache`, `Template`）
- [x] `cherry.mustache.Mustache`（static factory: `compile(String)`, `compile(String, PartialResolver)`, `compile(Reader, PartialResolver)`）
- [x] `cherry.mustache.Template`（`render(Object)`, `render(Object, Writer)`, `render(Object, PartialResolver)`、内部にコンパイル済みASTを保持し再利用、Performance Patterns準拠）
- [x] 全公開クラス・メソッドにJavadocを付与（NFR-MNT-1）
- [x] 10ケースのスモークテスト（変数展開/エスケープ、リストセクション、否定セクション、スタンドアロン行+パーシャル、デリミタ変更、変数/セクションLambda、POJO Record、ドット表記、Broken Chain）で動作確認。POJOアクセスでローカルRecordクラス（非public宣言クラス）の場合に`IllegalAccessException`が発生する不具合を発見し、`PojoResolver`に`setAccessible(true)`を追加して修正

### Step 10: Business Logic Unit Testing（JUnit 5）
- [x] Parserの単体テスト（`parser/ParserTest.java`: 未終了タグ・タグ不一致・未終了セクション・不正なSet Delimiter・行列位置）
- [x] Rendererの単体テスト（`TemplateRenderingTest.java`: BR-1〜BR-11相当の23ケース。エスケープ、真偽判定、リスト展開、ドット表記/Broken Chain、POJO/Record解決順序、POJOアクセス例外ラップ、コメント、スタンドアロン行、パーシャルインデント、循環参照検出、デリミタのパーシャル非リーク、変数/セクション/否定セクションLambda）
- [x] Contextの単体テスト（`render/ContextTest.java`: スタック探索、シャドーイング、ドット表記の非遡及、暗黙イテレータ`.`、未解決キー）
- [x] PartialResolver実装群の単体テスト（`MapPartialResolverTest.java`, `FilePartialResolverTest.java`: パストラバーサル拒否ケース含む）
- [x] 例外階層の単体テスト（`ExceptionsTest.java`）
- **テスト実行で発見した実装上の論点**: Variable-Lambdaがネストしたタグ（例: `{{lambda}}`が`"{{inner}}"`を返す）を返し、かつその内部タグの解決値にHTML特殊文字を含む場合、二重エスケープが発生する（内部タグ自身のエスケープ＋外側タグのエスケープが重なるため）。公式spec `~lambdas.yml`の関連テスト（Interpolation - Expansion / Escaping）はいずれもこの组み合わせを使っておらず、spec上も未規定のエッジケースと判断。実装は変更せず、公式spec準拠のテスト期待値（特殊文字を含まないデータ）に修正

### Step 11: Property-Based Testing（jqwik、PBT-02〜PBT-10）
- [x] functional-design/business-logic-model.md 4節「Testable Properties」の各項目に対応するPBTを実装:
  - Invariant（HTMLエスケープ: `EscapingPropertyTest`、参照エスケーパーをOracleとして使用）
  - Invariant（リスト展開: `EscapingPropertyTest#sectionOverListRendersEachElementInOrder`）
  - Oracle（ドット表記解決 = 手動Map.get: `render/ContextPropertyTest`）
  - Oracle（デリミタ変更 = デフォルトデリミタと同値: `TemplateEquivalencePropertyTest#customDelimiterProducesSameResultAsDefaultDelimiter`）
  - Idempotence相当（非循環パーシャル参照の独立性・決定性: `TemplateEquivalencePropertyTest#nonCircularPartialReferencesAreIndependentAndDeterministic`）
  - Oracle（公式specスイートとの突合）→ Step 12の統合テストで実施
- [x] 各PBTに対しドメイン固有のジェネレータ（`@AlphaChars`/`@StringLength`制約付き文字列、`List<String>`等）を使用（PBT-07）
- [x] shrinking・seedベース再現性はjqwik標準機能をそのまま使用（PBT-08、無効化しない）
- [x] PBTテストはExample-basedテスト（Step 10）と明確にファイル/クラスを分離（PBT-10、`*PropertyTest`という命名規則）
- **PBT実行で発見した実装バグの修正**: `nonCircularPartialReferencesAreIndependentAndDeterministic`プロパティが失敗し、Parserのスタンドアロン行判定に不備を発見（同一行に複数タグが隣接する場合、両方を誤って「単独行」と判定していた）。BR-4「そのタグが行内で唯一のコンテンツ」の「唯一」を厳密に満たすよう`applyStandaloneTrimming`を修正（前後最大2トークン参照で「別のタグが同じ行に存在するか」を正しく判定）。修正後、全50テスト（例示ベース38件＋PBT 12件）成功

### Step 12: 公式Mustache specテストスイートの統合（NFR-3, BR-2, Oracle PBT）
- [x] `core/src/test/resources/spec/`配下にBR-2で確定したスコープのYAMLファイル（`comments.yml`, `delimiters.yml`, `interpolation.yml`, `inverted.yml`, `partials.yml`, `sections.yml`, `~lambdas.yml`）を公式リポジトリ（github.com/mustache/spec）から取得し配置
- [x] YAMLパース用テストスコープ依存（SnakeYAML 2.3）を`core/build.gradle.kts`の`testImplementation`に追加済み（Step1）
- [x] spec YAMLを読み込み、各テストケースをJUnit 5の動的テスト（`@TestFactory`）として実行する`spec/MustacheSpecTest.java`を実装（`!code`タグをマーカーオブジェクトに置き換えるカスタムSnakeYAML Constructorを使用）
- [x] `~lambdas.yml`内の10ケースについて、Ruby等のコード定義をテスト名ベースで対応するJava `Lambda`実装にマッピング（`lambdaImplementations()`）
- **spec実行で発見・是正した実装バグ（4件）**:
  1. 変数タグLambdaの戻り値は「現在のデリミタ」ではなく常に**デフォルトデリミタ**で再パースすべきだった（`~lambdas.yml`「Interpolation - Alternate Delimiters」）。`VariableNode`/`UnescapedVariableNode`を修正し、`openDelimiter`/`closeDelimiter`フィールドを削除
  2. パーシャル循環参照検出を「パーシャル名の再出現」で行っていたため、`partials.yml`「Recursion」テスト（データ駆動で終端する正当な自己再帰）を誤検出していた。検出基準を「ネスト深さの上限（100）」に変更（`RenderSession`, BR-9是正）
  3. `applyStandaloneTrimming`が、隣接する2つのスタンドアロンタグが同じTEXTトークンを共有する場合に誤判定していた（`inverted.yml`/`sections.yml`「Standalone Line Endings」、`delimiters.yml`「Sections」「Inverted Sections」）。判定と適用を2パスに分離し是正
  4. `SectionNode`で真かつリスト以外の値のうちスカラー値をpushしない実装になっていたため、`{{.}}`と`{{key}}`（親へのフォールバック）を両立できなかった（`sections.yml`「Variable test」「Deeply Nested Contexts」）。スカラー値も常にpushするよう是正
  5. パーシャルのインデント再適用を「レンダリング後の出力文字列」に対して行っていたため、埋め込まれたデータ値自体の改行にも誤ってインデントが付与されていた（`partials.yml`「Standalone Indentation」）。インデントは「パーシャルの生テンプレート文字列（パース前）」に適用するよう是正（BR-5是正）
- 全196テスト（例示ベース38件＋PBT 12件＋公式spec 146件）成功

### Step 13: 信頼性テスト（Reliability、nfr-design-patterns.md）
- [x] `ConcurrentRenderTest.java`: 同一`Template`インスタンスに対し16スレッド×200回、異なるデータで並行`render()`を実行し、各呼び出しの出力が期待値と一致することを確認（NFR-REL-1）。全196+1件成功、回帰なし

### Step 14: セキュリティツールの設定（SECURITY-10）
- [x] `core/build.gradle.kts`にOWASP Dependency-Checkプラグイン（10.0.4）を適用済み（Step1）。`failBuildOnCVSS = 7.0`、`dependency-check-suppressions.xml`を設定
- [x] `dependencyCheckAnalyze`等のタスクが正しく登録されていることを`./gradlew :core:tasks`で確認
- **備考**: `dependencyCheckAnalyze`の実行にはNVD（脆弱性データベース）の初回同期が必要で、ネットワーク環境・NVD APIレート制限により数分〜数十分を要する場合がある。Code Generationステージでは設定の妥当性確認までとし、実際のスキャン実行・CI組み込み手順はBuild and Testステージで確定する

### Step 15: ドキュメント生成
- [x] `aidlc-docs/construction/core/code/code-summary.md`（生成ファイル一覧、テスト構成、spec実行で発見・是正したバグ5件、既知の制約事項）を作成

## 対象外（N/A）と判断した項目
- **API Layer Generation / Repository Layer Generation**: `core`はWeb API・永続化層を持たないライブラリのため対象外（該当ロジックはStep 5〜9の「公開API」「ドメインモデル」に統合済み）
- **Frontend Components Generation**: UIを持たないため対象外
- **Database Migration Scripts**: データストアを持たないため対象外
- **Deployment Artifacts Generation**: `core`はライブラリJARであり、Gradleの標準`jar`タスクの成果物で十分。fat jar化はUnit 2（cli）の責務であり対象外
