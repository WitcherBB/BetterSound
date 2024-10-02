package com.witcherbb.bettersound.mixins.extenders;

import com.witcherbb.bettersound.blocks.entity.utils.Sittable;

public interface CouldSittingExtender {
    Sittable<?> betterSound$getSeat();

    boolean betterSound$isSitting();

    void betterSound$stopSitting();

    void betterSound$dismountSeat(Sittable<?> seat);

    boolean betterSound$startSitting(Sittable<?> pSeat);
}
