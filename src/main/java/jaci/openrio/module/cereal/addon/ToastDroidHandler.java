package jaci.openrio.module.cereal.addon;

import jaci.openrio.module.android.tile.Tile;
import jaci.openrio.module.android.tile.TileRegistry;
import jaci.openrio.module.cereal.Cereal;

import java.util.List;

public class ToastDroidHandler {

    static Tile tile;

    public static void toast_droid() {
        tile = new Tile("cereal_module", "Cereal", "Open Ports: ") {
            public String[] getSubtitles() {
                Cereal.refresh();
                List<String> ports = Cereal.getAvailablePorts();
                if (ports == null)
                    return new String[] {"No Ports Available"};
                String[] array = new String[ports.size() + 1];
                array[0] = "Available Ports: ";
                for (int i = 0; i < ports.size(); i++) {
                    array[i + 1] = ports.get(i);
                }
                return array;
            }
        };
        TileRegistry.register(tile);
    }

}
