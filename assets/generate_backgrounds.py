#!/usr/bin/env python3
"""
Generate pixel art parallax backgrounds for Rainbow Climb's 8 biomes.
Each biome gets a 240x400 background with themed pixel art elements.
Style: 16-bit retro, coherent with Pixel Frog sprite aesthetic.
"""

import random
from PIL import Image, ImageDraw

W, H = 240, 400

def lerp_color(c1, c2, t):
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(3))

def gradient(img, top_color, bottom_color):
    draw = ImageDraw.Draw(img)
    for y in range(H):
        t = y / H
        c = lerp_color(top_color, bottom_color, t)
        draw.line([(0, y), (W, y)], fill=c)

def draw_stars(draw, count, color, size=1):
    for _ in range(count):
        x = random.randint(0, W-1)
        y = random.randint(0, H-1)
        draw.rectangle([x, y, x+size, y+size], fill=color)

def draw_clouds(draw, count, color, y_range=(50, 200)):
    for _ in range(count):
        x = random.randint(-20, W)
        y = random.randint(*y_range)
        w = random.randint(20, 50)
        h = random.randint(8, 14)
        # Puffy cloud shape
        draw.ellipse([x, y, x+w, y+h], fill=color)
        draw.ellipse([x+w//4, y-h//2, x+w*3//4, y+h//2], fill=color)
        draw.ellipse([x+w//6, y-h//3, x+w//2, y+h//3], fill=color)

def draw_hills(draw, base_y, color, count=3):
    for i in range(count):
        cx = int(W * (i + 0.5) / count) + random.randint(-30, 30)
        r = random.randint(40, 80)
        draw.ellipse([cx-r, base_y-r//2, cx+r, base_y+r//2], fill=color)
    # Fill below
    draw.rectangle([0, base_y, W, H], fill=color)

def draw_buildings(draw, base_y, color, accent, count=8):
    for i in range(count):
        x = int(W * i / count) + random.randint(-5, 5)
        w = random.randint(15, 30)
        h = random.randint(40, 120)
        draw.rectangle([x, base_y-h, x+w, base_y], fill=color)
        # Windows
        for wy in range(base_y-h+4, base_y-4, 8):
            for wx in range(x+3, x+w-3, 6):
                if random.random() < 0.6:
                    draw.rectangle([wx, wy, wx+3, wy+3], fill=accent)

def draw_stalactites(draw, color, count=12):
    for _ in range(count):
        x = random.randint(0, W)
        h = random.randint(10, 40)
        w = random.randint(3, 8)
        # Triangle pointing down from top
        draw.polygon([(x, 0), (x+w, 0), (x+w//2, h)], fill=color)

def draw_stalagmites(draw, color, count=10):
    for _ in range(count):
        x = random.randint(0, W)
        h = random.randint(15, 50)
        w = random.randint(4, 10)
        draw.polygon([(x, H), (x+w, H), (x+w//2, H-h)], fill=color)

def draw_trees(draw, base_y, trunk_color, leaf_color, count=6):
    for _ in range(count):
        x = random.randint(5, W-5)
        trunk_h = random.randint(15, 30)
        # Trunk
        draw.rectangle([x-1, base_y-trunk_h, x+1, base_y], fill=trunk_color)
        # Foliage (triangles)
        for layer in range(3):
            ly = base_y - trunk_h - layer * 8
            lw = 12 - layer * 3
            draw.polygon([(x-lw, ly), (x+lw, ly), (x, ly-10)], fill=leaf_color)

def draw_crystals(draw, color, glow_color, count=8):
    for _ in range(count):
        x = random.randint(0, W)
        y = random.randint(H//2, H-20)
        h = random.randint(10, 25)
        w = random.randint(4, 8)
        # Crystal shape
        draw.polygon([(x, y), (x+w, y), (x+w//2, y-h)], fill=color)
        # Glow dot
        draw.rectangle([x+w//2-1, y-h+2, x+w//2+1, y-h+4], fill=glow_color)

def draw_lava(draw, base_y, color, bright):
    # Wavy lava surface
    for x in range(0, W, 2):
        wave = int(3 * (1 + random.random()))
        y = base_y - wave
        draw.rectangle([x, y, x+2, H], fill=color)
        if random.random() < 0.3:
            draw.rectangle([x, y-2, x+2, y], fill=bright)

def draw_candy(draw, color1, color2, count=15):
    for _ in range(count):
        x = random.randint(0, W)
        y = random.randint(0, H)
        r = random.randint(3, 8)
        c = color1 if random.random() < 0.5 else color2
        draw.ellipse([x-r, y-r, x+r, y+r], fill=c)

def draw_floating_platforms(draw, color, count=5):
    for _ in range(count):
        x = random.randint(0, W-30)
        y = random.randint(50, H-50)
        w = random.randint(20, 40)
        draw.rectangle([x, y, x+w, y+4], fill=color)


# ============================================================
# BIOME GENERATORS
# ============================================================

def sky_garden():
    img = Image.new("RGB", (W, H))
    draw = ImageDraw.Draw(img)
    gradient(img, (135, 206, 235), (200, 230, 255))
    draw_clouds(draw, 8, (255, 255, 255, 200), y_range=(20, 150))
    draw_clouds(draw, 4, (230, 240, 255), y_range=(60, 200))
    draw_hills(draw, 320, (100, 180, 80))
    draw_hills(draw, 350, (80, 150, 60))
    draw_trees(draw, 320, (80, 60, 40), (50, 160, 50), count=8)
    draw_trees(draw, 350, (60, 45, 30), (40, 130, 40), count=6)
    # Flowers
    for _ in range(20):
        x, y = random.randint(0, W), random.randint(320, H)
        c = random.choice([(255,100,100), (255,200,50), (200,100,255), (255,150,200)])
        draw.rectangle([x, y, x+2, y+2], fill=c)
    return img

def cloud_kingdom():
    img = Image.new("RGB", (W, H))
    draw = ImageDraw.Draw(img)
    gradient(img, (200, 220, 255), (240, 245, 255))
    # Big fluffy clouds
    draw_clouds(draw, 12, (255, 255, 255), y_range=(30, 350))
    draw_clouds(draw, 6, (245, 248, 255), y_range=(50, 300))
    # Golden accents
    draw_stars(draw, 15, (255, 215, 0), size=2)
    # Floating platforms
    draw_floating_platforms(draw, (220, 200, 170), count=4)
    return img

def neon_city():
    img = Image.new("RGB", (W, H))
    draw = ImageDraw.Draw(img)
    gradient(img, (15, 5, 40), (30, 15, 60))
    # Stars
    draw_stars(draw, 40, (150, 150, 200), size=1)
    # City skyline - back layer
    draw_buildings(draw, 350, (20, 15, 50), (0, 255, 230), count=6)
    # Front layer
    draw_buildings(draw, 380, (30, 20, 60), (255, 50, 200), count=8)
    # Neon signs (bright dots)
    for _ in range(25):
        x, y = random.randint(0, W), random.randint(250, 380)
        c = random.choice([(255,0,200), (0,255,230), (255,100,0), (150,0,255)])
        draw.rectangle([x, y, x+3, y+2], fill=c)
    return img

def crystal_cave():
    img = Image.new("RGB", (W, H))
    draw = ImageDraw.Draw(img)
    gradient(img, (10, 20, 50), (15, 30, 60))
    draw_stalactites(draw, (25, 40, 70), count=15)
    draw_stalagmites(draw, (20, 35, 65), count=12)
    draw_crystals(draw, (50, 130, 170), (100, 220, 255), count=10)
    draw_crystals(draw, (40, 100, 140), (80, 200, 230), count=6)
    # Ambient glow particles
    for _ in range(30):
        x, y = random.randint(0, W), random.randint(0, H)
        draw.rectangle([x, y, x+1, y+1], fill=(80, 180, 220))
    return img

def fire_ruins():
    img = Image.new("RGB", (W, H))
    draw = ImageDraw.Draw(img)
    gradient(img, (40, 10, 5), (80, 25, 10))
    # Ruined columns
    for _ in range(5):
        x = random.randint(10, W-20)
        h = random.randint(60, 150)
        w = random.randint(8, 14)
        draw.rectangle([x, H-h, x+w, H], fill=(100, 50, 30))
        # Broken top
        draw.rectangle([x-2, H-h, x+w+2, H-h+4], fill=(110, 60, 35))
    # Embers/sparks
    for _ in range(40):
        x, y = random.randint(0, W), random.randint(0, H)
        c = random.choice([(255,100,0), (255,200,50), (255,50,0)])
        draw.rectangle([x, y, x+1, y+1], fill=c)
    # Lava at bottom
    draw_lava(draw, H-30, (200, 50, 0), (255, 150, 0))
    return img

def candy_land():
    img = Image.new("RGB", (W, H))
    draw = ImageDraw.Draw(img)
    gradient(img, (255, 220, 240), (255, 240, 250))
    # Candy elements
    draw_candy(draw, (255, 150, 200), (200, 255, 200), count=20)
    # Lollipop sticks
    for _ in range(5):
        x = random.randint(20, W-20)
        draw.rectangle([x-1, 200, x+1, H], fill=(200, 150, 100))
        r = random.randint(8, 14)
        c = random.choice([(255,100,150), (150,200,255), (200,255,150), (255,200,100)])
        draw.ellipse([x-r, 200-r, x+r, 200+r], fill=c)
    # Cloud-like cotton candy
    draw_clouds(draw, 6, (255, 200, 220), y_range=(40, 180))
    draw_clouds(draw, 4, (200, 230, 255), y_range=(60, 160))
    # Sprinkles
    for _ in range(50):
        x, y = random.randint(0, W), random.randint(0, H)
        c = random.choice([(255,100,100),(100,255,100),(100,100,255),(255,255,100),(255,100,255)])
        draw.rectangle([x, y, x+1, y+2], fill=c)
    return img

def space_station():
    img = Image.new("RGB", (W, H))
    draw = ImageDraw.Draw(img)
    gradient(img, (2, 2, 15), (5, 5, 25))
    # Stars - many layers
    draw_stars(draw, 80, (200, 200, 220), size=1)
    draw_stars(draw, 20, (255, 255, 255), size=2)
    draw_stars(draw, 5, (100, 150, 255), size=3)
    # Nebula glow
    for _ in range(8):
        x, y = random.randint(0, W), random.randint(0, H)
        r = random.randint(15, 40)
        c = random.choice([(20,10,60), (10,20,50), (30,10,40)])
        draw.ellipse([x-r, y-r, x+r, y+r], fill=c)
    # Station elements
    for _ in range(3):
        x = random.randint(20, W-60)
        y = random.randint(200, 350)
        w, h = random.randint(30, 60), random.randint(15, 25)
        draw.rectangle([x, y, x+w, y+h], fill=(50, 50, 70))
        draw.rectangle([x+2, y+2, x+w-2, y+h-2], fill=(40, 40, 55))
        # Lights
        for lx in range(x+4, x+w-4, 6):
            draw.rectangle([lx, y+h-3, lx+2, y+h-1], fill=(50, 150, 255))
    return img

def haunted_forest():
    img = Image.new("RGB", (W, H))
    draw = ImageDraw.Draw(img)
    gradient(img, (10, 20, 15), (20, 35, 25))
    # Fog
    for _ in range(6):
        x = random.randint(-30, W)
        y = random.randint(200, 350)
        w = random.randint(40, 80)
        draw.ellipse([x, y, x+w, y+15], fill=(30, 50, 35))
    # Dead trees
    for _ in range(8):
        x = random.randint(5, W-5)
        h = random.randint(80, 200)
        draw.rectangle([x-2, H-h, x+2, H], fill=(30, 25, 20))
        # Branches
        for _ in range(3):
            by = H - h + random.randint(10, h//2)
            bdir = random.choice([-1, 1])
            blen = random.randint(8, 20)
            draw.line([(x, by), (x + bdir*blen, by - random.randint(3, 10))],
                     fill=(35, 30, 25), width=1)
    # Glowing eyes
    for _ in range(4):
        x = random.randint(10, W-10)
        y = random.randint(150, 350)
        draw.rectangle([x, y, x+2, y+2], fill=(200, 50, 200))
        draw.rectangle([x+5, y, x+7, y+2], fill=(200, 50, 200))
    # Fireflies
    for _ in range(15):
        x, y = random.randint(0, W), random.randint(0, H)
        draw.rectangle([x, y, x+1, y+1], fill=(150, 255, 100))
    return img


# ============================================================
# GENERATE ALL
# ============================================================

random.seed(42)  # Deterministic for reproducibility

biomes = {
    "sky_garden": sky_garden,
    "cloud_kingdom": cloud_kingdom,
    "neon_city": neon_city,
    "crystal_cave": crystal_cave,
    "fire_ruins": fire_ruins,
    "candy_land": candy_land,
    "space_station": space_station,
    "haunted_forest": haunted_forest,
}

import os
out_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "backgrounds")
os.makedirs(out_dir, exist_ok=True)

for name, gen_func in biomes.items():
    img = gen_func()
    path = os.path.join(out_dir, f"bg_{name}.png")
    img.save(path)
    print(f"Generated: bg_{name}.png ({img.width}x{img.height})")

print(f"\nAll backgrounds saved to: {out_dir}")
