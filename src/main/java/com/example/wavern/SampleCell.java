package com.example.wavern;

import javafx.geometry.Point3D;

public class SampleCell {
    Point3D position;
    double maxSize;

    public SampleCell(Point3D position, double maxSize) {
        this.position = position;
        this.maxSize = maxSize;
    }
}
