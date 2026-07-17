package cherry.mustache.render;

/**
 * 1階層分のプロパティ解決結果。{@code found}で「見つかったか」と「見つかった値がnullか」を区別する。
 */
record Lookup(boolean found, Object value) {

    static final Lookup NOT_FOUND = new Lookup(false, null);

    static Lookup of(Object value) {
        return new Lookup(true, value);
    }
}
