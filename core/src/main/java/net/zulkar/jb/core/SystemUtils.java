package net.zulkar.jb.core;

import org.apache.commons.lang3.StringUtils;

public class SystemUtils {

    public static long getLongProperty(String name, long def) {
        String s = System.getProperty(name);
        try {
            if (StringUtils.isEmpty(s)) {
                return def;
            }
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
