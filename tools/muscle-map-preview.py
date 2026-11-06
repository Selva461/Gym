#!/usr/bin/env python3
"""Preview the muscle map atlas without building the app.

Reads the point tables in domain/anatomy/Atlas*.kt, mirrors and warps them the way
BodyAtlas.kt does, and writes an SVG with the front and back views for both builds.
Muscles named on the command line are lit as primary; prefix one with + to light it
as secondary. With no names, every mapped muscle is lit in turn colours so gaps and
overlaps are easy to see.

    python3 tools/muscle-map-preview.py out.svg DELT_FRONT DELT_SIDE +TRICEPS
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "app/src/commonMain/kotlin/dev/avles/gym/domain/anatomy"
MID = 100.0

CALL = re.compile(r"\b(pair|span|detail|base|baseSpan)\(\s*([A-Z_]+,\s*)?(ARM|TRUNK|LEG|HEAD),([\s\d,]+)\)")

FEMALE = dict(
    profile=[(60, .92), (100, .9), (130, .9), (160, .86), (182, .85), (200, .98), (216, 1.07),
             (250, 1.06), (290, 1.02), (310, .98), (350, .95), (420, .94)],
    arms=.9, head=.96)
MALE = dict(profile=[], arms=1, head=1)


def width_at(b, y):
    p = b["profile"]
    if not p:
        return 1
    if y <= p[0][0]:
        return p[0][1]
    for (y0, k0), (y1, k1) in zip(p, p[1:]):
        if y <= y1:
            return k0 + (k1 - k0) * (y - y0) / (y1 - y0)
    return p[-1][1]


def factor(b, part, y):
    return b["head"] if part == "HEAD" else b["arms"] if part == "ARM" else width_at(b, y)


def regions(name):
    text = (SRC / name).read_text()
    for kind, muscle, part, nums in CALL.findall(text):
        v = [int(n) for n in nums.replace("\n", " ").split(",") if n.strip()]
        yield kind, (muscle or "").strip(" ,") or None, part, list(zip(v[::2], v[1::2]))


def outlines(kind, part, pts, b):
    if kind in ("span", "baseSpan"):
        full = pts + [(2 * MID - x, y) for x, y in reversed(pts) if x < MID]
        shapes = [full]
    else:
        shapes = [pts, [(2 * MID - x, y) for x, y in reversed(pts)]]
    for s in shapes:
        yield [(MID + (x - MID) * factor(b, part, y), y) for x, y in s]


def path(s):
    n = len(s)
    d = f"M{s[0][0]:.1f},{s[0][1]:.1f}"
    for i in range(n):
        p0, p1, p2, p3 = s[i - 1], s[i], s[(i + 1) % n], s[(i + 2) % n]
        c1 = (p1[0] + (p2[0] - p0[0]) / 6, p1[1] + (p2[1] - p0[1]) / 6)
        c2 = (p2[0] - (p3[0] - p1[0]) / 6, p2[1] - (p3[1] - p1[1]) / 6)
        d += f" C{c1[0]:.1f},{c1[1]:.1f} {c2[0]:.1f},{c2[1]:.1f} {p2[0]:.1f},{p2[1]:.1f}"
    return d + "Z"


def figure(view, b, primary, secondary, ox):
    out = [f'<g transform="translate({ox},10)">']
    for kind, _, part, pts in regions("AtlasBase.kt"):
        for s in outlines(kind, part, pts, b):
            out.append(f'<path d="{path(s)}" fill="#262c3a"/>')
    for kind, muscle, part, pts in regions(f"Atlas{view}.kt"):
        fill = "#3a4256"
        if muscle in primary:
            fill = "#f2a93b"
        elif muscle in secondary:
            fill = "#8a6a3a"
        elif muscle is None:
            fill = "#30374a"
        for s in outlines(kind, part, pts, b):
            out.append(f'<path d="{path(s)}" fill="{fill}" stroke="#12141c" stroke-width="1.1"/>')
    out.append(f'<text x="100" y="438" fill="#5a6175" font-size="11" text-anchor="middle" font-family="sans-serif">{view}</text></g>')
    return out


def main():
    dest = sys.argv[1] if len(sys.argv) > 1 else "muscle-map.svg"
    names = sys.argv[2:]
    primary = {n for n in names if not n.startswith("+")}
    secondary = {n[1:] for n in names if n.startswith("+")}
    svg = ['<svg xmlns="http://www.w3.org/2000/svg" width="880" height="460" viewBox="0 0 880 460">',
           '<rect width="880" height="460" fill="#12141c"/>']
    for i, (b, view) in enumerate([(MALE, "Front"), (MALE, "Back"), (FEMALE, "Front"), (FEMALE, "Back")]):
        svg += figure(view, b, primary, secondary, 10 + i * 218)
    svg.append("</svg>")
    Path(dest).write_text("\n".join(svg))


if __name__ == "__main__":
    main()
