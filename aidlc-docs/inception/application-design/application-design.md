# Application Design（統合版）

本ドキュメントは `components.md` / `component-methods.md` / `services.md` / `component-dependency.md` の統合版です。各詳細は個別ファイルを参照してください。

## 設計方針（application-design-plan.md 決定事項）
- Gradleマルチプロジェクト構成: `core`（ライブラリ本体）/ `cli`（CLIツール、`core`に依存）
- ベースパッケージ名: `cherry.mustache`
- Parser（構文解析）とAST（データモデル）は明確に分離
- Renderer（走査制御）とContext（データ解決）は明確に分離
- 公開API: `Mustache`ファクトリで`Template`（コンパイル済みテンプレート）を生成する形
- 例外階層: 共通基底`MustacheException` + 種別ごとのサブクラス（`MustacheParseException`, `MustacheRenderException`）
- CLI内部: `CliRunner`によるサービス層を設け、`Main`は薄いエントリポイントとする
- ASTノードのレンダリング: 各ノードクラスがポリモーフィズムで自身をレンダリングする

## コンポーネント一覧

### core サブプロジェクト
| コンポーネント | 種別 | 責務概要 |
|---|---|---|
| `Mustache` | ファサード/ファクトリ | テンプレート文字列を`Parser`に渡し`Template`を生成 |
| `Template` | コンパイル済みテンプレート | AST保持、`render()`でRenderer呼び出し |
| `Parser` | 処理コンポーネント | テンプレート文字列→ASTへの構文解析 |
| `ast.*`（Node群） | データモデル | Text/Variable/Section/InvertedSection/Partial/Comment等、自己レンダリング責務を持つ |
| `Renderer` | 処理コンポーネント | ASTの走査駆動、パーシャル解決の起点 |
| `Context` | 処理コンポーネント | Map/POJO解決、ドット表記、コンテキストスタック |
| `PartialResolver` | インターフェース | パーシャル名→テンプレート文字列の解決契約 |
| `MapPartialResolver` | デフォルト実装 | Mapベースのパーシャル解決 |
| `FilePartialResolver` | 標準実装 | ファイルシステムベースのパーシャル解決（CLI固有の概念を持たない汎用実装） |
| `Lambda` | 関数型インターフェース | セクション値としてのテキスト処理関数契約 |
| `MustacheException`系 | 例外 | 共通基底＋Parse/Render種別のサブクラス |

### cli サブプロジェクト
| コンポーネント | 種別 | 責務概要 |
|---|---|---|
| `Main` | エントリポイント | `CliRunner`呼び出し、終了コードでの`System.exit` |
| `CliRunner` | サービス層 | 引数解析〜出力までの一連のオーケストレーション |
| `ArgumentParser` | 処理コンポーネント | コマンドライン引数の解析 |
| `CliArguments` | 値オブジェクト | 解析済み引数の保持 |
| `DataLoader` | 処理コンポーネント | JSON/YAMLデータの読込・変換 |
| `OutputWriter` | 処理コンポーネント | 標準出力/ファイルへの結果書き込み |
| `ExitCode` | enum | エラー種別と終了コードの対応 |
| `ArgumentException` | 例外（CLI固有） | 引数不正の表現 |

（メソッドシグネチャの詳細は `component-methods.md` を参照）

## サービス層（オーケストレーション）
1. **コンパイルオーケストレーション**（`Mustache`）— `Parser`への委譲
2. **レンダリングオーケストレーション**（`Template.render` → `Renderer`）— `Renderer`/`Context`/`PartialResolver`の連携
3. **CLIオーケストレーション**（`CliRunner`）— 引数解析〜出力までの全体制御、エラー種別→`ExitCode`のマッピング

（フロー詳細は `services.md` を参照）

## コンポーネント依存関係（概要）
- モジュール依存: `cli` → `core`（一方向。`core`は`cli`を一切知らない）
- `core`内部: `Mustache`→`Parser`/`Template`、`Template`→`Renderer`/`Context`/`PartialResolver`、`Renderer`→`ast.*`/`Context`、`MapPartialResolver`/`FilePartialResolver`→`PartialResolver`（実装）
- `cli`内部: `Main`→`CliRunner`→（`ArgumentParser`, `DataLoader`, `Mustache`/`Template`/`FilePartialResolver`（いずれもcore）, `OutputWriter`, `ExitCode`）
- 通信パターン: すべて同期的なインプロセスのメソッド呼び出し（非同期・ネットワーク通信なし）
- `PartialResolver`はStrategyパターンであり、標準実装（`MapPartialResolver`, `FilePartialResolver`）は両方とも`core`が提供する。`cli`はどのディレクトリを使うかのポリシー決定のみを担う

（依存関係図の詳細は `component-dependency.md` を参照）

## 未確定事項（Functional Design以降で確定）
- `Context`のスタック実装（イミュータブル/ミュータブル）
- `PartialResolver.resolve()`が未解決時にnullを返すか例外を送出するか
- ASTノードの具体的なフィールド構成、スタンドアロン行判定ロジック
- JSON/YAMLパーサーライブラリの具体的な選定（NFR Requirementsステージ）
- 依存脆弱性スキャンツールの選定（NFR Requirementsステージ）
