# Unit of Work

決定事項（unit-of-work-plan.md 回答結果）:
- Unit構成は2Unit（Application Designの`core`/`cli`構成をそのまま採用）
- バージョニングは単一バージョン一括管理
- Gradleディレクトリ構成はルート直下に`core/`, `cli/`
- テストコード（単体テスト・公式specテストスイート）はすべて`core`の`src/test`配下に配置
- チーム・所有権境界は技術的境界（`core`/`cli`）のみで十分（単一開発者前提）

## Unit 1: Core Template Engine（`core`）

- **種別**: Module（ライブラリ、独立デプロイ対象ではないがビルド単位として独立）
- **責務**: 公式Mustache仕様にフル準拠したテンプレートエンジンのライブラリ本体
- **含まれるコンポーネント**（`components.md`参照）: `Mustache`, `Template`, `Parser`, `ast.*`（各Node）, `Renderer`, `Context`, `PartialResolver`, `MapPartialResolver`, `FilePartialResolver`, `Lambda`, `MustacheException`系
- **ベースパッケージ**: `cherry.mustache`
- **対応する要件**: FR-1（ライブラリ機能全般）, NFR-1（性能・コンパイル済み表現の再利用）, NFR-3（JUnit5単体テスト・公式specテストスイート）, NFR-4（Security Baseline：ライブラリ内の入力検証・デシリアライズ安全性・フェイルセーフ等）, NFR-6（Property-Based Testing）
- **依存**: なし（外部ライブラリは標準ライブラリ中心に留める）

## Unit 2: CLI Tool（`cli`）

- **種別**: Module（実行可能fat jarとして配布）
- **責務**: コマンドラインからMustacheエンジンを利用するためのラッパー
- **含まれるコンポーネント**（`components.md`参照）: `Main`, `CliRunner`, `ArgumentParser`, `CliArguments`, `DataLoader`, `OutputWriter`, `ExitCode`, `ArgumentException`
- **ベースパッケージ**: `cherry.mustache.cli`
- **対応する要件**: FR-2（CLI機能全般）, NFR-2（fat jar配布・エラーメッセージ言語）, NFR-3（CLI固有ロジックの単体テスト）, NFR-4（Security Baseline：CLI固有の入力検証・堅牢化）
- **依存**: `Unit 1: Core Template Engine`（`core`）

## Unit外（対象外）

- Resiliency Baseline（NFR-5）は無効のため、対応するUnitや作業は発生しない
- 公式specテストスイートは独立Unitとせず、`core`の一機能（テスト）として扱う（Question 1参照）

## コード構成戦略（Greenfield / Gradleマルチプロジェクト）

```
java-mustache-processor/            (リポジトリルート)
├── settings.gradle.kts             # include("core", "cli")
├── build.gradle.kts                # ルートプロジェクト共通設定（Java 25、UTF-8等）
├── gradle/wrapper/                 # Gradle Wrapper
├── gradlew, gradlew.bat
├── core/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/cherry/mustache/...
│       └── test/
│           ├── java/cherry/mustache/...
│           └── resources/          # 公式spec YAMLフィクスチャ等
├── cli/
│   ├── build.gradle.kts            # coreへの依存、fat jar化プラグイン設定
│   └── src/
│       ├── main/java/cherry/mustache/cli/...
│       └── test/java/cherry/mustache/cli/...
└── aidlc-docs/                     # ドキュメント専用（アプリケーションコードは含まない）
```

- ビルド順序: `core`が先にビルドされ、`cli`がそれに依存する（Gradleが自動的に解決）
- バージョン管理: ルートの`build.gradle.kts`（またはGradleプロパティ）で単一バージョンを定義し、両モジュールで共有する
