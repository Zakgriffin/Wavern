package com.example.wavern;

import javafx.geometry.Point3D;

public class Transducer {
    static double speedOfSound = 343; // m/s

    Point3D position;

    double frequency;
    double amplitude;
    double phase;

    double wavelength;
    double angularFrequency;
    double waveNumber;

    public Transducer(Point3D position) {
        this.position = position;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;

        this.wavelength = speedOfSound / frequency;
        this.angularFrequency = frequency * 2 * Math.PI;
        this.waveNumber = 2 * Math.PI / wavelength;
    }

    public double pressureAt(Point3D point, double time) {
        double distance = position.distance(point);
        return amplitude * Math.cos(waveNumber * distance - angularFrequency * (time - phase));
    }

    public double findOptimalPhase(Point3D target) {
        double distance = position.distance(target);
        return -waveNumber * distance / angularFrequency;
    }

    public double findOptimalPhaseTwoPoints(Point3D target1, Point3D target2) {
        double distance1 = position.distance(target1);
        double distance2 = position.distance(target2);
        double p1 = waveNumber * distance1;
        double p2 = waveNumber * distance2;
        double h = Math.atan2(Math.sin(p1) + Math.sin(p2), Math.cos(p1) + Math.cos(p2));
        return -h / angularFrequency;
    }

    public double findOptimalPhaseNPoints(Point3D[] targets) {
        double sinComponent = 0;
        double cosComponent = 0;
        for (Point3D target : targets) {
            double distance = position.distance(target);
            double p = waveNumber * distance;
            sinComponent += Math.sin(p);
            cosComponent += Math.cos(p);
        }

        double h = Math.atan2(sinComponent, cosComponent);
        return -h / angularFrequency;
    }
}
