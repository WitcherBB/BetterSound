package com.witcherbb.bettersound.network.protocol.server;

import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.blocks.entity.ExampleBlockEntity;
import com.witcherbb.bettersound.menu.inventory.ExampleMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.nio.charset.StandardCharsets;

public record SExampleNameChangedPacket(String name) {

	public void encode(FriendlyByteBuf pBuffer) {
		pBuffer.writeBytes(this.name.getBytes(StandardCharsets.UTF_8));
	}

	public static SExampleNameChangedPacket decode(FriendlyByteBuf buf) {
		return new SExampleNameChangedPacket(buf.toString(StandardCharsets.UTF_8));
	}

	public static void handle(SExampleNameChangedPacket packet, PacketContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer sender = ctx.sender();
			if (sender == null) return;
			//Do Stuff
			AbstractContainerMenu abstractContainerMenu = sender.containerMenu;
			if (abstractContainerMenu instanceof ExampleMenu exampleMenu) {
				if (!exampleMenu.stillValid(sender)) {
					Constants.LOGGER.debug("Player {} interacted with invalid menu {}", sender, sender.containerMenu);
					return;
				}
				ExampleBlockEntity blockEntity = exampleMenu.getBlockEntity();
				CompoundTag nbt = blockEntity.getUpdateTag();
				nbt.putString("Name", packet.name());
				blockEntity.load(nbt);
			}
		});
	}

}
