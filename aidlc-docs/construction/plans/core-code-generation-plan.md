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
- [ ] `cherry.mustache.MustacheException`（`RuntimeException`継承、`message`/`cause`コンストラクタ）
- [ ] `cherry.mustache.MustacheParseException`（`line`, `column`フィールド）
- [ ] `cherry.mustache.MustacheRenderException`（`key`フィールド、nullable）
- 対応BR: BR-8, BR-9 / 対応SECURITY: SECURITY-09, SECURITY-15

### Step 3: ASTドメインモデルの生成（domain-entities.md）
- [ ] `cherry.mustache.ast.Node`（抽象基底、`render(Context, PartialResolver, Writer)`）
- [ ] `cherry.mustache.ast.TextNode`
- [ ] `cherry.mustache.ast.VariableNode`
- [ ] `cherry.mustache.ast.UnescapedVariableNode`
- [ ] `cherry.mustache.ast.SectionNode`（`rawText`フィールド含む）
- [ ] `cherry.mustache.ast.InvertedSectionNode`
- [ ] `cherry.mustache.ast.CommentNode`
- [ ] `cherry.mustache.ast.PartialNode`
- 対応BR: BR-1〜BR-6, BR-11

### Step 4: Contextの生成（domain-entities.md, BR-6, BR-10）
- [ ] `cherry.mustache.render.Context`（イミュータブル、`data`/`parent`フィールド、`resolve(String)`/`push(Object)`）
- [ ] POJOプロパティ解決ロジック（getter→Recordアクセサ→publicフィールド、BR-7）をContext内部または専用ヘルパーとして実装
- 対応BR: BR-6, BR-7, BR-10

### Step 5: Parserの生成（business-logic-model.md 1節）
- [ ] `cherry.mustache.parser.Delimiters`（内部状態、`open`/`close`）
- [ ] `cherry.mustache.parser.Parser`（`Node parse(String template)`）
  - タグスキャン、タグ種別判定、スタンドアロン行判定・空白除去、スタックベースのツリー構築、パーシャル遅延解決（`PartialNode`生成のみ）
- 対応BR: BR-2, BR-4, BR-5, BR-11 / 対応SECURITY: SECURITY-05（入力の構文検証）

### Step 6: Rendererの生成（business-logic-model.md 2節）
- [ ] `cherry.mustache.render.Renderer`（`render(Node root, Context context, PartialResolver partialResolver, Writer out)`）
- [ ] HTMLエスケープ処理（BR-1）
- [ ] 真偽判定ロジック（BR-3）
- [ ] ラムダ処理（変数/セクション/否定セクション、BR-2.4節相当）
- [ ] パーシャル循環参照検出（`resolvingPartials`、呼び出しスタックローカル、BR-9）
- 対応BR: BR-1, BR-3, BR-6, BR-8, BR-9

### Step 7: PartialResolver実装群の生成（domain-entities.md, nfr-design-patterns.md）
- [ ] `cherry.mustache.PartialResolver`（interface、`resolve(String partialName)`）
- [ ] `cherry.mustache.MapPartialResolver`
- [ ] `cherry.mustache.FilePartialResolver`（パス正規化・`baseDir`前方一致ガード節、`IOException`の`MustacheRenderException`ラップ、`try-with-resources`）
- 対応NFR: NFR-SEC-1（パストラバーサル対策）、Resilience Patterns（I/Oエラー処理）

### Step 8: Lambdaの生成
- [ ] `cherry.mustache.Lambda`（functional interface、`String execute(String text)`）

### Step 9: 公開APIの生成（`Mustache`, `Template`）
- [ ] `cherry.mustache.Mustache`（static factory: `compile(String)`, `compile(String, PartialResolver)`, `compile(Reader, PartialResolver)`）
- [ ] `cherry.mustache.Template`（`render(Object)`, `render(Object, Writer)`, `render(Object, PartialResolver)`、内部にコンパイル済みASTを保持し再利用、Performance Patterns準拠）
- 全公開クラス・メソッドにJavadocを付与（NFR-MNT-1）

### Step 10: Business Logic Unit Testing（JUnit 5）
- [ ] Parserの単体テスト（BR-1〜BR-6, BR-11それぞれについて代表ケース + 境界値）
- [ ] Rendererの単体テスト（真偽判定の全パターン、ラムダ、循環参照検出でMustacheRenderExceptionが送出されること等）
- [ ] Contextの単体テスト（ドット表記解決、Broken Chain、POJO/Record/Map解決順序）
- [ ] PartialResolver実装群の単体テスト（MapPartialResolver、FilePartialResolverのパストラバーサル拒否ケースを含む）
- [ ] 例外階層の単体テスト

### Step 11: Property-Based Testing（jqwik、PBT-02〜PBT-10）
- [ ] functional-design/business-logic-model.md 4節「Testable Properties」の各項目に対応するPBTを実装:
  - Oracle（公式specスイートとの突合）→ Step 12の統合テストと合わせて実施
  - Invariant（HTMLエスケープ、リスト展開、ドット表記解決、デリミタ変更）
  - Idempotence相当（非循環パーシャル参照）
- [ ] 各PBTに対しドメイン固有のジェネレータ（テンプレート断片、POJO/Map混在データ等）を用意（PBT-07）
- [ ] shrinking・seedベース再現性はjqwik標準機能をそのまま使用（PBT-08、無効化しない）
- [ ] PBTテストはExample-basedテスト（Step 10）と明確にファイル/クラスを分離する（PBT-10、例: `*PropertyTest`という命名規則）

### Step 12: 公式Mustache specテストスイートの統合（NFR-3, BR-2, Oracle PBT）
- [ ] `core/src/test/resources/spec/`配下にBR-2で確定したスコープのYAMLファイル（`comments.yml`, `delimiters.yml`, `interpolation.yml`, `inverted.yml`, `partials.yml`, `sections.yml`, `~lambdas.yml`）を配置
- [ ] YAMLパース用テストスコープ依存（SnakeYAML等）を`core/build.gradle.kts`の`testImplementation`に追加
- [ ] spec YAMLを読み込み、各テストケースをJUnit 5の動的テスト（`@TestFactory`）として実行するテストランナーを実装
- [ ] `~lambdas.yml`内のラムダケースについては、YAML内のスクリプト定義（Ruby想定）をJavaの`Lambda`実装へ手動でマッピングする対応表を用意

### Step 13: 信頼性テスト（Reliability、nfr-design-patterns.md）
- [ ] 同一`Template`インスタンスへの並行`render()`呼び出しを検証するマルチスレッドテストを実装（複数スレッドが異なるデータで同時にrenderし、各スレッドの出力が期待値と一致することを確認）

### Step 14: セキュリティツールの設定（SECURITY-10）
- [ ] `core/build.gradle.kts`にOWASP Dependency-Checkプラグインを適用し、基本設定（失敗しきい値等）を行う

### Step 15: ドキュメント生成
- [ ] `aidlc-docs/construction/core/code/code-summary.md`（生成ファイル一覧、テスト構成、既知の制約事項のMarkdownサマリー）を作成

## 対象外（N/A）と判断した項目
- **API Layer Generation / Repository Layer Generation**: `core`はWeb API・永続化層を持たないライブラリのため対象外（該当ロジックはStep 5〜9の「公開API」「ドメインモデル」に統合済み）
- **Frontend Components Generation**: UIを持たないため対象外
- **Database Migration Scripts**: データストアを持たないため対象外
- **Deployment Artifacts Generation**: `core`はライブラリJARであり、Gradleの標準`jar`タスクの成果物で十分。fat jar化はUnit 2（cli）の責務であり対象外
