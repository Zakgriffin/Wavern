package com.example.wavern;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class WaveSimulation {
    private Map<Transducer, Cylinder> transducerToVisuals;
    private Map<SampleCell, Box> sampleCellToVisuals;
    private final Group transducerGroup = new Group();
    private final Group sampleCellGroup = new Group();

    int size = 16;

    private double time; // seconds
    private final double maxValue = size * size;
    private final Point3D centerPosition;

    private final Function<SampleCell, Double> pressureForSampleCell = (e) -> {
        List<Transducer> a = transducerToVisuals.keySet().stream().toList();
        return pressureFromTransducersAt(a, e.position, time);
    };

    private final static double CM = 0.01; // centimeters

    public WaveSimulation(Group root) {
        root.getChildren().add(transducerGroup);
        root.getChildren().add(sampleCellGroup);

        double c = size * CM / 2;
        this.centerPosition = new Point3D(c, c, c);

        List<Transducer> transducerGrid = create2DTransducerGrid(size, size, CM, size * CM);
        updateTransducers(transducerGrid);

        List<SampleCell> sampleCellGrid = create3DSampleCellGrid(size * CM, size);
        updateSampleCells(sampleCellGrid);

        updateAllPressures();
    }

    public List<SampleCell> create3DSampleCellGrid(double size, int samplesPerAxis) {
        double spacing = size / samplesPerAxis;

        List<SampleCell> sampleCells = new ArrayList<>();
        for (int x = 0; x < samplesPerAxis; x++) {
            for (int y = 0; y < samplesPerAxis; y++) {
                for (int z = 0; z < samplesPerAxis; z++) {
                    Point3D position = new Point3D(x * spacing, y * spacing, z * spacing);

                    SampleCell sampleCell = new SampleCell(position, spacing);
                    sampleCells.add(sampleCell);
                }
            }
        }
        return sampleCells;
    }

    public void updateSampleCells(List<SampleCell> sampleCells) {
        sampleCellGroup.getChildren().removeAll();

        sampleCellToVisuals = new HashMap<>();
        for (SampleCell sampleCell : sampleCells) {
            double boxSize = sampleCell.maxSize * 0.5;
            Box sampleCellVisual = new Box(boxSize, boxSize, boxSize);

            sampleCellToVisuals.put(sampleCell, sampleCellVisual);

            PhongMaterial mat = new PhongMaterial();

            sampleCellVisual.setMaterial(mat);
            Point3D position = sampleCell.position;
            sampleCellVisual.setTranslateX(position.getX());
            sampleCellVisual.setTranslateY(position.getY());
            sampleCellVisual.setTranslateZ(position.getZ());

            sampleCellGroup.getChildren().add(sampleCellVisual);
        }
    }

    public static List<Transducer> create2DTransducerGrid(int sizeX, int sizeZ, double spacing, double height) {
        List<Transducer> transducers = new ArrayList<>();
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                Transducer transducer = new Transducer(new Point3D(
                        x * spacing,
                        height,
                        z * spacing
                ));

                transducer.amplitude = 1;
                transducer.setFrequency(44000);

                transducers.add(transducer);
            }
        }
        return transducers;
    }

    public void updateTransducers(List<Transducer> transducers) {
        transducerGroup.getChildren().removeAll();

        transducerToVisuals = new HashMap<>();

        for (Transducer transducer : transducers) {
            Point3D position = transducer.position;

            Cylinder transducerVisual = new Cylinder(0.005, 0.004);
            transducerToVisuals.put(transducer, transducerVisual);

            transducerVisual.setOnMouseClicked((e) -> System.out.println(e.getX()));

            PhongMaterial mat = new PhongMaterial();
            transducerVisual.setMaterial(mat);

            transducerVisual.setTranslateX(position.getX());
            transducerVisual.setTranslateY(position.getY());
            transducerVisual.setTranslateZ(position.getZ());

            transducerGroup.getChildren().add(transducerVisual);
        }
    }

    public Point3D getCenterPosition() {
        return centerPosition;
    }

    public void updateTransducer(Transducer transducer) {
        Cylinder cylinder = transducerToVisuals.get(transducer);
        Color color = phaseToColor(transducer);
        ((PhongMaterial) cylinder.getMaterial()).setDiffuseColor(color);
    }

    public void updateTransducersToTargetPoints(Point3D[] points) {
        for (Transducer transducer : transducerToVisuals.keySet()) {
            transducer.phase = transducer.findOptimalPhaseNPoints(points);
            updateTransducer(transducer);
        }
    }

    public Color pressureToColor(double pressure) {
        double positive = Math.max(pressure, 0);
        double negative = -Math.min(pressure, 0);

        return Color.color(positive / maxValue, 0, negative / maxValue, 0.5);
    }

    public double pressureFromTransducersAt(List<Transducer> transducers, Point3D point, double time) {
        double sum = 0;
        for (Transducer transducer : transducers) {
            sum += transducer.pressureAt(point, time);
        }
        return sum;
    }

    public void updateAllPressures() {
        for (SampleCell sampleCell : sampleCellToVisuals.keySet()) {
            double pressure = pressureForSampleCell.apply(sampleCell);
            setPressureForSampleCellVisual(sampleCellToVisuals.get(sampleCell), pressure);
        }
    }

    public void setPressureForSampleCellVisual(Box sampleCellVisual, double pressure) {
        Color color = pressureToColor(pressure);
        ((PhongMaterial) sampleCellVisual.getMaterial()).setDiffuseColor(color);
    }

    public Color phaseToColor(Transducer transducer) {
        double a = transducer.phase * transducer.angularFrequency / Math.PI;

        double positive = Math.max(a, 0);
        double negative = -Math.min(a, 0);

        return Color.color(positive, 1, negative);
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
