package com.witcherbb.bettersound;

import com.witcherbb.bettersound.blocks.utils.ShapeUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 方块朝北时的碰撞箱绕 Y 轴旋转。90/180/270 三个分支的公式各不相同，
 * 这里用 <b>坐标 1.0 减去原坐标</b> 的镜像关系逐个钉死，避免以后改错一个加减号。
 */
public class ShapeUtilTest {

    private static final double EPS = 1.0E-7;

    /** 待旋转的基准箱：故意取三个轴都不一样宽，这样 x/z 写反立刻暴露。 */
    private static VoxelShape base() {
        return Shapes.box(0.1D, 0.2D, 0.3D, 0.4D, 0.5D, 0.6D);
    }

    private static AABB onlyBox(VoxelShape shape) {
        List<AABB> boxes = shape.toAabbs();
        assertEquals("这个用例只处理单箱形状", 1, boxes.size());
        return boxes.get(0);
    }

    private static void assertBox(AABB box, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        assertEquals("minX", minX, box.minX, EPS);
        assertEquals("minY", minY, box.minY, EPS);
        assertEquals("minZ", minZ, box.minZ, EPS);
        assertEquals("maxX", maxX, box.maxX, EPS);
        assertEquals("maxY", maxY, box.maxY, EPS);
        assertEquals("maxZ", maxZ, box.maxZ, EPS);
    }

    @Test
    public void zeroDegreesIsIdentity() {
        VoxelShape shape = base();
        assertBox(onlyBox(ShapeUtil.rotateShapeY(shape, 0)),
                shape.min(Direction.Axis.X), shape.min(Direction.Axis.Y), shape.min(Direction.Axis.Z),
                shape.max(Direction.Axis.X), shape.max(Direction.Axis.Y), shape.max(Direction.Axis.Z));
    }

    @Test
    public void quarterTurnsMapXOntoZ() {
        // 90°：(x,z) -> (1-maxZ, minX .. 1-minZ, maxX)
        assertBox(onlyBox(ShapeUtil.rotateShapeY(base(), 90)), 0.4D, 0.2D, 0.1D, 0.7D, 0.5D, 0.4D);
        // 180°：两个水平轴同时镜像
        assertBox(onlyBox(ShapeUtil.rotateShapeY(base(), 180)), 0.6D, 0.2D, 0.4D, 0.9D, 0.5D, 0.7D);
        // 270°：与 90° 反向
        assertBox(onlyBox(ShapeUtil.rotateShapeY(base(), 270)), 0.3D, 0.2D, 0.6D, 0.6D, 0.5D, 0.9D);
    }

    @Test
    public void fourQuarterTurnsReturnToTheStart() {
        VoxelShape shape = base();
        for (int i = 0; i < 4; i++) {
            shape = ShapeUtil.rotateShapeY(shape, 90);
        }
        assertBox(onlyBox(shape), 0.1D, 0.2D, 0.3D, 0.4D, 0.5D, 0.6D);
    }

    @Test
    public void anglesAreFlooredToQuarterTurns() {
        // 不足一个 90° 的零头一律抹掉：89° 落回原样，179.9° 只够到第二档（=90°），269.9° 只够到第三档（=180°）
        assertBox(onlyBox(ShapeUtil.rotateShapeY(base(), 89)), 0.1D, 0.2D, 0.3D, 0.4D, 0.5D, 0.6D);
        assertBox(onlyBox(ShapeUtil.rotateShapeY(base(), 179.9D)), 0.4D, 0.2D, 0.1D, 0.7D, 0.5D, 0.4D);
        assertBox(onlyBox(ShapeUtil.rotateShapeY(base(), 269.9D)), 0.6D, 0.2D, 0.4D, 0.9D, 0.5D, 0.7D);
    }

    @Test
    public void fullCircleWrapsAround() {
        assertBox(onlyBox(ShapeUtil.rotateShapeY(base(), 360)), 0.1D, 0.2D, 0.3D, 0.4D, 0.5D, 0.6D);
        assertBox(onlyBox(ShapeUtil.rotateShapeY(base(), 450)), 0.4D, 0.2D, 0.1D, 0.7D, 0.5D, 0.4D);
    }

    /** 负角度会算出 -1 这种非 0..3 的档位，当前实现直接抛异常而不是给出错误形状。 */
    @Test
    public void negativeAnglesAreRejectedInsteadOfSilentlyWrong() {
        try {
            ShapeUtil.rotateShapeY(base(), -90);
            fail("负角度应当抛 IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Shape Wrong!", expected.getMessage());
        }
    }

    @Test
    public void eswShapesAreTheOtherThreeQuadrants() {
        VoxelShape[] esw = ShapeUtil.getESWShapes(base());
        assertEquals(3, esw.length);
        assertBox(onlyBox(esw[0]), 0.4D, 0.2D, 0.1D, 0.7D, 0.5D, 0.4D);   // 90°
        assertBox(onlyBox(esw[1]), 0.6D, 0.2D, 0.4D, 0.9D, 0.5D, 0.7D);   // 180°
        assertBox(onlyBox(esw[2]), 0.3D, 0.2D, 0.6D, 0.6D, 0.5D, 0.9D);   // 270°
    }

    @Test
    public void eswShapesOfAnEmptyShapeStayEmpty() {
        VoxelShape[] esw = ShapeUtil.getESWShapes(Shapes.empty());
        assertEquals(3, esw.length);
        for (VoxelShape shape : esw) {
            assertTrue("空形状旋转之后还是空的", shape.isEmpty());
            assertTrue(shape.toAabbs().isEmpty());
        }
    }
}
