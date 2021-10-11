package com.simibubi.create.content.curiosities.toolbox;

import java.util.function.Supplier;

import com.simibubi.create.lib.helper.EntityHelper;
import com.simibubi.create.lib.transfer.item.ItemHandlerHelper;

import me.pepperbell.simplenetworking.C2SPacket;

import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.core.BlockPos;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.entity.BlockEntity;

import org.apache.commons.lang3.mutable.MutableBoolean;

public class ToolboxDisposeAllPacket implements C2SPacket {

	private BlockPos toolboxPos;

	public ToolboxDisposeAllPacket(BlockPos toolboxPos) {
		this.toolboxPos = toolboxPos;
	}

	public void read(FriendlyByteBuf buffer) {
		toolboxPos = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(toolboxPos);
	}


	@Override
	public void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, SimpleChannel.ResponseTarget responseTarget) {
		server.execute(() -> {
			Level world = player.level;
			BlockEntity blockEntity = world.getBlockEntity(toolboxPos);

			double maxRange = ToolboxHandler.getMaxRange(player);
			if (player.distanceToSqr(toolboxPos.getX() + 0.5, toolboxPos.getY(), toolboxPos.getZ() + 0.5) > maxRange
				* maxRange)
				return;
			if (!(blockEntity instanceof ToolboxTileEntity))
				return;
			ToolboxTileEntity toolbox = (ToolboxTileEntity) blockEntity;

			CompoundTag compound = EntityHelper.getExtraCustomData(player)
				.getCompound("CreateToolboxData");
			MutableBoolean sendData = new MutableBoolean(false);

			toolbox.inventory.inLimitedMode(inventory -> {
				for (int i = 0; i < 36; i++) {
					String key = String.valueOf(i);
					if (compound.contains(key) && NbtUtils.readBlockPos(compound.getCompound(key)
						.getCompound("Pos"))
						.equals(toolboxPos)) {
						ToolboxHandler.unequip(player, i, true);
						sendData.setTrue();
					}

					ItemStack itemStack = player.getInventory().getItem(i);
					ItemStack remainder = ItemHandlerHelper.insertItemStacked(toolbox.inventory, itemStack, false);
					if (remainder.getCount() != itemStack.getCount())
						player.getInventory().setItem(i, remainder);
				}
			});

			if (sendData.booleanValue())
				ToolboxHandler.syncData(player);

		});
	}

}
