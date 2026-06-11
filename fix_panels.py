import re

with open('mall-frontend/src/views/HomeView.vue', 'r', encoding='utf-8') as f:
    home_vue = f.read()

# 1. Flow Panel
home_vue = re.sub(
    r'<div class="flow-panel">\s*<div class="panel-header">\s*<div class="ph-left">\s*<h3>交易链路</h3>\s*<span class="ph-tag">\(演示流程\)</span>\s*</div>\s*<p class="ph-desc">完整电商交易闭环体验</p>\s*</div>',
    r'''<el-card class="panel flow-panel" shadow="never">
          <template #header>
            <div class="panel-title" style="display: flex; justify-content: space-between; align-items: center;">
              <div style="display: flex; align-items: center; gap: 8px;">
                <span style="font-size: 16px; font-weight: bold; color: var(--color-text-primary);">交易链路</span>
                <span style="font-size: 12px; color: var(--color-brand); background: var(--color-brand-light); padding: 2px 8px; border-radius: 12px;">(演示流程)</span>
              </div>
              <span style="font-size: 13px; color: #666; font-weight: normal;">完整电商交易闭环体验</span>
            </div>
          </template>''',
    home_vue, count=1
)
# Close flow panel (it closes before recommend section)
home_vue = home_vue.replace('        </div>\n      </div>\n\n      <!-- Recommendation Section -->', '        </el-card>\n      </div>\n\n      <!-- Recommendation Section -->')

# 2. Recommend Section
home_vue = re.sub(
    r'<div class="recommend-section">\s*<div class="section-header">\s*<h2 class="section-title">为你推荐</h2>\s*</div>',
    r'''<el-card class="panel recommend-section" shadow="never">
        <template #header>
          <div class="panel-title" style="font-size: 16px; font-weight: bold; color: var(--color-text-primary);">为你推荐</div>
        </template>''',
    home_vue, count=1
)
# Close recommend section
home_vue = home_vue.replace('        </div>\n      </div>\n\n      <!-- Flash Sale Section -->', '        </div>\n      </el-card>\n\n      <!-- Flash Sale Section -->')

# 3. Seckill Section
home_vue = re.sub(
    r'<div class="seckill-section">\s*<div class="seckill-header">\s*<div class="sk-title">\s*<h2>限时秒杀</h2>\s*<span>每日优选好货</span>\s*</div>\s*<RouterLink to="/seckill" class="more-link" style="margin-left: auto;">更多秒杀 <el-icon><ArrowRight /></el-icon></RouterLink>\s*</div>',
    r'''<el-card class="panel seckill-section" shadow="never">
        <template #header>
          <div class="panel-title" style="display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; align-items: baseline; gap: 8px;">
              <span style="font-size: 16px; font-weight: bold; color: var(--color-text-primary);">限时秒杀</span>
              <span style="font-size: 13px; color: #999; font-weight: normal;">每日优选好货</span>
            </div>
            <RouterLink to="/seckill" class="more-link" style="font-size: 14px; font-weight: normal;">更多秒杀 <el-icon><ArrowRight /></el-icon></RouterLink>
          </div>
        </template>''',
    home_vue, count=1
)
# Close seckill section
home_vue = home_vue.replace('        </div>\n      </div>\n\n      <!-- Tech Dashboard Section -->', '        </div>\n      </el-card>\n\n      <!-- Tech Dashboard Section -->')

# 4. Tech Section
home_vue = re.sub(
    r'<div class="tech-section">\s*<div class="tech-header">\s*<h2>演示工具 <span>\(微服务治理与中间件\)</span></h2>\s*<RouterLink to="/tech" class="more-link" style="margin-left: auto;">进入控制台 <el-icon><ArrowRight /></el-icon></RouterLink>\s*</div>',
    r'''<el-card class="panel tech-section" shadow="never">
        <template #header>
          <div class="panel-title" style="display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; align-items: baseline; gap: 8px;">
              <span style="font-size: 16px; font-weight: bold; color: var(--color-text-primary);">演示工具</span>
              <span style="font-size: 13px; color: #999; font-weight: normal;">(微服务治理与中间件)</span>
            </div>
            <RouterLink to="/tech" class="more-link" style="font-size: 14px; font-weight: normal;">进入控制台 <el-icon><ArrowRight /></el-icon></RouterLink>
          </div>
        </template>''',
    home_vue, count=1
)
# Close tech section
home_vue = home_vue.replace('        </div>\n      </div>\n\n      <!-- Bottom Promises -->', '        </div>\n      </el-card>\n\n      <!-- Bottom Promises -->')

# Clean up CSS inside HomeView.vue that is now redundant or conflicts with panel
home_vue = re.sub(r'\.flow-panel\s*\{\s*background: white;\s*border-radius: var\(--radius-xl\);\s*padding: 24px;\s*display: flex;\s*flex-direction: column;\s*justify-content: space-between;\s*\}', 
                  '.flow-panel {\n  display: flex;\n  flex-direction: column;\n  justify-content: space-between;\n}', home_vue)

home_vue = re.sub(r'\.seckill-section\s*\{\s*background: white;\s*border-radius: var\(--radius-xl\);\s*padding: 24px;\s*\}', '', home_vue)

# Remove the broken <el-card class="panel mt"> injections from my last botched regex script if they exist anywhere else
# Note: they don't exist anymore because my previous commit amended and the regexes in micro_adjust didn't match the new HTML structure! Wait, did they match? No, they didn't match `recommend-section`. 

with open('mall-frontend/src/views/HomeView.vue', 'w', encoding='utf-8') as f:
    f.write(home_vue)

# 5. Fix app.css for .panel
with open('mall-frontend/src/styles/app.css', 'r', encoding='utf-8') as f:
    app_css = f.read()

panel_css = """
.panel {
  border: 1px solid var(--color-border) !important;
  border-radius: var(--radius-lg) !important;
  background: var(--color-surface) !important;
  box-shadow: var(--shadow-sm) !important;
  overflow: hidden;
}
.panel .el-card__header {
  padding: 18px 24px !important;
  border-bottom: 1px solid var(--color-border) !important;
  background: #fdfdfd;
}
.panel .el-card__body {
  padding: 24px !important;
}
"""
if '.panel {' not in app_css:
    app_css += panel_css
else:
    # Just to be safe, append it anyway or replace.
    app_css += panel_css

with open('mall-frontend/src/styles/app.css', 'w', encoding='utf-8') as f:
    f.write(app_css)
