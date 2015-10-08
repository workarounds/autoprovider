package in.workarounds.autoprovider.compiler.utils;

/**
 * Created by madki on 08/10/15.
 */
public class StringUtils {

    public static boolean isEmpty(String data) {
        return data == null || data.isEmpty();
    }

    public static String toSnakeCase(String name) {
        return name.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    public static String toCamelCase(String s){
        String[] parts = s.split("_");
        String camelCaseString = "";
        for (String part : parts){
            camelCaseString = camelCaseString + toProperCase(part);
        }
        return camelCaseString;
    }

    public static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }
}
