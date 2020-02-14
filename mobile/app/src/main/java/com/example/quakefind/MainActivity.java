package com.example.quakefind;

import android.content.Context;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CameraView cameraView;
    private LocationManager locationManager;
    private boolean found;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private HashMap<Double, Double> lonlat;
    private ArrayList<Date> date;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private int personCount;
    private Vibrator v;
    private TextView tv;

    Runnable r = new Runnable() {
        @Override
        public void run() {
            getLocation();

        }
    };
    Handler h = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        personCount = 0;
        found = false;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("person/");
        lonlat = new HashMap<Double, Double>();
        date = new ArrayList<Date>();
        tv = findViewById(R.id.tv);
        cameraView = findViewById(R.id.cameraView);
        cameraView.setLifecycleOwner(this);
        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                extractDataFromFrame(frame);
                h.postDelayed(r, 10000);
            }
        });
    }


    private FirebaseVisionImage getVisionImageFromFrame(Frame frame) {
        byte[] data = frame.getData();
        FirebaseVisionImageMetadata imageMetaData = new FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(FirebaseVisionImageMetadata.ROTATION_90)
                .setHeight(frame.getSize().getHeight())
                .setWidth(frame.getSize().getWidth())
                .build();
        FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(data, imageMetaData);
        return image;
    }

    private void extractDataFromFrame(Frame frame) {
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .enableTracking()
                .build();

        FirebaseVisionFaceDetector faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        faceDetector.detectInImage(getVisionImageFromFrame(frame))
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        for (FirebaseVisionFace i : firebaseVisionFaces) {
                            Rect bounds = i.getBoundingBox();
                            found = true;
                            writeToDB();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ;
                    }
                });

    }

    private void getLocation() {
        if (found) {
            tv.setText("Person detected");
            v.vibrate(400);
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lonlat.put(location.getLongitude(), location.getLatitude());
                                date.add(Calendar.getInstance().getTime());
                            }
                        }
                    });
        }
        found = false;
    }

    private void writeToDB() {
        if (personCount<=100) {
            int dindex = 0;
            for (Double d : lonlat.keySet()) {
                reference.child("person" + String.valueOf(personCount)).child("longitude").setValue(d);
                reference.child("person" + String.valueOf(personCount)).child("latitude").setValue(lonlat.get(d));
                reference.child("person" + String.valueOf(personCount)).child("date").setValue(String.valueOf(date.get(dindex)));
                dindex++;
                personCount++;
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            tv.setText("No person detected");
        }
        return true;
    }

}

