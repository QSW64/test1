package net.kaikk.mc.serverredirect.forge.event;

import net.minecraftforge.eventbus.api.*;

@Cancelable
@Deprecated
public class RedirectEvent extends Event
{
    protected final String address;
    
    public RedirectEvent(final String address) {
        this.address = address;
    }
    
    public String getAddress() {
        return this.address;
    }
}
