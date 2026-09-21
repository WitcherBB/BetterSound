package com.witcherbb.bettersound;

import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 方块实体的每刻回调。{@code createTicker()} 会把 tick 里的异常整个吞掉，
 * 这是刻逻辑“坏一个方块不能拖垮整个区块”的兜底，所以两个方向都要钉住：
 * 正常情况必须真的被调到，异常情况必须不往外冒。
 */
public class TickableBlockEntityTest {

    /** 只为测试而生的方块实体：不碰任何注册表，直接构造。 */
    private static final class FakeBlockEntity extends BlockEntity implements TickableBlockEntity {
        private final boolean explode;
        private int ticks;

        private FakeBlockEntity(boolean explode) {
            super(null, BlockPos.ZERO, null);
            this.explode = explode;
        }

        @Override
        public void tick() {
            this.ticks++;
            if (this.explode) {
                throw new IllegalStateException("tick 里炸了");
            }
        }
    }

    @Test
    public void tickerForwardsEveryCallToTick() {
        FakeBlockEntity blockEntity = new FakeBlockEntity(false);
        BlockEntityTicker<FakeBlockEntity> ticker = TickableBlockEntity.createTicker();

        ticker.tick(null, BlockPos.ZERO, null, blockEntity);
        ticker.tick(null, BlockPos.ZERO, null, blockEntity);
        ticker.tick(null, BlockPos.ZERO, null, blockEntity);

        assertEquals(3, blockEntity.ticks);
    }

    @Test
    public void tickerSwallowsExceptionsAndKeepsGoing() {
        FakeBlockEntity blockEntity = new FakeBlockEntity(true);
        BlockEntityTicker<FakeBlockEntity> ticker = TickableBlockEntity.createTicker();

        ticker.tick(null, BlockPos.ZERO, null, blockEntity);
        ticker.tick(null, BlockPos.ZERO, null, blockEntity);

        assertEquals("异常被吞掉之后，下一次刻回调仍然照常执行", 2, blockEntity.ticks);
    }
}
