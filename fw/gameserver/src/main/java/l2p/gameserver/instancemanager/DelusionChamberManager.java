package l2p.gameserver.instancemanager;

import l2p.commons.geometry.Rectangle;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SimpleSpawner;
import l2p.gameserver.model.Territory;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.TeleportToLocation;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DelusionChamberManager {
    private static final Logger _log = LoggerFactory.getLogger(DelusionChamberManager.class);
    private static DelusionChamberManager _instance;
    private Map<Integer, Map<Integer, DelusionChamberRoom>> _rooms = new ConcurrentHashMap<Integer, Map<Integer, DelusionChamberRoom>>();

    public static DelusionChamberManager getInstance() {
        if (_instance == null)
            _instance = new DelusionChamberManager();

        return _instance;
    }

    public DelusionChamberManager() {
        load();
    }

    public DelusionChamberRoom getRoom(int type, int room) {
        return _rooms.get(type).get(room);
    }

    public Map<Integer, DelusionChamberRoom> getRooms(int type) {
        return _rooms.get(type);
    }

    public void load() {
        int countGood = 0, countBad = 0;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);

            File file = new File(Config.DATAPACK_ROOT, "data/delusion_chamber.xml");
            if (!file.exists())
                throw new IOException();

            Document doc = factory.newDocumentBuilder().parse(file);
            NamedNodeMap attrs;
            int type;
            int roomId;
            int mobId, delay, count;
            SimpleSpawner spawnDat;
            NpcTemplate template;
            Location tele = new Location();
            int xMin = 0, xMax = 0, yMin = 0, yMax = 0, zMin = 0, zMax = 0;
            boolean isBossRoom;

            for (Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling())
                if ("rift".equalsIgnoreCase(rift.getNodeName()))
                    for (Node area = rift.getFirstChild(); area != null; area = area.getNextSibling())
                        if ("area".equalsIgnoreCase(area.getNodeName())) {
                            attrs = area.getAttributes();
                            type = Integer.parseInt(attrs.getNamedItem("type").getNodeValue());

                            for (Node room = area.getFirstChild(); room != null; room = room.getNextSibling())
                                if ("room".equalsIgnoreCase(room.getNodeName())) {
                                    attrs = room.getAttributes();
                                    roomId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                                    Node boss = attrs.getNamedItem("isBossRoom");
                                    isBossRoom = boss != null && Boolean.parseBoolean(boss.getNodeValue());
                                    Territory territory = null;
                                    for (Node coord = room.getFirstChild(); coord != null; coord = coord.getNextSibling())
                                        if ("teleport".equalsIgnoreCase(coord.getNodeName())) {
                                            attrs = coord.getAttributes();
                                            tele = Location.parseLoc(attrs.getNamedItem("loc").getNodeValue());
                                        } else if ("zone".equalsIgnoreCase(coord.getNodeName())) {
                                            attrs = coord.getAttributes();
                                            xMin = Integer.parseInt(attrs.getNamedItem("xMin").getNodeValue());
                                            xMax = Integer.parseInt(attrs.getNamedItem("xMax").getNodeValue());
                                            yMin = Integer.parseInt(attrs.getNamedItem("yMin").getNodeValue());
                                            yMax = Integer.parseInt(attrs.getNamedItem("yMax").getNodeValue());
                                            zMin = Integer.parseInt(attrs.getNamedItem("zMin").getNodeValue());
                                            zMax = Integer.parseInt(attrs.getNamedItem("zMax").getNodeValue());

                                            territory = new Territory().add(new Rectangle(xMin, yMin, xMax, yMax).setZmin(zMin).setZmax(zMax));
                                        }

                                    if (territory == null)
                                        _log.error("DimensionalRiftManager: invalid spawn data for room id " + roomId + "!");

                                    if (!_rooms.containsKey(type))
                                        _rooms.put(type, new ConcurrentHashMap<Integer, DelusionChamberRoom>());

                                    _rooms.get(type).put(roomId, new DelusionChamberRoom(territory, tele, isBossRoom));

                                    for (Node spawn = room.getFirstChild(); spawn != null; spawn = spawn.getNextSibling())
                                        if ("spawn".equalsIgnoreCase(spawn.getNodeName())) {
                                            attrs = spawn.getAttributes();
                                            mobId = Integer.parseInt(attrs.getNamedItem("mobId").getNodeValue());
                                            delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
                                            count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());

                                            template = NpcHolder.getInstance().getTemplate(mobId);
                                            if (template == null)
                                                _log.warn("Template " + mobId + " not found!");
                                            if (!_rooms.containsKey(type))
                                                _log.warn("Type " + type + " not found!");
                                            else if (!_rooms.get(type).containsKey(roomId))
                                                _log.warn("Room " + roomId + " in Type " + type + " not found!");

                                            if (template != null && _rooms.containsKey(type) && _rooms.get(type).containsKey(roomId)) {
                                                spawnDat = new SimpleSpawner(template);
                                                spawnDat.setTerritory(territory);
                                                spawnDat.setHeading(-1);
                                                spawnDat.setRespawnDelay(delay);
                                                spawnDat.setAmount(count);
                                                _rooms.get(type).get(roomId).getSpawns().add(spawnDat);
                                                countGood++;
                                            } else
                                                countBad++;
                                        }
                                }
                        }
        } catch (Exception e) {
            _log.error("DelusionChamberManager: Error on loading delusion chamber spawns!", e);
        }
        int typeSize = _rooms.keySet().size();
        int roomSize = 0;

        for (int b : _rooms.keySet())
            roomSize += _rooms.get(b).keySet().size();

        _log.info("DelusionChamberManager: Loaded " + typeSize + " room types with " + roomSize + " rooms.");
        _log.info("DelusionChamberManager: Loaded " + countGood + " delusion chamber spawns, " + countBad + " errors.");
    }

    public void reload() {
        for (int b : _rooms.keySet())
            _rooms.get(b).clear();

        _rooms.clear();
        load();
    }

    public void teleportToWaitingRoom(Player player) {
        teleToLocation(player, Location.findPointToStay(getRoom(0, 0).getTeleportCoords(), 0, 250, ReflectionManager.DEFAULT.getGeoIndex()), null);
    }

    public class DelusionChamberRoom {
        private final Territory _territory;
        private final Location _teleportCoords;
        private final boolean _isBossRoom;
        private final List<SimpleSpawner> _roomSpawns;

        public DelusionChamberRoom(Territory territory, Location tele, boolean isBossRoom) {
            _territory = territory;
            _teleportCoords = tele;
            _isBossRoom = isBossRoom;
            _roomSpawns = new ArrayList<SimpleSpawner>();
        }

        public Location getTeleportCoords() {
            return _teleportCoords;
        }

        public boolean checkIfInZone(Location loc) {
            return checkIfInZone(loc.x, loc.y, loc.z);
        }

        public boolean checkIfInZone(int x, int y, int z) {
            return _territory.isInside(x, y, z);
        }

        public boolean isBossRoom() {
            return _isBossRoom;
        }

        public List<SimpleSpawner> getSpawns() {
            return _roomSpawns;
        }
    }

    public void showHtmlFile(Player player, String file, NpcInstance npc) {
        NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
        html.setFile(file);
        html.replace("%t_name%", npc.getName());
        player.sendPacket(html);
    }

    public static void teleToLocation(Player player, Location loc, Reflection ref) {
        if (player.isTeleporting() || player.isDeleted())
            return;
        player.setIsTeleporting(true);

        player.setTarget(null);
        player.stopMove();

        if (player.isInBoat())
            player.setBoat(null);

        player.breakFakeDeath();

        player.decayMe();

        player.setLoc(loc);

        if (ref == null)
            player.setReflection(ReflectionManager.DEFAULT);

        // Нужно при телепорте с более высокой точки на более низкую, иначе наносится вред от "падения"
        player.setLastClientPosition(null);
        player.setLastServerPosition(null);
        player.sendPacket(new TeleportToLocation(player, loc));
    }
}