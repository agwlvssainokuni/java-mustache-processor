# Domain Entities - cli (Unit 2: CLI Tool)

Application Design（`components.md`/`component-methods.md`）で識別したコンポーネントのうち、データモデルとしての側面を詳細化する。

## CliArguments（`cherry.mustache.cli.CliArguments`、値オブジェクト）
- **フィールド**:
  - `templateArgs: List<String>`（1件以上。各要素はファイルパス、または標準入力を表す特殊値`"-"`）
  - `dataArg: String`（nullable。`null`は標準入力からの読込を意味する。値が`"-"`の場合も同様に標準入力を意味する）
  - `format: String`（nullable。`"json"` または `"yaml"`。BR-3参照）
  - `outputPath: Path`（nullable。`null`は標準出力への出力を意味する）
  - `partialDir: Path`（nullable。`null`はテンプレートごとの既定ディレクトリ決定（BR-5）を意味する）
  - `helpRequested: boolean`（`--help`/`-h`が指定されたか）
- **不変性**: `Context`と同様、生成後は変更されないイミュータブルな値オブジェクトとする

## ExitCode（`cherry.mustache.cli.ExitCode`、enum）
- **定数と値**（BR-8）: `SUCCESS(0)`, `ARGUMENT_ERROR(1)`, `PARSE_ERROR(2)`, `RENDER_ERROR(3)`, `IO_ERROR(4)`
- **フィールド**: `codeValue: int`（各定数に紐づく終了コード数値）
- **操作**: `int code()` — `codeValue`を返す

## ArgumentException（`cherry.mustache.cli.ArgumentException`）
- **継承**: `RuntimeException`（`cherry.mustache.MustacheException`階層とは独立。CLI固有の関心事のため）
- **フィールド**: `message: String`（標準の例外コンストラクタのみ。位置情報等の付加フィールドは持たない）
- **送出箇所**: `ArgumentParser`（構文・値の妥当性検証）、および`CliRunner`のstdin競合検出（`business-logic-model.md` 3節）

## CliRunnerの内部状態（公開APIではない）
- テンプレート解決・パーシャルディレクトリ決定・出力バッファ蓄積は`CliRunner.run()`呼び出し内のローカル変数として管理し、インスタンスフィールドとしては保持しない（`ConcurrentRenderTest`と同様の考え方で、`CliRunner`自体は状態を持たない無状態コンポーネントとする）

## DataLoader / OutputWriterの入出力型
- `DataLoader.load(CliArguments, InputStream): Map<String, Object>` — Jackson（BR-1）でパースした結果を`Map<String, Object>`として返す。ネストしたオブジェクト・配列もJackson標準のマッピング規則（`Map`/`List`/`String`/`Number`/`Boolean`/`null`）に従う。coreの`Context`はMap/POJOいずれも受け付けるため、この`Map`表現がそのまま`Template.render(Object data)`に渡せる
- `OutputWriter.write(String content, CliArguments, PrintStream stdout): void` — `CliArguments.outputPath`の有無で書き込み先を分岐する（BR-6）。ファイル書き込みはUTF-8固定（NFR-2）
