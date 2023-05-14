package com.simibubi.create.content.curiosities.armor;

import io.github.fabricators_of_create.porting_lib.enchant.CustomEnchantingTableBehaviorEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class CapacityEnchantment extends Enchantment implements CustomEnchantingTableBehaviorEnchantment {

	public CapacityEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot[] slots) {
		super(rarity, category, slots);
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return stack.getItem() instanceof ICapacityEnchantable;
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return canApplyAtEnchantingTable(stack);
	}

	public interface ICapacityEnchantable {
	}

}
