package com.fongmi.quickjs.method;

import com.github.catvod.utils.UriUtil;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Drpy {

    private static final Pattern INDEX_SELECTOR = Pattern.compile(":(eq|lt|gt)\\((-?\\d+)\\)");
    private static final Pattern STYLE_URL = Pattern.compile("url\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_ATTR = Pattern.compile("(url|src|href|-original|-src|-play|-url|style)$|^(data-|url-|src-)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPECIAL_URL = Pattern.compile("^(ftp|magnet|thunder|ws|data):", Pattern.CASE_INSENSITIVE);

    private Drpy() {
    }

    static String pdfh(String html, String parse) {
        for (String item : parses(parse)) {
            String result = value(select(html, selectorParts(item)), attrPart(item));
            if (!result.isEmpty()) return result;
        }
        return "";
    }

    static String pdfa(String html, String parse) {
        JSONArray array = new JSONArray();
        for (String item : parses(parse)) {
            Elements elements = select(html, parts(item));
            if (elements.isEmpty()) continue;
            for (Element element : elements) array.put(element.outerHtml());
            break;
        }
        return array.toString();
    }

    static String pd(String html, String parse, String baseUrl) {
        String result = pdfh(html, parse);
        if (result.isEmpty()) return "";
        if (!URL_ATTR.matcher(parse == null ? "" : parse).find()) return result;
        if (SPECIAL_URL.matcher(result).find()) return result;
        int index = result.indexOf("http");
        if (index >= 0) return result.substring(index);
        return UriUtil.resolve(baseUrl, result);
    }

    static String pdfl(String html, String listParse, String titleParse, String urlParse, String baseUrl) {
        JSONArray array = new JSONArray();
        for (String parse : parses(listParse)) {
            Elements elements = select(html, parts(parse));
            if (elements.isEmpty()) continue;
            for (Element element : elements) {
                String item = element.outerHtml();
                String title = pdfh(item, titleParse).trim();
                String url = pd(item, urlParse, baseUrl);
                array.put(title + "$" + url);
            }
            break;
        }
        return array.toString();
    }

    private static List<String> parses(String parse) {
        List<String> result = new ArrayList<>();
        if (parse == null) return result;
        for (String item : parse.split("\\|\\|")) {
            item = item.trim();
            if (!item.isEmpty()) result.add(item);
        }
        return result;
    }

    private static List<String> selectorParts(String parse) {
        List<String> parts = parts(parse);
        if (parts.size() > 1) parts.remove(parts.size() - 1);
        else if (parts.size() == 1 && isValue(parts.get(0))) parts.clear();
        return parts;
    }

    private static String attrPart(String parse) {
        List<String> parts = parts(parse);
        if (parts.isEmpty()) return "Text";
        if (parts.size() > 1) return parts.get(parts.size() - 1);
        return isValue(parts.get(0)) ? parts.get(0) : "Text";
    }

    private static List<String> parts(String parse) {
        List<String> result = new ArrayList<>();
        if (parse == null) return result;
        for (String item : parse.split("&&")) {
            item = item.trim();
            if (!item.isEmpty()) result.add(item);
        }
        return result;
    }

    private static Elements select(String html, List<String> parts) {
        Document document = Jsoup.parse(html == null ? "" : html);
        Elements current = new Elements(document);
        for (String part : parts) current = select(current, part);
        return current;
    }

    private static Elements select(Elements bases, String selector) {
        String[] split = selector.split("--", 2);
        Elements selected = selectOnly(bases, split[0].trim());
        if (split.length > 1) {
            String remove = split[1].trim();
            try {
                if (!remove.isEmpty()) selected.select(remove).remove();
            } catch (Throwable ignored) {
            }
        }
        return selected;
    }

    private static Elements selectOnly(Elements bases, String selector) {
        selector = normalizeSelector(selector);
        if (selector.isEmpty()) return bases;
        try {
            return bases.select(selector);
        } catch (Selector.SelectorParseException e) {
            return selectWithIndex(bases, selector);
        } catch (Throwable e) {
            return new Elements();
        }
    }

    private static String normalizeSelector(String selector) {
        return selector.replaceAll(":first(?![-\\w(])", ":eq(0)").replaceAll(":last(?![-\\w(])", ":eq(-1)");
    }

    private static Elements selectWithIndex(Elements bases, String selector) {
        Matcher matcher = INDEX_SELECTOR.matcher(selector);
        List<Index> indexes = new ArrayList<>();
        while (matcher.find()) indexes.add(new Index(matcher.group(1), Integer.parseInt(matcher.group(2))));
        String plain = matcher.replaceAll("");
        Elements selected;
        try {
            selected = plain.isEmpty() ? bases : bases.select(plain);
        } catch (Throwable e) {
            return new Elements();
        }
        for (Index index : indexes) selected = index.apply(selected);
        return selected;
    }

    private static String value(Elements elements, String attr) {
        if (elements.isEmpty()) return "";
        Element element = elements.first();
        if (element == null) return "";
        String name = attr == null ? "Text" : attr.trim().replaceFirst("^@", "");
        if (name.equalsIgnoreCase("Text")) return element.text().trim();
        if (name.equalsIgnoreCase("Html")) return element.html();
        if (name.equalsIgnoreCase("OuterHtml")) return element.outerHtml();
        String value = element.attr(name);
        if (name.equalsIgnoreCase("style")) value = styleUrl(value);
        return value == null ? "" : value.trim();
    }

    private static String styleUrl(String value) {
        if (value == null) return "";
        Matcher matcher = STYLE_URL.matcher(value);
        if (!matcher.find()) return value;
        return matcher.group(1).replaceAll("^['\"]|['\"]$", "");
    }

    private static boolean isValue(String part) {
        String value = part.toLowerCase(Locale.ROOT);
        return "text".equals(value) || "html".equals(value) || "outerhtml".equals(value);
    }

    private static final class Index {

        private final String type;
        private final int value;

        private Index(String type, int value) {
            this.type = type;
            this.value = value;
        }

        private Elements apply(Elements elements) {
            Elements result = new Elements();
            if ("eq".equals(type)) {
                int index = value < 0 ? elements.size() + value : value;
                if (index >= 0 && index < elements.size()) result.add(elements.get(index));
            } else if ("lt".equals(type)) {
                int limit = value < 0 ? elements.size() + value : value;
                for (int i = 0; i < elements.size() && i < limit; i++) result.add(elements.get(i));
            } else if ("gt".equals(type)) {
                int start = value < 0 ? elements.size() + value : value;
                for (int i = Math.max(0, start + 1); i < elements.size(); i++) result.add(elements.get(i));
            }
            return result;
        }
    }
}
