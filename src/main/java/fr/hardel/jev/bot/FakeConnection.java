package fr.hardel.jev.bot;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;

/** The bot's side of the wire: an open in-memory channel with no client behind it. What the server sends is what a client would perceive, the ears keep the sounds. */
final class FakeConnection extends Connection {
    private final Ears ears;

    FakeConnection(Ears ears) {
        super(PacketFlow.SERVERBOUND);
        this.ears = ears;
        this.channel = new EmbeddedChannel();
    }

    @Override
    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> protocol, T listener) {
        this.packetListener = listener;
    }

    @Override
    public void setupOutboundProtocol(ProtocolInfo<?> protocol) {
    }

    @Override
    public void send(Packet<?> packet) {
        switch (packet) {
            case ClientboundSoundPacket sound -> ears.hear(sound);
            case ClientboundSoundEntityPacket sound -> ears.hear(sound);
            default -> { }
        }
    }

    @Override
    public void send(Packet<?> packet, ChannelFutureListener listener) {
        send(packet);
    }

    @Override
    public void send(Packet<?> packet, ChannelFutureListener listener, boolean flush) {
        send(packet);
    }

    @Override
    public void flushChannel() {
    }
}
