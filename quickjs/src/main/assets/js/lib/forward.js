(function () {
    const API = __FORWARD_API_PLACEHOLDER__;
    const SOURCE = __FORWARD_SOURCE_PLACEHOLDER__;
    const DEFAULT_TMDB_KEY = __FORWARD_TMDB_KEY_PLACEHOLDER__;
    const SPIDER = "__JS_SPIDER__";
    const state = {
        ext: {},
        entries: [],
        loaded: false,
        activeEntry: null,
        storageKeys: [],
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
        if (options.timeout != null) req.timeout = Number(options.timeout) || 10000;
        if (options.base64Data) req.buffer = 2;
        if (method === "POST") {
            if (typeof body === "string") {
                req.body = body;
                if (!req.headers["Content-Type"] && !req.headers["content-type"]) req.headers["Content-Type"] = "text/plain; charset=utf-8";
            } else if (body != null) {
                req.data = body;
                req.postType = options.postType || "json";
            }
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

    function rememberStorageKey(key) {
        const value = String(key);
        if (!state.storageKeys.includes(value)) state.storageKeys.push(value);
    }

    function decodeStoredValue(value) {
        if (value === "") return null;
        if (typeof value !== "string") return value;
        try {
            const envelope = JSON.parse(value);
            if (envelope && envelope.__forwardStorage === 1) {
                if (envelope.expiresAt && Date.now() >= envelope.expiresAt) return null;
                return envelope.value;
            }
        } catch (e) {
        }
        return value;
    }

    function encodeStoredValue(value, ttl) {
        if (value === undefined) value = null;
        const seconds = Number(ttl) || 0;
        return JSON.stringify({
            __forwardStorage: 1,
            expiresAt: seconds > 0 ? Date.now() + seconds * 1000 : 0,
            value,
        });
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
                    const token = ext.tmdbBearer || ext.tmdbToken || ext.TMDB_BEARER_TOKEN || "";
                    const apiKey = ext.tmdbApiKey || ext.TMDB_API_KEY || DEFAULT_TMDB_KEY || "";
                    if (token) headers.Authorization = String(token).startsWith("Bearer ") ? token : "Bearer " + token;
                    const params = Object.assign({}, options.params || {});
                    if (!token && apiKey && params.api_key == null) params.api_key = apiKey;
                    const resp = await globalThis.Widget.http.get("https://api.themoviedb.org/3/" + String(url).replace(/^\/+/, ""), Object.assign({}, options, { headers, params }));
                    return resp.data;
                },
            },
            html: {
                load(html, options, isDocument) {
                    const cheerio = globalThis.__FORWARD_CHEERIO__;
                    if (!cheerio || typeof cheerio.load !== "function") throw new Error("cheerio is not available");
                    return cheerio.load(String(html == null ? "" : html), options, isDocument);
                },
            },
            storage: {
                get(key) {
                    return decodeStoredValue(local.get("forward", storageKey(key)));
                },
                set(key, value, ttl) {
                    rememberStorageKey(key);
                    local.set("forward", storageKey(key), encodeStoredValue(value, ttl));
                },
                remove(key) {
                    local.delete("forward", storageKey(key));
                },
                keys() {
                    return state.storageKeys.slice();
                },
                clear() {
                    for (const key of state.storageKeys) local.delete("forward", storageKey(key));
                    state.storageKeys = [];
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

    function captureRuntime(entry) {
        entry.runtime = {
            loadDetail: typeof globalThis.loadDetail === "function" ? globalThis.loadDetail : null,
        };
        const modules = (entry.metadata && entry.metadata.modules) || [];
        for (const module of modules) {
            if (module && module.functionName && typeof globalThis[module.functionName] === "function") {
                entry.runtime[module.functionName] = globalThis[module.functionName];
            }
        }
        const search = entry.metadata && entry.metadata.search;
        if (search && search.functionName && typeof globalThis[search.functionName] === "function") {
            entry.runtime[search.functionName] = globalThis[search.functionName];
        }
    }

    function exposeWidgetRuntime(entry) {
        const runtime = (entry && entry.runtime) || {};
        globalThis.loadDetail = runtime.loadDetail || undefined;
        const modules = (entry && entry.metadata && entry.metadata.modules) || [];
        for (const module of modules) {
            if (module && module.functionName && typeof runtime[module.functionName] === "function") {
                globalThis[module.functionName] = runtime[module.functionName];
            }
        }
        const search = entry && entry.metadata && entry.metadata.search;
        if (search && search.functionName && typeof runtime[search.functionName] === "function") {
            globalThis[search.functionName] = runtime[search.functionName];
        }
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
        captureRuntime(entry);
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
        return module && ["", "video", "list"].includes(String(module.type || "").toLowerCase()) && module.functionName && module.requiresWebView !== true;
    }

    function isStreamModule(module) {
        return module && String(module.type || "").toLowerCase() === "stream" && module.functionName && module.requiresWebView !== true;
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
            moduleType: String(module.type || "").toLowerCase(),
        };
    }

    function findEntry(widgetId, url) {
        return state.entries.find((entry) => entry.id === widgetId || sourceUrl(entry) === url) || null;
    }

    async function activateSpec(spec) {
        await ensureEntries();
        const entry = findEntry(spec.widgetId, spec.url);
        if (!entry) throw new Error("Forward widget not found: " + (spec.widgetId || spec.url));
        if (!entry.metadata || !entry.runtime) await loadEntry(entry);
        state.activeEntry = entry;
        exposeWidgetRuntime(entry);
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

    function imageFromItem(item, key, fallbackKey) {
        return normalizeImage(item && (item[key] || item[fallbackKey]), key === "backdropPath" ? "w780" : "w500");
    }

    function itemTitle(item, index) {
        return sanitizeName((item && (item.title || item.name || item.episodeName || item.id)) || String(index + 1).padStart(2, "0"));
    }

    function sanitizeName(value) {
        return String(value == null ? "" : value).replace(/[$#]/g, " ").trim();
    }

    function itemRemark(item) {
        if (!item) return "";
        return item.subTitle || item.rating || item.releaseDate || item.durationText || item.genreTitle || item.description || "";
    }

    function vodFromItem(item, spec) {
        item = item || {};
        return {
            vod_id: encodePayload({ spec, item }),
            vod_name: item.title || item.name || item.id || "",
            vod_pic: imageFromItem(item, "posterPath", "coverUrl") || imageFromItem(item, "backdropPath", "backdropUrl"),
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
        if (Array.isArray(item.episodeItems) && item.episodeItems.length) return item.episodeItems.filter(Boolean);
        if (Array.isArray(item.episodes) && item.episodes.length) return item.episodes.filter(Boolean);
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

    async function loadPlayable(item, params) {
        if (item && item.videoUrl) return item;
        const link = item && (item.link || item.url || item.id);
        const entry = state.activeEntry;
        const loadDetail = entry && entry.runtime && entry.runtime.loadDetail;
        if (!link || typeof loadDetail !== "function") return item || {};
        let detail = await loadDetail.call(globalThis, params || link);
        if (Array.isArray(detail)) detail = detail[0] || {};
        return Object.assign({}, item || {}, detail || {});
    }

    async function moduleFunction(spec) {
        const entry = await activateSpec(spec);
        const func = entry.runtime && entry.runtime[spec.functionName];
        if (typeof func !== "function") throw new Error("Forward function not found: " + spec.functionName);
        return func;
    }

    function streamItem(item, seriesName) {
        item = Object.assign({}, item || {});
        const source = item.name || "";
        item.title = seriesName || item.title || source;
        item.subTitle = item.subTitle || [source, item.description].filter(Boolean).join(" · ");
        item.videoUrl = item.videoUrl || item.url || "";
        return item;
    }

    async function searchStream(entry, module, key, pg) {
        const spec = specFor(entry, module);
        const base = buildParams(spec, moduleParams(entry, module), pg || "1", {}, key);
        const ext = state.ext || {};
        const configuredType = ext.streamType || ext.mediaType || "";
        const types = configuredType ? [configuredType] : ["tv", "movie"];
        const func = await moduleFunction(spec);
        for (const type of types) {
            const params = Object.assign({}, base, {
                seriesName: key,
                title: key,
                type,
                season: ext.streamSeason || ext.season || "",
                episode: ext.streamEpisode || ext.episode || "",
            });
            const items = normalizeItems(await func.call(globalThis, params));
            if (items.length) return items.map((item) => vodFromItem(streamItem(item, key), spec));
        }
        return [];
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
            const detailParams = Object.assign({}, state.ext || {}, base.params || {}, base, {
                id: base.id,
                tmdbId: base.tmdbId || (base.type === "tmdb" ? base.id : undefined),
                seriesName: base.seriesName || base.title || base.name || "",
                title: base.title || base.name || "",
                type: base.mediaType || base.type || "",
            });
            const detail = await loadPlayable(base, detailParams);
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
                if (search && search.functionName && search.requiresWebView !== true) {
                    const spec = {
                        widgetId: entry.id,
                        widgetTitle: (entry.metadata && entry.metadata.title) || entry.title || entry.id,
                        url: sourceUrl(entry),
                        functionName: search.functionName,
                        moduleId: "search",
                        moduleTitle: search.title || "Search",
                    };
                    try {
                        const params = buildParams(spec, moduleParams(entry, search), pg || "1", {}, key);
                        const func = await moduleFunction(spec);
                        normalizeItems(await func.call(globalThis, params)).forEach((item) => list.push(vodFromItem(item, spec)));
                    } catch (e) {
                        console.error("forward search failed", entry.id, e && e.message ? e.message : e);
                    }
                }
                for (const module of ((entry.metadata || {}).modules || []).filter(isStreamModule)) {
                    try {
                        list.push.apply(list, await searchStream(entry, module, key, pg));
                    } catch (e) {
                        console.error("forward stream search failed", entry.id, e && e.message ? e.message : e);
                    }
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
            state.activeEntry = null;
        },
    };
})();
