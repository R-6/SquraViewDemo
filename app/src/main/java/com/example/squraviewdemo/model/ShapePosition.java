package com.example.squraviewdemo.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Create by linshicong on 4/22/21
 */
public class ShapePosition implements Parcelable {
    public int type;
    public int x;
    public int y;
    public int angle;

    public ShapePosition(int type, int x, int y, int angle) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.angle = angle;
    }


    protected ShapePosition(Parcel in) {
        type = in.readInt();
        x = in.readInt();
        y = in.readInt();
        angle = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(x);
        dest.writeInt(y);
        dest.writeInt(angle);
    }

    @Override
    public String toString() {
        return "ShapePosition{" +
                "type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", angle=" + angle +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ShapePosition> CREATOR = new Creator<ShapePosition>() {
        @Override
        public ShapePosition createFromParcel(Parcel in) {
            return new ShapePosition(in);
        }

        @Override
        public ShapePosition[] newArray(int size) {
            return new ShapePosition[size];
        }
    };
}
