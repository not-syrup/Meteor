package com.syrup.meteor.runner;

import com.syrup.meteor.config.MeteorConfig;
import com.syrup.meteor.data.MeteorData;
import com.syrup.meteor.data.MeteorImpactData;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import me.lucko.helper.random.RandomSelector;
import me.lucko.helper.random.VariableAmount;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;

import java.util.ArrayList;
import java.util.List;

import static com.syrup.meteor.util.StringUtil.locationToString;


public class MeteorRunner {

    @Getter
    private boolean isRunning = false;
    @Getter
    private final MeteorImpactData data;
    //data class
    private final MeteorConfig config;

    public MeteorRunner(MeteorConfig config) {
        this.config = config;
        this.data = new MeteorImpactData(null, config.getInterval()); //초기화 된 시점.
    }


    public void run() {
        if (!isRunning) {
            //동시 호출 막기 위함.
            isRunning = true;
            Schedulers.sync().runRepeating((task) -> {
                int remain = data.getRemain();
                Location impact = data.getImpact();
                if (impact == null) {
                    selectImpactLocation();
                    return;
                }
                //1초 주기 측정
                if (remain == 0)
                    callMeteor(impact, meteorStrikeEndCallback()); //메테오 요청된 플레이어의 위치에 작동될 callback (뭐.. 창고생성이든지.. 등등)
                else if (remain < 0)
                    data.setRemain(config.getInterval()); //초기화
                else if (remain == 60 || remain == 30)
                    sendMeteorWarn();

                data.setRemain(data.getRemain() - 1);
            }, 200, 20L);
        }
    }

    //기준 좌표로부터 메테오 발생.
    public void callMeteor(Location location, Consumer<Location> finishStrikeCallback) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        int widthX = config.getX();
        int heightY = config.getY();
        int widthZ = config.getZ();
        int amount = config.getAmount();
        World world = location.getWorld();
        sendMeteorImpact();
        selectImpactLocation();
        if (!world.isChunkLoaded(x, z)) {
            // 메테오가 추락하는 최소, 최대 범위
            VariableAmount randX = VariableAmount.range(x - widthX, x + widthX);
            VariableAmount randZ = VariableAmount.range(z - widthZ, z + widthZ);
            List<MeteorData> dataList = new ArrayList<>();

            for (int i = 0; i < amount; i++) {
                Location start = new Location(world, randX.getFlooredAmount(), y + heightY, randZ.getFlooredAmount()); //시작지점
                int lastRandX = randX.getFlooredAmount();
                int lastRandZ = randZ.getFlooredAmount();
                int highestY = world.getHighestBlockYAt(lastRandX, lastRandZ);
                Location end = new Location(world, lastRandX, highestY, lastRandZ); //착탄지점
                dataList.add(new MeteorData(start, end.subtract(start).toVector()
                        .normalize(), meteorStrikeIndividualCallback()));
            }

            Schedulers.sync().runRepeating((task) -> {
                List<MeteorData> removeList = new ArrayList<>();
                dataList.forEach((data) -> {
                    Location current = data.getCurrentLocation();
                    World locationWorld = current.getWorld();
                    if (current.getBlock().getType() == Material.AIR) {
                        locationWorld.spawnParticle(Particle.SMOKE_LARGE, current, 50, 0.1, 0.1, 0.1, 0.1);
                        locationWorld.spawnParticle(Particle.FLAME, current, 10, 0.1, 0.1, 0.1, 0.1);
                        current.add(data.getDirection()); //한칸씩 전진
                    }
                    else {
                        data.getHitCallback().accept(current); //충돌 당시 지점에 콜백 실행
                        removeList.add(data);
                    }
                });
                removeList.forEach(dataList::remove);
                if (dataList.size() == 0) {
                    finishStrikeCallback.accept(location);
                    task.stop();
                }

            }, 0L, 2L);
        }
        else
            Common.warning("meteor was called at unloaded chunk [X:" + x + ", Z:" + z + "]");
    }

    private Consumer<Location> meteorStrikeIndividualCallback() {
        return (it) -> {
            it.getBlock().setType(Material.MAGMA_BLOCK);
            it.getWorld().createExplosion(it, 0);
            it.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, it, 5, 5, 5, 0);
            it.getWorld().spawnParticle(Particle.SMOKE_LARGE, it, 50, 1, 1, 0.5);//마그마 블록 설치
        };
    }

    private Consumer<Location> meteorStrikeEndCallback() {
        return (it) -> {
            World world = it.getWorld();
            it.getBlock().setType(Material.CHEST);
            world.spawnParticle(Particle.SMOKE_LARGE, it, 500, 0.4, 0.4, 0.4, 0.5);
            for (int i = 0; i < 4; i++) {
                //약간의 불붙이기?
                VariableAmount random = VariableAmount.range(-4, 4);
                int x = it.getBlockX() + random.getFlooredAmount();
                int z = it.getBlockZ() + random.getFlooredAmount();
                int y = world.getHighestBlockYAt(x, z) + 1;
                world.getBlockAt(x, y, z).setType(Material.FIRE);
            }
        };
    }

    private void selectImpactLocation() {
        if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
            Player selected = RandomSelector.uniform(Bukkit.getServer().getOnlinePlayers()).pick();
            Location target = selected.getLocation();
            World world = target.getWorld();
            int x = target.getBlockX();
            int z = target.getBlockZ();
            int heightY = target.getWorld().getHighestBlockYAt(x, z) + 1;
            data.setImpact(new Location(world, x, heightY, z));
        }
    }
    private void sendMeteorWarn() {
        Messenger.broadcastWarn(ChatUtil.center(config.getWarn().replace("{TIME}", String.valueOf(data.getRemain())).replace("{loc}", locationToString(data.getImpact()))));
    }

    private void sendMeteorImpact() {
        Messenger.broadcastWarn(ChatUtil.center(config.getImpact().replace("{loc}", locationToString(data.getImpact()))));
    }


}
