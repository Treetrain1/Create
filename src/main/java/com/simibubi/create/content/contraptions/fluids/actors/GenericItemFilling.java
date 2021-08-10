package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.fluid.FluidHelper;

import com.simibubi.create.lib.transfer.FluidStack;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;

public class GenericItemFilling {

	/**
	 * Checks if an ItemStack's IFluidHandlerItem is valid. Ideally, this check would
	 * not be necessary. Unfortunately, some mods that copy the functionality of the
	 * MilkBucketItem copy the FluidBucketWrapper capability that is patched in by
	 * Forge without looking into what it actually does. In all cases this is
	 * incorrect because having a non-bucket item turn into a bucket item does not
	 * make sense.
	 *
	 * <p>This check is only necessary for filling since a FluidBucketWrapper will be
	 * empty if it is initialized with a non-bucket item.
	 *
	 * @param stack The ItemStack.
	 * @param fluidHandler The IFluidHandlerItem instance retrieved from the ItemStack.
	 * @return If the IFluidHandlerItem is valid for the passed ItemStack.
	 */ // this doesn't exist on fabric so this method is kinda useless
	public static boolean isFluidHandlerValid(ItemStack stack, IFluidHandlerItem fluidHandler) {
//		// Not instanceof in case a correct subclass is made
//		if (fluidHandler.getClass() == FluidBucketWrapper.class) {
//			Item item = stack.getItem();
//			// Forge does not patch the FluidBucketWrapper onto subclasses of BucketItem
//			if (item.getClass() != BucketItem.class && !(item instanceof MilkBucketItem)) {
//				return false;
//			}
//		}
		return true;
	}

	public static boolean canItemBeFilled(Level world, ItemStack stack) {
		if (stack.getItem() == Items.GLASS_BOTTLE)
			return true;
		if (stack.getItem() == Items.MILK_BUCKET)
			return false;

		LazyOptional<IFluidHandlerItem> capability =
				TransferUtil.getFluidHandlerItem(stack);

		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return false;
		if (!isFluidHandlerValid(stack, tank))
			return false;
		for (int i = 0; i < tank.getTanks(); i++) {
			if (tank.getFluidInTank(i)
				.getAmount() < tank.getTankCapacity(i))
				return true;
		}
		return false;
	}

	public static int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
		if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(availableFluid))
			return PotionFluidHandler.getRequiredAmountForFilledBottle(stack, availableFluid);
		if (stack.getItem() == Items.BUCKET && canFillBucketInternally(availableFluid))
			return 1000;

//		LazyOptional<IFluidHandlerItem> capability =
//			stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
//		IFluidHandlerItem tank = capability.orElse(null);
//		if (tank == null)
//			return -1;
//		if (tank instanceof FluidBucketWrapper) {
			Item filledBucket = availableFluid.getFluid()
				.getBucket();
			if (filledBucket == null || filledBucket == Items.AIR)
				return -1;
//			if (!((FluidBucketWrapper) tank).getFluid()
//				.isEmpty())
//				return -1;
			return 1000;
//		}

//		int filled = tank.fill(availableFluid, FluidAction.SIMULATE);
//		return filled == 0 ? -1 : filled;
	}

	private static boolean canFillGlassBottleInternally(FluidStack availableFluid) {
		return availableFluid.getFluid()
			.isSame(Fluids.WATER)
			|| availableFluid.getFluid()
				.isSame(AllFluids.POTION.get());
	}

	private static boolean canFillBucketInternally(FluidStack availableFluid) {
		return false;
	}

	public static ItemStack fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
		FluidStack toFill = (FluidStack) availableFluid.withAmount(FluidUtil.millibucketsToFluidAmount(requiredAmount));

//		availableFluid.shrink(requiredAmount);

		if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(toFill)) {
			ItemStack fillBottle = ItemStack.EMPTY;
			if (FluidHelper.isWater(toFill.getFluid()))
				fillBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
			else
				fillBottle = PotionFluidHandler.fillBottle(stack, toFill);
			stack.shrink(1);
			return fillBottle;
		}


		ItemStack split = stack.copy();
		split.setCount(1);
//		LazyOptional<IFluidHandlerItem> capability =
//			split.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
//		IFluidHandlerItem tank = capability.orElse(null);
//		if (tank == null)
//			return ItemStack.EMPTY;
//		tank.fill(toFill, FluidAction.EXECUTE);
//		ItemStack container = tank.getContainer()
//			.copy();
		stack.shrink(1);
//		return container;
	return ItemStack.EMPTY;
	}

}
