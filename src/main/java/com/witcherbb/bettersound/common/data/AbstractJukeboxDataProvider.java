package com.witcherbb.bettersound.common.data;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractJukeboxDataProvider extends ModDataProvider<List<JukeboxEntityData>> {
    protected Map<Pair<String, String>, JukeboxEntityData> data;

    protected AbstractJukeboxDataProvider(String fileName, Type type, Codec<JukeboxEntityData> codec) {
        super(fileName, type, codec.listOf(), ArrayList::new);
        this.data = new HashMap<>();
    }

    @Override
    public List<JukeboxEntityData> getData() {
        return ImmutableList.<JukeboxEntityData>builder().addAll(data.values()).build();
    }

    public JukeboxEntityData get(String name, String dimension) {
        return this.data.get(new Pair<>(name, dimension));
    }

    // 是否可以通过监听方块加载过程添加方块数据
    @Override
    public void setData(List<JukeboxEntityData> vlist) {
        this.data = new HashMap<>();
        for (var v : vlist) {
            String k1 = v.getName();
            String k2 = v.getDimension();
            data.put(new Pair<>(k1, k2), v);
        }
    }

    public void addData(JukeboxEntityData jd) {
        this.data.put(new Pair<>(jd.getName(), jd.getDimension()), jd);
    }

    public void removeData(JukeboxEntityData jd) {
        this.data.remove(new Pair<>(jd.getName(), jd.getDimension()));
    }

    public abstract boolean exists(JukeboxEntityData jd);
}
