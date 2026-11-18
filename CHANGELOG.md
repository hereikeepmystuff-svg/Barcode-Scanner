# Changelog

Panel Barcode Tracker. The same list is shown inside the app (version button / scanner menu ⋯ → Version).

**Versioning:** `MAJOR.MINOR.PATCH`. MAJOR = the list format or barcode numbering changed (the list must be reloaded and stickers reprinted);
MINOR = new feature; PATCH = fix. Every release adds an entry here, bumps `VERSION`, and ships as APK + HTML with the version in the file name.
Dates are shown where recorded; the earliest versions were reconstructed afterwards and have no date.

## v2.2.0 – 2026-11-18  (build #13)

- Version number and changelog: shown in the app (header button / scanner menu), in every Excel report (new About sheet), in backups, and in the APK's version name.
- The app logs an 'App updated' entry in the activity log when a new version is opened for the first time.

## v2.1.0 – 2026-11-18  (build #12)

> **Heads-up:** Barcode numbers change: BOQ lists now get one running number per panel. Load the BOQ again once; this clears old scan statuses and reprints are needed.

- Barcode numbers are sequential without a '-1' suffix (000001, 000002 …). The Sr. no. stays in its own Excel column.
- Revisions keep every existing number; added panels continue after the highest number in use.
- Barcode drawn smaller (9 mm tall, narrower); tower name and Reuse/New 13 pt, unit up to 28 pt, 'A Code B' line up to 20 pt.
- Typing a plain number in the scan box finds that barcode number.

## v2.0.0 – 2026-11-17  (build #11)

> **Heads-up:** New BOQ list structure (units as columns, Reuse/New, Remark). The old list format still imports, but loading a BOQ over an old list replaces it.

- Imports the BOQ format: unit columns (A, B, C, Passage, ST1, ST2) split into one barcode per panel per unit; Reuse and New counts assigned per panel.
- New sticker layout from sheet 2: tower name and Reuse/New on the left, unit on the right, 'A Code B' bottom line.
- Remark column: rows tagged Add become the next revision lot, rows tagged Remove are put on hold or cancelled.
- Tower name taken from the file name (T-A gives Tower-A). Excel report gained Reuse, New, Unit-wise qty, Remark columns.

## v1.10.0 – 2026-10-14  (build #10)

- Scanner view: review page lists scanned items before applying, with Remove for mistakes.
- Scanner view: Excel list import available on the mode and finished screens.

## v1.9.0 – 2026-10-14  (build #9)

- List revisions: compare a re-imported list (added, removed, changed) before applying.
- Removed panels go on hold or cancelled; scanning them shows a warning that stays until dismissed; panels can be released by hand.
- Added panels continue the same series with a revision number; Revision and Hold/Cancelled columns and a Revision History sheet in Excel.

## v1.8.0 – 2026-10-14  (build #8)

- Simplified scanner view on phones and tablets: load list, choose Sort / Pack / Unpack, full-screen camera with project and tower on top, bulk and hold toggles, timed warnings, Excel report button. Laptops keep the full layout.

## v1.7.0 – 2026-10-14  (build #7)

- Three 0/1 columns (Sorted, Packed, Unpacked & Verified) in the Item List and Barcode Register sheets.

## v1.6.0 – (date not recorded)  (build #6)

- Strict Sorted, then Packed, then Unpacked & Verified sequence with warnings and an override.
- Camera reads one barcode at a time unless Bulk scanning is on.
- APK signed with one fixed key so updates install over the old version.

## v1.5.0 – (date not recorded)  (build #5)

- Print layouts by page type that fit the maximum number of stickers per page.

## v1.4.0 – (date not recorded)  (build #4)

- Standalone Android APK (camera and storage access) built automatically on GitHub.

## v1.3.0 – (date not recorded)  (build #3)

- Camera permission handling and diagnostics on Android and iPhone, with a photo fallback.
- An Excel list must be loaded before scanning; it is kept until a different file is loaded.

## v1.2.0 – (date not recorded)  (build #2)

- Activity log of uploads, scans and exports; automatic backup every 15 minutes, newest 3 kept.

## v1.1.0 – (date not recorded)  (build #1)

- Updated sticker format: project name only at the top, two barcodes with logo, Code + Panel Size line that shrinks to fit, fixed-digit numbers.
- Batch scanning with a pre-selected status.

## v1.0.0 – (date not recorded)

- First version: single offline HTML file, Excel import, Code 128 barcode per panel, 100 x 50 mm stickers, scan section with Sorted / Packed / Unpacked & Verified, barcode numbers listed back in Excel.
