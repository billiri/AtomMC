package net.minecraft.network.play.server;

import java.io.IOException;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketSignEditorOpen implements Packet<INetHandlerPlayClient> {
    private BlockPos signPosition;

    public SPacketSignEditorOpen() {
    }

    public SPacketSignEditorOpen(BlockPos posIn) {
        this.signPosition = posIn;
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleSignEditorOpen(this);
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        this.signPosition = buf.readBlockPos();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBlockPos(this.signPosition);
    }

    @SideOnly(Side.CLIENT)
    public BlockPos getSignPosition() {
        return this.signPosition;
    }
}