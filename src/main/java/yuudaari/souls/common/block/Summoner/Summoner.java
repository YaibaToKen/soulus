package yuudaari.souls.common.block.Summoner;

import yuudaari.souls.Souls;
import yuudaari.souls.common.ModBlocks;
import yuudaari.souls.common.ModItems;
import yuudaari.souls.common.block.Summoner.SummonerTileEntity.Upgrade;
import yuudaari.souls.common.item.Soulbook;
import yuudaari.souls.common.item.SummonerUpgrade;
import yuudaari.souls.common.util.Material;
import yuudaari.souls.common.util.MobTarget;
import yuudaari.souls.common.util.ModBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Souls.MODID)
public class Summoner extends ModBlock {

	@ObjectHolder("souls:blood_crystal")
	public static SummonerUpgrade CountUpgrade;
	@ObjectHolder("souls:gear_oscillating")
	public static SummonerUpgrade DelayUpgrade;
	@ObjectHolder("souls:orb_murky")
	public static SummonerUpgrade RangeUpgrade;

	private static class Upgrades {
		public int delayUpgrades;
		public int rangeUpgrades;
		public int countUpgrades;

		public Upgrades(int delay, int range, int count) {
			delayUpgrades = delay;
			rangeUpgrades = range;
			countUpgrades = count;
		}
	}

	private static Upgrades getSummonerUpgrades(NBTTagCompound summonerData) {
		NBTTagCompound upgradeData = summonerData.getCompoundTag("upgrades");
		return new Upgrades(upgradeData.getByte("delay"), upgradeData.getByte("range"), upgradeData.getByte("count"));
	}

	private static String getSummonerEntity(NBTTagCompound summonerData) {
		return summonerData.getString("entity_type");
	}

	public Summoner() {
		super("summoner", new Material(MapColor.STONE).setTransparent());
		setHasItem();
		setHardness(5F);
		setResistance(30F);
		setHarvestLevel("pickaxe", 1);
		setSoundType(SoundType.METAL);
		disableStats();
		setCreativeTab(null);
	}

	@SubscribeEvent
	public void registerTileEntities(RegistryEvent.Register<Item> event) {
	}

	@SubscribeEvent
	public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		IBlockState blockState = event.getWorld().getBlockState(event.getPos());
		if (blockState.getBlock() instanceof Summoner && event.getItemStack().getItem() instanceof SummonerUpgrade) {
			event.setUseBlock(Result.ALLOW);
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public Class<? extends TileEntity> getTileEntityClass() {
		return SummonerTileEntity.class;
	}

	@Override
	@Nonnull
	@ParametersAreNonnullByDefault
	public TileEntity createTileEntity(World worldIn, IBlockState blockState) {
		return new SummonerTileEntity();
	}

	public static NBTTagCompound lastBrokenSummonerData;

	@SubscribeEvent
	public static void onSummonerBreak(BlockEvent.BreakEvent event) {
		if (event.getState().getBlock() == ModBlocks.SUMMONER) {
			SummonerTileEntity tileEntity = (SummonerTileEntity) event.getWorld().getTileEntity(event.getPos());
			if (tileEntity == null)
				throw new RuntimeException("Summoner has no tile entity");
			lastBrokenSummonerData = tileEntity.writeToNBT(new NBTTagCompound());
		}
	}

	@Nonnull
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune) {
		List<ItemStack> drops = getDrops(lastBrokenSummonerData);

		drops.add(ModBlocks.SUMMONER_EMPTY.getItemStack());

		return drops;
	}

	private ItemStack getSoulbook(NBTTagCompound summonerData) {
		String entityName = getSummonerEntity(summonerData);
		ItemStack soulbook = ModItems.SOULBOOK.getItemStack();
		MobTarget.setMobTarget(soulbook, entityName);
		Soulbook.setContainedEssence(soulbook, Souls.getSoulInfo(entityName).quantity);

		return soulbook;
	}

