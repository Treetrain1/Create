// PORTED CREATE SOURCE

package com.simibubi.create.content.contraptions.relays.encased;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;

import com.simibubi.create.AllBlockEntities;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;

public class AdjustablePulleyBlockEntity extends KineticBlockEntity {

	int signal;
	boolean signalChanged;

	public AdjustablePulleyBlockEntity() {
		super(AllBlockEntities.ADJUSTABLE_PULLEY);
		signal = 0;
		setLazyTickRate(40);
	}

	@Override
	public void toTag(CompoundTag compound, boolean clientPacket) {
		compound.putInt("Signal", signal);
		super.toTag(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		signal = compound.getInt("Signal");
		super.fromTag(state, compound, clientPacket);
	}

	public float getModifier() {
		return getModifierForSignal(signal);
	}

	public void neighborChanged() {
		if (!hasWorld())
			return;
		int power = world.getReceivedRedstonePower(pos);
		if (power != signal) 
			signalChanged = true;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		neighborChanged();
	}

	@Override
	public void tick() {
		super.tick();
		if (signalChanged) {
			signalChanged = false;
			analogSignalChanged(world.getReceivedRedstonePower(pos));
		}
	}

	protected void analogSignalChanged(int newSignal) {
		detachKinetics();
		removeSource();
		signal = newSignal;
		attachKinetics();
	}

	protected float getModifierForSignal(int newPower) {
		if (newPower == 0)
			return 1;
		return 1 + ((newPower + 1) / 16f);
	}

}