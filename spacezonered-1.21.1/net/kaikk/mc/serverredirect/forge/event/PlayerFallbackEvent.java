package net.kaikk.mc.serverredirect.forge.event;

import net.minecraftforge.eventbus.api.*;
import net.minecraft.server.level.*;

@Cancelable
public class PlayerFallbackEvent extends Event
{
    protected final ServerPlayer player;
    protected final String address;
    
    public PlayerFallbackEvent(final ServerPlayer player, final String address) {
        this.player = player;
        this.address = address;
    }
    
    public ServerPlayer getPlayer() {
        return this.player;
    }
    
    public String getAddress() {
        return this.address;
    }
}
