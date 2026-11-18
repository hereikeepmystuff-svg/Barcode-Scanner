#!/usr/bin/env python3
"""Keeps version + changelog consistent across every file.

Single source of truth: the JSON block <script id="changelog"> inside web/Panel_Barcode_Tracker.html
(newest entry first; entry[0].v is the app version).

  python3 tools/release_check.py            check only (used by CI, fails on any mismatch)
  python3 tools/release_check.py --write    regenerate VERSION and CHANGELOG.md, copy the HTML into the APK assets
  python3 tools/release_check.py --notes    print the release notes for the newest version
"""
import json, re, sys, pathlib
root = pathlib.Path(__file__).resolve().parent.parent
web = root / 'web' / 'Panel_Barcode_Tracker.html'
assets = root / 'app' / 'src' / 'main' / 'assets' / 'index.html'
html = web.read_text(encoding='utf-8')
m = re.search(r'<script id="changelog" type="application/json">(.*?)</script>', html, re.S)
if not m:
    sys.exit('changelog block not found in the HTML')
cl = json.loads(m.group(1).replace('<\\/', '</'))
ver = cl[0]['v']

def key(v): return tuple(int(x) for x in v.split('.'))
for a, b in zip(cl, cl[1:]):
    if key(a['v']) <= key(b['v']):
        sys.exit(f"changelog versions must be strictly descending ({a['v']} before {b['v']})")

def section(c):
    t = f"## v{c['v']}" + (f" – {c['date']}" if c.get('date') else " – (date not recorded)")
    t += f"  (build #{c['build']})\n" if c.get('build') else "\n"
    if c.get('breaking'):
        t += f"\n> **Heads-up:** {c['breaking']}\n"
    return t + "\n" + "\n".join(f"- {i}" for i in c['items']) + "\n"

head = """# Changelog

Panel Barcode Tracker. The same list is shown inside the app (version button / scanner menu ⋯ → Version).

**Versioning:** `MAJOR.MINOR.PATCH`. MAJOR = the list format or barcode numbering changed (the list must be reloaded and stickers reprinted);
MINOR = new feature; PATCH = fix. Every release adds an entry here, bumps `VERSION`, and ships as APK + HTML with the version in the file name.
Dates are shown where recorded; the earliest versions were reconstructed afterwards and have no date.

"""
md = head + "\n".join(section(c) for c in cl)

if '--notes' in sys.argv:
    print(f"Panel Barcode Tracker v{ver}\n\n" + section(cl[0]).split('\n', 1)[1]); sys.exit(0)
if '--write' in sys.argv:
    (root / 'VERSION').write_text(ver + '\n')
    (root / 'CHANGELOG.md').write_text(md, encoding='utf-8')
    assets.write_text(html, encoding='utf-8')
    print('written: VERSION', ver)
    sys.exit(0)
errs = []
if (root / 'VERSION').read_text().strip() != ver: errs.append('VERSION file does not match the newest changelog entry')
if (root / 'CHANGELOG.md').read_text(encoding='utf-8') != md: errs.append('CHANGELOG.md is out of date (run with --write)')
if assets.read_text(encoding='utf-8') != html: errs.append('app/src/main/assets/index.html differs from web/Panel_Barcode_Tracker.html')
if errs: sys.exit('\n'.join(errs))
print('OK – version', ver)
