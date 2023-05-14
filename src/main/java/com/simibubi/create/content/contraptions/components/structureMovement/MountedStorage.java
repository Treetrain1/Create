package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.contraptions.processing.ProcessingInventory;
import com.simibubi.create.content.logistics.block.inventories.BottomlessItemHandler;
import com.simibubi.create.content.logistics.block.vault.ItemVaultBlockEntity;
import com.simibubi.create.foundation.utility.NBTHelper;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class MountedStorage {

	private static final ItemStackHandler dummyHandler = new ItemStackHandler();

	ItemStackHandler handler;
	boolean noFuel;
	boolean valid;

	private BlockEntity blockEntity;

	public static boolean canUseAsStorage(BlockEntity be) {
		if (be == null)
			return false;
		if (be instanceof MechanicalCrafterBlockEntity)
			return false;
		if (AllBlockEntityTypes.CREATIVE_CRATE.is(be))
			return true;
		if (be instanceof ShulkerBoxBlockEntity)
			return true;
		if (be instanceof ChestBlockEntity)
			return true;
		if (be instanceof BarrelBlockEntity)
			return true;
		if (be instanceof ItemVaultBlockEntity)
			return true;

		try {
			Storage<ItemVariant> handler = TransferUtil.getItemStorage(te);
			if (handler instanceof ItemStackHandler)
				return !(handler instanceof ProcessingInventory);
			return canUseModdedInventory(be, handler);

		} catch (Exception e) {
			return false;
		}
	}

	public static boolean canUseModdedInventory(BlockEntity be, IItemHandler handler) {
		if (!(handler instanceof IItemHandlerModifiable validItemHandler))
			return false;
		BlockState blockState = be.getBlockState();
		if (AllBlockTags.CONTRAPTION_INVENTORY_DENY.matches(blockState))
			return false;

		// There doesn't appear to be much of a standard for tagging chests/barrels
		String blockId = blockState.getBlock()
			.getRegistryName()
			.getPath();
		return blockId.endsWith("_chest") || blockId.endsWith("_barrel");
	}

	public MountedStorage(BlockEntity be) {
		this.blockEntity = be;
		handler = dummyHandler;
		noFuel = be instanceof ItemVaultBlockEntity;
	}

	public void removeStorageFromWorld() {
		valid = false;
		if (blockEntity == null)
			return;

		if (blockEntity instanceof ChestBlockEntity chest) {
			CompoundTag tag = blockEntity.saveWithFullMetadata();
			if (tag.contains("LootTable", 8))
				return;

			handler = new ItemStackHandler(chest.getContainerSize());
			for (int i = 0; i < handler.getSlots(); i++) {
				handler.setStackInSlot(i, chest.getItem(i));
			}
			valid = true;
			return;
		}

		Storage<ItemVariant> beHandler = TransferUtil.getItemStorage(blockEntity);
		if (beHandler == null)
			return;

		// multiblock vaults need to provide individual invs
		if (blockEntity instanceof ItemVaultBlockEntity) {
			handler = ((ItemVaultBlockEntity) blockEntity).getInventoryOfBlock();
			valid = true;
			return;
		}

		// be uses ItemStackHandler
		if (beHandler instanceof ItemStackHandler) {
			handler = (ItemStackHandler) beHandler;
			valid = true;
			return;
		}

		// serialization not accessible -> fill into a serializable handler
		if (beHandler instanceof InventoryStorage inv && teHandler.supportsInsertion() && teHandler.supportsExtraction()) {
			try (Transaction t = TransferUtil.getTransaction()) {
				List<SingleSlotStorage<ItemVariant>> slots = inv.getSlots();
				ItemStack[] stacks = new ItemStack[slots.size()];
				for (int i = 0; i < slots.size(); i++) {
					SingleSlotStorage<ItemVariant> slot = slots.get(i);
					if (slot.isResourceBlank()) {
						stacks[i] = ItemStack.EMPTY;
						continue;
					}
					long contained = slot.getAmount();
					ItemVariant variant = slot.getResource();
					long extracted = slot.extract(variant, contained, t);
					if (extracted != contained) return; // can't extract it all for whatever reason - that's bad, give up
					stacks[i] = variant.toStack((int) extracted);
				}
				handler = new ItemStackHandler(stacks);
				valid = true;
			}
		}
	}

	public void addStorageToWorld(BlockEntity be) {
		// FIXME: More dynamic mounted storage in .4
		if (handler instanceof BottomlessItemHandler)
			return;

		if (te instanceof ChestBlockEntity chest) {
			for (int i = 0; i < chest.getContainerSize(); i++) {
				ItemStack stack = i < handler.getSlots() ? handler.getStackInSlot(i) : ItemStack.EMPTY;
				chest.setItem(i, stack);
			}
			return;
		}

		if (be instanceof ItemVaultBlockEntity) {
			((ItemVaultBlockEntity) be).applyInventoryToBlock(handler);
			return;
		}

		Storage<ItemVariant> teHandler = TransferUtil.getItemStorage(be);
		if (teHandler != null && teHandler.supportsInsertion()) {
			try (Transaction t = TransferUtil.getTransaction()) {
				// we need to remove whatever is in there to fill with our modified contents
				TransferUtil.clearStorage(teHandler);
				for (StorageView<ItemVariant> view : handler.nonEmptyIterable()) {
					teHandler.insert(view.getResource(), view.getAmount(), t);
				}
				t.commit();
			}
		}
	}

	public Storage<ItemVariant> getItemHandler() {
		return handler;
	}

	public CompoundTag serialize() {
		if (!valid)
			return null;

		CompoundTag tag = handler.serializeNBT();
		if (noFuel)
			NBTHelper.putMarker(tag, "NoFuel");
		if (!(handler instanceof BottomlessItemHandler))
			return tag;

		NBTHelper.putMarker(tag, "Bottomless");
		tag.put("ProvidedStack", NBTSerializer.serializeNBT(handler.getStackInSlot(0)));
		return tag;
	}

	public static MountedStorage deserialize(CompoundTag nbt) {
		MountedStorage storage = new MountedStorage(null);
		storage.handler = new ItemStackHandler();
		if (nbt == null)
			return storage;
		storage.valid = true;
		storage.noFuel = nbt.contains("NoFuel");

		if (nbt.contains("Bottomless")) {
			ItemStack providedStack = ItemStack.of(nbt.getCompound("ProvidedStack"));
			storage.handler = new BottomlessItemHandler(() -> providedStack);
			return storage;
		}

		storage.handler.deserializeNBT(nbt);
		return storage;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean canUseForFuel() {
		return !noFuel;
	}

}
