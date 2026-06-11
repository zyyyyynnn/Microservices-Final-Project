import os

svg_configs = {
    "1001-iphone-15-pro.svg": {"color": "#4B4B4D", "text": "iPhone 15 Pro\nTitanium", "shape": "rect"},
    "1002-iphone-15-pink.svg": {"color": "#FFC0CB", "text": "iPhone 15\nPink", "shape": "rect"},
    "1003-mi-14-pro.svg": {"color": "#1F1F1F", "text": "Mi 14 Pro\nBlack", "shape": "rect"},
    "1004-huawei-mate-60.svg": {"color": "#8DB6CD", "text": "Mate 60 Pro\nCyan", "shape": "rect"},
    "1005-macbook-air.svg": {"color": "#E5E5EA", "text": "MacBook Air\n13-inch", "shape": "laptop"},
    "1006-logitech-mouse.svg": {"color": "#333333", "text": "MX Master 3S", "shape": "mouse"},
    "1007-ysl-lipstick.svg": {"color": "#A020F0", "text": "YSL Lipstick\n1966", "shape": "lipstick"},
    "1008-loreal-serum.svg": {"color": "#8B0000", "text": "L'Oreal Serum", "shape": "bottle"},
    "1009-coffee-gift-box.svg": {"color": "#6F4E37", "text": "Blue Mountain\nCoffee", "shape": "box"},
    "1010-nuts-gift-box.svg": {"color": "#D2B48C", "text": "Daily Nuts\nBox", "shape": "box"},
    "1011-nike-running-shoes.svg": {"color": "#FF4500", "text": "Nike Pegasus", "shape": "shoe"},
    "1012-yoga-mat.svg": {"color": "#20B2AA", "text": "Yoga Mat", "shape": "cylinder"}
}

def generate_svg(filename, config):
    color = config["color"]
    text_lines = config["text"].split("\n")
    shape = config["shape"]

    # 600x600, 15% margin = 90px. Center area is 420x420 (from 90 to 510).
    # Background
    svg = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600" width="600" height="600">\n'
    svg += f'  <rect width="600" height="600" fill="#F8F9FA"/>\n'
    
    # Shapes
    if shape == "rect":
        # Phone: 200x420, centered
        svg += f'  <rect x="200" y="90" width="200" height="420" rx="30" fill="{color}"/>\n'
    elif shape == "laptop":
        # Laptop screen + base
        svg += f'  <rect x="100" y="150" width="400" height="250" rx="15" fill="{color}"/>\n'
        svg += f'  <path d="M 50 420 L 550 420 Q 560 420 550 430 L 50 430 Q 40 420 50 420 Z" fill="#B0B0B0"/>\n'
    elif shape == "mouse":
        # Mouse: 160x280
        svg += f'  <rect x="220" y="160" width="160" height="280" rx="80" fill="{color}"/>\n'
    elif shape == "lipstick":
        svg += f'  <rect x="260" y="150" width="80" height="120" fill="#FFD700"/>\n' # gold top
        svg += f'  <rect x="260" y="270" width="80" height="180" fill="{color}"/>\n'
    elif shape == "bottle":
        svg += f'  <rect x="270" y="120" width="60" height="60" fill="#CCCCCC"/>\n' # cap
        svg += f'  <rect x="220" y="180" width="160" height="260" rx="20" fill="{color}"/>\n'
    elif shape == "box":
        svg += f'  <rect x="150" y="150" width="300" height="300" rx="10" fill="{color}"/>\n'
    elif shape == "shoe":
        svg += f'  <path d="M 150 350 Q 200 200 450 300 Q 500 350 450 400 L 150 400 Z" fill="{color}"/>\n'
    elif shape == "cylinder":
        # Yoga mat rolled
        svg += f'  <rect x="250" y="100" width="100" height="400" rx="10" fill="{color}"/>\n'
        svg += f'  <ellipse cx="300" cy="100" rx="50" ry="20" fill="#008080"/>\n'
        svg += f'  <ellipse cx="300" cy="500" rx="50" ry="20" fill="#008080"/>\n'
    
    # Text
    y_offset = 300 if shape not in ["laptop", "shoe"] else 275
    y_offset -= (len(text_lines) - 1) * 20
    for i, line in enumerate(text_lines):
        fill_color = "#FFFFFF" if shape in ["rect", "bottle", "box", "mouse", "lipstick", "cylinder"] else "#333333"
        svg += f'  <text x="300" y="{y_offset + i * 40}" font-family="sans-serif" font-size="24" font-weight="bold" fill="{fill_color}" text-anchor="middle">{line}</text>\n'

    svg += '</svg>'
    
    out_dir = "E:/微服务开发/Microservices-Final-Project/mall-frontend/public/products"
    os.makedirs(out_dir, exist_ok=True)
    with open(os.path.join(out_dir, filename), "w", encoding="utf-8") as f:
        f.write(svg)

for filename, config in svg_configs.items():
    generate_svg(filename, config)
    print(f"Generated {filename}")
