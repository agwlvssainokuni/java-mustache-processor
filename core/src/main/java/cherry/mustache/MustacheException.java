package cherry.mustache;

/**
 * Mustacheテンプレート処理中に発生するすべてのエラーの共通基底例外。
 */
public class MustacheException extends RuntimeException {

    /**
     * @param message エラーメッセージ
     */
    public MustacheException(String message) {
        super(message);
    }

    /**
     * @param message エラーメッセージ
     * @param cause   根本原因
     */
    public MustacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
