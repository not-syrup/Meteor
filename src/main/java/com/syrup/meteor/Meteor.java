package com.syrup.meteor;

import com.syrup.meteor.command.MeteorCommand;
import com.syrup.meteor.config.MeteorConfig;
import com.syrup.meteor.runner.MeteorRunner;
import me.lucko.helper.Schedulers;
import org.bukkit.plugin.java.JavaPlugin;

public final class Meteor extends JavaPlugin {

    //현재 SimplePlugin 상속하면 Foundation 에 의해 로드되는게 X => 오류나서 임시로 JavaPlugin 상속.
    private MeteorConfig meteorConfig;
    private MeteorRunner runner;

    @Override
    public void onEnable() {
        getLogger().info("Plugin starts.");
        meteorConfig = new MeteorConfig().loadFromConfig();
        runner = new MeteorRunner(meteorConfig);
        new MeteorCommand(runner.getData()).init();
        Schedulers.sync().runLater(() -> runner.run(), 200L);
    }

    @Override
    public void onDisable() {
        meteorConfig.saveToConfig(false);
    }
}
