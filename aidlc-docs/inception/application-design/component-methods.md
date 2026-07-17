# Component Methods

メソッドシグネチャの概要（インターフェース契約）を示す。詳細な業務ロジック（パース規則の詳細、空白・スタンドアロン行処理等）はConstruction PhaseのFunctional Designで定義する。

## core サブプロジェクト

### `Mustache`（static factory）
- `static Template compile(String template)` — テンプレート文字列をパースし`Template`を返す（パーシャルリゾルバーはデフォルトの空実装/後から指定）
- `static Template compile(String template, PartialResolver partialResolver)` — パーシャルリゾルバーを指定してコンパイル
- `static Template compile(Reader reader, PartialResolver partialResolver)` — `Reader`からの読み込みに対応

### `Template`
- `String render(Object data)` — データを渡して文字列としてレンダリング結果を得る
- `void render(Object data, Writer out)` — `Writer`へ直接書き込む（大きな出力向け）
- `String render(Object data, PartialResolver partialResolver)` — コンパイル時と異なるパーシャルリゾルバーで上書きしてレンダリング（オーバーロード）

### `Parser`
- `Node parse(String template)` — テンプレート文字列をASTルートノードに変換する。構文エラー時は`MustacheParseException`を送出

### `Node`（AST基底、抽象）
- `void render(Context context, PartialResolver partialResolver, Writer out)` — 自身をレンダリングし`out`に書き込む（各サブクラスが実装）

### `Renderer`
- `void render(Node root, Context context, PartialResolver partialResolver, Writer out)` — ルートノードからレンダリングを駆動する（内部的には`root.render(...)`を呼び出すエントリポイント）

### `Context`
- `Object resolve(String key)` — ドット表記を含むキーから値を解決する（現在のスタックを上位に向かって探索）
- `Context push(Object data)` — 新しいスコープをスタックに積んだ子`Context`を返す（セクション処理用）
- `Context pop()` — 直近のスコープを外した`Context`を返す（またはイミュータブルな設計のためpush時に親を保持する形でも可、詳細はFunctional Designで確定）

### `PartialResolver`（interface）
- `String resolve(String partialName)` — パーシャル名からテンプレート文字列を返す。解決できない場合の挙動（null/例外）はFunctional Designで確定

### `MapPartialResolver`
- `MapPartialResolver(Map<String, String> partials)` — コンストラクタ
- `String resolve(String partialName)` — `PartialResolver`実装

### `Lambda`（functional interface）
- `String execute(String text)` — セクション内の生テキスト（未レンダリング）を受け取り、置換後の文字列を返す

### 例外
- `MustacheException(String message, Throwable cause)` — 共通コンストラクタ
- `MustacheParseException` — 追加でパース位置情報（行・列 等）を保持する想定
- `MustacheRenderException` — 追加でレンダリング中に問題となったキー/ノード情報を保持する想定

## cli サブプロジェクト

### `Main`
- `static void main(String[] args)` — エントリポイント。`CliRunner`を呼び出し、結果に応じ`System.exit(exitCode)`

### `CliRunner`
- `ExitCode run(String[] args, InputStream in, PrintStream out, PrintStream err)` — CLI全体のオーケストレーション。テスト容易性のため標準入出力をパラメータとして受け取る

### `ArgumentParser`
- `CliArguments parse(String[] args)` — 引数を解析し値オブジェクトを返す。不正な引数の場合`ArgumentException`を送出

### `CliArguments`（値オブジェクト）
- テンプレートパスのリスト、`--data`の値（パス or 標準入力指定）、`--format`、`--output`、パーシャルディレクトリ指定、等を保持するプロパティ群（詳細フィールドはFunctional Designで確定）

### `DataLoader`
- `Map<String, Object> load(CliArguments args, InputStream stdin)` — `--data`やフォーマット指定に基づきJSON/YAMLを読み込みMapへ変換する

### `FilePartialResolver`
- `FilePartialResolver(Path baseDir)` — コンストラクタ（探索基準ディレクトリを指定）
- `String resolve(String partialName)` — `PartialResolver`実装（ファイル探索）

### `OutputWriter`
- `void write(String content, CliArguments args, PrintStream stdout)` — `--output`指定の有無に応じ標準出力/ファイルへ書き込む

### `ExitCode`（enum）
- `int code()` — 各定数（`SUCCESS`, `ARGUMENT_ERROR`, `PARSE_ERROR`, `RENDER_ERROR`, `IO_ERROR`）に対応する終了コード値を返す

### `ArgumentException`
- `ArgumentException(String message)` — コンストラクタ
