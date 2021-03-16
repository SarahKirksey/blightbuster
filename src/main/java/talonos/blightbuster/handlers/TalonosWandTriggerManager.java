package talonos.blightbuster.handlers;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import talonos.blightbuster.blocks.BBBlock;
import talonos.blightbuster.multiblock.BlockMultiblock;
import talonos.blightbuster.multiblock.Multiblock;
import talonos.blightbuster.multiblock.entries.MultiblockEntry;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.IWandTriggerManager;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.research.ResearchManager;

public class TalonosWandTriggerManager implements IWandTriggerManager {

    private BlockTransportManager blockTransportManager = new BlockTransportManager();

    @Override
    public boolean performTrigger(World world, ItemStack wand,
                                  EntityPlayer player, int x, int y, int z, int side, int event) {

        switch (event) {
            case 0:
                if (ResearchManager.isResearchComplete(
                        player.getCommandSenderName(), "DAWNMACHINE"))
                {
                    boolean success = createDawnMachine(wand, player, world, x, y, z);
                    if (success) {
                        Block convertedBlock = world.getBlock(x, y, z);
                        if (convertedBlock instanceof BlockMultiblock) {
                            TileEntity controller = ((BlockMultiblock)convertedBlock).getMultiblockController(world, x, y, z);
                            if (controller != null) {
                                pairDawnMachineToWand(wand, controller.getWorldObj().provider.dimensionId, controller.xCoord, controller.yCoord, controller.zCoord);
                                return true;
                            }
                        }

                        player.addChatMessage(new ChatComponentTranslation("gui.offering.pairFailed"));
                    }
                    return success;
                }
                break;
            case 1:
                Block convertedBlock = world.getBlock(x, y, z);

                if (convertedBlock instanceof BlockMultiblock) {
                    TileEntity controller = ((BlockMultiblock)convertedBlock).getMultiblockController(world, x, y, z);
                    if (controller != null) {
                        if (world.isRemote)
                            return false;

                        if (!isWandPaired(wand, controller.getWorldObj().provider.dimensionId, controller.xCoord, controller.yCoord, controller.zCoord)) {
                            pairDawnMachineToWand(wand, controller.getWorldObj().provider.dimensionId, controller.xCoord, controller.yCoord, controller.zCoord);
                            player.addChatMessage(new ChatComponentTranslation("gui.offering.pairSucceeded"));
                        } else
                            player.addChatMessage(new ChatComponentTranslation("gui.offering.pairAlreadyExists"));
                        return true;
                    }
                }

                break;
            case 2:
                if (world.isRemote)
                    return false;

                NBTTagCompound wandTag = wand.getTagCompound();
                if (wandTag == null) {
                    player.addChatMessage(new ChatComponentTranslation("gui.offering.noPairing"));
                    return false;
                }

                if (!wandTag.hasKey("DawnMachine", 10)) {
                    player.addChatMessage(new ChatComponentTranslation("gui.offering.noPairing"));
                    return false;
                }

                NBTTagCompound pairingTag = wandTag.getCompoundTag("DawnMachine");
                if (pairingTag.getInteger("Dimension") != world.provider.dimensionId) {
                    player.addChatMessage(new ChatComponentTranslation("gui.offering.wrongDimension"));
                    return false;
                }

                int dawnMachineX = pairingTag.getInteger("X");
                int dawnMachineY = pairingTag.getInteger("Y");
                int dawnMachineZ = pairingTag.getInteger("Z");

                if (world.getBlock(dawnMachineX, dawnMachineY, dawnMachineZ) != BBBlock.dawnMachine) {
                    player.addChatMessage(new ChatComponentTranslation("gui.offering.pairingDestroyed"));
                    return false;
                }

                for (int clearY = 0; clearY < 5; clearY++) {
                    for (int clearX = -2; clearX <= 2; clearX++) {
                        for (int clearZ = -2; clearZ <= 2; clearZ++) {
                            if (clearY == 0 && clearX == 0 && clearZ == 0)
                                continue;

                            if (!world.isAirBlock(x+clearX, y+clearY, z+clearZ)) {
                                Block clearBlock = world.getBlock(x + clearX, y + clearY, z + clearZ);
                                if (!clearBlock.getMaterial().isReplaceable()) {
                                    player.addChatMessage(new ChatComponentTranslation("gui.offering.clearArea"));
                                    return false;
                                }
                            }
                        }
                    }
                }

                boolean result = blockTransportManager.transport(world, dawnMachineX, dawnMachineY+1, dawnMachineZ, x, y+2, z);

                if (result) {
                    TileEntity controller = world.getTileEntity(x, y+1, z);
                    pairDawnMachineToWand(wand, controller.getWorldObj().provider.dimensionId, controller.xCoord, controller.yCoord, controller.zCoord);
                }
        }
        return false;
    }

    private boolean isWandPaired(ItemStack wand, int dimension, int x, int y, int z) {
        if (wand.getTagCompound() == null)
            return false;

        NBTTagCompound wandTag = wand.getTagCompound();

        if (!wandTag.hasKey("DawnMachine", 10)) {
            return false;
        }

        NBTTagCompound dawnMachineTag = wandTag.getCompoundTag("DawnMachine");
        if (dawnMachineTag.getInteger("Dimension") != dimension)
            return false;
        if (dawnMachineTag.getInteger("X") != x)
            return false;
        if (dawnMachineTag.getInteger("Y") != y)
            return false;
        if (dawnMachineTag.getInteger("Z") != z)
            return false;
        return true;
    }

    private void pairDawnMachineToWand(ItemStack wand, int dimension, int x, int y, int z) {
        NBTTagCompound wandTag = wand.getTagCompound();
        if (wandTag == null) {
            wandTag = new NBTTagCompound();
            wand.setTagCompound(wandTag);
        }

        NBTTagCompound dawnMachineTag = wandTag.getCompoundTag("DawnMachine");
        dawnMachineTag.setInteger("Dimension", dimension);
        dawnMachineTag.setInteger("X", x);
        dawnMachineTag.setInteger("Y", y);
        dawnMachineTag.setInteger("Z", z);
        wandTag.setTag("DawnMachine", dawnMachineTag);
    }

    private boolean createDawnMachine(ItemStack stack, EntityPlayer player,
                                      World world, int x, int y, int z) {
        if (world.isRemote)
            return false;

        ItemWandCasting wand = (ItemWandCasting) stack.getItem();

        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        Pair<MultiblockEntry, Integer> entry = BBBlock.dawnMachineMultiblock.getEntry(world, x, y, z, -1, block, meta);

        if (entry != null) {
            if (wand.consumeAllVisCrafting(stack, player, new AspectList().add(Aspect.ORDER, 20), true)) {
                if (!world.isRemote) {
                    BBBlock.dawnMachineMultiblock.convertMultiblockWithOrientationFromSideBlock(world, x, y, z, entry.getValue(), false, entry.getKey());
                    return true;
                }
                return false;
            }
        }

        return false;
    }
}
