package com.simibubi.create.lib.mixin;

import com.simibubi.create.lib.extensions.AbstractMinecartEntityExtensions;

import net.minecraft.entity.MoverType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.block.MinecartPassHandlerBlock;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin implements AbstractMinecartEntityExtensions {

	@Shadow public abstract boolean isBeingRidden();
	@Shadow public abstract Vector3d getMotion();
	@Shadow public abstract void move(MoverType moverType, Vector3d vector3d);

	// this *should* inject into right before the 4th reference to bl, right in between the 2 if statements.
	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/minecart/AbstractMinecartEntity;moveAlongTrack(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", ordinal = 3),
			method = "Lnet/minecraft/entity/item/minecart/AbstractMinecartEntity;moveAlongTrack(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
	protected void create$moveAlongTrack(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		if (blockState.getBlock() instanceof MinecartPassHandlerBlock) {
			((MinecartPassHandlerBlock) blockState.getBlock()).onMinecartPass(blockState, MixinHelper.<Entity>cast(this).world, blockPos, MixinHelper.cast(this));
		}
	}

	@Override
	public void create$moveMinecartOnRail(BlockPos pos) {
		double d24 = isBeingRidden() ? 0.75D : 1.0D;
		double d25 = getMaxSpeedWithRail();
		Vector3d vec3d1 = getMotion();
		move(MoverType.SELF, new Vector3d(MathHelper.clamp(d24 * vec3d1.x, -d25, d25), 0.0D, MathHelper.clamp(d24 * vec3d1.z, -d25, d25)));
	}
}