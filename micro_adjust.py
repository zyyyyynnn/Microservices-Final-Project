import re

# 1. Update app.css
with open('mall-frontend/src/styles/app.css', 'r', encoding='utf-8') as f:
    app_css = f.read()

app_css = app_css.replace('--page-max-width: 1440px;', '--page-max-width: 1360px;')
app_css = app_css.replace('--page-gutter: 40px;', '--page-gutter: 32px;')

# Update product image aspect ratio
app_css = app_css.replace('aspect-ratio: 4 / 3;', 'aspect-ratio: 1 / 1;')

# Update product title
old_product_title = """.product-body h2 {
  min-height: 46px;
  margin: 0;
  font-size: var(--font-lg);
}"""
new_product_title = """.product-body h2 {
  margin: 0;
  font-size: var(--font-lg);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 46px;
}"""
if old_product_title in app_css:
    app_css = app_css.replace(old_product_title, new_product_title)
else:
    app_css += "\n.product-body h2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }"

# Update product desc
old_product_desc = """.product-body p {
  display: -webkit-box;
  min-height: 42px;
  margin: 0;
  overflow: hidden;
  color: var(--color-text-secondary);
  font-size: var(--font-sm);
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}"""
new_product_desc = """.product-body p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-sm);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 20px;
}"""
if old_product_desc in app_css:
    app_css = app_css.replace(old_product_desc, new_product_desc)

with open('mall-frontend/src/styles/app.css', 'w', encoding='utf-8') as f:
    f.write(app_css)


# 2. Update HomeView.vue
with open('mall-frontend/src/views/HomeView.vue', 'r', encoding='utf-8') as f:
    home_vue = f.read()

# Update Hero CSS
home_vue = re.sub(r'\.hero-banner\s*\{[\s\S]*?\}', """.hero-banner {
  display: flex;
  align-items: center;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  min-height: 310px;
  padding: 48px 44px;
}
@media (max-width: 768px) {
  .hero-banner {
    min-height: 420px;
    background-position: center bottom !important;
    padding: 32px 24px;
  }
}
""", home_vue, count=1)

# Convert section blocks to el-card panels
home_vue = re.sub(r'<div class="section-block mt">\s*<div class="section-heading">\s*<strong>(.*?)</strong>\s*<span>(.*?)</span>\s*</div>',
                  r'<el-card class="panel mt">\n        <template #header>\n          <div class="panel-title">\n            <strong>\1</strong> <span style="font-size:12px;color:#999;font-weight:normal;">\2</span>\n          </div>\n        </template>', home_vue)

home_vue = re.sub(r'<div class="section-block">\s*<div class="section-heading">\s*<strong>(.*?)</strong>\s*<span>(.*?)</span>\s*</div>',
                  r'<el-card class="panel">\n        <template #header>\n          <div class="panel-title">\n            <strong>\1</strong> <span style="font-size:12px;color:#999;font-weight:normal;">\2</span>\n          </div>\n        </template>', home_vue)

# Close el-cards instead of divs
home_vue = home_vue.replace('<!-- Today Recommendations -->', '<!-- Today Recommendations -->') # Marker
# Replace closing divs for those sections with closing el-cards
# Since it's hard to precisely replace closing divs with regex without breaking HTML, I will manually patch HomeView.vue component blocks where needed or do a simple replace.
# Actually, if I just replace `<div class="section-block">` with `<el-card class="panel">` and `</div>` with `</el-card>` it's risky. Let's do it precisely.

home_vue = home_vue.replace('<div class="category-grid">', '<div class="category-grid" style="border-top:none;">')
home_vue = home_vue.replace('</div>\n      </el-card>', '</div>\n      </el-card>') # Safe check

# Remove fake statuses in Tech panel
home_vue = home_vue.replace('<span>状态：<strong class="text-success">运行中</strong></span>', '<span>微服务网关入口</span>')
home_vue = home_vue.replace('<span>注册实例：<strong>32 个</strong></span>', '<span>服务注册与配置中心</span>')
home_vue = home_vue.replace('<span>全局事务成功率：<strong class="text-success">99.96%</strong></span>', '<span>分布式事务协调</span>')
home_vue = home_vue.replace('<span>积压消息：<strong class="text-brand">0</strong></span>', '<span>异步消息与解耦</span>')
home_vue = home_vue.replace('<span>QPS：<strong>2450</strong></span>', '<span>聚合搜索与分析</span>')
home_vue = home_vue.replace('<span>当前状态：<strong class="text-success">正常</strong></span>', '<span>限流与熔断保护</span>')

# It's safer to just replace all `</div>` that match the end of these blocks. 
# A cleaner way is to keep them as `div` but apply `.panel` class to `.section-block` directly in app.css so we don't have to touch HTML tags!
# Let's revert the HTML tags change and do it via CSS.

with open('mall-frontend/src/styles/app.css', 'r', encoding='utf-8') as f:
    app_css = f.read()

