# -*- coding: utf-8 -*-
"""Render SRS diagrams (architecture, navigation, ticket lifecycle, ERD) as PNGs via PIL."""
import os
from PIL import Image, ImageDraw, ImageFont

OUT = os.path.join(os.path.dirname(__file__), "..", "docs", "img")
os.makedirs(OUT, exist_ok=True)

# Brand palette (matches ui/theme/Color.kt)
PRIMARY = (0, 100, 147)        # #006493
PRIMARY_CT = (202, 230, 255)   # primaryContainer
SECONDARY = (80, 96, 110)
INK = (20, 28, 36)
LINE = (90, 105, 120)
WHITE = (255, 255, 255)
OPEN_C = (33, 118, 174)
PROG_C = (224, 152, 30)
CLOSE_C = (46, 125, 50)
SCALE = 2  # supersample for crisp text


def font(size, bold=False):
    names = (["arialbd.ttf", "segoeuib.ttf"] if bold else ["arial.ttf", "segoeui.ttf"])
    for n in names:
        for base in ("C:/Windows/Fonts/", "/usr/share/fonts/truetype/dejavu/"):
            try:
                return ImageFont.truetype(base + n, size)
            except Exception:
                pass
    try:
        return ImageFont.truetype("DejaVuSans.ttf", size)
    except Exception:
        return ImageFont.load_default()


def canvas(w, h):
    img = Image.new("RGB", (w * SCALE, h * SCALE), WHITE)
    return img, ImageDraw.Draw(img)


