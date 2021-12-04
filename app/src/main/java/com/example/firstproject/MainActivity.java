package com.example.firstproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView tvFileName;
    private Button btnDownload, btnView;

    private String filepath = "http://africau.edu/images/default/sample.pdf";
    private URL url = null;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setListeners();
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
        tvFileName.setText(fileName);
    }

    private void setListeners() {

        btnDownload.setOnClickListener(v -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url + ""));
            request.setTitle(fileName);
            request.setMimeType("applcation/pdf");
            request.allowScanningByMediaScanner();
            request.setAllowedOverMetered(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
        });

    btnView.setOnClickListener(v -> {
        File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+fileName);
        Uri uri= FileProvider.getUriForFile(MainActivity.this,"com.example.firstproject"+".provider",file);
        Intent i=new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri,"application/pdf");
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(i);

    });
    }

}