package com.witcherbb.bettersound.network.protocol.server;

import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.blocks.entity.BetterJukeboxBlockEntity;
import com.witcherbb.bettersound.blocks.entity.JukeboxControllerBlockEntity;
import com.witcherbb.bettersound.common.data.impl.JukeboxEntityDataProvider;
import com.witcherbb.bettersound.common.utils.Util;
import com.witcherbb.bettersound.menu.inventory.BetterJukeboxMenu;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.PacketContext;
import com.witcherbb.bettersound.network.protocol.client.CBetterJukeboxNameConfirmPacket;
import com.witcherbb.bettersound.world.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record SBetterJukeboxNamePacket(String name, String dimension) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(this.name);
		buf.writeUtf(this.dimension);
	}

	public static SBetterJukeboxNamePacket decode(FriendlyByteBuf buf) {
		String name = buf.readUtf();
		String dimension = buf.readUtf();
		return new SBetterJukeboxNamePacket(name, dimension);
	}

	public static void handle(SBetterJukeboxNamePacket packet, PacketContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer sender = ctx.sender();
			if (sender == null) return;
			//Do Stuff
			AbstractContainerMenu abstractContainerMenu = sender.containerMenu;
			if (abstractContainerMenu instanceof BetterJukeboxMenu jukeboxMenu) {
				if (!jukeboxMenu.stillValid(sender)) {
					Constants.LOGGER.debug("Player {} interacted with invalid menu {}", sender, sender.containerMenu);
					return;
				}
				JukeboxEntityDataProvider provider = JukeboxControllerBlockEntity.getProvider();

				boolean existFlag = provider.exists(packet.name, packet.dimension);
				if (!packet.name.isEmpty() && existFlag) {
					BetterJukeboxBlockEntity blockEntity = jukeboxMenu.getBlockEntity();
					ServerLevel level = (ServerLevel) blockEntity.getLevel();
					CompoundTag nbt = blockEntity.saveWithoutMetadata();
					String oldName = nbt.getString("Name");
					String oldDimension = level.dimension().location().getPath();
					nbt.putString("Name", packet.name);
					blockEntity.load(nbt);

					BlockPos blockPos = blockEntity.getBlockPos();
					// 保证客户端tag同步
					level.getChunkSource().blockChanged(blockPos);
					if (!oldName.isEmpty())
						JukeboxControllerBlockEntity.removePos(oldName, oldDimension, blockPos, sender.level());
					JukeboxControllerBlockEntity.putPos(nbt.getString("Name"), WorldUtil.getDimensionName(sender.level()), blockPos, sender.level());
					ModNetwork.sendToPlayer(new CBetterJukeboxNameConfirmPacket(Util.Status.SUCCESS), sender);
				} else if (packet.name.isEmpty()) {
					ModNetwork.sendToPlayer(new CBetterJukeboxNameConfirmPacket(Util.Status.NULL), sender);
				} else {
					ModNetwork.sendToPlayer(new CBetterJukeboxNameConfirmPacket(Util.Status.FAIL), sender);
				}

			}
		});
	}
}
