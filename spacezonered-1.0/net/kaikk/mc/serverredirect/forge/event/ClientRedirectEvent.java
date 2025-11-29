package net.kaikk.mc.serverredirect.forge.event;

import net.minecraftforge.eventbus.api.*;

@Cancelable
public class ClientRedirectEvent extends Event
{
    protected final String address;
    
    public ClientRedirectEvent(final String address) {
        this.address = address;
    }
    
    public String getAddress() {
        return this.address;
    }
}
