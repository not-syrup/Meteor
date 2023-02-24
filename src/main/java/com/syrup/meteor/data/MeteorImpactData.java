package com.syrup.meteor.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;

@Data
@AllArgsConstructor
public class MeteorImpactData {

    private Location impact;
    private int remain;
}
