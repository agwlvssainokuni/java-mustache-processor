package cherry.mustache;

/**
 * テンプレートのレンダリング中に検出されたエラーを表す例外。
 * パーシャルの循環参照、POJOプロパティアクセス時の例外、パーシャルファイルの
 * I/Oエラー等、正常系（未解決キー・未解決パーシャル）とは区別すべき異常系で送出される。
 */
public class MustacheRenderException extends MustacheException {

    private final String key;

    /**
     * @param message エラーメッセージ
     * @param key     関連するタグ名・パーシャル名（無ければ{@code null}）
     */
    public MustacheRenderException(String message, String key) {
        super(message);
        this.key = key;
    }

    /**
     * @param message エラーメッセージ
     * @param key     関連するタグ名・パーシャル名（無ければ{@code null}）
     * @param cause   根本原因
     */
    public MustacheRenderException(String message, String key, Throwable cause) {
        super(message, cause);
        this.key = key;
    }

    /**
     * @return 関連するタグ名・パーシャル名（無ければ{@code null}）
     */
    public String getKey() {
        return key;
    }
}
