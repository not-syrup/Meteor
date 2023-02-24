package com.syrup.meteor.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

@AllArgsConstructor
@Data
public class MeteorData {

    private Location currentLocation;
    private Vector direction;
    private Consumer<Location> hitCallback;

}
