"""
SERPENT — Snake Game
Built with Python's built-in tkinter library.
Run with:  python serpent.py
Controls:  Arrow Keys / WASD  |  P = Pause  |  R = Restart
"""

import tkinter as tk
import random
import math
import time
import os
import json

# ── Constants ────────────────────────────────────────────────────────────────
CELL       = 22           # pixels per grid cell
COLS       = 26
ROWS       = 26
W          = COLS * CELL  # canvas width
H          = ROWS * CELL  # canvas height
SCORE_FILE = os.path.join(os.path.expanduser("~"), ".serpent_best.json")

# Colors
BG         = "#08080e"
GRID_DOT   = "#141428"
HEAD_CLR   = "#00ffaa"
FOOD_CLR   = "#ff4466"
TEXT_CLR   = "#e8e8f0"
DIM_CLR    = "#3a3a5c"
GOLD       = "#ffcc00"
PANEL_BG   = "#0d0d18"


# ── Helpers ──────────────────────────────────────────────────────────────────
def lerp_color(c1, c2, t):
    """Linearly interpolate between two hex colors."""
    r1,g1,b1 = int(c1[1:3],16), int(c1[3:5],16), int(c1[5:7],16)
    r2,g2,b2 = int(c2[1:3],16), int(c2[3:5],16), int(c2[5:7],16)
    r = int(r1 + (r2-r1)*t)
    g = int(g1 + (g2-g1)*t)
    b = int(b1 + (b2-b1)*t)
    return f"#{r:02x}{g:02x}{b:02x}"

def load_best():
    try:
        with open(SCORE_FILE) as f:
            return json.load(f).get("best", 0)
    except Exception:
        return 0

def save_best(score):
    try:
        with open(SCORE_FILE, "w") as f:
            json.dump({"best": score}, f)
    except Exception:
        pass


# ── Particle ─────────────────────────────────────────────────────────────────
class Particle:
    def __init__(self, x, y):
        angle = random.uniform(0, 2 * math.pi)
        speed = random.uniform(2, 5)
        self.x  = x
        self.y  = y
        self.vx = math.cos(angle) * speed
        self.vy = math.sin(angle) * speed
        self.life = 1.0
        self.size = random.uniform(2, 5)
        self.color = random.choice([FOOD_CLR, GOLD, "#ff8899"])

    def update(self):
        self.x  += self.vx
        self.y  += self.vy
        self.vy += 0.15
        self.vx *= 0.96
        self.life -= 0.055

    @property
    def alive(self):
        return self.life > 0


