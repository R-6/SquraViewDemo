package com.example.squraviewdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.squraviewdemo.model.ShapePosition;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ZoomLayout zoomlayout;

    ArrayList<ShapePosition> shapePositionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCusView();
    }

    void initCusView(){
        zoomlayout= this.findViewById(R.id.zoomLayout);
        CanvasLayout canvasLayout = this.findViewById(R.id.canvas);
        zoomlayout.init(MainActivity.this);
        zoomlayout.postDelayed(() -> {
            zoomlayout.setScale(zoomlayout.getMeasuredWidth()*1f/CanvasLayout.CANVAS_SIZE);
            canvasLayout.init();
        },50);

        canvasLayout.setCallBack((scale, point) -> {
            float sc = zoomlayout.getMeasuredWidth()*1f/CanvasLayout.CANVAS_SIZE / (scale*2);
            zoomlayout.setScaleAndTransitionXY(sc, point);
        });
//        canvasLayout.setShapeData(new ArrayList<>(), CanvasLayout.Type.defaultType);
        canvasLayout.setMType(CanvasLayout.Type.edit);
        canvasLayout.reSetCanvas(true);
//        zoomlayout.addCanvas(this);

        /*双击聚拢*/
        zoomlayout.setDoubleClickListener(v -> {
//            canvasLayout.move2defaultPosition();
            if (canvasLayout.getMDefaultOffsetPoint() != null){
                zoomlayout.setTransitionXY(canvasLayout.getMDefaultOffsetPoint());
            }
        });

        Button btnFocus = this.findViewById(R.id.btn_focus);
        Button btnRotation = this.findViewById(R.id.btn_rotation);
        Button btnDel = this.findViewById(R.id.btn_del);
        Button btnNext = this.findViewById(R.id.btn_next);
        Button btnCheck = this.findViewById(R.id.btn_check);
        Button btnInstallNext = this.findViewById(R.id.btn_install_next);
        Button btnCheckNext = this.findViewById(R.id.btn_check_next);
        Button btnColorMode = this.findViewById(R.id.btn_color_mode);

        btnFocus.setOnClickListener(v -> {
//            shapePositionList = canvasLayout.getShapeRotations();
//            canvasLayout.setShapeData(shapePositionList, CanvasLayout.Type.edit);
            if (canvasLayout.getMDefaultOffsetPoint() != null){
                zoomlayout.setTransitionXY(canvasLayout.getMDefaultOffsetPoint());
            }
        });
        btnRotation.setOnClickListener(v -> canvasLayout.setShapeRotation(30));
        btnDel.setOnClickListener(v -> {
            canvasLayout.reSetCanvas(true);
        });
        btnNext.setOnClickListener(v -> {
            shapePositionList = canvasLayout.getShapeRotations();
            canvasLayout.setShapeData(shapePositionList, CanvasLayout.Type.install);
        });
        btnCheck.setOnClickListener(v -> {
            shapePositionList = canvasLayout.getShapeRotations();
            canvasLayout.setShapeData(shapePositionList, CanvasLayout.Type.check);
        });
        btnInstallNext.setOnClickListener(v -> {
            if (canvasLayout.installNextShape() == null){
                Toast.makeText(this, "all_done", Toast.LENGTH_SHORT).show();
            }
        });
        btnCheckNext.setOnClickListener(v -> {
            if (canvasLayout.checkNextShape(1) == null){
                Toast.makeText(this, "all_done", Toast.LENGTH_SHORT).show();
            }
        });
        btnColorMode.setOnClickListener(v -> {
            shapePositionList = canvasLayout.getShapeRotations();
            canvasLayout.setShapeData(shapePositionList, CanvasLayout.Type.colorMode);
        });
    }




}