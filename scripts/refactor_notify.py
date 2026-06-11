import os
import re

views_dir = r"E:\微服务开发\Microservices-Final-Project\mall-frontend\src\views"

for root, _, files in os.walk(views_dir):
    for f in files:
        if not f.endswith(".vue"): continue
        path = os.path.join(root, f)
        with open(path, "r", encoding="utf-8") as file:
            content = file.read()
            
        original = content
        
        # 1. Remove :error prop passing
        content = re.sub(r':error="[a-zA-Z0-9_]+"', ':error="\'\'"', content)
        
        # 2. Replace error.value = ... with notifyError(...)
        # We need to be careful not to replace `error.value = '';` which is used to clear errors.
        def replacer(match):
            val = match.group(1).strip()
            if val == "''" or val == '""':
                return match.group(0)
            return f"notifyError({val});"

        content = re.sub(r'error\.value\s*=\s*([^;]+);', replacer, content)

        # 3. Import notifyError if we added it
        if "notifyError(" in content and "import { notifyError }" not in content:
            content = content.replace('<script setup lang="ts">', '<script setup lang="ts">\nimport { notifyError } from \'../utils/notify\';')

        if content != original:
            with open(path, "w", encoding="utf-8") as file:
                file.write(content)
            print(f"Updated {f}")
