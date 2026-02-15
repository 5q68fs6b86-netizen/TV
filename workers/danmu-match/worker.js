// CF Worker: 弹幕自动匹配中间层
// 使用 Workers AI 格式化标题/集数，调用 danmu_api match 接口
// 部署后将 Worker URL 硬编码到 Android 端

const DANMU_API_BASE = "https://d2.114514heihei.eu.org/mapiwbhpass";
const AI_MODEL = "@cf/qwen/qwen1.5-14b-chat-awq";

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type",
};

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") {
      return new Response(null, { headers: CORS_HEADERS });
    }

    const url = new URL(request.url);

    if (url.pathname === "/api/danmu/match" && request.method === "POST") {
      return handleMatch(request, env);
    }

    if (url.pathname === "/api/danmu/search" && request.method === "GET") {
      return handleSearch(url, env);
    }

    if (url.pathname.startsWith("/api/danmu/bangumi/") && request.method === "GET") {
      return handleBangumi(url);
    }

    return jsonResponse({ error: "Not Found" }, 404);
  },
};

async function handleMatch(request, env) {
  try {
    const { title, episode } = await request.json();
    if (!title) {
      return jsonResponse({ success: false, error: "title is required" }, 400);
    }

    // 用 AI 格式化 fileName
    const fileName = await formatWithAI(env, title, episode || "");

    // 调用 danmu_api match 接口
    const matchResult = await callMatchAPI(fileName);

    if (!matchResult.isMatched) {
      return jsonResponse({
        success: false,
        error: "no match found",
        fileName,
        matchResult,
      });
    }

    // 拼接弹幕 URL
    const danmuUrl = `${DANMU_API_BASE}/api/v2/comment/${matchResult.episodeId}?format=xml`;

    return jsonResponse({
      success: true,
      danmuUrl,
      fileName,
      matchInfo: {
        animeTitle: matchResult.animeTitle || "",
        episodeTitle: matchResult.episodeTitle || "",
        episodeId: matchResult.episodeId,
      },
    });
  } catch (e) {
    return jsonResponse({ success: false, error: e.message }, 500);
  }
}

async function handleSearch(url) {
  try {
    const keyword = url.searchParams.get("keyword");
    if (!keyword) {
      return jsonResponse({ success: false, error: "keyword is required" }, 400);
    }

    const resp = await fetch(
      `${DANMU_API_BASE}/api/v2/search/anime?keyword=${encodeURIComponent(keyword)}`
    );
    const data = await resp.json();

    return jsonResponse({ success: true, data });
  } catch (e) {
    return jsonResponse({ success: false, error: e.message }, 500);
  }
}

async function handleBangumi(url) {
  try {
    const animeId = url.pathname.split("/").pop();
    if (!animeId) {
      return jsonResponse({ success: false, error: "animeId is required" }, 400);
    }

    const resp = await fetch(`${DANMU_API_BASE}/api/v2/bangumi/${animeId}`);
    const data = await resp.json();

    return jsonResponse({ success: true, data });
  } catch (e) {
    return jsonResponse({ success: false, error: e.message }, 500);
  }
}

async function formatWithAI(env, title, episode) {
  // 快速预处理：如果标题和集数已经很规范，跳过 AI
  const quickResult = quickFormat(title, episode);
  if (quickResult) return quickResult;

  try {
    const prompt = `你是一个视频文件名格式化工具。将输入的视频标题和集数信息格式化为弹幕搜索用的标准文件名。

规则：
1. 提取纯净的中文/英文标题，去除多余的修饰词（如"高清"、"4K"、"蓝光"等）
2. 将集数转换为 SxxExx 格式（如果有季数）或 Exx 格式（如果没有季数）
3. 如果集数信息不明确，保持原样
4. 输出格式：标题 S01E03 或 标题 E03

示例：
输入：标题="庆余年 第二季" 集数="第3集"  输出：庆余年 S02E03
输入：标题="斗破苍穹" 集数="EP05"  输出：斗破苍穹 E05
输入：标题="The Last of Us" 集数="S01E04"  输出：The Last of Us S01E04
输入：标题="进击的巨人 最终季" 集数="12"  输出：进击的巨人 最终季 E12
输入：标题="我的阿勒泰" 集数="第1集"  输出：我的阿勒泰 E01

现在请格式化：
标题="${title}" 集数="${episode}"

只输出格式化后的文件名，不要其他内容。`;

    const resp = await env.AI.run(AI_MODEL, {
      messages: [{ role: "user", content: prompt }],
      max_tokens: 100,
    });

    const result = (resp.response || "").trim();
    if (result && result.length < 200) {
      return result;
    }
  } catch (e) {
    console.error("AI format failed:", e.message);
  }

  // AI 失败时 fallback
  return fallbackFormat(title, episode);
}

function quickFormat(title, episode) {
  if (!episode) return null;

  // 已经是 SxxExx 格式
  const sxxexx = episode.match(/[Ss](\d{1,2})[Ee](\d{1,3})/);
  if (sxxexx) return `${title.trim()} ${episode.trim()}`;

  // 已经是 Exx 格式
  const exx = episode.match(/^[Ee][Pp]?(\d{1,3})$/);
  if (exx) return `${title.trim()} E${exx[1].padStart(2, "0")}`;

  return null;
}

function fallbackFormat(title, episode) {
  let cleanTitle = title.trim();
  let formatted = cleanTitle;

  if (episode) {
    let ep = episode.trim();
    // 中文"第X集" → Exx
    const zhMatch = ep.match(/第(\d+)[集话話期]/);
    if (zhMatch) {
      formatted += ` E${zhMatch[1].padStart(2, "0")}`;
    } else {
      // 纯数字
      const numMatch = ep.match(/^(\d+)$/);
      if (numMatch) {
        formatted += ` E${numMatch[1].padStart(2, "0")}`;
      } else {
        formatted += ` ${ep}`;
      }
    }
  }

  return formatted;
}

async function callMatchAPI(fileName) {
  const resp = await fetch(`${DANMU_API_BASE}/api/v2/match`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ fileName }),
  });

  const data = await resp.json();

  if (data.isMatched && data.matches && data.matches.length > 0) {
    const match = data.matches[0];
    return {
      isMatched: true,
      animeTitle: match.animeTitle || "",
      episodeTitle: match.episodeTitle || "",
      episodeId: match.episodeId,
    };
  }

  return { isMatched: false };
}

function jsonResponse(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "Content-Type": "application/json",
      ...CORS_HEADERS,
    },
  });
}
