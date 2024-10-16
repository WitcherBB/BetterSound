package com.witcherbb.bettersound.network.protocol.server;

import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.entity.ExampleBlockEntity;
import com.witcherbb.bettersound.menu.inventory.ExampleMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public record SExampleNameChangedPacket(String name) {

	public void encode(FriendlyByteBuf pBuffer) {
		pBuffer.writeBytes(this.name.getBytes(StandardCharsets.UTF_8));
	}

	public static SExampleNameChangedPacket decode(FriendlyByteBuf buf) {
		return new SExampleNameChangedPacket(buf.toString(StandardCharsets.UTF_8));
	}

	public static void handle(SExampleNameChangedPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			if (sender == null) return;
			//Do Stuff
			AbstractContainerMenu abstractContainerMenu = sender.containerMenu;
			if (abstractContainerMenu instanceof ExampleMenu exampleMenu) {
				if (!exampleMenu.stillValid(sender)) {
					BetterSound.LOGGER.debug("Player {} interacted with invalid menu {}", sender, sender.containerMenu);
					return;
				}
				ExampleBlockEntity blockEntity = exampleMenu.getBlockEntity();
				CompoundTag nbt = blockEntity.getUpdateTag();
				nbt.putString("Name", packet.name());
				blockEntity.load(nbt);
			}
		});
		ctx.get().setPacketHandled(true);
	}


}
