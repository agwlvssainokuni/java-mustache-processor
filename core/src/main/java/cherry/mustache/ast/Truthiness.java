package cherry.mustache.ast;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * BR-3で確定した真偽判定ルール。{@code null}/{@code false}/空のList・配列のみがfalsy。
 * 空文字列・数値0はtruthyとして扱う。
 */
final class Truthiness {

    private Truthiness() {
    }

    static boolean isTruthy(Object value) {
        if (value == null || Boolean.FALSE.equals(value)) {
            return false;
        }
        if (value instanceof Collection<?> collection) {
            return !collection.isEmpty();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) > 0;
        }
        return true;
    }

    static boolean isListLike(Object value) {
        return value instanceof Collection<?> || value.getClass().isArray();
    }
}
