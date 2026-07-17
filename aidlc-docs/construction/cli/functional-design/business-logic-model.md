# Business Logic Model - cli (Unit 2: CLI Tool)

`cli-functional-design-plan.md`の回答結果に基づき、`CliRunner`のオーケストレーションフローを詳細化する。

## 1. CliRunner.run(args, in, out, err) の処理フロー

```
1. CliArguments を ArgumentParser.parse(args) で構築する
   - 失敗した場合: ArgumentException を捕捉し、err へエラーメッセージを出力して ExitCode.ARGUMENT_ERROR を返す（4.1節）

2. CliArguments.helpRequested が true の場合:
   - 使用方法（Usage）を out へ出力し、即座に ExitCode.SUCCESS を返す（以降の処理は一切行わない）

3. データを読み込む: DataLoader.load(cliArguments, in) で Map<String, Object> を得る
   - データソース: --data 指定時はそのパス（"-" の場合は in から読む）、未指定時は in から読む（BR-3）
   - データ形式: --format 指定時はそれを優先。未指定時はファイル拡張子（.json / .yaml / .yml）から判定。
     いずれも決定できない場合（stdinかつ--format未指定、または未対応拡張子）は ArgumentException を送出する
   - 失敗した場合:
     - データファイルが見つからない・読み取れない: IOException を捕捉し ExitCode.IO_ERROR
     - JSON/YAMLとして構文的に不正: パース例外を捕捉し ExitCode.PARSE_ERROR

4. パーシャル解決ディレクトリを決定する（テンプレートごと。5節参照）

5. 各テンプレート（CliArguments.templates の順序通り）について:
   a. テンプレートソースを解決する（ファイルパス、または "-" の場合は in から読む。ただし in は
      データ読込（Step3）と合わせて1回しか消費できない。BR-2の競合検出により、この状態には到達しない）
   b. そのテンプレートに対応するパーシャル解決ディレクトリで FilePartialResolver を構築する
      （ディレクトリが決定できない場合はパーシャル未解決＝常にnullを返すPartialResolverを使う）
   c. Mustache.compile(templateSource, partialResolver) でコンパイルする
      - 失敗した場合: MustacheParseException を捕捉し ExitCode.PARSE_ERROR
   d. Template.render(data) でレンダリングする
      - 失敗した場合: MustacheRenderException を捕捉し ExitCode.RENDER_ERROR
   e. レンダリング結果を出力バッファに追記する（テンプレート間の区切りなし、BR-4）

6. 出力バッファの内容を OutputWriter.write(...) で書き込む
   - CliArguments.outputPath が指定されていればそのファイルへ（常に上書き、BR-6）
   - 未指定なら out（標準出力）へ
   - ファイル書き込みに失敗した場合: IOException を捕捉し ExitCode.IO_ERROR

7. すべて成功した場合、ExitCode.SUCCESS を返す

8. 上記のいずれにも該当しない予期しない例外（RuntimeException）が送出された場合:
   - SLF4Jで詳細（スタックトレース含む）をERRORレベルでログ出力する
   - err へは内部詳細を含まない汎用エラーメッセージのみを出力する（SECURITY-09）
   - ExitCode.RENDER_ERROR を返す（他のカテゴリに明確に属さない処理失敗の既定カテゴリとする）
```

## 2. Main.main(args) の役割
- `CliRunner.run(args, System.in, System.out, System.err)` を呼び出し、戻り値の`ExitCode.code()`を`System.exit()`に渡す
- 業務ロジックは一切持たない、純粋な配線（wiring）のみの責務

## 3. 標準入力の競合検出（BR-2関連）
標準入力（`in`）は1回の`run()`呼び出しにつき最大1箇所からしか消費できない。以下のいずれかに該当する場合、Step1の引数解析段階で`ArgumentException`を送出する:
- テンプレート位置引数に`-`が2つ以上指定されている
- テンプレート位置引数に`-`が指定され、かつ`--data`が省略されている（データも標準入力を要求するため）
- テンプレート位置引数に`-`が指定され、かつ`--data`に明示的に`-`が指定されている

## 4. エラーハンドリングの全体方針

### 4.1 例外種別とExitCodeの対応
| 例外・状況 | ExitCode |
|---|---|
| `ArgumentException`（引数解析・検証エラー、stdin競合含む） | `ARGUMENT_ERROR` |
| データのJSON/YAML構文エラー | `PARSE_ERROR` |
| `MustacheParseException`（テンプレート構文エラー） | `PARSE_ERROR` |
| `MustacheRenderException`（レンダリングエラー） | `RENDER_ERROR` |
| ファイルI/Oエラー（テンプレート/データ/出力ファイルの読み書き失敗） | `IO_ERROR` |
| 上記に該当しない予期しない例外 | `RENDER_ERROR`（既定カテゴリ） |

### 4.2 エラーメッセージの出力先と内容
- すべてのエラーメッセージは`err`（標準エラー出力）へ、簡潔な1行程度のメッセージとして出力する
- 内部実装詳細（スタックトレース、クラス名等）は`err`には含めない（SECURITY-09）。詳細はSLF4Jのログ（`debug`/`error`）としてのみ出力する
- `err`/`out`への実際のCLI出力（レンダリング結果・エラーメッセージ・Usage）は、`CliRunner.run`の引数として注入された`PrintStream`を通じて行う。これはテスト容易性のための設計であり、内部診断ログ（SLF4J）とは明確に区別する

## 5. パーシャル解決ディレクトリの決定ロジック（BR-5関連）
```
if CliArguments.partialDir が指定されている:
    全テンプレート共通で、そのディレクトリを基準に FilePartialResolver を構築する
else:
    テンプレートごとに:
        テンプレートがファイル由来の場合: そのファイルの親ディレクトリを基準に FilePartialResolver を構築する
        テンプレートが標準入力(-)由来の場合: 基準ディレクトリが存在しないため、
            常にnullを返す（＝パーシャルが常に未解決として扱われる）PartialResolverを使う
```
