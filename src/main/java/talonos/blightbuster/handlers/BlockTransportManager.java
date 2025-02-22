package talonos.blightbuster.handlers;

import java.util.ArrayList;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyStorage;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import talonos.blightbuster.blocks.BBBlock;
import talonos.blightbuster.tileentity.DawnChargerTileEntity;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.aspects.IEssentiaTransport;

public class BlockTransportManager {

    private static class BlockData {
        private int offsetX;
        private int offsetY;
        private int offsetZ;
        private Block block;
        private int metadata;
        private NBTTagCompound tileEntityData;

        public BlockData(int offsetX, int offsetY, int offsetZ, Block block, int metadata, NBTTagCompound tileEntityData) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.block = block;
            this.metadata = metadata;
            this.tileEntityData = tileEntityData;
        }

        public int getOffsetX() { return offsetX; }
        public int getOffsetY() { return offsetY; }
        public int getOffsetZ() { return offsetZ; }
        public Block getBlock() { return block; }
        public int getMetadata() { return metadata; }
        public NBTTagCompound getTileEntityData() { return tileEntityData; }
    }

    public boolean transport(World world, int srcX, int srcY, int srcZ, int destX, int destY, int destZ) {
        ArrayList<BlockData> srcData = captureData(world, srcX, srcY, srcZ);
        wipeData(world, destX, destY, destZ);

        generateData(world, destX, destY, destZ, srcData);

        wipeCapturedData(world, srcX, srcY, srcZ, srcData);

        return true;
    }

    protected ArrayList<BlockData> captureData(World world, int originX, int originY, int originZ) {
        ArrayList<BlockData> blockData = new ArrayList<BlockData>(125);

        for (int y = -2; y <= 2; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockData block = getBlockData(world, originX, originY, originZ, x, y, z);
                    if (block != null)
                        blockData.add(block);
                }
            }
        }

        return blockData;
    }

    protected BlockData getBlockData(World world, int originX, int originY, int originZ, int offsetX, int offsetY, int offsetZ) {
        Block block = world.getBlock(originX+offsetX, originY+offsetY, originZ+offsetZ);
        TileEntity te = world.getTileEntity(originX+offsetX, originY+offsetY, originZ+ offsetZ);
        if (!shouldCaptureBlock(block) && !shouldCaptureTileEntity(te)) {
            return null;
        }

        NBTTagCompound tileEntityData = null;

        if (te != null) {
            tileEntityData = new NBTTagCompound();
            te.writeToNBT(tileEntityData);
        }

        return new BlockData(offsetX, offsetY, offsetZ, block, world.getBlockMetadata(originX+offsetX, originY+offsetY, originZ+offsetZ), tileEntityData);
    }

    protected void wipeData(World world, int wipeX, int wipeY, int wipeZ) {
        for (int y = -2; y <= 2; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    wipeBlock(world, wipeX+x, wipeY+y, wipeZ+z);
                }
            }
        }
    }

    protected void wipeCapturedData(World world, int wipeX, int wipeY, int wipeZ, ArrayList<BlockData> data) {
        int dataSize = data.size();
        for (int i = 0; i < dataSize; i++) {
            BlockData block = data.get(i);
            wipeBlock(world, wipeX+block.getOffsetX(), wipeY+block.getOffsetY(), wipeZ+block.getOffsetZ());
        }
    }

    protected void generateData(World world, int genX, int genY, int genZ, ArrayList<BlockData> data) {
        int dataSize = data.size();
        for (int i = 0; i < dataSize; i++) {
            BlockData block = data.get(i);
            generateBlock(world, genX+block.getOffsetX(), genY+block.getOffsetY(), genZ+block.getOffsetZ(), block);
        }
    }

    protected boolean shouldCaptureBlock(Block block) {
        return (block == BBBlock.dawnMachineInput || block == BBBlock.dawnMachineBuffer || block == BBBlock.dawnMachine);
    }

    protected boolean shouldCaptureTileEntity(TileEntity tileEntity) {
        if (tileEntity == null)
            return false;
        if (tileEntity instanceof IEssentiaContainerItem)
            return true;
        if (tileEntity instanceof IEssentiaTransport)
            return true;
        
        return false;
    }

    protected void wipeBlock(World world, int x, int y, int z) {
        world.removeTileEntity(x, y, z);
        world.setBlock(x, y, z, Blocks.air, 0, 2);
    }

    protected void generateBlock(World world, int x, int y, int z, BlockData block) {
        world.setBlock(x, y, z, block.getBlock(), block.getMetadata(), 2);
        NBTTagCompound teTag = block.getTileEntityData();

        if (teTag != null) {
            teTag.setInteger("x", x);
            teTag.setInteger("y", y);
            teTag.setInteger("z", z);
            TileEntity te = world.getTileEntity(x, y, z);
            if (te != null)
                te.readFromNBT(teTag);
        }
    }
}
