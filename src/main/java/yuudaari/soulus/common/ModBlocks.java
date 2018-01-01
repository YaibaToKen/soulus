package yuudaari.soulus.common;

import yuudaari.soulus.common.util.IBlock;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import yuudaari.soulus.common.block.*;
import yuudaari.soulus.common.block.summoner.Summoner;
import yuudaari.soulus.common.block.skewer.Skewer;

public class ModBlocks {
	public static final BlockEndersteel BLOCK_ENDERSTEEL = new BlockEndersteel();
	public static final FossilDirt FOSSIL_DIRT = new FossilDirt();
	public static final FossilDirtFrozen FOSSIL_DIRT_FROZEN = new FossilDirtFrozen();
	public static final FossilDirtFungal FOSSIL_DIRT_FUNGAL = new FossilDirtFungal();
	public static final FossilDirtEnder FOSSIL_DIRT_ENDER = new FossilDirtEnder();
	public static final FossilNetherrack FOSSIL_NETHERRACK = new FossilNetherrack();
	public static final FossilNetherrackEnder FOSSIL_NETHERRACK_ENDER = new FossilNetherrackEnder();
	public static final FossilSand FOSSIL_SAND = new FossilSand();
	public static final FossilSandScale FOSSIL_SAND_SCALE = new FossilSandScale();
	public static final FossilSandEnder FOSSIL_SAND_ENDER = new FossilSandEnder();
	public static final FossilEndStone FOSSIL_END_STONE = new FossilEndStone();
	public static final Summoner SUMMONER = new Summoner();
	public static final BarsEndersteel BARS_ENDERSTEEL = new BarsEndersteel();
	public static final Skewer SKEWER = new Skewer();

	public static IBlock[] blocks = new IBlock[] { SUMMONER, BARS_ENDERSTEEL, BLOCK_ENDERSTEEL, FOSSIL_DIRT,
			FOSSIL_DIRT_ENDER, FOSSIL_DIRT_FROZEN, FOSSIL_DIRT_FUNGAL, FOSSIL_SAND, FOSSIL_SAND_SCALE,
			FOSSIL_SAND_ENDER, FOSSIL_NETHERRACK, FOSSIL_NETHERRACK_ENDER, FOSSIL_END_STONE, SKEWER };

	public static void registerBlocks(IForgeRegistry<Block> registry) {
		for (IBlock block : blocks) {
			registry.register((Block) block);
		}
	}

	public static void registerItems(IForgeRegistry<Item> registry) {
		for (IBlock block : blocks) {
			if (block.hasItem()) {
				List<ItemBlock> items = block.getItemBlocks();
				for (ItemBlock item : items) {
					registry.register(item);
					for (String dict : block.getOreDicts()) {
						OreDictionary.registerOre(dict, item);
					}
				}
			}

			Class<? extends TileEntity> te = block.getTileEntityClass();
			if (te != null) {
				GameRegistry.registerTileEntity(te, block.getRegistryName().toString());
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels() {
		for (IBlock block : blocks) {
			if (block.hasItem()) {
				block.registerItemModel();
			}
		}
	}

	public static void registerRecipes(IForgeRegistry<IRecipe> registry) {
		for (IBlock block : blocks) {
			for (IRecipe recipe : block.getRecipes()) {
				registry.register(recipe);
			}
		}
	}
}