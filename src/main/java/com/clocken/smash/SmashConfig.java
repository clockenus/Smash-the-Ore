package com.clocken.smash;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = Smash.MOD_NAME)
public class SmashConfig implements ConfigData {

    public boolean enabled = true;

    public SmashBehavior smashBehavior = SmashBehavior.BY_LAYER;

    enum SmashBehavior {
        BY_BLOCK,
        BY_LAYER
    }

    public static SmashConfig get() {
        return AutoConfig.getConfigHolder(SmashConfig.class).getConfig();
    }
}
