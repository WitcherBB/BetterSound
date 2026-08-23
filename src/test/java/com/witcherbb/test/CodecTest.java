package com.witcherbb.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.List;
import java.util.Optional;

public class CodecTest {
    public static final Codec<Example> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("age").forGetter(Example::age),
                    Codec.STRING.fieldOf("name").forGetter(Example::name),
                    Farther.FARTHER.fieldOf("farther").forGetter(Example::farther)
            ).apply(instance, Example::new)
    );

    public static void main(String[] args) {
        CompoundTag fartherTag = new CompoundTag();
        fartherTag.putInt("age", 44);

        CompoundTag tag = new CompoundTag();
        tag.putInt("age", 21);
        tag.putString("name", "hry");
        tag.put("farther", fartherTag);

        DataResult<Tag> xhj = CODEC.encodeStart(NbtOps.INSTANCE, new Example(18, "xhj", new Farther(40)));

//        System.out.println(xhj.getOrThrow(false, System.out::println));
//        System.out.println(CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(null));
        Optional<Tag> tag1 = BlockPos.CODEC.listOf().encodeStart(NbtOps.INSTANCE,
                List.of(new BlockPos(1, 1, 1), new BlockPos(2, 3, 4))).result();
        tag1.ifPresent(pTag -> {
            CompoundTag iTag = new CompoundTag();
            iTag.put("positions", pTag);
            System.out.println(iTag);
        });
    }
}
