package com.example.firstproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView tvFileName;
    private Button btnDownload, btnView;

    private String filepath = "https://github.com/dangquanuet/wifi-hunter-public/raw/develop/apk/wifi-hunter-version-1-0-2.apk";
    private URL url = null;
    private String fileName;
    private String filePath;
    private long downloadID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setListeners();

        // Đăng ký BroadcastReceiver để lắng nghe sự kiện tải xong
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void initViews() {
        tvFileName = findViewById(R.id.tvUrl);
        btnDownload = findViewById(R.id.btnDownload);
        btnView = findViewById(R.id.btnView);

        try {
            url = new URL(filepath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        fileName = url.getPath();
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
        tvFileName.setText(fileName);
    }

    private void setListeners() {
        btnDownload.setOnClickListener(v -> downloadFileFromUrl(filepath));

        btnView.setOnClickListener(v -> {
            // Lấy URI của file từ DownloadManager
            Uri fileUri = getUriFromDownloadManager(this, downloadID);
            if (fileUri != null) {
                openApkFile(fileUri);
            } else {
                Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadFileFromUrl(String url) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);

        // Đặt vị trí tải về là thư mục Download
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Uri downloadLocation = Uri.fromFile(new File(dir, fileName));

        // Bắt đầu tải file
        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setTitle(fileName)
                .setDescription("Downloading...")
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(downloadLocation);
        request.allowScanningByMediaScanner();
        downloadID = downloadManager.enqueue(request);
    }

    private void openApkFile(Uri uri) {
        if (uri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getUriFromDownloadManager(Context context, long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            if (columnIndex != -1) {
                String fileUriString = cursor.getString(columnIndex);
                cursor.close();
                return Uri.parse(fileUriString);
            }
            cursor.close();
        }
        return null;
    }


    @Override
    protected void onDestroy() {
        // Hủy đăng ký BroadcastReceiver khi Activity bị hủy
        unregisterReceiver(onDownloadComplete);
        super.onDestroy();
    }

    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID == id) {
                // Lấy URI của file từ DownloadManager
                Uri fileUri = getUriFromDownloadManager(context, id);
                if (fileUri != null) {
                    Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show();
                    //openApkFile(fileUri);
                } else {
                    Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
}