# ── Main Game ─────────────────────────────────────────────────────────────────
class SerpentGame:
    def __init__(self, root):
        self.root = root
        self.root.title("SERPENT")
        self.root.configure(bg=PANEL_BG)
        self.root.resizable(False, False)

        self.best = load_best()
        self._build_ui()
        self._bind_keys()
        self.state = "menu"
        self._show_overlay("SERPENT", "Arrow Keys / WASD to move", show_start=True)
        self._animate()

    # ── UI Construction ──────────────────────────────────────────────────────
    def _build_ui(self):
        # Top HUD
        hud = tk.Frame(self.root, bg=PANEL_BG, padx=14, pady=8)
        hud.pack(fill="x")

        self.title_lbl = tk.Label(hud, text="SERPENT", font=("Courier", 22, "bold"),
                                   bg=PANEL_BG, fg=HEAD_CLR)
        self.title_lbl.pack(side="left")

        right = tk.Frame(hud, bg=PANEL_BG)
        right.pack(side="right")

        for attr, label, col in [
            ("score_var", "SCORE", TEXT_CLR),
            ("length_var", "LENGTH", TEXT_CLR),
            ("best_var", "BEST", GOLD),
        ]:
            var = tk.StringVar(value="0")
            setattr(self, attr, var)
            f = tk.Frame(right, bg=PANEL_BG, padx=10)
            f.pack(side="left")
            tk.Label(f, textvariable=var, font=("Courier", 18, "bold"),
                     bg=PANEL_BG, fg=col).pack()
            tk.Label(f, text=label, font=("Courier", 7),
                     bg=PANEL_BG, fg=DIM_CLR).pack()

        self.best_var.set(str(self.best))

        # Canvas
        self.canvas = tk.Canvas(self.root, width=W, height=H,
                                 bg=BG, highlightthickness=1,
                                 highlightbackground="#1a1a33")
        self.canvas.pack()

        # Footer
        foot = tk.Frame(self.root, bg=PANEL_BG, pady=6)
        foot.pack(fill="x")
        tk.Label(foot, text="[P] Pause    [R] Restart    Arrow Keys / WASD",
                 font=("Courier", 8), bg=PANEL_BG, fg=DIM_CLR).pack()

    def _bind_keys(self):
        dirs = {
            "Up":    (0,-1), "w": (0,-1), "W": (0,-1),
            "Down":  (0, 1), "s": (0, 1), "S": (0, 1),
            "Left":  (-1,0), "a": (-1,0), "A": (-1,0),
            "Right": ( 1,0), "d": ( 1,0), "D": ( 1,0),
        }
        def on_key(e):
            key = e.keysym
            if key in ("p","P"):
                self._toggle_pause(); return
            if key in ("r","R"):
                self._restart(); return
            if key in dirs and self.state == "running":
                dx, dy = dirs[key]
                # prevent reversing
                if (dx, dy) != (-self.dir[0], -self.dir[1]):
                    self.next_dir = (dx, dy)

        self.root.bind("<Key>", on_key)

    # ── Overlay ───────────────────────────────────────────────────────────────
    def _show_overlay(self, title, sub, score_line=None, show_start=False):
        self.canvas.delete("overlay")
        cx, cy = W // 2, H // 2

        # Dim background
        self.canvas.create_rectangle(0, 0, W, H,
            fill="#08080e", stipple="gray50", tags="overlay")
        self.canvas.create_rectangle(cx-160, cy-80, cx+160, cy+90,
            fill="#0d0d1e", outline="#1a1a44", width=1, tags="overlay")

        self.canvas.create_text(cx, cy-52, text=title,
            font=("Courier", 28, "bold"), fill=HEAD_CLR, tags="overlay")
        self.canvas.create_text(cx, cy-18, text=sub,
            font=("Courier", 9), fill=DIM_CLR, tags="overlay")

        if score_line:
            self.canvas.create_text(cx, cy+8, text=score_line,
                font=("Courier", 11, "bold"), fill=GOLD, tags="overlay")

        if show_start:
            # Clickable start button region
            bx1,by1,bx2,by2 = cx-70, cy+35, cx+70, cy+62
            self.canvas.create_rectangle(bx1,by1,bx2,by2,
                outline=HEAD_CLR, fill="#0d0d1e", width=1, tags="overlay")
            self.canvas.create_text(cx, cy+48, text="START GAME",
                font=("Courier", 10, "bold"), fill=HEAD_CLR, tags="overlay")
            self.canvas.tag_bind("overlay", "<Button-1>", lambda e: self._start())

    def _hide_overlay(self):
        self.canvas.delete("overlay")

    # ── Game State ────────────────────────────────────────────────────────────
    def _start(self):
        sx, sy = COLS // 2, ROWS // 2
        self.snake    = [(sx,sy),(sx-1,sy),(sx-2,sy)]
        self.dir      = (1, 0)
        self.next_dir = (1, 0)
        self.score    = 0
        self.particles= []
        self.eat_ring  = None        # (x, y, t)  eat animation ring
        self.paused   = False
        self.state    = "running"
        self._place_food()
        self._update_hud()
        self._hide_overlay()
        self.last_step = time.time()

    def _restart(self):
        if self.state in ("dead","menu"):
            self._start()

    def _toggle_pause(self):
        if self.state == "running":
            self.paused = not self.paused
            if self.paused:
                self._show_overlay("PAUSED", "Press P to continue")
            else:
                self._hide_overlay()

    def _place_food(self):
        occupied = set(self.snake)
        while True:
            pos = (random.randint(0, COLS-1), random.randint(0, ROWS-1))
            if pos not in occupied:
                self.food = pos
                self.food_pulse = 0.0
                break

    def _update_hud(self):
        self.score_var.set(str(self.score))
        self.length_var.set(str(len(self.snake)))
        self.best_var.set(str(self.best))

    def _get_speed(self):
        n = len(self.snake)
        if n < 8:   return 0.145
        if n < 15:  return 0.120
        if n < 25:  return 0.100
        return 0.085

    # ── Step Logic ────────────────────────────────────────────────────────────
    def _step(self):
        self.dir = self.next_dir
        hx = self.snake[0][0] + self.dir[0]
        hy = self.snake[0][1] + self.dir[1]

        # Wall collision
        if not (0 <= hx < COLS and 0 <= hy < ROWS):
            self._die(); return

        # Self collision
        if (hx, hy) in self.snake:
            self._die(); return

        self.snake.insert(0, (hx, hy))

        if (hx, hy) == self.food:
            bonus = 10 + len(self.snake) // 3
            self.score += bonus
            if self.score > self.best:
                self.best = self.score
                save_best(self.best)
            self._spawn_particles(hx, hy)
            self.eat_ring = [hx, hy, 1.0]
            self._place_food()
        else:
            self.snake.pop()

        self._update_hud()

    def _die(self):
        self.state = "dead"
        self._show_overlay(
            "GAME OVER",
            "Press R to restart",
            score_line=f"Score: {self.score}   Length: {len(self.snake)}",
            show_start=False
        )

    # ── Particles ─────────────────────────────────────────────────────────────
    def _spawn_particles(self, gx, gy):
        px = gx * CELL + CELL // 2
        py = gy * CELL + CELL // 2
        for _ in range(14):
            self.particles.append(Particle(px, py))

    # ── Draw ──────────────────────────────────────────────────────────────────
    def _draw(self):
        c = self.canvas
        c.delete("game")

        # Grid dots
        for row in range(ROWS):
            for col in range(COLS):
                x = col * CELL + CELL // 2
                y = row * CELL + CELL // 2
                c.create_oval(x-1, y-1, x+1, y+1, fill=GRID_DOT,
                              outline="", tags="game")

        if self.state not in ("running","dead") and len(getattr(self,"particles",[])) == 0:
            return

        # Snake
        n = len(self.snake)
        for i, (gx, gy) in enumerate(self.snake):
            t = i / max(n - 1, 1)
            color = lerp_color(HEAD_CLR, "#006644", t)
            x1 = gx * CELL + 2
            y1 = gy * CELL + 2
            x2 = x1 + CELL - 4
            y2 = y1 + CELL - 4
            r = 5 if i == 0 else 3
            self._rounded_rect(c, x1, y1, x2, y2, r, color)

            # Head glow (fake with slightly larger pale rect)
            if i == 0:
                self._rounded_rect(c, x1-2, y1-2, x2+2, y2+2, r+2,
                                   "#003322", tags_extra="game")
                self._rounded_rect(c, x1, y1, x2, y2, r, HEAD_CLR)
                # Eyes
                dx, dy = self.dir
                if dx == 1:   eye_pts = [(x2-4,y1+4),(x2-4,y2-4)]
                elif dx == -1: eye_pts = [(x1+4,y1+4),(x1+4,y2-4)]
                elif dy == -1: eye_pts = [(x1+4,y1+4),(x2-4,y1+4)]
                else:          eye_pts = [(x1+4,y2-4),(x2-4,y2-4)]
                for ex, ey in eye_pts:
                    c.create_oval(ex-2,ey-2,ex+2,ey+2,
                                  fill="#08080e", outline="", tags="game")

        # Food — pulsing
        self.food_pulse += 0.12
        pulse = 1 + 0.15 * math.sin(self.food_pulse)
        fx = self.food[0] * CELL + CELL // 2
        fy = self.food[1] * CELL + CELL // 2
        r_food = int(6 * pulse)
        # Glow ring
        c.create_oval(fx-r_food-3, fy-r_food-3, fx+r_food+3, fy+r_food+3,
                      fill="#330011", outline="", tags="game")
        c.create_oval(fx-r_food, fy-r_food, fx+r_food, fy+r_food,
                      fill=FOOD_CLR, outline="", tags="game")
        # Highlight
        c.create_oval(fx-r_food+2, fy-r_food+2, fx-r_food+5, fy-r_food+5,
                      fill="#ffaaaa", outline="", tags="game")

        # Eat ring animation
        if self.eat_ring:
            gx, gy, t = self.eat_ring
            px = gx * CELL + CELL // 2
            py = gy * CELL + CELL // 2
            radius = int((1 - t) * CELL * 1.8 + 4)
            alpha_hex = format(max(0, int(t * 180)), "02x")
            color = f"#ffcc00"  # tkinter can't do real alpha on canvas; use stipple
            c.create_oval(px-radius, py-radius, px+radius, py+radius,
                          outline=GOLD, width=2, fill="", tags="game")
            self.eat_ring[2] -= 0.08
            if self.eat_ring[2] <= 0:
                self.eat_ring = None

        # Particles
        alive = []
        for p in self.particles:
            p.update()
            if p.alive:
                r = max(1, int(p.size * p.life))
                c.create_oval(p.x-r, p.y-r, p.x+r, p.y+r,
                              fill=p.color, outline="", tags="game")
                alive.append(p)
        self.particles = alive

    def _rounded_rect(self, c, x1, y1, x2, y2, r, fill, tags_extra="game"):
        """Draw a rounded rectangle on the canvas."""
        c.create_arc(x1, y1, x1+2*r, y1+2*r, start=90, extent=90,
                     fill=fill, outline="", tags=tags_extra)
        c.create_arc(x2-2*r, y1, x2, y1+2*r, start=0, extent=90,
                     fill=fill, outline="", tags=tags_extra)
        c.create_arc(x1, y2-2*r, x1+2*r, y2, start=180, extent=90,
                     fill=fill, outline="", tags=tags_extra)
        c.create_arc(x2-2*r, y2-2*r, x2, y2, start=270, extent=90,
                     fill=fill, outline="", tags=tags_extra)
        c.create_rectangle(x1+r, y1, x2-r, y2, fill=fill, outline="", tags=tags_extra)
        c.create_rectangle(x1, y1+r, x2, y2-r, fill=fill, outline="", tags=tags_extra)

    # ── Main Loop ─────────────────────────────────────────────────────────────
    def _animate(self):
        now = time.time()

        if self.state == "running" and not self.paused:
            if now - self.last_step >= self._get_speed():
                self._step()
                self.last_step = now

        if self.state in ("running", "dead"):
            self._draw()

        self.root.after(16, self._animate)   # ~60fps render loop


# ── Entry Point ───────────────────────────────────────────────────────────────
if __name__ == "__main__":
    root = tk.Tk()
    game = SerpentGame(root)
    root.mainloop()
