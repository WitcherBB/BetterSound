package com.witcherbb.bettersound.blocks.utils;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.concurrent.atomic.AtomicReference;

public class ShapeUtil {
    public static VoxelShape rotateShapeY(VoxelShape shape, double angle) {
        int times = (int) Math.floor(angle / 90) % 4;
        double minX = shape.min(Direction.Axis.X);
        double maxX = shape.max(Direction.Axis.X);
        double minY = shape.min(Direction.Axis.Y);
        double maxY = shape.max(Direction.Axis.Y);
        double minZ = shape.min(Direction.Axis.Z);
        double maxZ = shape.max(Direction.Axis.Z);
        double size = 1.0D;

        return switch (times) {
            case 0 -> Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
            case 1 -> Shapes.box(size - maxZ, minY, minX, size - minZ, maxY, maxX);
            case 2 -> Shapes.box(size - maxX, minY, size - maxZ, size - minX, maxY, size - minZ);
            case 3 -> Shapes.box(minZ, minY, size - maxX, maxZ, maxY, size - minX);
            default -> throw new IllegalStateException("Shape Wrong!");
        };
    }

    public static VoxelShape[] getESWShapes(VoxelShape nShape) {
        AtomicReference<VoxelShape> shape1 = new AtomicReference<>(Shapes.empty());
        AtomicReference<VoxelShape> shape2 = new AtomicReference<>(Shapes.empty());
        AtomicReference<VoxelShape> shape3 = new AtomicReference<>(Shapes.empty());
        nShape.forAllBoxes(((pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ) -> {
            VoxelShape shape = Shapes.create(pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ);
            shape1.set(Shapes.or(shape1.get(), rotateShapeY(shape, 90)));
            shape2.set(Shapes.or(shape2.get(), rotateShapeY(shape, 180)));
            shape3.set(Shapes.or(shape3.get(), rotateShapeY(shape, 270)));
        }));
        return new VoxelShape[]{shape1.get(), shape2.get(), shape3.get()};
    }
}
