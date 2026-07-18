# Build Instructions

## Prerequisites
- **Build Tool**: Gradle（Gradle Wrapper同梱、`./gradlew`を使用。開発者のGradleインストール状況に依存しない）
- **JDK**: Java 25（`build.gradle.kts`のtoolchain設定により、Gradle実行環境が別バージョンでも自動的にJava 25 toolchainを取得・使用する）
- **依存関係**: すべてMaven Centralから解決（`build.gradle.kts`/`core/build.gradle.kts`/`cli/build.gradle.kts`に記載。個別インストール作業は不要）
- **環境変数**: 不要（OWASP Dependency-CheckでNVD APIキーを使う場合は任意で`NVD_API_KEY`等を設定可能。詳細は`security-test-instructions.md`参照）
- **システム要件**: 特別な要件なし（一般的な開発マシンで動作確認済み）

## Build Steps

### 1. Gradle Wrapperの実行権限確認（初回のみ）
```bash
chmod +x gradlew
```

### 2. 全モジュールのビルド
```bash
./gradlew clean build
```
`core`（ライブラリ）と`cli`（CLIツール、fat jar含む）の両方をコンパイル・テスト・パッケージングする。

### 3. ビルド成功の確認
- **期待される出力**: `BUILD SUCCESSFUL`
- **生成される成果物**:
  - `core/build/libs/core-<version>.jar`（ライブラリ本体）
  - `cli/build/libs/cli-<version>.jar`（`core`への依存を含まない通常jar）
  - `cli/build/libs/cli-<version>-all.jar`（Gradle Shadow pluginによる実行可能fat jar。依存関係一式を同梱）
- **確認済みの警告**: `Consider enabling configuration cache...`というGradleの一般的な推奨メッセージのみ（ビルドの成否には影響しない）

### 4. fat jarの動作確認（任意）
```bash
java -jar cli/build/libs/cli-*-all.jar --help
```

## Troubleshooting

### ビルドが依存関係エラーで失敗する
- **原因**: Maven Centralへのネットワークアクセスがブロックされている
- **対処**: プロキシ設定を確認するか、社内ミラーリポジトリを`build.gradle.kts`の`repositories { }`に追加する

### コンパイルエラーで失敗する
- **原因**: Java 25 toolchainが自動取得できない環境（オフライン環境等）
- **対処**: `./gradlew build --info`で詳細ログを確認し、toolchainのダウンロード状況を確認する。事前にJava 25をインストール済みの環境であればtoolchain自動検出が使われる場合もある

## 実施結果（本ステージで実行・確認済み）
`./gradlew clean build` を実行し、`BUILD SUCCESSFUL`（18 actionable tasks、全て実行）を確認済み。`core`/`cli`双方のjar・fat jarが正しく生成された。
