package cherry.mustache.ast;

import cherry.mustache.render.Context;
import cherry.mustache.render.RenderSession;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

/**
 * Lambdaの戻り値を、呼び出し元タグの開始/終了デリミタで再パースし、
 * 現在のContextで再レンダリングして文字列化する共通処理（BR-2.4節）。
 */
final class LambdaSupport {

    private LambdaSupport() {
    }

    static String renderLambdaOutput(String templateText, Context context, RenderSession session,
                                      String openDelimiter, String closeDelimiter) {
        Node reparsed = session.reparse(templateText, openDelimiter, closeDelimiter);
        StringWriter buffer = new StringWriter();
        try {
            reparsed.render(context, session, buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return buffer.toString();
    }
}
