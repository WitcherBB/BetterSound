package com.witcherbb.bettersound.common.platform;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 各 loader 之间「行为不同」的那部分：
 * common 里只用这里的接口，具体实现放在 forge（以及将来的 fabric）模块。
 */
public interface LoaderHooks {

    /**
     * 音符盒音高变更钩子。
     *
     * <p>Forge 会在这里触发 {@code NoteBlockEvent.Change}，允许其它 mod 改写音高（返回 -1 表示取消）；
     * 其它 loader 没有对应事件时直接原样返回 {@code newNote}。
     *
     * @return 最终生效的音高，-1 表示这次变更被取消
     */
    int onNoteChange(Level level, BlockPos pos, BlockState state, int oldNote, int newNote);

    /** 服务端打开一个菜单界面（把界面与方块位置关联）。 */
    void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos);

    /** 当前运行的服务器实例；没有（比如纯客户端）时返回 {@code null}。 */
    @Nullable
    MinecraftServer currentServer();

    /**
     * 创建一个「只在 GUI 里生效」的按键绑定。
     *
     * <p>Forge 用 {@code KeyConflictContext.GUI} 限定冲突范围；vanilla / 其它 loader 没有这个概念，
     * 直接 new 一个普通 {@link KeyMapping} 即可。
     */
    KeyMapping createGuiKeyMapping(String name, InputConstants.Type type, int keyCode, String category);

    /** 把按键绑定序列化成 options 文件里的一行（Forge 会额外带上按键修饰键）。 */
    String serializeKeyMapping(KeyMapping mapping);

    /** 把 options 文件里读到的字符串还原到按键绑定上（与 {@link #serializeKeyMapping} 配对）。 */
    void applySerializedKeyMapping(KeyMapping mapping, String serialized);

    /**
     * 创建一个方块实体类型。
     *
     * <p>vanilla 的 {@code BlockEntityType.Builder.of(...)} 参数类型
     * （{@code BlockEntityType.BlockEntitySupplier}）不是 public，common 侧无法调用，
     * 所以这一步交给 loader 实现（Forge 那边是公开的）。
     */
    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BlockEntityFactory<T> factory, Block... validBlocks);
}
