package com.syrup.meteor.command;

import com.syrup.meteor.data.MeteorImpactData;
import me.lucko.helper.Commands;
import org.mineacademy.fo.Messenger;

import static com.syrup.meteor.util.StringUtil.locationToString;

public class MeteorCommand {

    private final MeteorImpactData data;

    public MeteorCommand(MeteorImpactData data) {
        this.data = data;
    }

    public void init() {
        Commands.create().assertPlayer().handler(e -> {
            Messenger.info(e.sender(), locationToString(data.getImpact()));
            Messenger.info(e.sender(), "remain : " + data.getRemain());
        }).register("meteor");
    }
}
