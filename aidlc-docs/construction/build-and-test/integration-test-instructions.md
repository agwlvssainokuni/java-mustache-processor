# Integration Test Instructions

## Purpose
`cli`が`core`（テンプレートエンジン本体）を正しく利用できることを、実際にビルドしたfat jarをプロセスとして起動して確認する。

**本プロジェクトの構成に関する補足**: 本プロジェクトはUnit間連携が「`cli`→`core`」の1方向のみのシンプルな構成（マイクロサービス間のネットワーク越し連携ではなく、単一プロセス内でのライブラリ呼び出し）である。そのため、本ファイルの統合テストは実質的にエンドユーザーの利用シナリオ（E2E）と一致する。この構成上、独立した`e2e-test-instructions.md`は別途作成せず、本ファイルに統合する。

## Test Scenarios

### Scenario 1: 変数展開・エスケープ（core → cli）
- **説明**: `cli`経由でデータファイルを読み込み、`core`の`Mustache.compile`/`Template.render`によるHTMLエスケープ（BR-1）が正しく適用されることを確認する
- **セットアップ**: テンプレート`Hi, {{name}} & {{{raw}}}`、データ`{"name":"<World>","raw":"<b>ok</b>"}`
- **テスト手順**: `java -jar cherry-mustache-cli-*-all.jar --data d1.json t1.mustache`
- **期待結果**: `Hi, &lt;World&gt; & <b>ok</b>`、終了コード`0`
- **実施結果**: ✅ 期待通り（本ステージで実行・確認済み）

### Scenario 2: パーシャル解決（core `FilePartialResolver` → cli）
- **説明**: `cli`が構築した`FilePartialResolver`（テンプレートと同一ディレクトリを基準、BR-5）を通じて、`core`が正しくパーシャルを解決できることを確認する
- **セットアップ**: `partial.mustache`（`{{name}}`）と同一ディレクトリの`t2.mustache`（`Hi, {{>partial}}!`）
- **テスト手順**: `java -jar cherry-mustache-cli-*-all.jar --data d1.json t2.mustache`
- **期待結果**: `Hi, <World>\n!`相当のエスケープ済み出力、終了コード`0`
- **実施結果**: ✅ 期待通り（本ステージで実行・確認済み）

### Scenario 3: テンプレート構文エラーの伝播（core `MustacheParseException` → cli `PARSE_ERROR`）
- **説明**: `core`が送出する`MustacheParseException`が`cli`のcatchチェーンで正しく`PARSE_ERROR`にマッピングされることを確認する
- **セットアップ**: 不正なテンプレート`{{#unclosed`
- **テスト手順**: `java -jar cherry-mustache-cli-*-all.jar --data d1.json bad.mustache`
- **期待結果**: `Error: Unclosed tag`、終了コード`2`
- **実施結果**: ✅ 期待通り（本ステージで実行・確認済み）

### Scenario 4: 循環パーシャル参照の伝播（core `MustacheRenderException` → cli `RENDER_ERROR`）
- **説明**: `core`のネスト深さ上限検出（BR-9）による`MustacheRenderException`が`cli`で正しく`RENDER_ERROR`にマッピングされることを確認する
- **セットアップ**: 相互に参照し合う`circA.mustache`（`{{>circB}}`）と`circB.mustache`（`{{>circA}}`）
- **テスト手順**: `java -jar cherry-mustache-cli-*-all.jar --data d1.json circB.mustache`
- **期待結果**: SLF4Jの`WARN`ログ出力後、`Error: Partial nesting too deep, possible circular reference: circA`、終了コード`3`
- **実施結果**: ✅ 期待通り（本ステージで実行・確認済み）

### Scenario 5: セクション・リスト展開
- **説明**: `core`のセクション処理（BR-3）が`cli`経由のJSON配列データに対して正しく機能することを確認する
- **セットアップ**: テンプレート`{{#items}}[{{.}}]{{/items}}`、データ`{"items":["a","b","c"]}`
- **テスト手順**: `java -jar cherry-mustache-cli-*-all.jar --data d5.json t5.mustache`
- **期待結果**: `[a][b][c]`、終了コード`0`
- **実施結果**: ✅ 期待通り（本ステージで実行・確認済み）

## Setup Integration Test Environment

### 1. fat jarのビルド
```bash
./gradlew :cherry-mustache-cli:shadowJar
```

### 2. テストフィクスチャの配置
上記シナリオのテンプレート・データファイルを任意の作業ディレクトリに配置する（外部サービスの起動は不要。本プロジェクトはネットワークサービスを持たないため）。

## Run Integration Tests

### 1. シナリオの実行
上記各シナリオの「テスト手順」を順に実行する。

### 2. 結果の確認
- **確認ポイント**: 標準出力の内容、終了コード（`echo $?`で確認）
- **ログの確認**: SLF4Jログはデフォルトで標準エラー出力（`slf4j-simple`のデフォルト設定）に出る。詳細な診断ログが必要な場合は`-Dorg.slf4j.simpleLogger.defaultLogLevel=debug`をJVM引数に追加する

### 3. Cleanup
作業ディレクトリの一時ファイルを削除する（`rm -rf` 対象は自分で作成した一時ディレクトリのみ）。

## 実施結果（本ステージで実行・確認済み）
上記5シナリオすべてを実際にfat jar（`cherry-mustache-cli-0.1.0-SNAPSHOT-all.jar`）で実行し、期待通りの出力・終了コードであることを確認済み。
