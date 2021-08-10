package com.simibubi.create.lib.transfer;

import net.minecraft.nbt.CompoundTag;

public class FluidTank implements IFluidHandler {
	protected FluidStack fluid = FluidStack.empty();
	protected long capacity;

	public FluidTank(FluidStack fluid, long capacity) {
		this(capacity);
		this.fluid = fluid;
	}

	public FluidTank(long capacity) {
		this.capacity = capacity;
	}

	public FluidTank setCapacity(long capacity) {
		this.capacity = capacity;
		return this;
	}

	public long getCapacity() {
		return capacity;
	}

	public FluidStack getFluid() {
		return fluid;
	}

	public void setFluid(FluidStack fluid) {
		this.fluid = fluid;
	}

	public CompoundTag writeToNBT(CompoundTag tag) {
		fluid.writeToNBT(tag);
		tag.putLong("Capacity", capacity);
		return tag;
	}

	public FluidTank readFromNBT(CompoundTag tag) {
		FluidStack stack = FluidStack.fromNBT(tag);
		long capacity = tag.getLong("Capacity");
		return new FluidTank(stack, capacity);
	}

	public boolean isEmpty() {
		return getCapacity() == 0;
	}

	public long getFluidAmount() {
		return getFluid().getAmount();
	}

	public long getSpace() {
		return Math.max(0, capacity - getFluid().getAmount());
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return fluid;
	}

	@Override
	public long getTankCapacity(int tank) {
		return capacity;
	}

	@Override
	public long fill(FluidStack stack, boolean sim) {
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack stack, boolean sim) {
		return null;
	}

	@Override
	public FluidStack drain(long amount, boolean sim) {
		return null;
	}
}
