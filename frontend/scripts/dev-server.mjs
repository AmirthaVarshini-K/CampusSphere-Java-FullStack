import { createServer } from 'node:http';
import { existsSync } from 'node:fs';
import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { buildFrontend } from './build.mjs';

const rootDir = path.dirname(fileURLToPath(import.meta.url));
const projectDir = path.resolve(rootDir, '..');
const distDir = path.join(projectDir, 'dist');
const port = Number(process.env.PORT || 5173);
const previewMode = process.argv.includes('--preview');

function contentType(filePath) {
  if (filePath.endsWith('.html')) return 'text/html; charset=utf-8';
  if (filePath.endsWith('.css')) return 'text/css; charset=utf-8';
  if (filePath.endsWith('.js')) return 'application/javascript; charset=utf-8';
  if (filePath.endsWith('.json')) return 'application/json; charset=utf-8';
  return 'application/octet-stream';
}

async function serve() {
  if (!existsSync(path.join(distDir, 'index.html'))) {
    await buildFrontend();
  }

  if (!existsSync(path.join(distDir, 'index.html'))) {
    throw new Error('Build output not found. Run `npm run build` before starting the dev server.');
  }

  const server = createServer(async (req, res) => {
    const urlPath = new URL(req.url || '/', `http://${req.headers.host}`).pathname;
    const relativePath = urlPath === '/' ? '/index.html' : urlPath;
    const filePath = path.join(distDir, relativePath);

    if (existsSync(filePath) && !filePath.endsWith(path.sep)) {
      const data = await readFile(filePath);
      res.writeHead(200, { 'Content-Type': contentType(filePath) });
      res.end(data);
      return;
    }

    const html = await readFile(path.join(distDir, 'index.html'), 'utf8');
    res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
    res.end(html);
  });

  server.listen(port, () => {
    const mode = previewMode ? 'preview' : 'dev';
    console.log(`CampusSphere frontend ${mode} server running at http://localhost:${port}`);
  });
}

await serve();
