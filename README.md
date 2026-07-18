# java-mustache-processor

公式[Mustache仕様](https://github.com/mustache/spec)にフル準拠したJava向けテンプレートエンジン（ライブラリ）と、それを利用するCLIツールです。

- `core`: テンプレートエンジン本体（ライブラリ）。パッケージ: `cherry.mustache`
- `cli`: コマンドラインからMustacheエンジンを利用するCLIツール。パッケージ: `cherry.mustache.cli`

## ビルド

```
./gradlew build
```

## ライブラリ（core）の利用

```java
Template template = Mustache.compile("Hello, {{name}}!");
String result = template.render(Map.of("name", "World")); // "Hello, World!"
```

パーシャル（部分テンプレート）は`PartialResolver`で解決します。`MapPartialResolver`（`Map<String, String>`ベース）と`FilePartialResolver`（ファイルシステムベース）を標準実装として提供します。詳細は各クラスのJavadocを参照してください。

## CLIツール（cli）の利用

### ビルドと実行

```
./gradlew :cherry-mustache-cli:shadowJar
java -jar cherry-mustache-cli/build/libs/cherry-mustache-cli-<version>-all.jar [OPTIONS] TEMPLATE...
```

### 使用方法

```
Usage: mustache-cli [OPTIONS] TEMPLATE...

Renders one or more Mustache templates against a single data source and
concatenates the results.

Positional arguments:
  TEMPLATE                Template file path, or "-" to read from standard input

Options:
  --data <path|->         Input data file (JSON or YAML). Defaults to standard input
  --format <json|yaml>    Explicit data format. Required when data is read from standard input
  --output <path>         Output file path. Defaults to standard output
  --partial-dir <path>    Base directory for partial resolution. Defaults to each template's own directory
  --help, -h               Show this usage information and exit
```

### 例

```
# ファイルを指定してレンダリング（標準出力へ）
java -jar cli.jar --data data.json template.mustache

# 標準入力からデータを読む（--format必須）
cat data.json | java -jar cli.jar --format json template.mustache

# 複数テンプレートを連結してファイルへ出力
java -jar cli.jar --data data.json --output result.txt header.mustache body.mustache footer.mustache

# パーシャル探索ディレクトリを明示指定
java -jar cli.jar --data data.json --partial-dir ./partials template.mustache
```

### 制約

- 入力データはJSON/YAMLの静的データのみに対応します。CLIモードではラムダ（実行可能なコード）をテンプレートに渡すことはできません。ラムダが必要な場合はライブラリAPI（`core`）を直接利用してください。
- テンプレート・パーシャルファイルの読み込みはUTF-8固定です。

## テスト

```
./gradlew test
```

`core`は単体テスト・Property-Based Testing（jqwik）・公式Mustache specテストスイートを実施します。`cli`はCLI固有ロジック（引数解析、データ読込、終了コード等）の単体テストを実施します。

## ライセンス

[Apache License 2.0](LICENSE)
