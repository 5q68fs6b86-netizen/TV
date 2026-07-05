let req = (url, options) => http(url, Object.assign({
    async: false
}, options));

function http(url, options = {}) {
    if (options?.async === false) return _http(url, options)
    return new Promise(resolve => _http(url, Object.assign({
        complete: res => resolve(res)
    }, options))).catch(err => {
        console.error(err.name, err.message, err.stack)
        return {
            ok: false,
            status: 500,
            url
        }
    })
}

function defineGlobalAlias(name) {
    const descriptor = Object.getOwnPropertyDescriptor(globalThis, name);
    if (descriptor && !descriptor.configurable) return;
    Object.defineProperty(globalThis, name, {
        enumerable: true,
        configurable: true,
        get() {
            return globalThis;
        },
        set() {}
    });
}

['global', 'window', 'self'].forEach(defineGlobalAlias);

globalThis.http = globalThis.http || http;
globalThis.req = globalThis.req || req;

if (typeof globalThis.TextEncoder !== 'function') {
    globalThis.TextEncoder = class TextEncoder {
        encode(input = '') {
            const text = String(input);
            const bytes = [];
            for (let i = 0; i < text.length; i++) {
                let point = text.charCodeAt(i);
                if (point >= 0xd800 && point <= 0xdbff && i + 1 < text.length) {
                    const next = text.charCodeAt(i + 1);
                    if (next >= 0xdc00 && next <= 0xdfff) {
                        point = 0x10000 + ((point - 0xd800) << 10) + next - 0xdc00;
                        i++;
                    }
                }
                if (point < 0x80) {
                    bytes.push(point);
                } else if (point < 0x800) {
                    bytes.push(0xc0 | point >> 6, 0x80 | point & 0x3f);
                } else if (point < 0x10000) {
                    bytes.push(0xe0 | point >> 12, 0x80 | point >> 6 & 0x3f, 0x80 | point & 0x3f);
                } else {
                    bytes.push(0xf0 | point >> 18, 0x80 | point >> 12 & 0x3f, 0x80 | point >> 6 & 0x3f, 0x80 | point & 0x3f);
                }
            }
            return new Uint8Array(bytes);
        }
        encodeInto(input, destination) {
            const bytes = this.encode(input);
            const written = Math.min(bytes.length, destination.length);
            destination.set(bytes.subarray(0, written));
            return {
                read: String(input).length,
                written
            };
        }
    };
}

if (typeof globalThis.TextDecoder !== 'function') {
    globalThis.TextDecoder = class TextDecoder {
        decode(input = new Uint8Array()) {
            const bytes = input instanceof Uint8Array ? input : new Uint8Array(input);
            let output = '';
            for (let i = 0; i < bytes.length;) {
                const b1 = bytes[i++];
                if (b1 < 0x80) {
                    output += String.fromCharCode(b1);
                } else if (b1 >= 0xc0 && b1 < 0xe0 && i < bytes.length) {
                    const b2 = bytes[i++] & 0x3f;
                    output += String.fromCharCode((b1 & 0x1f) << 6 | b2);
                } else if (b1 >= 0xe0 && b1 < 0xf0 && i + 1 < bytes.length) {
                    const b2 = bytes[i++] & 0x3f;
                    const b3 = bytes[i++] & 0x3f;
                    output += String.fromCharCode((b1 & 0x0f) << 12 | b2 << 6 | b3);
                } else if (b1 >= 0xf0 && b1 < 0xf8 && i + 2 < bytes.length) {
                    const b2 = bytes[i++] & 0x3f;
                    const b3 = bytes[i++] & 0x3f;
                    const b4 = bytes[i++] & 0x3f;
                    let point = (b1 & 0x07) << 18 | b2 << 12 | b3 << 6 | b4;
                    point -= 0x10000;
                    output += String.fromCharCode(0xd800 | point >> 10, 0xdc00 | point & 0x3ff);
                } else {
                    output += '\ufffd';
                }
            }
            return output;
        }
    };
}

function createStorage(scope) {
    function store() {
        return globalThis.local;
    }
    return {
        getItem(key) {
            const local = store();
            if (!local) return null;
            const value = local.get(scope, String(key));
            return value === '' ? null : value;
        },
        setItem(key, value) {
            const local = store();
            if (local) local.set(scope, String(key), String(value ?? ''));
        },
        removeItem(key) {
            const local = store();
            if (local) local.delete(scope, String(key));
        },
        clear() {}
    };
}

globalThis.storage0 = globalThis.storage0 || createStorage('storage0');
globalThis.localStorage = globalThis.localStorage || createStorage('localStorage');
