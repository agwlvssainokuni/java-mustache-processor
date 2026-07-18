# Unit Test Execution

## Run Unit Tests

### 1. 全モジュールの単体テスト実行
```bash
./gradlew test
```

### 2. モジュール別の実行（任意）
```bash
./gradlew :cherry-mustache-core:test
./gradlew :cherry-mustache-cli:test
```

### 3. テスト結果の確認
- **期待される結果**: 全231件（core 197件 + cli 34件）成功、失敗0件
- **テストレポート**:
  - `cherry-mustache-core/build/reports/tests/test/index.html`
  - `cherry-mustache-cli/build/reports/tests/test/index.html`
- **テスト結果XML**:
  - `cherry-mustache-core/build/test-results/test/*.xml`
  - `cherry-mustache-cli/build/test-results/test/*.xml`

### 4. テスト構成の内訳

#### core（197件）
| 種別 | 内容 | 件数 |
|---|---|---|
| 例示ベース | Parser構文エラー、業務ルール（BR-1〜BR-11）、Context、PartialResolver、例外階層、並行render | 44 |
| Property-Based Testing（jqwik） | HTMLエスケープ・リスト展開・ドット表記解決・デリミタ等価性・パーシャル独立性のInvariant/Oracle | 6 |
| 公式Mustache specテストスイート | `comments`/`delimiters`/`interpolation`/`inverted`/`partials`/`sections`/`~lambdas`（`mustache/spec`準拠） | 146 |
| 信頼性テスト | 同一`Template`インスタンスへの並行render（16スレッド×200回） | 1 |

#### cli（34件）
| 種別 | 内容 | 件数 |
|---|---|---|
| `ArgumentParserTest` | 引数解析の正常系・異常系、`--help`優先処理、標準入力競合検出 | 12 |
| `DataLoaderTest` | JSON/YAML判定、Jackson構文エラー | 7 |
| `OutputWriterTest` | 標準出力/ファイル出力、上書き | 3 |
| `CliRunnerTest` | 標準入出力モック化による全ExitCode経路の検証 | 12 |

### 5. テスト失敗時の対処
1. 該当モジュールのテストレポート（上記HTML）で失敗ケースの詳細（期待値・実際値・スタックトレース）を確認する
2. `core`の失敗の場合、多くは公式Mustache仕様（`cherry-mustache-core/src/test/resources/spec/*.yml`）との差異が原因である可能性が高い。仕様の記述を再確認し、実装（`business-rules.md`の該当BR）またはテスト自体の妥当性を検証する
3. `cli`の失敗の場合、`ExitCode`マッピング（`business-logic-model.md` 4.1節）やcatch順序（`nfr-design-patterns.mdパターン1・2`）を確認する
4. 修正後、該当モジュールの`./gradlew :{module}:test`を再実行する

## 実施結果（本ステージで実行・確認済み）
`./gradlew clean test`（`clean build`に含む）を実行し、core 197件・cli 34件、合計231件すべて成功（失敗0件、エラー0件）を確認済み（`test-results/test/*.xml`のtests/failures/errors属性を集計して検証）。
