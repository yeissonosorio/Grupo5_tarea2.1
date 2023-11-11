package com.example.grupo5_tarea21;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.grupo5_tarea21.Conecxion.Conecxion;
import com.example.grupo5_tarea21.Conecxion.Video;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int TOMA_VEDEO=1;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private VideoView video;
    Button btnsalvar, btngrabar;
    static final int peticion_acceso_camara = 102;
    String encodedVideo="";
    Conecxion conexion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        video=findViewById(R.id.videoView);
        btngrabar=findViewById(R.id.btnGrabar);
        btnsalvar=findViewById(R.id.btnSalvar);

        btngrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisos();
            }
        });

        btnsalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveVideoSQLite();
            }
        });
    }
    private void SaveVideoSQLite() {

        conexion = new Conecxion(this, Video.name_database,null,1);
        SQLiteDatabase db = conexion.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Video.columnVideo, encodedVideo);
        db.insert(Video.tableVideo, null, values);
        Toast.makeText(getApplicationContext(), "Registro ingresado en la base de datos",Toast.LENGTH_LONG).show();
        db.close();
    }

    private void permisos(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},REQUEST_VIDEO_CAPTURE);
        }else{
            take_video();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == peticion_acceso_camara )
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED )
            {
                take_video();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Se necesita el permiso para accder a la camara", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void take_video(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TOMA_VEDEO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TOMA_VEDEO && resultCode == RESULT_OK) {
            Uri video_uri = data.getData();
            video.setVideoURI(video_uri);
            video.start();

            int targetBitRate = 2 * 1024 * 1024;
            int targetFrameRate = 30;

            InputStream inputStream = null;

            try {
                inputStream = getContentResolver().openInputStream(video_uri);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, video_uri);

            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
            } catch (Exception e) {

            }

            byte[] videoBytes = byteArrayOutputStream.toByteArray();
            encodedVideo = Base64.encodeToString(videoBytes, Base64.DEFAULT);

            Toast.makeText(getApplicationContext(), "Se ha guardado con exito en el Storage", Toast.LENGTH_LONG).show();

        }

        }
    }