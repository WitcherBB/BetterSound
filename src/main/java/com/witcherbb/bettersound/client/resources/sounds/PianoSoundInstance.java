package com.witcherbb.bettersound.client.resources.sounds;

import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.mixins.extenders.SoundInstanceExtender;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.piano.SPianoKeyReleasedPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class PianoSoundInstance extends AbstractTickableSoundInstance implements SoundInstanceExtender {
    private int tickCount = 0;
    private int lastTick = 0;
    private boolean isShort;
    private boolean wasSetStopped;
    private short shortDelay;
    private int tone;
    private UUID playerUUID;
    private BlockPos pos;
    private final float firstVolume;
    private final float range;

    private static final float FULL_VOLUME = 3.0F;
    /** UI 虚拟音源与听者的固定距离（米）。配合 {@link SoundInstance.Attenuation#NONE}，距离不参与音量计算，只决定声道 */
    private static final double UI_VIRTUAL_DISTANCE = 1.0D;
    /** 88 键的中央音调索引（0~87 的中值），把音调映射为 [-1, 1] 的声道偏移 */
    private static final double UI_TONE_CENTER = 43.5D;
    /** 声道偏移半宽：pan = (tone - 43.5) / 44.0，最左键 ≈ 左声道，最右键 ≈ 右声道 */
    private static final double UI_PAN_HALF_WIDTH = 44.0D;
    public static final Minecraft MC = Minecraft.getInstance();

    public static PianoSoundInstance forUI(SoundEvent soundEvent, BlockPos pos, Vec3 relative, UUID playerUUID, int tone) {
        return forUI(soundEvent, pos, relative, playerUUID, tone, FULL_VOLUME);
    }

    public static PianoSoundInstance forUI(SoundEvent soundEvent, BlockPos pos, Vec3 relative, UUID playerUUID, int tone, float volume) {
        return forUIRelative(soundEvent, pos, relative, playerUUID, tone, volume);
    }

    public static PianoSoundInstance forUIRelative(SoundEvent soundEvent, BlockPos pos, Vec3 relative, UUID playerUUID, int tone, float volume) {
        // UI 弹奏不走世界坐标：isRelative=false 让引擎按 OpenAL 音源方位做左右声道分离，
        // Attenuation.NONE 关闭距离衰减（音量恒定），音源位置由 updateUIPosition() 每 tick 跟随相机重算。
        // relative 参数为旧签名遗留，UI 发声不再使用任何世界坐标。
        PianoSoundInstance instance = new PianoSoundInstance(soundEvent, volume, 1.0F, 0.0D, 0.0D, 0.0D, false, false);
        instance.attenuation = SoundInstance.Attenuation.NONE;
        instance.setPos(pos);
        instance.setPlayerUUID(playerUUID);
        instance.setTone(tone);
        instance.updateUIPosition();
        return instance;
    }

    public static PianoSoundInstance forBlock(SoundEvent soundEvent, BlockPos pos, UUID playerUUID, int tone, boolean isShort) {
        return forPosition(soundEvent, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, playerUUID, tone, FULL_VOLUME, isShort);
    }

    public static PianoSoundInstance forBlock(SoundEvent soundEvent, BlockPos pos, UUID playerUUID, int tone, float volume, boolean isShort) {
        return forPosition(soundEvent, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, playerUUID, tone, volume, isShort);
    }

    public static PianoSoundInstance forPosition(SoundEvent soundEvent, Vec3 vec3, UUID playerUUID, int tone, float volume, boolean isShort) {
        return forPosition(soundEvent, vec3.x, vec3.y, vec3.z, playerUUID, tone, volume, isShort);
    }

    public static PianoSoundInstance forPosition(SoundEvent soundEvent, double x, double y, double z, UUID playerUUID, int tone, float volume, boolean isShort) {
        PianoSoundInstance instance = new PianoSoundInstance(soundEvent, volume, 1.0F, x, y, z, false, isShort);
        instance.setPlayerUUID(playerUUID);
        instance.setTone(tone);
        return instance;
    }

    private PianoSoundInstance(SoundEvent soundEvent, float volume, float pitch, double x, double y, double z, boolean relative, boolean isShort) {
        super(soundEvent, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.range = soundEvent.getRange(volume);
        this.volume = volume;
        this.firstVolume = volume;
        this.pitch = pitch;
        this.relative = relative;
        this.looping = false;
        this.x = x;
        this.y = y;
        this.z = z;
        if (!this.relative) this.pos = BlockPos.containing(this.x, this.y, this.z);
        this.isShort = isShort;
        this.shortDelay = 2;
    }

    @Override
    public void tick() {
        this.tickCount++;
        if (!this.relative) {
            // 每 tick 跟随相机重算虚拟音源位置：左右声道只与音调有关，与玩家移动、转向及世界坐标无关
            this.updateUIPosition();
        }
        if (wasSetStopped) {
            this.volume += this.getDeltaVolume(this.tickCount - this.lastTick);
            if (this.volume <= 0) {
                this.stop();
            }
        }
        if (isShort && (--shortDelay <= 0)) {
            this.setStoped();
            this.isShort = false;
        }
    }

    /**
     * 将 UI 弹奏的虚拟音源锁定在听者正前方 {@value UI_VIRTUAL_DISTANCE} 米处，并按音调在听者左右方向偏移：
     * 音调越低越靠左、越高越靠右。由于该实例的衰减模式为 {@link SoundInstance.Attenuation#NONE}，
     * 音源距离不参与音量计算，只有方向参与左右声道计算，因此与钢琴和玩家的世界坐标完全无关。
     */
    private void updateUIPosition() {
        if (MC.gameRenderer.getMainCamera().isInitialized()) {
            Camera camera = MC.gameRenderer.getMainCamera();
            Vec3 camPos = camera.getPosition();
            Vec3 look = new Vec3(camera.getLookVector());
            Vec3 right = look.cross(new Vec3(camera.getUpVector())).normalize();
            double pan = ((double) this.tone - UI_TONE_CENTER) / UI_PAN_HALF_WIDTH;
            this.x = camPos.x + look.x * UI_VIRTUAL_DISTANCE + right.x * pan;
            this.y = camPos.y + look.y * UI_VIRTUAL_DISTANCE + right.y * pan;
            this.z = camPos.z + look.z * UI_VIRTUAL_DISTANCE + right.z * pan;
        }
    }

    public void setStoped() {
        this.wasSetStopped = true;
        this.lastTick = this.tickCount;
    }

    @Override
    public boolean canPlaySound() {
        // UI 弹奏不参与世界距离判定，音量恒定
        if (!this.relative && MC.player != null) {
            return MC.player.position().distanceToSqr(this.x, this.y, this.z) < this.range * this.range;
        }
        return super.canPlaySound();
    }

    private float getDeltaVolume(int tick) {
        return -1.74F / (tick * tick) * this.firstVolume / FULL_VOLUME - 0.015F;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void setTone(int tone) {
        this.tone = tone;
    }

    @Override
    public void betterSound$onStop() {
        //判断是否是手动停止很有必要！！！这样就不会在声音彻底停止以后再清除一次，否则可能会把清除之前播放的另一个音清除掉！！！
        if (!this.wasSetStopped) {
            ModSoundManager.INSTANCE.getPlayingPianoNotes().removeFirst(this.pos, this.playerUUID, this.tone);
        }
//        ModNetwork.sendToServer(new SPianoKeyReleasedPacket(positions, this.tone, true));
    }
}
