package cherry.mustache;

/**
 * テンプレートの構文解析（パース）中に検出されたエラーを表す例外。
 * タグの対応不整合、未終了タグ、不正なデリミタ指定等で送出される。
 */
public class MustacheParseException extends MustacheException {

    private final int line;
    private final int column;

    /**
     * @param message エラーメッセージ
     * @param line    エラー発生位置の行番号（1始まり）
     * @param column  エラー発生位置の列番号（1始まり）
     */
    public MustacheParseException(String message, int line, int column) {
        super(message);
        this.line = line;
        this.column = column;
    }

    /**
     * @param message エラーメッセージ
     * @param line    エラー発生位置の行番号（1始まり）
     * @param column  エラー発生位置の列番号（1始まり）
     * @param cause   根本原因
     */
    public MustacheParseException(String message, int line, int column, Throwable cause) {
        super(message, cause);
        this.line = line;
        this.column = column;
    }

    /**
     * @return エラー発生位置の行番号（1始まり）
     */
    public int getLine() {
        return line;
    }

    /**
     * @return エラー発生位置の列番号（1始まり）
     */
    public int getColumn() {
        return column;
    }
}
