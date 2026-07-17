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
