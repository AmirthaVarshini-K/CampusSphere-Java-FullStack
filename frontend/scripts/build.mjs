import { existsSync } from 'node:fs';
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { createRequire } from 'node:module';
import { fileURLToPath, pathToFileURL } from 'node:url';

const require = createRequire(import.meta.url);

const rootDir = path.dirname(fileURLToPath(import.meta.url));
const projectDir = path.resolve(rootDir, '..');
const distDir = path.join(projectDir, 'dist');
const assetsDir = path.join(distDir, 'assets');
const sourceIndexPath = path.join(projectDir, 'index.html');
const outputIndexPath = path.join(distDir, 'index.html');
const virtualRoot = '/project';

function toVirtualPath(realPath) {
  const normalized = path.resolve(realPath);
  const relative = path.relative(projectDir, normalized).split(path.sep).join('/');
  return `${virtualRoot}/${relative}`;
}

function toRealPath(virtualPath) {
  const normalizedVirtualPath = String(virtualPath).replace(/\\/g, '/');
  const comparablePath = normalizedVirtualPath.replace(/^C:\/project/, '/project');
  if (!comparablePath.startsWith(virtualRoot)) {
    throw new Error(`Unsupported virtual path: ${virtualPath}`);
  }

  const relative = comparablePath.slice(virtualRoot.length).replace(/^\/+/, '');
  return path.join(projectDir, relative.split('/').join(path.sep));
}

function loaderForFile(filePath) {
  switch (path.extname(filePath).toLowerCase()) {
    case '.jsx':
      return 'jsx';
    case '.js':
      return 'js';
    case '.css':
      return 'css';
    case '.json':
      return 'json';
    default:
      return 'js';
  }
}

function resolveRealFile(candidatePath) {
  if (existsSync(candidatePath)) {
    return candidatePath;
  }

  const extensions = ['.js', '.jsx', '.css', '.json', '.ts', '.tsx'];
  for (const extension of extensions) {
    const nextPath = `${candidatePath}${extension}`;
    if (existsSync(nextPath)) {
      return nextPath;
    }
  }

  if (existsSync(candidatePath) && path.extname(candidatePath) === '') {
    for (const extension of extensions) {
      const nextPath = path.join(candidatePath, `index${extension}`);
      if (existsSync(nextPath)) {
        return nextPath;
      }
    }
  }

  return candidatePath;
}

async function buildFrontend() {
  globalThis.self ??= globalThis;

  const esbuildModule = await import('esbuild-wasm/lib/browser.js');
  const esbuild = esbuildModule.default ?? esbuildModule;
  const wasmBytes = await readFile(path.join(projectDir, 'node_modules', 'esbuild-wasm', 'esbuild.wasm'));
  const wasmModule = await WebAssembly.compile(wasmBytes);

  await esbuild.initialize({
    wasmModule,
    worker: false
  });

  await mkdir(assetsDir, { recursive: true });

  const entryContents = await readFile(path.join(projectDir, 'src', 'main.jsx'), 'utf8');

  const result = await esbuild.build({
    absWorkingDir: virtualRoot,
    bundle: true,
    format: 'esm',
    platform: 'browser',
    jsx: 'automatic',
    jsxImportSource: 'react',
    target: ['es2020'],
    outdir: `${virtualRoot}/dist/assets`,
    write: false,
    loader: {
      '.css': 'css',
      '.svg': 'file',
      '.png': 'file',
      '.jpg': 'file',
      '.jpeg': 'file',
      '.webp': 'file'
    },
    define: {
      'process.env.NODE_ENV': '"production"'
    },
    logLevel: 'info',
    minify: true,
    sourcemap: false,
    stdin: {
      contents: entryContents,
      resolveDir: virtualRoot,
      sourcefile: `${virtualRoot}/src/main.jsx`,
      loader: 'jsx'
    },
    plugins: [
      {
        name: 'campussphere-fs-loader',
        setup(build) {
          build.onResolve({ filter: /.*/ }, args => {
            if (args.path.startsWith('http:') || args.path.startsWith('https:') || args.path.startsWith('data:')) {
              return { external: true };
            }

            if (args.path === 'axios') {
              const resolved = require.resolve('axios/dist/browser/axios.cjs', { paths: [projectDir] });
              return { path: toVirtualPath(resolved), namespace: 'campussphere-file' };
            }

            if (args.path === 'react') {
              const resolved = path.join(projectDir, 'node_modules', 'react', 'index.js');
              return {
              path: toVirtualPath(resolved),
    namespace: 'campussphere-file'
  };
}

if (args.path === 'react/jsx-runtime') {
  const resolved = path.join(
    projectDir,
    'node_modules',
    'react',
    'jsx-runtime.js'
  );

  return {
    path: toVirtualPath(resolved),
    namespace: 'campussphere-file'
  };
}

if (args.path === 'react/jsx-dev-runtime') {
  const resolved = path.join(
    projectDir,
    'node_modules',
    'react',
    'jsx-dev-runtime.js'
  );

  return {
    path: toVirtualPath(resolved),
    namespace: 'campussphere-file'
  };
}

if (args.path === 'react-dom') {
  const resolved = path.join(
    projectDir,
    'node_modules',
    'react-dom',
    'index.js'
  );

  return {
    path: toVirtualPath(resolved),
    namespace: 'campussphere-file'
  };
}

if (args.path === 'react-dom/client') {
  const resolved = path.join(
    projectDir,
    'node_modules',
    'react-dom',
    'client.js'
  );

  return {
    path: toVirtualPath(resolved),
    namespace: 'campussphere-file'
  };
}

            if (args.path.startsWith('.') || args.path.startsWith('/')) {
              const importerPath = args.importer ? toRealPath(args.importer) : projectDir;
              const resolved = resolveRealFile(path.resolve(path.dirname(importerPath), args.path));
              return { path: toVirtualPath(resolved), namespace: 'campussphere-file' };
            }

            try {
              const resolved = require.resolve(args.path, { paths: [projectDir] });
              return { path: toVirtualPath(resolved), namespace: 'campussphere-file' };
            } catch {
              return { external: true };
            }
          });

          build.onLoad({ filter: /.*/, namespace: 'campussphere-file' }, async args => {
            const realPath = toRealPath(args.path);
            const contents = await readFile(realPath, 'utf8');
            const loader = loaderForFile(realPath);
            const resolveDir = path.posix.dirname(args.path);
            return { contents, loader, resolveDir };
          });
        }
      }
    ]
  });

  for (const file of result.outputFiles ?? []) {
    const baseOutputPath = toRealPath(file.path);
    const outputName = file.path.endsWith('stdin.js')
      ? 'app.js'
      : file.path.endsWith('stdin.css')
        ? 'app.css'
        : path.basename(baseOutputPath);
    const realOutputPath = path.join(path.dirname(baseOutputPath), outputName);
    await mkdir(path.dirname(realOutputPath), { recursive: true });
    await writeFile(realOutputPath, file.contents);
  }

  const html = await readFile(sourceIndexPath, 'utf8');
  const rewritten = html
    .replace('</head>', '    <link rel="stylesheet" href="/assets/app.css" />\n  </head>')
    .replace('<script type="module" src="/src/main.jsx"></script>', '<script type="module" src="/assets/app.js"></script>');

  await writeFile(outputIndexPath, rewritten, 'utf8');
}

export { buildFrontend };

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  await buildFrontend();
}
