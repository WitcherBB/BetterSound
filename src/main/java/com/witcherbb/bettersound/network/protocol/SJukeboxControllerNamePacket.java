package com.witcherbb.bettersound.network.protocol;

import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.entity.JukeboxControllerBlockEntity;
import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;
import com.witcherbb.bettersound.menu.inventory.JukeboxControllerMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public record SJukeboxControllerNamePacket(String name, String dimension) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(this.name);
		buf.writeUtf(this.dimension);
	}

	public static SJukeboxControllerNamePacket decode(FriendlyByteBuf buf) {
		String name = buf.readUtf();
		String dimension = buf.readUtf();;
		return new SJukeboxControllerNamePacket(name, dimension);
	}

	public static void handle(SJukeboxControllerNamePacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			if (sender == null) return;
			//Do Stuff
			AbstractContainerMenu abstractContainerMenu = sender.containerMenu;
			if (abstractContainerMenu instanceof JukeboxControllerMenu jukeboxControllerMenu) {
				if (!jukeboxControllerMenu.stillValid(sender)) {
					BetterSound.LOGGER.debug("Player {} interacted with invalid menu {}", sender, sender.containerMenu);
					return;
				}
				JukeboxControllerBlockEntity blockEntity = jukeboxControllerMenu.getBlockEntity();
				if (blockEntity instanceof JukeboxControllerBlockEntity) {
					CompoundTag nbt = blockEntity.getUpdateTag();
					String oldName = nbt.getString("Name");
					nbt.putString("Name", packet.getName());
					blockEntity.load(nbt);
					if (!packet.getName().isEmpty()) {
						JukeboxControllerBlockEntity.getProvider().addData(new JukeboxEntityData(packet.getName(), packet.dimension), JukeboxEntityData.createPos(blockEntity.getBlockPos()));
					}
					if (blockEntity.getLevel() != null && !oldName.isEmpty() && !oldName.equals(packet.getName())) {
						JukeboxControllerBlockEntity.removeControllerAndJukebox(oldName, blockEntity.getLevel(), blockEntity.getBlockPos());
					}
				}

			}
		});
		ctx.get().setPacketHandled(true);
	}

	public String getName() {
		return this.name;
	}
}
