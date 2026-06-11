import os

views_dir = r"E:\微服务开发\Microservices-Final-Project\mall-frontend\src\views"

for root, _, files in os.walk(views_dir):
    for f in files:
        if not f.endswith(".vue"): continue
        path = os.path.join(root, f)
        with open(path, "r", encoding="utf-8") as file:
            content = file.read()
        
        # Replace :error="\'\'" with :error="''"
        new_content = content.replace(r":error=\"\'\'\"", ":error=\"''\"")
        
        if new_content != content:
            with open(path, "w", encoding="utf-8") as file:
                file.write(new_content)
            print(f"Fixed {f}")
