package com.kec.panelbarcode;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hosts the Panel Barcode Tracker web app (assets/index.html) in a WebView.
 * The page is served from the fake secure origin https://appassets.local/ so the
 * browser APIs that need a secure context (camera, IndexedDB) work.
 */
public class MainActivity extends Activity {
    private static final String HOST = "appassets.local";
    private static final int REQ_FILE = 1, REQ_PERMS = 2;
    private static final String BACKUP_DIR = "PanelBarcodeBackups";

    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private PermissionRequest pendingWebPermission;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);

        web.addJavascriptInterface(new Bridge(), "Android");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (HOST.equals(u.getHost())) {
                    String path = u.getPath();
                    if (path == null || path.equals("/") || path.isEmpty()) path = "/index.html";
                    try {
                        InputStream in = getAssets().open(path.substring(1));
                        return new WebResourceResponse(mime(path), "utf-8", in);
                    } catch (IOException e) {
                        return new WebResourceResponse("text/plain", "utf-8", 404, "Not found", null, null);
                    }
                }
                return super.shouldInterceptRequest(v, r);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return !HOST.equals(r.getUrl().getHost()); // keep everything inside the app
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    if (!HOST.equals(request.getOrigin().getHost())) { request.deny(); return; }
                    boolean wantsCamera = Arrays.asList(request.getResources()).contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE);
                    if (wantsCamera && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        pendingWebPermission = request;
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_PERMS);
                    } else {
                        request.grant(request.getResources());
                    }
                });
            }

            @Override
            public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = cb;
                try {
                    startActivityForResult(p.createIntent(), REQ_FILE);
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }
                return true;
            }

            // WebView swallows alert/confirm/prompt unless handled here
            @Override
            public boolean onJsAlert(WebView v, String url, String msg, final JsResult r) {
                new AlertDialog.Builder(MainActivity.this).setMessage(msg)
                        .setPositiveButton(android.R.string.ok, (d, i) -> r.confirm())
                        .setOnCancelListener(d -> r.confirm()).show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView v, String url, String msg, final JsResult r) {
                new AlertDialog.Builder(MainActivity.this).setMessage(msg)
                        .setPositiveButton(android.R.string.ok, (d, i) -> r.confirm())
                        .setNegativeButton(android.R.string.cancel, (d, i) -> r.cancel())
                        .setOnCancelListener(d -> r.cancel()).show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView v, String url, String msg, String def, final JsPromptResult r) {
                final EditText et = new EditText(MainActivity.this);
                et.setText(def);
                new AlertDialog.Builder(MainActivity.this).setMessage(msg).setView(et)
                        .setPositiveButton(android.R.string.ok, (d, i) -> r.confirm(et.getText().toString()))
                        .setNegativeButton(android.R.string.cancel, (d, i) -> r.cancel())
                        .setOnCancelListener(d -> r.cancel()).show();
                return true;
            }
        });

        // Ask for camera (and legacy storage) access up front, on first launch
        List<String> need = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            need.add(Manifest.permission.CAMERA);
        if (Build.VERSION.SDK_INT <= 28 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            need.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!need.isEmpty()) requestPermissions(need.toArray(new String[0]), REQ_PERMS);

        web.loadUrl("https://" + HOST + "/index.html");
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (pendingWebPermission != null) {
            boolean ok = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            if (ok) pendingWebPermission.grant(pendingWebPermission.getResources());
            else pendingWebPermission.deny();
            pendingWebPermission = null;
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_FILE && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
            fileCallback = null;
        }
    }

    private static String mime(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private void toast(final String m) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, m, Toast.LENGTH_LONG).show());
    }

    /** Writes a file into the public Downloads folder (optionally in a sub-folder). */
    private void saveToDownloads(String sub, String name, String mime, byte[] data) throws IOException {
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues v = new ContentValues();
            v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            v.put(MediaStore.MediaColumns.MIME_TYPE, mime);
            v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + (sub.isEmpty() ? "" : "/" + sub));
            Uri u = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
            if (u == null) throw new IOException("Could not create file");
            try (OutputStream o = getContentResolver().openOutputStream(u)) {
                o.write(data);
            }
        } else {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), sub);
            dir.mkdirs();
            try (FileOutputStream o = new FileOutputStream(new File(dir, name))) {
                o.write(data);
            }
        }
    }

    /** Deletes older backups so only the newest `keep` remain. */
    private void pruneBackups(int keep) {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                Uri col = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                try (Cursor c = getContentResolver().query(col,
                        new String[]{MediaStore.MediaColumns._ID},
                        MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ?",
                        new String[]{Environment.DIRECTORY_DOWNLOADS + "/" + BACKUP_DIR + "/", "Panel_Barcode_Backup_%"},
                        MediaStore.MediaColumns.DISPLAY_NAME + " DESC")) {
                    int i = 0;
                    while (c != null && c.moveToNext()) {
                        if (++i > keep) {
                            try { getContentResolver().delete(ContentUris.withAppendedId(col, c.getLong(0)), null, null); }
                            catch (Exception ignored) { /* files from a previous install cannot be deleted */ }
                        }
                    }
                }
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), BACKUP_DIR);
                File[] fs = dir.listFiles((d, n) -> n.startsWith("Panel_Barcode_Backup_"));
                if (fs != null) {
                    Arrays.sort(fs, (a, b2) -> b2.getName().compareTo(a.getName()));
                    for (int i = keep; i < fs.length; i++) fs[i].delete();
                }
            }
        } catch (Exception ignored) { }
    }

    /** Custom paper size in mm, as the page sent it (width x height). */
    private static PrintAttributes.MediaSize pageSize(int wMm, int hMm) {
        int w = Math.round(wMm / 25.4f * 1000), h = Math.round(hMm / 25.4f * 1000);
        PrintAttributes.MediaSize m = new PrintAttributes.MediaSize("PBT_PAGE", wMm + " x " + hMm + " mm", Math.min(w, h), Math.max(w, h));
        return w > h ? m.asLandscape() : m;
    }

    /** Methods callable from the web page as window.Android.* */
    private class Bridge {
        @JavascriptInterface
        public void saveFile(String name, String mime, String b64) {
            try {
                saveToDownloads("", name, mime, Base64.decode(b64, Base64.DEFAULT));
                toast("Saved to Downloads: " + name);
            } catch (Exception e) {
                toast("Save failed: " + e.getMessage());
            }
        }

        @JavascriptInterface
        public String saveBackup(String name, String b64, int keep) {
            try {
                saveToDownloads(BACKUP_DIR, name, "application/json", Base64.decode(b64, Base64.DEFAULT));
                pruneBackups(keep);
                return "Saved " + name;
            } catch (Exception e) {
                return "Phone folder write failed: " + e.getMessage();
            }
        }

        @JavascriptInterface
        public void printLabels(final int wMm, final int hMm) {
            runOnUiThread(() -> {
                PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                PrintDocumentAdapter ad = web.createPrintDocumentAdapter("Panel stickers");
                PrintAttributes attrs = new PrintAttributes.Builder()
                        .setMediaSize(pageSize(wMm, hMm))
                        .setResolution(new PrintAttributes.Resolution("r300", "300 dpi", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build();
                pm.print("Panel stickers", ad, attrs);
            });
        }
    }
}
