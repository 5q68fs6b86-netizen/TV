var pdfh = globalThis.pdfh || function pdfh(html, parse) {
    return drpyPdfh(String(html || ''), String(parse || '')) || '';
};

var pdfa = globalThis.pdfa || function pdfa(html, parse) {
    try {
        const result = JSON.parse(drpyPdfa(String(html || ''), String(parse || '')) || '[]');
        return Array.isArray(result) ? result : [];
    } catch (e) {
        return [];
    }
};

var pd = globalThis.pd || function pd(html, parse, baseUrl) {
    return drpyPd(String(html || ''), String(parse || ''), String(baseUrl || globalThis.MY_URL || '')) || '';
};

var pdfl = globalThis.pdfl || function pdfl(html, listParse, titleParse, urlParse, baseUrl) {
    try {
        const result = JSON.parse(drpyPdfl(String(html || ''), String(listParse || ''), String(titleParse || ''), String(urlParse || ''), String(baseUrl || globalThis.MY_URL || '')) || '[]');
        return Array.isArray(result) ? result : [];
    } catch (e) {
        return [];
    }
};

globalThis.pdfh = pdfh;
globalThis.pdfa = pdfa;
globalThis.pd = pd;
globalThis.pdfl = pdfl;
