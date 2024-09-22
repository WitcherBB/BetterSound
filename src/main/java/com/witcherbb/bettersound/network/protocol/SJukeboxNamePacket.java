package com.witcherbb.bettersound.network.protocol;

import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.entity.JukeboxControllerBlockEntity;
import com.witcherbb.bettersound.common.utils.Util;
import com.witcherbb.bettersound.common.data.JukeboxEntityDataProvider;
import com.witcherbb.bettersound.menu.inventory.JukeboxMenu;
import com.witcherbb.bettersound.network.ModNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SJukeboxNamePacket(String name, String dimension) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(this.name);
		buf.writeUtf(this.dimension);
	}

	public static SJukeboxNamePacket decode(FriendlyByteBuf buf) {
		String name = buf.readUtf();
		String dimension = buf.readUtf();
		return new SJukeboxNamePacket(name, dimension);
	}

	public static void handle(SJukeboxNamePacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			if (sender == null) return;
			//Do Stuff
			AbstractContainerMenu abstractContainerMenu = sender.containerMenu;
			if (abstractContainerMenu instanceof JukeboxMenu jukeboxMenu) {
				if (!jukeboxMenu.stillValid(sender)) {
					BetterSound.LOGGER.debug("Player {} interacted with invalid menu {}", sender, sender.containerMenu);
					return;
				}
				JukeboxEntityDataProvider provider = JukeboxControllerBlockEntity.getProvider();

				boolean existFlag = provider.exists(packet.name, packet.dimension);
				if (!packet.name.isEmpty() && existFlag) {
					JukeboxBlockEntity blockEntity = jukeboxMenu.getBlockEntity();
					CompoundTag nbt = blockEntity.getUpdateTag();
					String oldName = nbt.getString("Name");
					String oldDimension = blockEntity.getLevel().dimension().location().getPath();
					nbt.putString("Name", packet.name);
					blockEntity.load(nbt);

					BlockPos blockPos = blockEntity.getBlockPos();
					JukeboxControllerBlockEntity.removeFromList(oldName, oldDimension, blockPos);
					JukeboxControllerBlockEntity.putPos2List(nbt.getString("Name"), sender.level().dimension().location().getPath(), blockPos);
					ModNetwork.sendToPlayer(new CJukeboxNameConfirmPacket(Util.Status.SUCCESS), sender);
				} else if (packet.name.isEmpty()) {
					ModNetwork.sendToPlayer(new CJukeboxNameConfirmPacket(Util.Status.NULL), sender);
				} else {
					ModNetwork.sendToPlayer(new CJukeboxNameConfirmPacket(Util.Status.FAIL), sender);
				}

			}
		});
		ctx.get().setPacketHandled(true);
	}
}
