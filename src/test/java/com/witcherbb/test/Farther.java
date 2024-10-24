package com.witcherbb.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Farther(int age) {
    public static final Codec<Farther> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("age").forGetter(Farther::age)
                    ).apply(instance, Farther::new )
    );

    public static PrimitiveCodec<Farther> FARTHER = new PrimitiveCodec<Farther>() {
        @Override
        public <T> DataResult<Farther> read(DynamicOps<T> ops, T input) {
            return CODEC.parse(ops, input);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Farther value) {
            return CODEC.encodeStart(ops, value).result().orElse(null);
        }
    };

    @Override
    public String toString() {
        return "Farther{" +
                "age=" + age +
                '}';
    }
}
