package com.example.squraviewdemo.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.squraviewdemo.R;
import com.example.squraviewdemo.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Create by linshicong on 4/22/21
 */
public class Shape {
    public static final int HEXAGON = 0;
    public static final int TRIANGLE = 1;
    public static final int ROUND = 2;

    public static Integer[] supportShape = new Integer[]{HEXAGON};

    public static int getTypeIc(int type) {
        switch (type) {
            case HEXAGON:
                return 6;
            case TRIANGLE:
                return 3;
            case ROUND:
                return 5;
            default:
                return 0;
        }
    }


    public static List<Integer> filterSupportType(@NonNull List<Integer> list) {
        List<Integer> supportList = Arrays.asList(supportShape);
        Iterator<Integer> iterator = list.iterator();
        if (iterator.hasNext()) {
            Integer next = iterator.next();
            if (!supportList.contains(next)) {
                iterator.remove();
            }
        }
        return list;
    }

    public static ArrayList<ShapePosition> getShapes(String shapes) {
        ArrayList<ShapePosition> shapePositions = new ArrayList<>();
        if (shapes == null || TextUtils.isEmpty(shapes))
            return shapePositions;
        byte[] bytes = Utils.decryByBase64(shapes);
        if (bytes == null) return shapePositions;
        int size = bytes[0];
        if (bytes.length != 1 + 7 * size) {

            return shapePositions;
        }
        int pos = 1;
        for (int i = 0; i < size; i++) {
            int type = bytes[pos++];
            int x = Utils.convertTwoBytesToShort(bytes[pos++], bytes[pos++]);
            int y = Utils.convertTwoBytesToShort(bytes[pos++], bytes[pos++]);
            int angle = Utils.convertTwoBytesToShort(bytes[pos++], bytes[pos++]);
            shapePositions.add(new ShapePosition(type, x, y, angle));
        }
        return shapePositions;
    }

    public static final int op_type_splice = 0;
    public static final int op_type_calibration = 1;
    public static final int op_type_adjust = 2;


    public static boolean isOpSplice(int opType) {
        return opType == op_type_splice;
    }

    public static boolean isOpCalibration(int opType) {
        return opType == op_type_calibration;
    }

    public static boolean isOpAdjust(int opType) {
        return opType == op_type_adjust;
    }
}
