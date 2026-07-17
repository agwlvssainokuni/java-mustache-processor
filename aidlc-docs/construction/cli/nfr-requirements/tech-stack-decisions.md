# Tech Stack Decisions - cli (Unit 2: CLI Tool)

requirements.md（NFR-2, NFR-3, NFR-4）および本ステージの回答結果に基づく技術選定を確定する。

## 言語・ビルド環境（requirements.md NFR-2で確定済み、再掲）
- **言語**: Java 25
- **ビルドツール**: Gradle（Gradle Wrapper使用、`core`と単一バージョン一括管理）
- **文字エンコーディング**: UTF-8固定

## データ形式パースライブラリ
- **JSON/YAML統一パーサー**: Jackson（`com.fasterxml.jackson.core:jackson-databind` + `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml`）を`implementation`スコープで導入する（`functional-design/business-rules.md` BR-1、Q1=A）
  - `ObjectMapper#readValue(source, Map.class)`相当の呼び出しで常に`Map<String, Object>`型として読み込み、多相型ハンドリングは有効化しない（NFR-SEC-2）

## fat jar構築
- **プラグイン**: Gradle Shadow plugin（`com.gradleup.shadow`）を`cli/build.gradle.kts`に導入し、依存関係（Jackson等）を含む単一の実行可能fat jarを構築する（Q4=A）
  - Manifestの`Main-Class`に`cherry.mustache.cli.Main`を設定する
  - 具体的なタスク名・出力ファイル名規則はCode Generationステージで確定する

## セキュリティ関連ツール
- **依存関係脆弱性スキャン**: `cli/build.gradle.kts`にも`core`と同じOWASP Dependency-Check Gradleプラグイン（`org.owasp.dependencycheck`）を導入する（Q1=A、NFR-SEC-1）
  - 実行タイミング・失敗時のビルド停止条件（しきい値）は`core`と同様、Build and Testステージで確定する

## テストフレームワーク
- **単体テスト**: JUnit 5（`org.junit.jupiter:junit-jupiter`）。`core`と同一バージョンを使用する
- Property-Based Testing（jqwik）は本Unitでは使用しない（`nfr-requirements.md` PBT Compliance参照）

## ロギング
- **ログAPI**: SLF4J（`org.slf4j:slf4j-api`）を`implementation`スコープで導入する。`System.out`/`System.err`は使用しない（coreと同じ方針、NFR-SEC節SECURITY-03参照）
- ユーザー向けCLI出力（レンダリング結果・エラーメッセージ・Usage）は`CliRunner.run`に注入された`PrintStream`経由で行い、SLF4Jログ（診断用）とは明確に区別する

## 引数解析
- 外部CLIフレームワーク（picocli等）は導入せず、手書きの`ArgumentParser`とする（`functional-design/business-rules.md` BR-2、Functional Design Q2=Aで決定済み、本ステージでの変更なし）

## 未確定事項（Code Generationステージで確定）
- Jackson・JUnit 5・OWASP Dependency-Check・Shadow pluginの具体的なバージョン番号
- OWASP Dependency-Checkのビルド組み込み方法・失敗しきい値（`core`と共通の方針をBuild and Testステージで確定）
- fat jarの出力ファイル名・タスク名の具体的な設定
