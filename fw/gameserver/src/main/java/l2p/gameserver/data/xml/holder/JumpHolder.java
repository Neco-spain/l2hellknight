package l2p.gameserver.data.xml.holder;

import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.model.jump.JumpLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Ragnarok
 * @date : 16.01.12  15:45
 */
public class JumpHolder extends AbstractHolder {
    private static JumpHolder ourInstance = new JumpHolder();
    // Список локаций для начала прыжка
    private static Map<String, List<JumpLocation>> startLocations;
    private static Map<Integer, JumpLocation> locationById;

    public static JumpHolder getInstance() {
        return ourInstance;
    }

    private JumpHolder() {
        startLocations = new HashMap<String, List<JumpLocation>>();
        locationById = new HashMap<Integer, JumpLocation>();
    }

    @Override
    public int size() {
        return locationById.size();
    }

    @Override
    public void clear() {
        startLocations.clear();
        locationById.clear();
    }

    public void addLocation(JumpLocation location) {
        if (location.getZoneName() != null) {
            if (!startLocations.containsKey(location.getZoneName())) {
                startLocations.put(location.getZoneName(), new ArrayList<JumpLocation>());
            }
            startLocations.get(location.getZoneName()).add(location);
        }
        locationById.put(location.getId(), location);
    }

    public List<JumpLocation> getJumpLocations(String zoneName) {
        if (startLocations.containsKey(zoneName)) {
            return startLocations.get(zoneName);
        }
        return new ArrayList<JumpLocation>();
    }

    public JumpLocation getJumpLocationById(int id) {
        return locationById.get(id);
    }
}
