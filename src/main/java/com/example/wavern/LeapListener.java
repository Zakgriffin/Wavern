package com.example.wavern;

import com.leapmotion.leap.*;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.util.HashMap;
import java.util.Map;

public class LeapListener extends Listener {
    Map<Finger.Type, Map<Bone.Type, Cylinder>> fingerVisuals;

    public LeapListener(Group root) {
        fingerVisuals = new HashMap<>();

        for (Finger.Type fingerType : Finger.Type.values()) {
            Map<Bone.Type, Cylinder> boneMap = new HashMap<>();
            for (Bone.Type boneType : Bone.Type.values()) {
                Cylinder bone = new Cylinder(0.005, 0);
                boneMap.put(boneType, bone);
                root.getChildren().add(bone);
            }
            fingerVisuals.put(fingerType, boneMap);
        }
    }

    public void updateConnection(Cylinder line, Point3D origin, Point3D target) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        line.setHeight(height);
        line.getTransforms().clear();
        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
    }

    public void onFrame(Controller controller) {
        Platform.runLater(() -> {
            Frame frame = controller.frame();

            for (Hand hand : frame.hands()) {
                for (Finger finger : hand.fingers()) {
                    Finger.Type fingerType = finger.type();
                    for (Bone.Type boneType : Bone.Type.values()) {
                        Bone bone = finger.bone(boneType);

                        Cylinder boneVisual = fingerVisuals.get(fingerType).get(boneType);
                        Point3D prev = leapVectorToPoint(bone.prevJoint());
                        Point3D next = leapVectorToPoint(bone.nextJoint());

                        updateConnection(boneVisual, prev, next);
                    }
                    if (frame.id() % 10 == 0 && finger.type() == Finger.Type.TYPE_INDEX) {
                        Vector tip = finger.jointPosition(Finger.Joint.JOINT_TIP);
                        Point3D e = leapVectorToPoint(tip);

                        Wavern.waveSimulation.updateTransducersToTargetPoints(new Point3D[]{e});
                        Wavern.waveSimulation.updateAllPressures();
                    }
                }
            }
        });
    }


    public static Point3D leapVectorToPoint(Vector leapVector) {
        return new Point3D(
                leapVector.getX(),
                -leapVector.getY(),
                -leapVector.getZ()
        ).multiply(0.001).add(0.08,0.16,0.08);
    }
}