# Make section-block look exactly like el-card .panel
app_css = app_css.replace('.section-block {\n  overflow: hidden;\n}', '.section-block {\n  border: 1px solid var(--color-border);\n  border-radius: var(--radius-lg);\n  background: var(--color-surface);\n  box-shadow: var(--shadow-sm);\n  overflow: hidden;\n}')
app_css = app_css.replace('.section-block > .section-heading {\n  margin: 0;\n  padding: var(--spacing-md) var(--spacing-lg);\n  border-bottom: 1px solid var(--color-border);\n  background: var(--color-surface);\n}', '.section-block > .section-heading {\n  margin: 0;\n  padding: 18px 20px;\n  border-bottom: 1px solid var(--el-card-border-color);\n  background: var(--color-surface);\n  font-weight: bold;\n  font-size: 16px;\n}')

with open('mall-frontend/src/styles/app.css', 'w', encoding='utf-8') as f:
    f.write(app_css)

# Re-read home_vue without the el-card HTML replacements
with open('mall-frontend/src/views/HomeView.vue', 'r', encoding='utf-8') as f:
    home_vue = f.read()

# Redo Hero CSS and tech statuses
home_vue = re.sub(r'\.hero-banner\s*\{[\s\S]*?\}\s*\.hero-content\s*\{[\s\S]*?\}', """.hero-banner {
  display: flex;
  align-items: center;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  min-height: 310px;
  padding: 48px 44px;
}
.hero-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}
@media (max-width: 768px) {
  .hero-banner {
    min-height: 420px;
    background-position: center bottom !important;
    padding: 32px 24px;
  }
}
""", home_vue, count=1)

home_vue = home_vue.replace('<span>状态：<strong class="text-success">运行中</strong></span>', '<span>微服务网关入口</span>')
home_vue = home_vue.replace('<span>注册实例：<strong>32 个</strong></span>', '<span>服务注册与配置中心</span>')
home_vue = home_vue.replace('<span>全局事务成功率：<strong class="text-success">99.96%</strong></span>', '<span>分布式事务协调</span>')
home_vue = home_vue.replace('<span>积压消息：<strong class="text-brand">0</strong></span>', '<span>异步消息与解耦</span>')
home_vue = home_vue.replace('<span>QPS：<strong>2450</strong></span>', '<span>聚合搜索与分析</span>')
home_vue = home_vue.replace('<span>当前状态：<strong class="text-success">正常</strong></span>', '<span>限流与熔断保护</span>')

with open('mall-frontend/src/views/HomeView.vue', 'w', encoding='utf-8') as f:
    f.write(home_vue)

# 3. Update SearchView.vue
with open('mall-frontend/src/views/SearchView.vue', 'r', encoding='utf-8') as f:
    search_vue = f.read()

# Remove the search input box but keep hot words
search_vue = re.sub(r'<div class="search-row large-search">[\s\S]*?</el-input>\s*</div>', '', search_vue)

# Add watch to update search when URL query changes (because header search box is used)
if 'watch(' not in search_vue:
    search_vue = search_vue.replace('import { computed, onMounted, ref } from \'vue\';', 'import { computed, onMounted, ref, watch } from \'vue\';')
    search_vue = search_vue.replace('onMounted(() => {', """watch(() => route.query.keyword, (newVal) => {
  if (newVal !== undefined) {
    keyword.value = String(newVal);
    pageNum.value = 1;
    search();
  }
});

onMounted(() => {""")

with open('mall-frontend/src/views/SearchView.vue', 'w', encoding='utf-8') as f:
    f.write(search_vue)

# 4. Update docs
# DESIGN.md
with open('DESIGN.md', 'r', encoding='utf-8') as f:
    design_md = f.read()
design_md = design_md.replace('- 首页采用居中内容画布，桌面端最大宽度控制在 1440～1480px；', '- 首页采用居中内容画布，桌面端最大宽度控制在 1360～1440px；\n- Hero 背景图为首页主视觉基线，后续布局修复不得替换该背景图，也不得改变 background-image 实现方式。\n- 商品图允许外链，但必须具备 ProductImage fallback。')
with open('DESIGN.md', 'w', encoding='utf-8') as f:
    f.write(design_md)

# FINAL_REPORT.md
with open('docs/FINAL_REPORT.md', 'r', encoding='utf-8') as f:
    final_md = f.read()
final_md = re.sub(r'前端 UI 正在进行视觉质量收口；首页画布宽度与 Hero 视觉仍在修正，真实成功态截图待完成。，真实成功态截图仍待采集。', '前端 UI 正在进行视觉质量收口；Hero 背景图与首页画布方向已确认，细节样式和真实成功态截图待完成。', final_md)
final_md = re.sub(r'前端 UI 正在进行视觉质量收口；首页画布宽度与 Hero 视觉仍在修正，真实成功态截图待完成。', '前端 UI 正在进行视觉质量收口；Hero 背景图与首页画布方向已确认，细节样式和真实成功态截图待完成。', final_md)
with open('docs/FINAL_REPORT.md', 'w', encoding='utf-8') as f:
    f.write(final_md)

# ui-quality-kill-20260611.md
with open('docs/test/frontend/ui-quality-kill-20260611.md', 'r', encoding='utf-8') as f:
    ui_md = f.read()
ui_md = ui_md.replace('- 本轮只修首页宽度和 Hero 视觉回归；', '- 本轮只进行细节样式微调；\n- Hero 背景图锁定不变；\n- 未进行最终成功态截图；\n- 未声明全站视觉最终通过。')
with open('docs/test/frontend/ui-quality-kill-20260611.md', 'w', encoding='utf-8') as f:
    f.write(ui_md)
