package cherry.mustache.render;

import cherry.mustache.MustacheRenderException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * BR-7で確定したPOJOプロパティ解決順序（getter → Recordアクセサ → publicフィールド）を実装する。
 * アクセサ呼び出し自体が例外を送出した場合は{@link MustacheRenderException}にラップする（BR-8）。
 */
final class PojoResolver {

    private PojoResolver() {
    }

    static Lookup resolve(Object target, String property) {
        Class<?> type = target.getClass();

        Method getter = findGetter(type, property);
        if (getter != null) {
            return Lookup.of(invoke(getter, target, property));
        }

        if (type.isRecord()) {
            Method accessor = findNoArgMethod(type, property);
            if (accessor != null) {
                return Lookup.of(invoke(accessor, target, property));
            }
        }

        Field field = findField(type, property);
        if (field != null) {
            return Lookup.of(readField(field, target, property));
        }

        return Lookup.NOT_FOUND;
    }

    private static Method findGetter(Class<?> type, String property) {
        String capitalized = capitalize(property);
        Method getMethod = findNoArgMethod(type, "get" + capitalized);
        if (getMethod != null) {
            return getMethod;
        }
        Method isMethod = findNoArgMethod(type, "is" + capitalized);
        if (isMethod != null
                && (isMethod.getReturnType() == boolean.class || isMethod.getReturnType() == Boolean.class)) {
            return isMethod;
        }
        return null;
    }

    private static Method findNoArgMethod(Class<?> type, String name) {
        try {
            Method method = type.getMethod(name);
            if (Modifier.isStatic(method.getModifiers())) {
                return null;
            }
            // 宣言クラス自体がpublicでない場合（ローカルレコード等）でもpublicメソッドを呼び出せるようにする
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Field findField(Class<?> type, String name) {
        try {
            Field field = type.getField(name);
            if (Modifier.isStatic(field.getModifiers())) {
                return null;
            }
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static Object invoke(Method method, Object target, String property) {
        try {
            return method.invoke(target);
        } catch (IllegalAccessException e) {
            throw new MustacheRenderException("Failed to access property '" + property + "'", property, e);
        } catch (InvocationTargetException e) {
            throw new MustacheRenderException("Property '" + property + "' accessor threw an exception", property, e.getCause());
        }
    }

    private static Object readField(Field field, Object target, String property) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new MustacheRenderException("Failed to access field '" + property + "'", property, e);
        }
    }

    private static String capitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