def save(img, name):
    img = img.resize((img.width // SCALE, img.height // SCALE), Image.LANCZOS)
    p = os.path.join(OUT, name)
    img.save(p)
    print("wrote", os.path.normpath(p))


def s(v):
    return v * SCALE


def center_text(d, box, lines, fnt, color=INK, line_gap=6):
    x0, y0, x1, y1 = box
    heights, widths = [], []
    for ln in lines:
        bb = d.textbbox((0, 0), ln, font=fnt)
        widths.append(bb[2] - bb[0]); heights.append(bb[3] - bb[1])
    total = sum(heights) + line_gap * SCALE * (len(lines) - 1)
    cy = (y0 + y1) / 2 - total / 2
    for ln, hh in zip(lines, heights):
        bb = d.textbbox((0, 0), ln, font=fnt)
        w = bb[2] - bb[0]
        d.text(((x0 + x1) / 2 - w / 2, cy), ln, font=fnt, fill=color)
        cy += hh + line_gap * SCALE


def box(d, x, y, w, h, lines, fnt, fill=PRIMARY_CT, outline=PRIMARY, txt=INK, radius=12, width=2):
    d.rounded_rectangle([s(x), s(y), s(x + w), s(y + h)], radius=s(radius),
                        fill=fill, outline=outline, width=s(width))
    center_text(d, (s(x), s(y), s(x + w), s(y + h)), lines, fnt, color=txt)
    return (x, y, w, h)


def arrow(d, p1, p2, color=LINE, width=2, label=None, fnt=None):
    import math
    x1, y1 = s(p1[0]), s(p1[1]); x2, y2 = s(p2[0]), s(p2[1])
    d.line([x1, y1, x2, y2], fill=color, width=s(width))
    ang = math.atan2(y2 - y1, x2 - x1); al = s(9)
    d.polygon([(x2, y2),
               (x2 - al * math.cos(ang - 0.45), y2 - al * math.sin(ang - 0.45)),
               (x2 - al * math.cos(ang + 0.45), y2 - al * math.sin(ang + 0.45))], fill=color)
    if label and fnt:
        mx, my = (x1 + x2) / 2, (y1 + y2) / 2
        bb = d.textbbox((0, 0), label, font=fnt)
        d.rectangle([mx - 2, my - (bb[3]-bb[1]) - 2, mx + (bb[2]-bb[0]) + 4, my + 4], fill=WHITE)
        d.text((mx, my - (bb[3]-bb[1])), label, font=fnt, fill=color)


# ---------------------------------------------------------------- 1. Architecture
def diagram_architecture():
    img, d = canvas(760, 520)
    f = font(s(15), bold=True); fs = font(s(12))
    title = font(s(18), bold=True)
    d.text((s(24), s(18)), "Arsitektur MVVM — E-Ticketing Helpdesk (Native Android)", font=title, fill=PRIMARY)
    layers = [
        ("UI Layer — Jetpack Compose", ["Screens (Splash, Login, Dashboard, Ticket, ...)", "Reusable Components + Material 3 Theme (light/dark)"], PRIMARY_CT),
        ("Presentation — ViewModel", ["TicketViewModel  •  StateFlow<UI State>", "AuthMessage (typed), filter per-role"], (215, 229, 245)),
        ("Domain Layer", ["UseCases (GetTickets, GetTicketDetail)", "Models (Ticket, AppUser, ...)  •  Repository contract"], (225, 240, 230)),
        ("Data Layer", ["FakeTicketRepository (in-memory, dummy data)", "Backend-ready: REST API + MySQL (lihat docs/)"], (245, 238, 222)),
    ]
    x, w, h, gap = 60, 640, 86, 28
    y = 70
    cx = x + w / 2
    for i, (head, body, fill) in enumerate(layers):
        d.rounded_rectangle([s(x), s(y), s(x + w), s(y + h)], radius=s(12),
                            fill=fill, outline=SECONDARY, width=s(2))
        d.text((s(x + 18), s(y + 12)), head, font=f, fill=INK)
        center_text(d, (s(x), s(y + 30), s(x + w), s(y + h)), body, fs, color=(60, 70, 80))
        if i < len(layers) - 1:
            arrow(d, (cx, y + h), (cx, y + h + gap), color=PRIMARY, width=3)
        y += h + gap
    save(img, "arch.png")


# ---------------------------------------------------------------- 2. Navigation flow
def diagram_navigation():
    img, d = canvas(900, 600)
    fb = font(s(13), bold=True); fs = font(s(11)); fl = font(s(10))
    title = font(s(18), bold=True)
    d.text((s(24), s(16)), "Peta Navigasi Aplikasi", font=title, fill=PRIMARY)

    def b(x, y, w, h, label, fill=PRIMARY_CT):
        box(d, x, y, w, h, [label], fb, fill=fill, outline=PRIMARY, radius=10)
        return (x + w / 2, y, x + w / 2, y + h, x, y, w, h)

    AUTH = (227, 242, 253)
    MAIN = (232, 245, 233)
    splash = b(360, 60, 170, 46, "Splash", (255, 244, 222))
    login = b(120, 150, 150, 46, "Login", AUTH)
    register = b(20, 240, 150, 44, "Register", AUTH)
    reset = b(200, 240, 160, 44, "Reset Password", AUTH)
    dash = b(560, 150, 180, 50, "Dashboard", MAIN)
    # Column A (left): Daftar Tiket -> Detail Tiket
    tlist = b(450, 280, 170, 44, "Daftar Tiket", MAIN)
    detail = b(450, 380, 170, 44, "Detail Tiket", MAIN)
    # Column B (right): Buat / Notifikasi / Profil
    create = b(680, 270, 180, 42, "Buat Tiket", MAIN)
    notif = b(680, 350, 180, 42, "Notifikasi", MAIN)
    profile = b(680, 430, 180, 42, "Profil", MAIN)

    arrow(d, (445, 106), (300, 150), label="belum login", fnt=fl)
    arrow(d, (490, 106), (645, 150), label="sudah login", fnt=fl)
    arrow(d, (150, 196), (110, 240), label="Daftar", fnt=fl)
    arrow(d, (230, 196), (270, 240), label="Lupa pw", fnt=fl)
    arrow(d, (270, 173), (560, 173), label="login sukses", fnt=fl)
    arrow(d, (610, 200), (540, 280))           # Dashboard -> Daftar Tiket
    arrow(d, (535, 324), (535, 380), label="tap kartu", fnt=fl)  # Daftar -> Detail
    arrow(d, (690, 200), (740, 270))           # Dashboard -> Buat Tiket
    arrow(d, (700, 200), (745, 350))           # Dashboard -> Notifikasi
    arrow(d, (710, 200), (752, 430))           # Dashboard -> Profil

    # legend
    d.rounded_rectangle([s(120), s(540), s(140), s(556)], fill=AUTH, outline=PRIMARY, width=s(1))
    d.text((s(148), s(540)), "Layar publik / auth", font=fs, fill=INK)
    d.rounded_rectangle([s(360), s(540), s(380), s(556)], fill=MAIN, outline=PRIMARY, width=s(1))
    d.text((s(388), s(540)), "Layar utama (perlu login)", font=fs, fill=INK)
    save(img, "nav_flow.png")


# ---------------------------------------------------------------- 3. Ticket lifecycle
def diagram_lifecycle():
    img, d = canvas(860, 320)
    fb = font(s(15), bold=True); fl = font(s(11)); fs = font(s(11))
    title = font(s(18), bold=True)
    d.text((s(24), s(18)), "Siklus Hidup Tiket", font=title, fill=PRIMARY)
    y = 120; h = 70; w = 200
    box(d, 40, y, w, h, ["OPEN"], fb, fill=(225, 240, 252), outline=OPEN_C, txt=OPEN_C, width=3)
    box(d, 330, y, w, h, ["IN_PROGRESS"], fb, fill=(253, 243, 224), outline=PROG_C, txt=(150, 96, 10), width=3)
    box(d, 620, y, w, h, ["CLOSED"], fb, fill=(230, 244, 231), outline=CLOSE_C, txt=CLOSE_C, width=3)
    arrow(d, (240, y + 28), (330, y + 28), color=SECONDARY, width=3, label="ditangani", fnt=fl)
    arrow(d, (530, y + 28), (620, y + 28), color=SECONDARY, width=3, label="diselesaikan", fnt=fl)
    arrow(d, (330, y + 52), (240, y + 52), color=SECONDARY, width=2, label="dibuka lagi", fnt=fl)
    d.text((s(40), s(y + h + 40)), "Setiap transisi status, assign, dan komentar dicatat pada audit trail", font=fs, fill=INK)
    d.text((s(40), s(y + h + 62)), "(ticket_activities) dan dapat memicu notifikasi.", font=fs, fill=INK)
    save(img, "lifecycle.png")


# ---------------------------------------------------------------- 4. ERD
def diagram_erd():
    img, d = canvas(900, 620)
    fh = font(s(13), bold=True); fc = font(s(10)); fl = font(s(11), bold=True)
    title = font(s(18), bold=True)
    d.text((s(24), s(16)), "Entity Relationship Diagram (ERD)", font=title, fill=PRIMARY)

    def table(x, y, w, name, cols):
        rowh = 20; h = 26 + rowh * len(cols)
        d.rounded_rectangle([s(x), s(y), s(x + w), s(y + h)], radius=s(8), fill=WHITE, outline=PRIMARY, width=s(2))
        d.rectangle([s(x), s(y), s(x + w), s(y + 24)], fill=PRIMARY)
        bb = d.textbbox((0, 0), name, font=fh)
        d.text((s(x) + (s(w) - (bb[2]-bb[0]))/2, s(y + 5)), name, font=fh, fill=WHITE)
        cy = y + 28
        for c in cols:
            d.text((s(x + 10), s(cy)), c, font=fc, fill=INK)
            cy += rowh
        return (x, y, w, h)

    users = table(350, 60, 200, "users", ["PK id", "name", "username (UQ)", "email (UQ)", "password", "role"])
    tickets = table(330, 250, 240, "tickets", ["PK id", "title", "status", "created_at", "FK applicant_id", "FK assigned_to (NL)", "attachment_source"])
    comments = table(40, 470, 200, "comments", ["PK id", "FK ticket_id", "sender", "message"])
    acts = table(340, 480, 210, "ticket_activities", ["PK id", "FK ticket_id", "title", "actor"])
    notifs = table(650, 470, 210, "notifications", ["PK id", "FK ticket_id (NL)", "title", "is_read"])

    arrow(d, (440, 158), (440, 250), color=LINE, width=2, label="1 .. N (applicant)", fnt=fl)
    arrow(d, (140, 360), (140, 470), color=LINE, width=2, label="1..N", fnt=fl)
    d.line([s(330), s(285), s(240), s(285)], fill=LINE, width=s(2)); d.line([s(240), s(285), s(140), s(470)], fill=LINE, width=s(2))
    arrow(d, (445, 360), (445, 480), color=LINE, width=2, label="1..N", fnt=fl)
    arrow(d, (560, 320), (755, 470), color=LINE, width=2, label="1..N", fnt=fl)
    d.text((s(40), s(60)), "PK=Primary Key  FK=Foreign Key", font=fc, fill=SECONDARY)
    d.text((s(40), s(76)), "UQ=Unique  NL=Nullable", font=fc, fill=SECONDARY)
    save(img, "erd.png")


if __name__ == "__main__":
    diagram_architecture()
    diagram_navigation()
    diagram_lifecycle()
    diagram_erd()
    print("All diagrams generated.")
