package com.simibubi.create.foundation.metadoc;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.content.schematics.SchematicWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class MetaDocWorld extends SchematicWorld {

	protected Map<BlockPos, BlockState> originalBlocks;
	protected Map<BlockPos, TileEntity> originalTileEntities;

	int overrideLight;
	Select mask;

	public MetaDocWorld(BlockPos anchor, World original) {
		super(anchor, original);
		originalBlocks = new HashMap<>();
		originalTileEntities = new HashMap<>();
	}

	public void createBackup() {
		originalBlocks.clear();
		originalTileEntities.clear();
		blocks.forEach((k, v) -> originalBlocks.put(k, v));
		tileEntities.forEach((k, v) -> originalTileEntities.put(k, TileEntity.create(v.write(new CompoundNBT()))));
	}
	
	public void restore() {
		blocks.clear();
		tileEntities.clear();
		renderedTileEntities.clear();
		originalBlocks.forEach((k, v) -> blocks.put(k, v));
		originalTileEntities.forEach((k, v) -> {
			TileEntity te = TileEntity.create(v.write(new CompoundNBT()));
			te.setLocation(this, te.getPos());
			tileEntities.put(k, te);
			renderedTileEntities.add(te);
		});
	}

	public void pushFakeLight(int light) {
		this.overrideLight = light;
	}

	public void popLight() {
		this.overrideLight = -1;
	}

	@Override
	public int getLightLevel(LightType p_226658_1_, BlockPos p_226658_2_) {
		return overrideLight == -1 ? 15 : overrideLight;
	}

	public void setMask(Select mask) {
		this.mask = mask;
	}

	public void clearMask() {
		this.mask = null;
	}

	@Override
	public BlockState getBlockState(BlockPos globalPos) {
		if (mask != null && !mask.test(globalPos.subtract(anchor)))
			return Blocks.AIR.getDefaultState();
		return super.getBlockState(globalPos);
	}

}
