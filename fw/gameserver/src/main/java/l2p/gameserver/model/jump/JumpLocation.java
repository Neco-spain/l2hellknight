package l2p.gameserver.model.jump;

import l2p.gameserver.utils.Location;

/**
 * @author : Ragnarok
 * @date : 17.01.12  16:10
 */
public class JumpLocation {
    private String zoneName;
    private int id;
    private boolean is_last;
    private int[] routes;
    private Location location;

    public JumpLocation(String zoneName, int id, boolean is_last, int[] routes, Location location) {
        this.zoneName = zoneName;
        this.id = id;
        this.is_last = is_last;
        this.routes = routes;
        this.location = location;
    }

    public String getZoneName() {
        return zoneName;
    }

    public int getId() {
        return id;
    }

    public boolean isLast() {
        return is_last;
    }

    public int[] getRoutes() {
        return routes;
    }

    public Location getLocation() {
        return location;
    }

    public int getX() {
        return location.getX();
    }

    public int getY() {
        return location.getY();
    }

    public int getZ() {
        return location.getZ();
    }
}
