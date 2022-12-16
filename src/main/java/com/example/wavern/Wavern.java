package com.example.wavern;

import com.leapmotion.leap.Controller;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;


public class Wavern extends Application {
    Controller leapController;
    LeapListener leapListener;

    int WIDTH = 1400;
    int HEIGHT = 800;

    static WaveSimulation waveSimulation;

    @Override
    public void start(Stage stage) {
        Group root = new Group();

        waveSimulation = new WaveSimulation(root);

        // orbital camera
        Point3D c = waveSimulation.getCenterPosition();
        Translate pivot = new Translate(c.getX(), c.getY(), c.getZ());
        Rotate pitchRotate = new Rotate(-20, Rotate.X_AXIS);
        Rotate yawRotate = new Rotate(-20, Rotate.Y_AXIS);

        Translate backup = new Translate(0, 0, -1);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setFarClip(-1); // no far clip plane
        camera.getTransforms().addAll(
                pivot,
                yawRotate,
                pitchRotate,
                backup
        );

        root.getChildren().add(camera);

        // just a little reference point
        Box corner = new Box(0.01, 0.01, 0.01);
        corner.setMaterial(new PhongMaterial(Color.color(0.1, 0.1, 0.1, 0.3)));
        root.getChildren().add(corner);

        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setCamera(camera);


        // orbital camera events
        Translate oldPos = new Translate();
        scene.setOnMousePressed((e) -> {
            oldPos.setX(e.getSceneX());
            oldPos.setY(e.getSceneY());
        });
        scene.setOnMouseDragged((e) -> {
            double deltaX = e.getSceneX() - oldPos.getX();
            double deltaY = e.getSceneY() - oldPos.getY();
            pitchRotate.setAngle(pitchRotate.getAngle() - deltaY * 0.1);
            yawRotate.setAngle(yawRotate.getAngle() + deltaX * 0.1);

            oldPos.setX(e.getSceneX());
            oldPos.setY(e.getSceneY());
        });

        scene.setOnScroll((e) -> {
            if (e.isAltDown()) {
                double time = waveSimulation.getTime();
                waveSimulation.setTime(time + e.getDeltaY() * 0.0000005);

                waveSimulation.updateAllPressures();
            } else {
                double newBackup = backup.getZ() + e.getDeltaY() * 0.002;
                if(newBackup > 0) newBackup = 0;
                backup.setZ(newBackup);
            }
        });

        // leap motion stuff
        leapController = new Controller();
        leapListener = new LeapListener(root);
        leapController.addListener(leapListener);

        stage.setTitle("Wavern");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        leapController.removeListener(leapListener);
    }
}
