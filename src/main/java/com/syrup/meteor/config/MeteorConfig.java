package com.syrup.meteor.config;

import lombok.Getter;
import org.mineacademy.fo.settings.YamlConfig;

public final class MeteorConfig extends YamlConfig {

    @Getter
    private int x; //기준 위치로부터 X너비
    @Getter
    private int y; //기준 위치로부터 Y 너비
    @Getter
    private int z; //X랑 동일
    @Getter
    private int interval; //메테오 발생주기 (second)
    @Getter
    private int amount; //메테오 갯수
    @Getter
    private String warn;
    @Getter
    private String impact;


    public MeteorConfig() {
        loadConfiguration(NO_DEFAULT, "meteor-config.yml");
    }
    @Override
    protected void onLoad() {
        loadFromConfig();
    }

    @Override
    public void onSave() {
        //세이브 호출은 잘 안되긴 하지만..
        saveToConfig(true);
    }

    public void saveToConfig(boolean isAutoSave) {
        //데이터 저장
        set("meteor.x", x);
        set("meteor.y", y);
        set("meteor.z", z);
        set("meteor.interval", interval);
        set("meteor.amount", amount);
        set("meteor.warn", warn);
        set("meteor.impact", impact);
        if (!isAutoSave)
            save();
    }

    public MeteorConfig loadFromConfig() {
        x = get("meteor.x", Integer.class, 25);
        y = get("meteor.y", Integer.class, 45);
        z = get("meteor.z", Integer.class, 25);
        interval = get("meteor.interval", Integer.class, 60);
        amount = get("meteor.amount", Integer.class, 15);
        warn = get("meteor.warn", String.class, "&cMeteor &fwill impact in &4{TIME} &fseconds! [{loc}]");
        impact = get("meteor.impact", String.class, "&cMeteor &fimpact at [{loc}]!");
        return this;
    }
}
