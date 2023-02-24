package com.syrup.meteor.runner;

import com.syrup.meteor.data.MeteorData;
import me.lucko.helper.Schedulers;
import me.lucko.helper.random.RandomSelector;
import me.lucko.helper.random.VariableAmount;
import org.bukkit.*;

import java.util.ArrayList;
import java.util.List;

public class MeteorRunner {

    private boolean isRunning = false;
    private static final int X_WIDTH = 15;

    private final int Y_HEIGHT = 20;
    private final int Z_WIDTH = 15;

    public void run() {
        if (!isRunning) {
            isRunning = true;
            Schedulers.sync().runRepeating((task) -> {
                callMeteor(RandomSelector.uniform(Bukkit.getServer().getOnlinePlayers()).pick().getLocation());
            }, 1200, 1200);
        }
    }

    public void callMeteor(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();
        World world = location.getWorld();
        // 메테오가 추락하는 최소, 최대 범위
        VariableAmount randX = VariableAmount.range(x - X_WIDTH, x + X_WIDTH);
        VariableAmount randZ = VariableAmount.range(z - Z_WIDTH, z + Z_WIDTH);
        List<MeteorData> dataList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Location start = new Location(world, randX.getFlooredAmount(), location.getY() + Y_HEIGHT, randZ.getFlooredAmount());
            int lastRandX = randX.getFlooredAmount();
            int lastRandZ = randZ.getFlooredAmount();
            Location end = new Location(world, lastRandX, world.getHighestBlockYAt(lastRandX, lastRandZ), lastRandZ);
            dataList.add(new MeteorData(start, end.subtract(start).toVector().normalize(), (it) -> world.createExplosion(it, 0)));
        }
        Schedulers.sync().runRepeating((task) -> {
            List<MeteorData> removeList = new ArrayList<>();
            dataList.forEach((key) -> {
                if (key.getCurrentLocation().getBlock().getType() == Material.AIR) {
                    key.getCurrentLocation().getWorld().spawnParticle(Particle.SMOKE_NORMAL, key.getCurrentLocation(), 50, 0.1, 0.1, 0.1, 0.1);
                    key.getCurrentLocation().getWorld().spawnParticle(Particle.FLAME, key.getCurrentLocation(), 20, 0.1, 0.1, 0.1, 0.1);
                    key.getCurrentLocation().add(key.getDirection());
                }
                else {
                    key.getHitCallback().accept(key.getCurrentLocation());
                    removeList.add(key);
                }
            });
            removeList.forEach(dataList::remove);
            if (dataList.size() == 0)
                task.stop();

        }, 0L, 2L);

    }

}
