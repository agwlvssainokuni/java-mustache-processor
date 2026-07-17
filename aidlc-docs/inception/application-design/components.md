# Components

決定事項（application-design-plan.md 回答結果）:
- Gradleマルチプロジェクト構成（`core` / `cli`、`cli`が`core`に依存）
- ベースパッケージ名: `cherry.mustache`
- Parser/ASTは明確に分離、Renderer/Contextも明確に分離
- ASTノードはポリモーフィズムで自身をレンダリングする

## core サブプロジェクト（ライブラリ本体）

### Mustache（`cherry.mustache.Mustache`）
- **種別**: ファサード / ファクトリ
- **責務**: ライブラリの公開エントリポイント。テンプレート文字列（または`Reader`）を受け取り`Parser`に処理を委譲し、コンパイル済みの`Template`を生成する。パーシャルリゾルバーの設定（デフォルト or カスタム）もここで受け付ける
- **インターフェース**: 静的メソッド中心のファクトリ

### Template（`cherry.mustache.Template`）
- **種別**: コンパイル済みテンプレート（値オブジェクト＋操作）
- **責務**: パース済みAST（ルートノード）を保持し、任意のデータ（Map/POJO）に対して繰り返しレンダリングできるようにする。レンダリング時に`Renderer`へ処理を委譲する
- **インターフェース**: インスタンスメソッド（render系）

### Parser（`cherry.mustache.parser.Parser`）
- **種別**: 処理コンポーネント
- **責務**: テンプレート文字列を解析し、AST（`Node`ツリー）を構築する。デリミタ変更（Set Delimiter）の追跡、スタンドアロン行の空白処理など、構文解析に関する全責務を持つ
- **インターフェース**: テンプレート文字列 → ASTルートノードへの変換メソッド

### AST Nodes（`cherry.mustache.ast.*`）
- **種別**: データモデル（+ 自己レンダリング責務）
- **責務**: テンプレートの構文要素をノードとして表現する。各ノードは自身を`Context`と出力先に対してレンダリングする方法を知っている（ポリモーフィズム）
- **想定ノード種別**: `Node`（基底/抽象）, `TextNode`, `VariableNode`（エスケープ有）, `UnescapedVariableNode`, `SectionNode`, `InvertedSectionNode`, `PartialNode`, `CommentNode`
- ルートは`Node`のリストを保持する`TemplateNode`（または`Template`が直接ルートノードリストを保持）

### Renderer（`cherry.mustache.render.Renderer`）
- **種別**: 処理コンポーネント
- **責務**: ASTルートノードの走査を開始し、各ノードのレンダリングを駆動する。パーシャル解決の起点（`PartialResolver`の呼び出し）にもなる
- **インターフェース**: ASTルート + `Context` + `PartialResolver` → 出力文字列/Writer書き込み

### Context（`cherry.mustache.render.Context`）
- **種別**: 処理コンポーネント（+ 状態保持）
- **責務**: レンダリング中のデータ解決を担う。Map/POJOからのプロパティ取得（リフレクション）、ドット表記のネストしたプロパティ参照、セクションによるコンテキストスタック（親子関係）の管理を行う
- **インターフェース**: キー名 → 値の解決、スタックへのpush/pop

### PartialResolver（`cherry.mustache.PartialResolver`）
- **種別**: インターフェース
- **責務**: パーシャル名からテンプレート文字列を解決する契約を定義する。呼び出し側（ライブラリ利用者、またはCLI）が実装を差し替え可能

### MapPartialResolver（`cherry.mustache.MapPartialResolver`）
- **種別**: デフォルト実装
- **責務**: `Map<String, String>`（パーシャル名→テンプレート文字列）を保持し、`PartialResolver`として振る舞う標準実装

### FilePartialResolver（`cherry.mustache.FilePartialResolver`）
- **種別**: 標準実装
- **責務**: 指定されたベースディレクトリを起点に、パーシャル名＋拡張子（`.mustache`）でファイルシステムからテンプレート文字列を解決する汎用実装。ファイルベースのパーシャルを扱いたいライブラリ利用者向けの既製クラスとして`core`が提供する（CLI固有の概念は持たない）
- **注**: 「テンプレートと同一ディレクトリを既定、オプションで上書き」というディレクトリ決定ポリシーはCLI固有の関心事のため、`cli`の`CliRunner`が担い、決定したディレクトリを渡して本クラスをインスタンス化する

### Lambda（`cherry.mustache.Lambda`）
- **種別**: 関数型インターフェース
- **責務**: セクション値としてコンテキストに渡せる「セクション内テキストを処理する関数」の契約を定義する

### 例外階層（`cherry.mustache.MustacheException` 他）
- **種別**: データモデル（例外）
- **責務**: ライブラリ内で発生するエラーを型で表現し、呼び出し側（特にCLI）がエラー種別ごとに異なる処理・終了コードへマッピングできるようにする
- **構成**: `MustacheException`（共通基底、非検査例外） / `MustacheParseException`（Parserが送出） / `MustacheRenderException`（Rendererが送出）

## cli サブプロジェクト（CLIツール）

### Main（`cherry.mustache.cli.Main`）
- **種別**: エントリポイント
- **責務**: `public static void main`。`CliRunner`に処理を委譲し、戻り値（`ExitCode`）または送出された例外をもとに`System.exit()`を呼び出す。実際の業務ロジックは持たない

### CliRunner（`cherry.mustache.cli.CliRunner`）
- **種別**: サービス層（オーケストレーション）
- **責務**: 引数解析結果を受け取り、データ読込・パーシャルリゾルバー構築・テンプレートのコンパイル/連結レンダリング・出力書き込みまでの一連の流れを制御し、`ExitCode`を決定する

### ArgumentParser（`cherry.mustache.cli.ArgumentParser`）
- **種別**: 処理コンポーネント
- **責務**: コマンドライン引数（`String[] args`）を解析し、`CliArguments`（テンプレートパス（複数可）、`--data`、`--format`、`--output`、パーシャルディレクトリ指定等）を構築する。引数不正時は`ArgumentException`を送出する

### DataLoader（`cherry.mustache.cli.DataLoader`）
- **種別**: 処理コンポーネント
- **責務**: JSON/YAML形式のデータ（ファイル or 標準入力）を読み込み、`Map<String, Object>`に変換する。ファイル拡張子による自動判定、`--format`指定時はそれを優先する

### OutputWriter（`cherry.mustache.cli.OutputWriter`）
- **種別**: 処理コンポーネント
- **責務**: レンダリング結果を標準出力またはファイルへ書き込む

### ExitCode（`cherry.mustache.cli.ExitCode`）
- **種別**: enum（データモデル）
- **責務**: 終了コードとエラー種別（成功、引数エラー、パースエラー、レンダリングエラー、I/Oエラー）の対応を定義する

### ArgumentException（`cherry.mustache.cli.ArgumentException`）
- **種別**: 例外（CLI固有）
- **責務**: コマンドライン引数の不正を表す。ライブラリ側の例外階層とは独立（CLI固有の関心事のため）
