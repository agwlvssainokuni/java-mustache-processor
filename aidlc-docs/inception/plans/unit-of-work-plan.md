# Unit of Work Plan

## 前提
- User Storiesはスキップ済みのため、本ステージでは「ストーリーとUnitの対応」の代わりに「要件（FR/NFR）とUnitの対応」をマッピングする
- Application Designで既にGradleマルチプロジェクト構成（`core`/`cli`、`cli`が`core`に依存）が決定しており、Unit分割の土台は概ね固まっている。本ステージではこの構成をUnit of Workとして正式に確定し、境界の妥当性を再検証する

## 実行ステップ

- [x] Step 1: Unit構成の確定（下記質問への回答を踏まえて確定）
- [x] Step 2: Unit間依存関係の整理
- [x] Step 3: 要件（FR/NFR）とUnitのマッピング
- [x] Step 4: コード構成（ディレクトリ構造）の確定（Greenfield）
- [x] Step 5: 成果物生成
  - [x] `aidlc-docs/inception/application-design/unit-of-work.md`
  - [x] `aidlc-docs/inception/application-design/unit-of-work-dependency.md`
  - [x] `aidlc-docs/inception/application-design/unit-of-work-story-map.md`（要件マッピング版として作成）
- [x] Step 6: Unit境界・依存関係の妥当性検証

## 質問

各質問について、選択肢の記号を `[Answer]:` の後に記入してください。該当する選択肢がない場合は最後の「Other」を選び、内容を記述してください。

### Question 1: Unit構成の確定
Application Designでの構成（`core`=Core Template Engine, `cli`=CLI Tool）をそのままUnit of Workとしますか？

A) 提案通り2 Unit（Core Template Engine, CLI Tool）とする

B) 追加で「公式specテストスイート対応」を独立した3つ目のUnitとして切り出す（例: `spec-test`）

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 2: リリース/バージョニング単位
`core`と`cli`のバージョン管理はどうしますか？（当面は外部公開しないため実務上の影響は小さいが、Gradleのバージョン設定方針として確認）

A) プロジェクト全体で単一バージョンを採用する（`core`と`cli`は常に同じバージョン番号でビルド）

B) `core`と`cli`を別々にバージョン管理する

C) 未定・後続フェーズ（NFR Requirements等）で決定する

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 3: Gradleディレクトリ構成
マルチプロジェクトのディレクトリ構造はどうしますか？

A) ルート直下に `core/`, `cli/` を配置し、`settings.gradle(.kts)` で `include("core", "cli")` する

B) `modules/core/`, `modules/cli/` のようにサブディレクトリの下に各モジュールをまとめる

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 4: テストコードの配置
JUnit 5単体テストと、公式Mustache specテストスイート（YAML fixtureを読み込んで実行する準拠テスト）は、どのUnitのどこに配置しますか？

A) すべて`core`モジュールの`src/test`配下に配置する（specテストはfixtureデータを`core`のテストリソースとして取り込む）

B) 単体テストは各モジュールの`src/test`に配置しつつ、specテストスイートは独立したテストモジュール/ソースセットに分離する

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 5: チーム・所有権境界
現状は単一の開発者による開発と理解していますが、将来的に複数人・複数チームでの分担を想定してUnit境界を設計する必要はありますか？

A) 不要（単一開発者/チーム前提でよい。現在の`core`/`cli`の技術的な境界のみで十分）

B) 必要（想定する分担方法を具体的に記述してください）

X) Other (please describe after [Answer]: tag below)

[Answer]: A
