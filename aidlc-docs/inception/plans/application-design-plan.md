# Application Design Plan

## 対象範囲
`requirements.md`（FR-1: Mustacheテンプレートエンジン ライブラリ、FR-2: CLIツール）に基づき、コンポーネント構成・メソッドシグネチャ（概要）・サービス層・依存関係を設計する。詳細な業務ロジック（パース規則の詳細、空白処理等）はConstruction PhaseのFunctional Designで扱う。

## 実行ステップ

- [ ] Step 1: コンポーネント識別（下記質問への回答を踏まえて確定）
- [ ] Step 2: コンポーネントメソッド（概要シグネチャ）の識別
- [ ] Step 3: サービス層設計（オーケストレーション方針の確定）
- [ ] Step 4: コンポーネント依存関係・通信パターンの整理
- [ ] Step 5: 成果物生成
  - [ ] `aidlc-docs/inception/application-design/components.md`
  - [ ] `aidlc-docs/inception/application-design/component-methods.md`
  - [ ] `aidlc-docs/inception/application-design/services.md`
  - [ ] `aidlc-docs/inception/application-design/component-dependency.md`
  - [ ] `aidlc-docs/inception/application-design/application-design.md`（上記4文書の統合版）
- [ ] Step 6: 設計の一貫性・完全性の検証

## 質問

各質問について、選択肢の記号を `[Answer]:` の後に記入してください。該当する選択肢がない場合は最後の「Other」を選び、内容を記述してください。

### Question 1: プロジェクト構成（コンポーネント境界の物理的な単位）
ライブラリ本体（Core Template Engine）とCLIツールは、Gradleのモジュール構成としてどう分割しますか？

A) マルチプロジェクト構成（例: `core/`, `cli/` の2つのGradleサブプロジェクト。`cli`が`core`に依存）

B) 単一プロジェクト内でパッケージのみ分離（例: `com.example.mustache.core`, `com.example.mustache.cli`）

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 2: パーサーとAST（内部表現）の境界
テンプレート文字列を解析してASTを構築する処理と、AST自体のデータモデルは、コンポーネントとしてどう分けますか？

A) 明確に分離する（`Lexer`/`Parser`コンポーネントと、独立した`ast`パッケージのノード群）

B) 1つの`Parser`コンポーネントにASTノード定義も内包する（密結合だが単純）

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 3: レンダリングとコンテキスト解決の境界
ASTを走査して出力文字列を生成する「レンダリング」処理と、Map/POJOに対してドット表記でプロパティ解決を行う「コンテキスト解決」処理は、別コンポーネントに分けますか？

A) 別コンポーネントに分ける（`Renderer`が`Context`（コンテキストスタック＋値解決）を利用する構成）

B) 1つの`Renderer`コンポーネントに両方の責務を持たせる

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 4: 公開API（ライブラリファサード）の形
ライブラリ利用者（Java開発者）向けの公開APIはどのような形にしますか？

A) `Mustache`のようなファクトリクラスの静的メソッドで`Template`（コンパイル済みテンプレート）を生成し、`Template.render(Object data)`のようなインスタンスメソッドで描画する（多くのMustache実装で採用されている形）

B) 1つの`MustacheEngine`インスタンスを生成し、そのインスタンスの`compile()`/`render()`メソッドを都度呼び出す（インスタンスに設定（パーシャルリゾルバー等）を保持させる）

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 5: 例外階層とCLI終了コードの対応
FR-2で「エラー種別ごとに異なる終了コード（引数エラー・パースエラー・レンダリングエラー・I/Oエラー等）」が要件化されています。ライブラリ側の例外設計はどうしますか？

A) 共通の基底例外（例: `MustacheException`）の下に、`MustacheParseException`, `MustacheRenderException`等の種別ごとのサブクラスを用意する（CLIはこの型でcatchして終了コードにマッピング）

B) 種別ごとに独立した例外クラス（共通基底なし）を用意する

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 6: CLIの内部構成（サービス層の要否）
CLIツール内部は、引数解析・データ読込（JSON/YAML）・パーシャル解決・ライブラリ呼び出し・出力書き込みをどう構成しますか？

A) `Main`から各コンポーネント（`ArgumentParser`, `DataLoader`, `FilePartialResolver`, `OutputWriter`等）を直接呼び出すシンプルな手続き型構成（サービス層なし）

B) `CliRunner`のようなサービス層コンポーネントを設け、`Main`はエントリポイントとしてこれに処理を委譲し、`CliRunner`が各コンポーネントをオーケストレーションする

X) Other (please describe after [Answer]: tag below)

[Answer]: B

### Question 7: ASTノードのレンダリング方式（設計パターン）
AST各ノード（Text, Variable, Section, InvertedSection, Partial, Comment等）をどう描画しますか？

A) 各ノードクラスが`render(Context, Writer)`のようなメソッドを持つ（ポリモーフィズムでノード種別ごとの処理を実現）

B) `Renderer`が全ノード種別を`switch`/`instanceof`で判別して処理を分岐する（Visitorパターンは使わない単純な集中処理）

C) Visitorパターン（`NodeVisitor`インターフェースを定義し、`Renderer`がVisitorとして各ノードを訪問する）

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## 追加決定事項

- **パッケージ名（ベース）**: `cherry.mustache`
  - `core`サブプロジェクト: `cherry.mustache`（ルート）配下に `cherry.mustache.ast`, `cherry.mustache.parser`, `cherry.mustache.render` 等
  - `cli`サブプロジェクト: `cherry.mustache.cli`
