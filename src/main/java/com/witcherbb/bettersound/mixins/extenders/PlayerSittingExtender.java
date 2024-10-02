package com.witcherbb.bettersound.mixins.extenders;

import com.witcherbb.bettersound.blocks.entity.utils.Sittable;

public interface PlayerSittingExtender {
    Sittable<?> getSeat();

    boolean isSitting();

    void stopSitting();

    void dismountVehicle(Sittable<?> seat);

    boolean startSitting(Sittable<?> pSeat);
}
