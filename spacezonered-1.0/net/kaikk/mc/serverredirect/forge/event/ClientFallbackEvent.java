package net.kaikk.mc.serverredirect.forge.event;

import net.minecraftforge.eventbus.api.*;

@Cancelable
public class ClientFallbackEvent extends Event
{
    protected final String address;
    
    public ClientFallbackEvent(final String address) {
        this.address = address;
    }
    
    public String getAddress() {
        return this.address;
    }
}
