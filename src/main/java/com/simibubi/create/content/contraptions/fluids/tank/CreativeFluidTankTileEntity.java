package com.simibubi.create.content.contraptions.fluids.tank;

import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.lib.transfer.FluidStack;

import com.simibubi.create.lib.transfer.IFluidHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeFluidTankTileEntity extends FluidTankTileEntity implements IFluidHandler {

	public CreativeFluidTankTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected SmartFluidTank createInventory() {
		return new CreativeSmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return false;
	}

	@Override
	public IFluidHandler getFluidStorage() {
		return tankInventory;
	}

	public static class CreativeSmartFluidTank extends SmartFluidTank {
		public CreativeSmartFluidTank(int capacity, Consumer<FluidStack> updateCallback) {
			super(capacity, updateCallback);
		}

		@Override
		public long getFluidAmount() {
			return getFluid().isEmpty() ? 0 : getTankCapacity(0);
		}

		public void setContainedFluid(FluidStack fluidStack) {
			setFluid(fluidStack.copy());
			if (!fluidStack.isEmpty()) {
				FluidStack newStack = new FluidStack(getFluid(), getTankCapacity(0));
				setFluid(newStack);
			}
			onContentsChanged();
		}

		@Override
		public long fill(FluidStack resource, boolean sim) {
			return resource.getAmount();
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean sim) {
			return super.drain(resource, true);
		}

		@Override
		public FluidStack drain(long maxDrain, boolean sim) {
			return super.drain(maxDrain, true);
		}

	}

}
