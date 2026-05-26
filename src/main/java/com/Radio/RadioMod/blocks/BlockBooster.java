package com.Radio.RadioMod.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBooster extends Block {
    public final float multiplier;
    public final int tier;

    public BlockBooster(float mult, int t, String name) {
        super(Material.IRON);
        setRegistryName(name);
        setUnlocalizedName(name);
        setHardness(2.0f);
        setResistance(10.0f);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.REDSTONE);
        this.multiplier = mult;
        this.tier = t;
    }

    public static float getTotalMultiplier(World world, BlockPos center) {
        float mult = 1.0f;
        int radius = 32;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();
                    if (block instanceof BlockBooster) {
                        mult *= ((BlockBooster)block).multiplier;
                    }
                }
            }
        }
        return Math.min(mult, 10.0f);
    }
}

