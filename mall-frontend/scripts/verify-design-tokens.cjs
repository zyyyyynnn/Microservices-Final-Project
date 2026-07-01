const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const src = path.join(root, 'src');
const tokenFile = path.join(src, 'styles', 'tokens.css');

const errors = [];
const legacyColorDeclarations = new Map([
  ['src/styles/app.css:background: rgba(255, 255, 255, 0.96);', 1],
  ['src/styles/app.css:background: rgba(255, 255, 255, 0.94);', 1],
  ['src/styles/app.css:background: #fdfdfd;', 1],
  ['src/styles/app.css:background: #fff;', 1],
]);

function walk(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  return entries.flatMap((entry) => {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) return walk(full);
    return full;
  });
}

function rel(file) {
  return path.relative(root, file).replace(/\\/g, '/');
}

if (!fs.existsSync(tokenFile)) {
  errors.push('src/styles/tokens.css 不存在');
}

const sourceFiles = walk(src).filter((file) => /\.(vue|css|scss|ts)$/.test(file));
const colorPattern = /#[0-9a-fA-F]{3,8}\b|rgba?\(|hsla?\(/;
const rawJsonPattern = /<pre\b|JSON\.stringify\(/;

for (const file of sourceFiles) {
  const relative = rel(file);
  const text = fs.readFileSync(file, 'utf8');
  const lines = text.split(/\r?\n/);

  if (relative !== 'src/styles/tokens.css') {
    lines.forEach((line, index) => {
      const legacyKey = `${relative}:${line.trim()}`;
      if (colorPattern.test(line)) {
        const remaining = legacyColorDeclarations.get(legacyKey) || 0;
        if (remaining > 0) {
          legacyColorDeclarations.set(legacyKey, remaining - 1);
        } else {
          errors.push(`${relative}:${index + 1} 禁止在业务源码中新增裸 hex/rgb/hsl：${line.trim()}`);
        }
      }
    });
  }

  if (!relative.endsWith('/stores/auth.ts')) {
    lines.forEach((line, index) => {
      if (rawJsonPattern.test(line)) {
        errors.push(`${relative}:${index + 1} 禁止 raw JSON / JSON.stringify 作为主要业务页面展示：${line.trim()}`);
      }
    });
  }
}

if (fs.existsSync(tokenFile)) {
  const tokenText = fs.readFileSync(tokenFile, 'utf8');
  ['--color-brand', '--color-bg-page', '--color-text-primary', '--color-border', '--shadow-md'].forEach((token) => {
    if (!tokenText.includes(token)) {
      errors.push(`src/styles/tokens.css 缺少关键 token：${token}`);
    }
  });
}

if (errors.length > 0) {
  console.error(errors.join('\n'));
  process.exit(1);
}

console.log(`Design token verification passed (${sourceFiles.length} files checked).`);
