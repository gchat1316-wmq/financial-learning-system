import { pool } from "../src/lib/db.js";
import { ensureContentSchema } from "../src/lib/content-schema.js";
import { createWriteStream } from "fs";
import { mkdir } from "fs/promises";
import { dirname } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const SUBJECT = "chinese";
const SUBJECT_DIR = `point_files/${SUBJECT}`;

const apiKey = process.argv[2] || process.env.SENSENOVA_API_KEY;
const workerIndex = parseInt(process.argv[3], 10);
const totalWorkers = parseInt(process.argv[4], 10);

if (!apiKey) {
  console.error("错误: 请提供 SENSENOVA_API_KEY");
  console.error("用法: node scripts/generate-chinese-images.js <API_KEY> [workerIndex] [totalWorkers]");
  process.exit(1);
}

if (isNaN(workerIndex) || isNaN(totalWorkers) || workerIndex < 0 || workerIndex >= totalWorkers) {
  console.error("错误: workerIndex 和 totalWorkers 必须提供且 workerIndex 必须在 [0, totalWorkers) 范围内");
  process.exit(1);
}

const IMAGE_PROMPT_TEMPLATE = `帮我生成一张面向专业投资者的信息图。图片要求：
1. 背景简洁，使用纯色或简单渐变（浅蓝色）
2. 不要有太多装饰元素、动画效果或复杂背景
3. 内容清晰，让投资者能快速理解知识点
4. 苹果风格，保持简洁
5. 知识内容准确无误

知识点内容：{point_summary}`;

async function ensureDir(dir) {
  try {
    await mkdir(dir, { recursive: true });
  } catch (err) {
    if (err.code !== "EEXIST") throw err;
  }
}

function safeFileName(pointKey) {
  return pointKey.replace(/[^a-zA-Z0-9_\-]/g, "_") + ".png";
}

async function generateImageForPoint(prompt) {
  const textResponse = await fetch("https://token.sensenova.cn/v1/chat/completions", {
    method: "POST",
    headers: { Authorization: "Bearer " + apiKey, "Content-Type": "application/json" },
    body: JSON.stringify({ model: "sensenova-6.7-flash-lite", messages: [{ role: "user", content: prompt }] })
  });
  if (!textResponse.ok) {
    const errorText = await textResponse.text();
    throw new Error("文本理解 API 错误: " + textResponse.status + " - " + errorText);
  }
  const textData = await textResponse.json();
  const refinedPrompt = textData.choices?.[0]?.message?.content || prompt;
  const cleanedPrompt = refinedPrompt
    .replace(/毛泽东/g, "名人")
    .replace(/人民币/g, "纸币")
    .replace(/#[A-Fa-f0-9]{6}/g, "")
    .replace(/\s+/g, " ")
    .trim();

  const imageResponse = await fetch("https://token.sensenova.cn/v1/images/generations", {
    method: "POST",
    headers: { Authorization: "Bearer " + apiKey, "Content-Type": "application/json" },
    body: JSON.stringify({ model: "sensenova-u1-fast", prompt: cleanedPrompt, size: "2752x1536", n: 1 })
  });
  if (!imageResponse.ok) {
    const errorText = await imageResponse.text();
    throw new Error("图片生成 API 错误: " + imageResponse.status + " - " + errorText);
  }
  const imageData = await imageResponse.json();
  const imageUrl = imageData.data?.[0]?.url;
  return { imageUrl };
}

async function downloadImage(url, destPath) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 60000);
  try {
    const response = await fetch(url, { signal: controller.signal });
    if (!response.ok) throw new Error("HTTP " + response.status);
    const fileStream = createWriteStream(destPath);
    if (!response.body) throw new Error("No response body");
    for await (const chunk of response.body) {
      fileStream.write(chunk);
    }
    fileStream.end();
    return new Promise((resolve, reject) => {
      fileStream.on("finish", () => resolve(destPath));
      fileStream.on("error", reject);
    });
  } finally {
    clearTimeout(timeout);
  }
}

async function main() {
  const baseDir = __dirname;
  await ensureDir(baseDir + "/" + SUBJECT_DIR);

  await ensureContentSchema(pool);

  const [points] = await pool.query(
    `SELECT id, point_key, point_title, point_summary
     FROM knowledge_points
     WHERE subject = ? AND local_image_path IS NULL
     ORDER BY sort_index, id`,
    [SUBJECT]
  );

  const filteredPoints = points.filter((_, index) => index % totalWorkers === workerIndex);

  console.log(`找到 ${points.length} 个语文知识点，本进程处理 ${filteredPoints.length} 个`);

  let successCount = 0;
  let failCount = 0;

  for (let i = 0; i < filteredPoints.length; i++) {
    const point = filteredPoints[i];
    const prompt = IMAGE_PROMPT_TEMPLATE.replace("{point_title}", point.point_title).replace("{point_summary}", point.point_summary);

    try {
      console.log(`[${i + 1}/${filteredPoints.length}] 生成: ${point.point_key}`);
      const { imageUrl } = await generateImageForPoint(prompt);

      if (!imageUrl) {
        console.error(`  错误: 未获取到图片 URL`);
        failCount++;
        continue;
      }

      const localFileName = safeFileName(point.point_key);
      const localRelativePath = `${SUBJECT}/${localFileName}`;
      const localDestPath = baseDir + "/point_files/" + localRelativePath;

      console.log(`  下载到: ${localRelativePath}`);
      await downloadImage(imageUrl, localDestPath);

      await pool.query(
        `UPDATE knowledge_points SET image_url = ?, local_image_path = ? WHERE point_key = ?`,
        [imageUrl, localRelativePath, point.point_key]
      );

      console.log(`  成功`);
      successCount++;
    } catch (err) {
      console.error(`  失败: ${err.message}`);
      failCount++;
    }

    if (i < filteredPoints.length - 1) {
      await new Promise((r) => setTimeout(r, 2000));
    }
  }

  console.log(`\n----- 完成 -----`);
  console.log(`成功: ${successCount}，失败: ${failCount}`);
  await pool.end();
}

main().catch((err) => {
  console.error(err);
  pool.end();
  process.exit(1);
});
