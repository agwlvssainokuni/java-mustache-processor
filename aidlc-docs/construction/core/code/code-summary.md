# Code Generation Summary - core (Unit 1: Core Template Engine)

`core-code-generation-plan.md`（全15ステップ）に基づき生成した成果物のサマリー。

## プロジェクト構成
- `settings.gradle.kts`, `build.gradle.kts`（ルート、Java 25 toolchain・UTF-8・group/version一元管理）
- `core/build.gradle.kts`（`java-library`, SLF4J API, JUnit 5, jqwik, SnakeYAML, OWASP Dependency-Check）
- `cli/build.gradle.kts`（Unit 2向けプレースホルダー、`core`への依存宣言のみ）
- Gradle Wrapper（`gradlew`, `gradlew.bat`, `gradle/wrapper/`）

## アプリケーションコード（`core/src/main/java/cherry/mustache/`）

| 分類 | クラス |
|---|---|
| 例外階層 | `MustacheException`, `MustacheParseException`, `MustacheRenderException` |
| ASTドメインモデル（`ast`パッケージ） | `Node`（抽象基底）, `RootNode`, `TextNode`, `VariableNode`, `UnescapedVariableNode`, `SectionNode`, `InvertedSectionNode`, `CommentNode`, `PartialNode`, `Reparser`（再パースコールバックinterface）, `HtmlEscaper`/`Truthiness`/`LambdaSupport`（package-privateユーティリティ） |
| レンダリング（`render`パッケージ） | `Context`（イミュータブル）, `Lookup`/`PojoResolver`（package-private、POJO解決）, `RenderSession`（パーシャル解決・Lambda再パース・循環参照検出を束ねる）, `Renderer` |
| パース（`parser`パッケージ） | `Parser`（タグスキャン・スタンドアロン行トリミング・スタックベースツリー構築） |
| パーシャル解決 | `PartialResolver`（interface）, `MapPartialResolver`, `FilePartialResolver`（パストラバーサル対策込み） |
| Lambda | `Lambda`（functional interface） |
| 公開API | `Mustache`（static factory）, `Template` |

全28ファイルにApache License 2.0のヘッダーコメントを付与（著作権者: agwlvssainokuni、2026年）。公開API（`Mustache`, `Template`, `PartialResolver`, `MapPartialResolver`, `FilePartialResolver`, `Lambda`, 例外クラス）にはJavadocを完備（NFR-MNT-1）。

## ロギング（ユーザー指示により追加）
`System.out`/`System.err`は使用せず、SLF4J（`slf4j-api`）をログAPIとして採用。`FilePartialResolver`（パストラバーサル拒否・I/Oエラー）、`PartialNode`（未解決パーシャル・循環参照）、`PojoResolver`（アクセサ例外）、`Parser`（構文エラー）、`Template`/`Mustache`（I/Oエラー）に適切なログレベル（debug/warn）で出力箇所を追加。

## テストコード（`core/src/test/java/cherry/mustache/`）

| 種別 | ファイル | 件数 |
|---|---|---|
| 例示ベース（Parser） | `parser/ParserTest.java` | 6 |
| 例示ベース（業務ルール） | `TemplateRenderingTest.java` | 23 |
| 例示ベース（Context） | `render/ContextTest.java` | 6 |
| 例示ベース（PartialResolver） | `MapPartialResolverTest.java`, `FilePartialResolverTest.java` | 5 |
| 例示ベース（例外） | `ExceptionsTest.java` | 4 |
| 例示ベース（信頼性） | `ConcurrentRenderTest.java` | 1 |
| PBT（jqwik、`*PropertyTest`命名） | `EscapingPropertyTest.java`, `TemplateEquivalencePropertyTest.java`, `render/ContextPropertyTest.java` | 6 |
| 公式spec統合（動的テスト） | `spec/MustacheSpecTest.java` + `src/test/resources/spec/*.yml` | 146 |

**合計197件、全件成功**（`./gradlew :core:test`）。

## 公式Mustache specテストスイート
`comments.yml`, `delimiters.yml`, `interpolation.yml`, `inverted.yml`, `partials.yml`, `sections.yml`, `~lambdas.yml`（github.com/mustache/spec より取得）を`core/src/test/resources/spec/`に配置。`~lambdas.yml`のRuby等のコード定義（`!code`タグ）はSnakeYAMLのカスタムConstructorでマーカーに置き換え、テスト名ベースで同等のJava `Lambda`実装（10ケース分）に手動マッピングした。

## spec実行を通じて発見・是正した実装バグ（5件）
Code Generation中、公式spec準拠テストの実行によって以下の実装バグを発見し是正した。詳細な是正記録は`functional-design/business-logic-model.md`・`business-rules.md`・`domain-entities.md`に反映済み。

1. **変数タグLambdaの再パースデリミタ**: 「現在のデリミタ」ではなく常に「デフォルトデリミタ」を使うべきだった（セクションタグLambdaとは異なる）
2. **パーシャル循環参照検出**: 「パーシャル名の再出現」方式では、データ駆動で終端する正当な自己再帰（木構造の描画等）を誤検出する。「ネスト深さの上限（100）」方式に変更
3. **スタンドアロン行トリミング**: 隣接する2つのタグが同じTEXTトークンを共有する場合の誤判定。判定と適用を2パスに分離
4. **セクションのスカラー値push**: `{{.}}`と親コンテキストへのフォールバックを両立するため、スカラー値も常にpushするよう修正
5. **パーシャルインデント再適用**: レンダリング後の出力ではなく、パーシャルの生テンプレート文字列（パース前）に適用するよう修正（埋め込みデータの改行がインデントされてしまう不具合の是正）

## 既知の制約事項
- `~inheritance.yml`（テンプレート継承）・`~dynamic-names.yml`（動的パーシャル名）は要件スコープ外（BR-2）
- OWASP Dependency-Checkの実際のスキャン実行（NVD初回同期）はBuild and Testステージで実施
- Unit 2（CLI Tool）は未着手。`cli/build.gradle.kts`はプレースホルダーのみ
