package com.fongmi.android.tv.utils;

import android.text.TextUtils;

import com.fongmi.android.tv.setting.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TextFilter {

    private static final String ARROW = "=>";

    public static String detail(String text) {
        return apply(text, Setting.getDetailFilter());
    }

    public static String flag(String text) {
        return apply(text, Setting.getFlagFilter());
    }

    private static String apply(String text, String config) {
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(config)) return TextUtils.isEmpty(text) ? "" : text;
        String result = text;
        for (Rule rule : parse(config)) result = rule.apply(result);
        return result.trim();
    }

    private static List<Rule> parse(String config) {
        List<Rule> rules = new ArrayList<>();
        for (String line : config.split("\\r?\\n")) {
            Rule rule = Rule.parse(line);
            if (rule != null) rules.add(rule);
        }
        return rules;
    }

    private record Rule(String target, String replacement, Pattern pattern) {

        static Rule parse(String line) {
            if (TextUtils.isEmpty(line)) return null;
            String text = line.trim();
            if (text.isEmpty()) return null;
            int index = text.indexOf(ARROW);
            String target = (index == -1 ? text : text.substring(0, index)).trim();
            String replacement = index == -1 ? "" : text.substring(index + ARROW.length());
            if (target.length() > 2 && target.startsWith("/") && target.endsWith("/")) {
                try {
                    return new Rule("", replacement, Pattern.compile(target.substring(1, target.length() - 1)));
                } catch (PatternSyntaxException ignored) {
                    return null;
                }
            }
            return target.isEmpty() ? null : new Rule(target, replacement, null);
        }

        String apply(String text) {
            if (pattern == null) return text.replace(target, replacement);
            try {
                return pattern.matcher(text).replaceAll(replacement);
            } catch (RuntimeException ignored) {
                return text;
            }
        }
    }
}
