package com.syrup.meteor;

import com.syrup.meteor.runner.MeteorRunner;
import me.lucko.helper.Schedulers;
import org.bukkit.plugin.java.JavaPlugin;
import org.mineacademy.fo.plugin.SimplePlugin;

public final class Meteor extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Plugin starts.");
        Schedulers.sync().runLater(() -> new MeteorRunner().run(), 200L);
    }
}
