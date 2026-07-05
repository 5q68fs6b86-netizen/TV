import { cheerio as __drpyCheerio } from '__DRPY_CORE_IMPORT__';

function __drpyLoad(html) {
    return __drpyCheerio.load(String(html ?? ''), { decodeEntities: false });
}

function __drpyParses(parse) {
    return String(parse || '').split('||').map(item => item.trim()).filter(Boolean);
}

function __drpyParts(parse) {
    return String(parse || '').split('&&').map(item => item.trim()).filter(Boolean);
}

function __drpySelect(html, selectors) {
    const $ = __drpyLoad(html);
    let nodes = $.root();
    selectors.forEach(selector => {
        nodes = nodes.find(selector);
    });
    return { $, nodes };
}

function __drpyValue($, nodes, attr) {
    const node = nodes.first();
    if (!node || !node.length) return '';
    const name = String(attr || 'Text').replace(/^@/, '');
    if (/^Text$/i.test(name)) return node.text().trim();
    if (/^Html$/i.test(name)) return node.html() || '';
    if (/^OuterHtml$/i.test(name)) return $.html(node) || '';
    return node.attr(name) || '';
}

var pdfh = globalThis.pdfh || function pdfh(html, parse) {
    for (const item of __drpyParses(parse)) {
        const parts = __drpyParts(item);
        let attr = 'Text';
        if (parts.length > 1 || /^(Text|Html|OuterHtml)$/i.test(parts[0] || '')) attr = parts.pop();
        const { $, nodes } = __drpySelect(html, parts);
        const result = __drpyValue($, nodes, attr);
        if (result) return result;
    }
    return '';
};

var pdfa = globalThis.pdfa || function pdfa(html, parse) {
    for (const item of __drpyParses(parse)) {
        const parts = __drpyParts(item);
        const { $, nodes } = __drpySelect(html, parts);
        const result = nodes.toArray().map(node => $.html(node)).filter(Boolean);
        if (result.length) return result;
    }
    return [];
};

var pd = globalThis.pd || function pd(html, parse, baseUrl) {
    const result = pdfh(html, parse);
    if (!result) return '';
    if (!/(url|src|href|-original|-src|-play|-url|style)$/i.test(String(parse || ''))) return result;
    if (/^(http|ftp|magnet|thunder|ws|data):/i.test(result)) return result;
    if (typeof joinUrl === 'function') return joinUrl(baseUrl || globalThis.MY_URL || '', result);
    return result;
};

var pdfl = globalThis.pdfl || function pdfl(html, listParse, titleParse, urlParse, baseUrl) {
    return pdfa(html, listParse).map(item => `${pdfh(item, titleParse).trim()}$${pd(item, urlParse, baseUrl)}`);
};

globalThis.pdfh = pdfh;
globalThis.pdfa = pdfa;
globalThis.pd = pd;
globalThis.pdfl = pdfl;
