package cherry.mustache.ast;

import cherry.mustache.Lambda;
import cherry.mustache.render.Context;
import cherry.mustache.render.RenderSession;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * セクション（{@code {{#key}}...{{/key}}}）を表すノード。
 * 真偽判定・リスト展開・Lambda呼び出しに応じて条件付き／繰り返しレンダリングを行う（BR-3）。
 */
public final class SectionNode extends Node {

    private final String key;
    private final List<Node> children;
    private final String rawText;
    private final String openDelimiter;
    private final String closeDelimiter;

    public SectionNode(String key, List<Node> children, String rawText, String openDelimiter, String closeDelimiter) {
        this.key = key;
        this.children = children;
        this.rawText = rawText;
        this.openDelimiter = openDelimiter;
        this.closeDelimiter = closeDelimiter;
    }

    @Override
    public void render(Context context, RenderSession session, Writer out) throws IOException {
        Object value = context.resolve(key);

        if (value instanceof Lambda lambda) {
            String text = LambdaSupport.renderLambdaOutput(lambda.execute(rawText), context, session, openDelimiter, closeDelimiter);
            out.write(text);
            return;
        }

        if (!Truthiness.isTruthy(value)) {
            return;
        }

        if (Truthiness.isListLike(value)) {
            for (Object element : toIterable(value)) {
                renderChildren(context.push(element), session, out);
            }
            return;
        }

        if (isScalar(value)) {
            renderChildren(context, session, out);
        } else {
            renderChildren(context.push(value), session, out);
        }
    }

    private void renderChildren(Context context, RenderSession session, Writer out) throws IOException {
        for (Node child : children) {
            child.render(context, session, out);
        }
    }

    private static boolean isScalar(Object value) {
        return value instanceof Boolean || value instanceof Number || value instanceof CharSequence || value instanceof Character;
    }

    private static Iterable<?> toIterable(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection;
        }
        int length = Array.getLength(value);
        Object[] elements = new Object[length];
        for (int i = 0; i < length; i++) {
            elements[i] = Array.get(value, i);
        }
        return Arrays.asList(elements);
    }
}
