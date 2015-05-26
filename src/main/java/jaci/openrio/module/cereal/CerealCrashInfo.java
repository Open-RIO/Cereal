package jaci.openrio.module.cereal;

import jaci.openrio.toast.lib.crash.CrashInfoProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides information in the Crash Log regarding Serial Ports.
 *
 * @author Jaci
 */
public class CerealCrashInfo implements CrashInfoProvider {
    @Override
    public String getName() {
        return "Cereal";
    }

    @Override
    public String getCrashInfoPre(Throwable t) {
        return null;
    }

    @Override
    public List<String> getCrashInfo(Throwable t) {
        ArrayList<String> s = new ArrayList<>();
        Cereal.refresh();
        if (Cereal.getAvailablePorts().size() > 0) {
            s.add("Available Serial Ports: ");
            s.add("\t" + Cereal.getAvailablePorts());
        } else
            s.add("No Available Serial Devices");
        if (Cereal.portWrappers.keySet().size() > 0) {
            s.add("Connected Serial Ports: ");
            s.add("\t" + Cereal.portWrappers.keySet());
        } else
            s.add("No Connected Serial Devices");
        return s;
    }
}
