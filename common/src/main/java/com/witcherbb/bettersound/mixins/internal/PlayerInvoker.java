package com.witcherbb.bettersound.mixins.internal;

import com.witcherbb.bettersound.blocks.entity.utils.Sittable;

public interface PlayerInvoker {
    boolean bs$startSittingF(Sittable<?> pSeat);

    void bs$stopSittingF();
}
