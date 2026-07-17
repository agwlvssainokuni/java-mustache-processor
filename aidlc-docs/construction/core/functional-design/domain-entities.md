# Domain Entities - core (Unit 1: Core Template Engine)

Application Design（`components.md`）で識別したコンポーネントのうち、データモデルとしての側面を詳細化する。

## AST Nodes（`cherry.mustache.ast`）

### Node（抽象基底）
- **フィールド**: なし（マーカー的な基底。共通のレンダリング契約`render(Context, PartialResolver, Writer)`のみを定義）

### TextNode
- **フィールド**: `text: String`（そのまま出力する静的テキスト）

### VariableNode
- **フィールド**: `key: String`（ドット表記を含みうる解決対象キー）
- **意味**: エスケープあり変数展開（BR-1）

### UnescapedVariableNode
- **フィールド**: `key: String`
- **意味**: エスケープなし変数展開（`{{{key}}}` / `{{&key}}`）

### SectionNode
- **フィールド**: `key: String`, `children: List<Node>`, `rawText: String`（Lambda呼び出し用に保持する、セクション内の生テンプレート文字列。BR-2.4節参照）
- **意味**: 真偽/リスト/Lambdaに応じた条件付き・繰り返しレンダリング（BR-3）

### InvertedSectionNode
- **フィールド**: `key: String`, `children: List<Node>`
- **意味**: 否定セクション（値がfalsyの場合のみ描画）

### CommentNode
- **フィールド**: `text: String`（保持はするがレンダリング時は無視）

### PartialNode
- **フィールド**: `partialName: String`, `indent: String`（スタンドアロン行検出時の行頭インデント。無ければ空文字列）

## Context（`cherry.mustache.render.Context`）
- **フィールド**: `data: Object`（現在のスコープのデータ、Map/POJO/リスト要素等）, `parent: Context`（nullable、親スコープへの参照。イミュータブル連結リスト構造、BR-10）
- **操作**: `resolve(String key): Object`（BR-6のドット表記・スタック探索ルールに従う）, `push(Object data): Context`（新しい子`Context`を生成して返す。既存インスタンスは変更しない）

## Delimiters（Parser内部、`cherry.mustache.parser`）
- **フィールド**: `open: String`（既定`"{{"`）, `close: String`（既定`"}}"`）
- **役割**: パース中に`{{=...=}}`で更新される、タグ境界の走査に使う現在の区切り文字列。AST自体には残らない（Parser内部の一時状態）

## PartialResolver（`cherry.mustache.PartialResolver`, interface）
- **契約**: `resolve(String partialName): String`（nullable戻り値。未解決時は`null`を返す契約とし、Renderer側で空文字列相当として扱う。BR参照）

## MapPartialResolver（`cherry.mustache.MapPartialResolver`）
- **フィールド**: `partials: Map<String, String>`

## FilePartialResolver（`cherry.mustache.FilePartialResolver`）
- **フィールド**: `baseDir: Path`
- **解決規則**: `baseDir.resolve(partialName + ".mustache")`が存在しファイルとして読み取り可能ならその内容（UTF-8）を返し、存在しなければ`null`を返す

## Lambda（`cherry.mustache.Lambda`, functional interface）
- **契約**: `execute(String rawText): String`（Variable由来の呼び出し時は`rawText`は空文字列。Section由来の呼び出し時はセクション内の生テンプレート文字列。BR参照）

## 例外階層（`cherry.mustache`）

### MustacheException（共通基底、非検査例外 `RuntimeException`を継承）
- **フィールド**: `message: String`, `cause: Throwable`（標準の例外コンストラクタ）

### MustacheParseException（`MustacheException`を継承）
- **フィールド**: `line: int`, `column: int`（構文エラー発生位置。パース中に検出された不整合・未終了タグ等）

### MustacheRenderException（`MustacheException`を継承）
- **フィールド**: `key: String`（nullable、エラーに関連するタグ/パーシャル名。BR-8のPOJO例外ラップ時やBR-9の循環参照検出時に設定）

## 循環参照検出用の内部状態（RenderSession内部、公開APIではない）
- **フィールド**: `partialDepth: int`（`render()`呼び出し1回のスコープで生存する、現在のパーシャル解決ネスト深さ。BR-9参照。上限（100）到達で循環参照とみなす。`RenderSession`はスレッドローカルではなく呼び出しスタック上のローカル変数として管理し、スレッドセーフ性を確保する。Code Generation Step12で「パーシャル名の再出現」から「ネスト深さの上限」に検出基準を是正）