	private List<ItemStack> getDrops(NBTTagCompound summonerData) {
		List<ItemStack> drops = new ArrayList<>();

		// soulbook
		drops.add(getSoulbook(summonerData));

		// upgrades
		Upgrades upgrades = getSummonerUpgrades(summonerData);
		drops.addAll(getUpgradeStacks(CountUpgrade, upgrades.countUpgrades));
		drops.addAll(getUpgradeStacks(DelayUpgrade, upgrades.delayUpgrades));
		drops.addAll(getUpgradeStacks(RangeUpgrade, upgrades.rangeUpgrades));

		return drops;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			SummonerTileEntity summoner = (SummonerTileEntity) world.getTileEntity(pos);

			ItemStack heldStack = player.inventory.mainInventory.get(player.inventory.currentItem);
			Item heldItem = heldStack.getItem();
			boolean sneaking = player.isSneaking();

			if (heldItem.equals(CountUpgrade)) {
				heldStack.shrink(summoner.addUpgradeStack(Upgrade.COUNT, sneaking ? heldStack.getCount() : 1));

			} else if (heldItem.equals(DelayUpgrade)) {
				heldStack.shrink(summoner.addUpgradeStack(Upgrade.DELAY, sneaking ? heldStack.getCount() : 1));

			} else if (heldItem.equals(RangeUpgrade)) {
				heldStack.shrink(summoner.addUpgradeStack(Upgrade.RANGE, sneaking ? heldStack.getCount() : 1));

			} else if (heldItem.equals(Items.AIR)) {
				if (sneaking) {
					// empty hand and sneaking = return all items from summoner
					for (ItemStack drop : getDrops(summoner.writeToNBT(new NBTTagCompound()))) {
						returnItemToPlayer(world, drop, player);
					}

					world.setBlockState(pos, ModBlocks.SUMMONER_EMPTY.getDefaultState());

				} else {
					Upgrade lastInserted = summoner.getLastInserted();
					if (lastInserted == null) {
						returnItemToPlayer(world, getSoulbook(summoner.writeToNBT(new NBTTagCompound())), player);
						world.setBlockState(pos, ModBlocks.SUMMONER_EMPTY.getDefaultState());

					} else {
						int amtRemoved = summoner.removeUpgrade(lastInserted);
						if (amtRemoved > 0) {

							List<ItemStack> stacks = null;
							switch (lastInserted) {

							case COUNT:
								stacks = getUpgradeStacks(CountUpgrade, amtRemoved);
								break;

							case DELAY:
								stacks = getUpgradeStacks(DelayUpgrade, amtRemoved);
								break;

							case RANGE:
								stacks = getUpgradeStacks(RangeUpgrade, amtRemoved);
								break;

							}

							if (stacks != null) {
								for (int i = 0; i < stacks.size(); i++) {
									returnItemToPlayer(world, stacks.get(i), player);
								}
							}
						}
					}
				}
			}
		}

		return true;
	}

	private List<ItemStack> getUpgradeStacks(SummonerUpgrade upgrade, int count) {
		List<ItemStack> result = new ArrayList<>();
		do {
			ItemStack stack = upgrade.getFilledStack();
			stack.setCount(Math.min(stack.getMaxStackSize(), count));
			count -= stack.getMaxStackSize();
			result.add(stack);
		} while (count > 0);

		return result;
	}

	private void returnItemToPlayer(World world, ItemStack item, EntityPlayer player) {
		EntityItem dropItem = new EntityItem(world, player.posX, player.posY, player.posZ, item);
		dropItem.setNoPickupDelay();
		world.spawnEntity(dropItem);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
			EntityPlayer player) {
		// if they're requesting the block with nbt data, it needs to be this block, not an empty summoner
		return player.isCreative() && GuiScreen.isCtrlKeyDown() ? getItemStack()
				: ModBlocks.SUMMONER_EMPTY.getItemStack();
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		SummonerTileEntity te = (SummonerTileEntity) world.getTileEntity(pos);
		return te.getSignalStrength();
	}
}