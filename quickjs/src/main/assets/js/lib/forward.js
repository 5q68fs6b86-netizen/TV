(function () {
    const API = __FORWARD_API_PLACEHOLDER__;
    const SOURCE = __FORWARD_SOURCE_PLACEHOLDER__;
    const SPIDER = "__JS_SPIDER__";
    const state = {
        ext: {},
        entries: [],
        loaded: false,
    };

    function asObject(value) {
        if (!value) return {};
        if (typeof value === "object") return value;
        if (typeof value !== "string") return {};
        try {
            return JSON.parse(value);
        } catch (e) {
            return {};
        }
    }

    function clone(value) {
        if (value == null) return value;
        return JSON.parse(JSON.stringify(value));
    }

    function safeParse(value) {
        if (typeof value !== "string") return value;
        try {
            return JSON.parse(value);
        } catch (e) {
            return value;
        }
    }

    function toPlainObject(value) {
        const result = {};
        if (!value) return result;
        for (const key of Object.keys(value)) result[key] = value[key];
        return result;
    }

    function appendParams(url, params) {
        if (!params) return url;
        let result = String(url);
        for (const key of Object.keys(params)) {
            const value = params[key];
            if (value == null || value === "") continue;
            result += result.includes("?") ? "&" : "?";
            result += encodeURIComponent(key) + "=" + encodeURIComponent(String(value));
        }
        return result;
    }

    function responseData(res) {
        if (!res) return null;
        if (Object.prototype.hasOwnProperty.call(res, "data")) return res.data;
        if (Object.prototype.hasOwnProperty.call(res, "content")) return safeParse(res.content);
        return res;
    }

    async function widgetRequest(method, url, body, options) {
        options = options || {};
        const req = {
            method: method.toLowerCase(),
            headers: options.headers || {},
            redirect: options.allow_redirects === false ? 0 : 1,
        };
        if (options.base64Data) req.buffer = 2;
        if (method === "POST") {
            if (typeof body === "string") req.body = body;
            else if (body != null) req.data = body;
        }
        const res = await http(appendParams(url, options.params), req);
        return {
            data: safeParse(res && res.content),
            statusCode: Number(res && res.code) || 0,
            headers: toPlainObject(res && res.headers),
        };
    }

    function storageKey(key) {
        return "forward_" + String(key);
    }

    function installWidgetRuntime() {
        globalThis.Widget = {
            http: {
                get(url, options) {
                    return widgetRequest("GET", url, null, options);
                },
                post(url, body, options) {
                    return widgetRequest("POST", url, body, options);
                },
            },
            tmdb: {
                async get(url, options) {
                    options = options || {};
                    const ext = state.ext || {};
                    const headers = Object.assign({ accept: "application/json" }, options.headers || {});
                    const token = ext.tmdbBearer || ext.tmdbToken || ext.TMDB_API_KEY || "";
                    if (token) headers.Authorization = token.startsWith("Bearer ") ? token : "Bearer " + token;
                    const resp = await globalThis.Widget.http.get("https://api.themoviedb.org/3/" + String(url).replace(/^\/+/, ""), Object.assign({}, options, { headers }));
                    return resp.data;
                },
            },
            html: {
                load(html, options, isDocument) {
                    const cheerio = globalThis.__FORWARD_CHEERIO__;
                    if (!cheerio || typeof cheerio.load !== "function") throw new Error("cheerio is not available");
                    return Promise.resolve(cheerio.load(html, options, isDocument));
                },
            },
            storage: {
                get(key) {
                    const value = local.get("forward", storageKey(key));
                    return Promise.resolve(value === "" ? null : value);
                },
                set(key, value) {
                    local.set("forward", storageKey(key), String(value == null ? "" : value));
                    return Promise.resolve();
                },
                remove(key) {
                    local.delete("forward", storageKey(key));
                    return Promise.resolve();
                },
                keys() {
                    return Promise.resolve([]);
                },
                clear() {
                    return Promise.resolve();
                },
            },
        };
        globalThis.WidgetMetadata = null;
    }

    function isForwardHubSource() {
        const path = API.split("?", 2)[0].toLowerCase();
        if (path.endsWith(".fwd")) return true;
        const parsed = asObject(SOURCE);
        return Array.isArray(parsed.widgets);
    }

    function sourceUrl(entry) {
        return entry.url || entry.api || API;
    }

    async function fetchText(url) {
        if (url === API) return SOURCE;
        const res = await globalThis.Widget.http.get(url);
        const data = responseData(res);
        return typeof data === "string" ? data : JSON.stringify(data || "");
    }

    function resetWidgetGlobals() {
        globalThis.WidgetMetadata = null;
        globalThis.loadDetail = undefined;
    }

    function evalWidgetSource(source, url) {
        resetWidgetGlobals();
        const safeUrl = String(url || "forward-widget").replace(/[\r\n]/g, "");
        (0, eval)(String(source) + "\n//# sourceURL=" + safeUrl);
        const metadata = clone(globalThis.WidgetMetadata || {});
        if (!metadata.id) metadata.id = safeUrl;
        if (!metadata.title) metadata.title = metadata.id;
        return metadata;
    }

    async function loadEntry(entry) {
        if (!entry.source) entry.source = await fetchText(sourceUrl(entry));
        entry.metadata = evalWidgetSource(entry.source, sourceUrl(entry));
        entry.id = entry.id || entry.metadata.id || sourceUrl(entry);
        entry.title = entry.title || entry.metadata.title || entry.id;
        return entry;
    }

    async function ensureEntries() {
        if (state.loaded) return state.entries;
        installWidgetRuntime();
        if (isForwardHubSource()) {
            const hub = asObject(SOURCE);
            const widgets = Array.isArray(hub.widgets) ? hub.widgets : [];
            state.entries = widgets.filter((item) => item && item.url).map((item) => Object.assign({}, item));
        } else {
            state.entries = [{ id: "default", title: "", url: API, source: SOURCE }];
        }
        const selected = selectedWidgets();
        if (selected.length) state.entries = state.entries.filter((entry) => selected.includes(entry.id) || selected.includes(entry.title) || selected.includes(sourceUrl(entry)));
        for (const entry of state.entries) {
            try {
                await loadEntry(entry);
            } catch (e) {
                console.error("forward metadata failed", sourceUrl(entry), e && e.message ? e.message : e);
            }
        }
        state.loaded = true;
        return state.entries;
    }

    function selectedWidgets() {
        const ext = state.ext || {};
        const selected = ext.widget || ext.widgets || ext.ids;
        if (!selected) return [];
        return Array.isArray(selected) ? selected.map(String) : String(selected).split(",").map((item) => item.trim()).filter(Boolean);
    }

    function moduleParams(entry, module) {
        const metadata = entry.metadata || {};
        return []
            .concat(Array.isArray(metadata.globalParams) ? metadata.globalParams : [])
            .concat(Array.isArray(module.params) ? module.params : []);
    }

    function isVideoModule(module) {
        return module && !module.type && module.functionName && module.requiresWebView !== true;
    }

    function moduleTitle(entry, module) {
        const widget = entry.metadata || entry;
        if (!widget.title || widget.title === module.title) return module.title || widget.title || entry.id;
        return widget.title + " - " + (module.title || module.functionName);
    }

    function encodePayload(payload) {
        return "fw:" + encodeURIComponent(JSON.stringify(payload));
    }

    function decodePayload(id) {
        const text = String(id || "");
        const raw = text.startsWith("fw:") ? text.slice(3) : text;
        return JSON.parse(decodeURIComponent(raw));
    }

    function specFor(entry, module) {
        return {
            widgetId: entry.id,
            widgetTitle: (entry.metadata && entry.metadata.title) || entry.title || entry.id,
            url: sourceUrl(entry),
            functionName: module.functionName,
            moduleId: module.id || module.functionName,
            moduleTitle: module.title || module.functionName,
        };
    }

    function findEntry(widgetId, url) {
        return state.entries.find((entry) => entry.id === widgetId || sourceUrl(entry) === url) || null;
    }

    async function activateSpec(spec) {
        await ensureEntries();
        const entry = findEntry(spec.widgetId, spec.url);
        if (!entry) throw new Error("Forward widget not found: " + (spec.widgetId || spec.url));
        await loadEntry(entry);
        return entry;
    }

    function extValue(spec, param) {
        const ext = state.ext || {};
        const names = [param.name];
        const keys = [spec.widgetId, spec.moduleId, spec.functionName].filter(Boolean);
        for (const key of keys) {
            if (ext[key] && typeof ext[key] === "object") {
                if (ext[key][param.name] != null) return ext[key][param.name];
                if (ext[key][spec.functionName] && ext[key][spec.functionName][param.name] != null) return ext[key][spec.functionName][param.name];
            }
        }
        if (ext.params && ext.params[param.name] != null) return ext.params[param.name];
        for (const name of names) if (ext[name] != null) return ext[name];
        return undefined;
    }

    function defaultValue(param) {
        if (param.value != null) return param.value;
        if (Array.isArray(param.enumOptions) && param.enumOptions.length) return param.enumOptions[0].value;
        if (Array.isArray(param.placeholders) && param.placeholders.length) return param.placeholders[0].value;
        if (param.type === "count") return "20";
        if (param.type === "offset") return "0";
        return "";
    }

    function buildParams(spec, params, page, extend, searchKey) {
        const result = {};
        extend = extend || {};
        for (const param of params || []) {
            if (!param || !param.name) continue;
            let value;
            if (param.type === "page") value = String(page || "1");
            else if (searchKey != null && /^(keyword|key|q|query|search|title|name)$/i.test(param.name)) value = searchKey;
            else if (extend[param.name] != null) value = extend[param.name];
            else {
                value = extValue(spec, param);
                if (value == null) value = defaultValue(param);
            }
            result[param.name] = value == null ? "" : value;
        }
        return result;
    }

    function filterValues(param) {
        const values = [];
        const source = Array.isArray(param.enumOptions) && param.enumOptions.length ? param.enumOptions : param.placeholders;
        if (!Array.isArray(source)) return values;
        for (const item of source) values.push({ n: item.title || item.value || "", v: item.value == null ? "" : String(item.value) });
        return values;
    }

    function buildFilter(spec, param) {
        if (!param || !param.name || param.type === "page" || param.type === "constant") return null;
        const values = filterValues(param);
        if (!values.length) return null;
        return {
            key: param.name,
            name: param.title || param.name,
            init: String(extValue(spec, param) != null ? extValue(spec, param) : defaultValue(param)),
            value: values,
        };
    }

    function releaseYear(item) {
        const date = item && (item.releaseDate || item.year || item.airDate);
        return date ? String(date).slice(0, 4) : "";
    }

    function normalizeImage(path, size) {
        if (!path) return "";
        const value = String(path).trim();
        if (!value) return "";
        if (/^https?:\/\//i.test(value) || /^data:/i.test(value)) return value;
        if (value.startsWith("//")) return "https:" + value;
        if (value.startsWith("/")) return "https://image.tmdb.org/t/p/" + (size || "w500") + value;
        return value;
    }

    function itemTitle(item, index) {
        return sanitizeName((item && (item.title || item.name || item.episodeName || item.id)) || String(index + 1).padStart(2, "0"));
    }

    function sanitizeName(value) {
        return String(value == null ? "" : value).replace(/[$#]/g, " ").trim();
    }

    function itemRemark(item) {
        if (!item) return "";
        return item.rating || item.releaseDate || item.durationText || item.genreTitle || item.description || "";
    }

    function vodFromItem(item, spec) {
        item = item || {};
        return {
            vod_id: encodePayload({ spec, item }),
            vod_name: item.title || item.name || item.id || "",
            vod_pic: normalizeImage(item.posterPath, "w500") || normalizeImage(item.backdropPath, "w780"),
            vod_remarks: itemRemark(item),
            vod_year: releaseYear(item),
            type_name: item.genreTitle || item.mediaType || item.type || "",
            vod_content: item.description || "",
        };
    }

    function normalizeItems(result) {
        result = responseData(result);
        if (!result) return [];
        if (Array.isArray(result)) return result.filter(Boolean);
        if (Array.isArray(result.items)) return result.items.filter(Boolean);
        if (Array.isArray(result.list)) return result.list.filter(Boolean);
        if (Array.isArray(result.results)) return result.results.filter(Boolean);
        if (Array.isArray(result.data)) return result.data.filter(Boolean);
        if (Array.isArray(result.sections)) {
            const items = [];
            for (const section of result.sections) if (Array.isArray(section.items)) items.push.apply(items, section.items);
            return items.filter(Boolean);
        }
        return [result];
    }

    function detailChildren(item) {
        if (!item) return [];
        if (Array.isArray(item.childItems) && item.childItems.length) return item.childItems.filter(Boolean);
        if (item.videoUrl || item.link || item.id) return [item];
        return [];
    }

    function playableUrl(item) {
        if (!item) return "";
        return item.videoUrl || item.url || item.link || (item.type === "url" ? item.id : "");
    }

    function playableHeaders(item) {
        return (item && (item.customHeaders || item.headers)) || {};
    }

    async function loadPlayable(item) {
        if (item && item.videoUrl) return item;
        const link = item && (item.link || item.url || item.id);
        if (!link || typeof globalThis.loadDetail !== "function") return item || {};
        const detail = await globalThis.loadDetail.call(globalThis, link);
        return Object.assign({}, item || {}, detail || {});
    }

    async function moduleFunction(spec) {
        await activateSpec(spec);
        const func = globalThis[spec.functionName];
        if (typeof func !== "function") throw new Error("Forward function not found: " + spec.functionName);
        return func;
    }

    globalThis[SPIDER] = {
        async init(ext) {
            state.ext = asObject(ext);
            await ensureEntries();
        },

        async home(filter) {
            const entries = await ensureEntries();
            const classes = [];
            const filters = {};
            for (const entry of entries) {
                const modules = ((entry.metadata || {}).modules || []).filter(isVideoModule);
                for (const module of modules) {
                    const spec = specFor(entry, module);
                    const tid = encodePayload({ spec });
                    classes.push({ type_id: tid, type_name: moduleTitle(entry, module) });
                    const moduleFilters = moduleParams(entry, module).map((param) => buildFilter(spec, param)).filter(Boolean);
                    if (moduleFilters.length) filters[tid] = moduleFilters;
                }
            }
            return JSON.stringify({ class: classes, filters });
        },

        async homeVod() {
            return JSON.stringify({ list: [] });
        },

        async category(tid, pg, filter, extend) {
            const payload = decodePayload(tid);
            const spec = payload.spec;
            const entry = await activateSpec(spec);
            const module = ((entry.metadata || {}).modules || []).find((item) => item && item.functionName === spec.functionName) || {};
            const params = buildParams(spec, moduleParams(entry, module), pg, extend);
            const func = await moduleFunction(spec);
            const result = await func.call(globalThis, params);
            const list = normalizeItems(result).map((item) => vodFromItem(item, spec));
            return JSON.stringify({ list, page: Number(pg) || 1, pagecount: list.length ? Number(pg || 1) + 1 : Number(pg || 1) });
        },

        async detail(id) {
            const payload = decodePayload(id);
            const spec = payload.spec;
            await activateSpec(spec);
            const base = payload.item || {};
            const detail = await loadPlayable(base);
            const item = Object.assign({}, base, detail || {});
            const vod = vodFromItem(item, spec);
            const children = detailChildren(item);
            if (children.length) {
                vod.vod_play_from = "Forward";
                vod.vod_play_url = children.map((child, index) => {
                    const name = itemTitle(child, index);
                    const url = encodePayload({ spec, item: child, parent: item });
                    return name + "$" + url;
                }).join("#");
            }
            return JSON.stringify({ list: [vod] });
        },

        async play(flag, id, vipFlags) {
            const payload = decodePayload(id);
            const spec = payload.spec;
            await activateSpec(spec);
            const item = await loadPlayable(payload.item || {});
            const url = playableUrl(item);
            return JSON.stringify({ parse: 0, url, header: playableHeaders(item) });
        },

        async search(key, quick, pg) {
            const entries = await ensureEntries();
            const list = [];
            for (const entry of entries) {
                const search = (entry.metadata || {}).search;
                if (!search || !search.functionName || search.requiresWebView === true) continue;
                const spec = {
                    widgetId: entry.id,
                    widgetTitle: (entry.metadata && entry.metadata.title) || entry.title || entry.id,
                    url: sourceUrl(entry),
                    functionName: search.functionName,
                    moduleId: "search",
                    moduleTitle: search.title || "Search",
                };
                try {
                    const params = buildParams(spec, search.params || [], pg || "1", {}, key);
                    const func = await moduleFunction(spec);
                    normalizeItems(await func.call(globalThis, params)).forEach((item) => list.push(vodFromItem(item, spec)));
                } catch (e) {
                    console.error("forward search failed", entry.id, e && e.message ? e.message : e);
                }
            }
            return JSON.stringify({ list });
        },

        async live(url) {
            return "";
        },

        async proxy(params) {
            return [404, "text/plain", ""];
        },

        async playUrl(flag, id) {
            return this.play(flag, id, []);
        },

        async action(action) {
            return "";
        },

        async sniffer() {
            return false;
        },

        async isVideo(url) {
            return false;
        },

        async destroy() {
            state.entries = [];
            state.loaded = false;
        },
    };
})();
