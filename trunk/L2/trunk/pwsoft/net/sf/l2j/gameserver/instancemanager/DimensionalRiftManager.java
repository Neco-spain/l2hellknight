package net.sf.l2j.gameserver.instancemanager;

import java.awt.Polygon;
import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Rnd;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DimensionalRiftManager
{
  private static Log _log = LogFactory.getLog(DimensionalRiftManager.class.getName());
  private static DimensionalRiftManager _instance;
  private FastMap<Byte, FastMap<Byte, DimensionalRiftRoom>> _rooms = new FastMap();
  private final short DIMENSIONAL_FRAGMENT_ITEM_ID = 7079;

  public static DimensionalRiftManager getInstance()
  {
    if (_instance == null) {
      _instance = new DimensionalRiftManager();
    }
    return _instance;
  }

  private DimensionalRiftManager()
  {
    loadRooms();
    loadSpawns();
  }

  public DimensionalRiftRoom getRoom(byte type, byte room)
  {
    return _rooms.get(Byte.valueOf(type)) == null ? null : (DimensionalRiftRoom)((FastMap)_rooms.get(Byte.valueOf(type))).get(Byte.valueOf(room));
  }

  private void loadRooms()
  {
    Connect con = null;
    PreparedStatement s = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      s = con.prepareStatement("SELECT * FROM dimensional_rift");
      rs = s.executeQuery();

      while (rs.next())
      {
        byte type = rs.getByte("type");
        byte room_id = rs.getByte("room_id");

        int xMin = rs.getInt("xMin");
        int xMax = rs.getInt("xMax");
        int yMin = rs.getInt("yMin");
        int yMax = rs.getInt("yMax");
        int z1 = rs.getInt("zMin");
        int z2 = rs.getInt("zMax");
        int xT = rs.getInt("xT");
        int yT = rs.getInt("yT");
        int zT = rs.getInt("zT");
        boolean isBossRoom = rs.getByte("boss") > 0;

        if (!_rooms.containsKey(Byte.valueOf(type))) {
          _rooms.put(Byte.valueOf(type), new FastMap());
        }
        ((FastMap)_rooms.get(Byte.valueOf(type))).put(Byte.valueOf(room_id), new DimensionalRiftRoom(type, room_id, xMin, xMax, yMin, yMax, z1, z2, xT, yT, zT, isBossRoom));
      }
    }
    catch (Exception e)
    {
      _log.warn("Can't load Dimension Rift zones. " + e);
    }
    finally
    {
      Close.CSR(con, s, rs);
    }

    int typeSize = _rooms.keySet().size();
    int roomSize = 0;

    for (Byte b : _rooms.keySet()) {
      roomSize += ((FastMap)_rooms.get(b)).keySet().size();
    }
    _log.info("DimensionalRiftManager: Loaded " + typeSize + " room types with " + roomSize + " rooms.");
  }

  public void loadSpawns()
  {
    int countGood = 0; int countBad = 0;
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);

      File file = new File(Config.DATAPACK_ROOT + "/data/dimensionalRift.xml");
      if (!file.exists()) {
        throw new IOException();
      }
      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling())
      {
        if (!"rift".equalsIgnoreCase(rift.getNodeName()))
          continue;
        for (Node area = rift.getFirstChild(); area != null; area = area.getNextSibling())
        {
          if (!"area".equalsIgnoreCase(area.getNodeName()))
            continue;
          NamedNodeMap attrs = area.getAttributes();
          byte type = Byte.parseByte(attrs.getNamedItem("type").getNodeValue());

          for (Node room = area.getFirstChild(); room != null; room = room.getNextSibling())
          {
            if (!"room".equalsIgnoreCase(room.getNodeName()))
              continue;
            attrs = room.getAttributes();
            byte roomId = Byte.parseByte(attrs.getNamedItem("id").getNodeValue());

            for (Node spawn = room.getFirstChild(); spawn != null; spawn = spawn.getNextSibling())
            {
              if (!"spawn".equalsIgnoreCase(spawn.getNodeName()))
                continue;
              attrs = spawn.getAttributes();
              int mobId = Integer.parseInt(attrs.getNamedItem("mobId").getNodeValue());
              int delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
              int count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());

              L2NpcTemplate template = NpcTable.getInstance().getTemplate(mobId);
              if (template == null) _log.warn("Template " + mobId + " not found!");
              if (!_rooms.containsKey(Byte.valueOf(type))) _log.warn("Type " + type + " not found!");
              else if (!((FastMap)_rooms.get(Byte.valueOf(type))).containsKey(Byte.valueOf(roomId))) _log.warn("Room " + roomId + " in Type " + type + " not found!");

              for (int i = 0; i < count; i++)
              {
                DimensionalRiftRoom riftRoom = (DimensionalRiftRoom)((FastMap)_rooms.get(Byte.valueOf(type))).get(Byte.valueOf(roomId));
                int x = riftRoom.getRandomX();
                int y = riftRoom.getRandomY();
                int z = riftRoom.getTeleportCoords()[2];

                if ((template != null) && (_rooms.containsKey(Byte.valueOf(type))) && (((FastMap)_rooms.get(Byte.valueOf(type))).containsKey(Byte.valueOf(roomId))))
                {
                  L2Spawn spawnDat = new L2Spawn(template);
                  spawnDat.setAmount(1);
                  spawnDat.setLocx(x);
                  spawnDat.setLocy(y);
                  spawnDat.setLocz(z);
                  spawnDat.setHeading(-1);
                  spawnDat.setRespawnDelay(delay);
                  SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                  ((DimensionalRiftRoom)((FastMap)_rooms.get(Byte.valueOf(type))).get(Byte.valueOf(roomId))).getSpawns().add(spawnDat);
                  countGood++;
                }
                else
                {
                  countBad++;
                }
              }
            }

          }

        }

      }

    }
    catch (Exception e)
    {
      _log.warn("Error on loading dimensional rift spawns: " + e);
      e.printStackTrace();
    }
    _log.info("DimensionalRiftManager: Loaded " + countGood + " dimensional rift spawns, " + countBad + " errors.");
  }

  public void reload()
  {
    for (Byte b : _rooms.keySet())
    {
      for (Iterator i$ = ((FastMap)_rooms.get(b)).keySet().iterator(); i$.hasNext(); ) { int i = ((Byte)i$.next()).byteValue();

        ((DimensionalRiftRoom)((FastMap)_rooms.get(b)).get(Integer.valueOf(i))).getSpawns().clear();
      }
      ((FastMap)_rooms.get(b)).clear();
    }
    _rooms.clear();
    loadRooms();
    loadSpawns();
  }

  public boolean checkIfInRiftZone(int x, int y, int z, boolean ignorePeaceZone)
  {
    if (ignorePeaceZone) {
      return ((DimensionalRiftRoom)((FastMap)_rooms.get(Byte.valueOf(0))).get(Byte.valueOf(1))).checkIfInZone(x, y, z);
    }
    return (((DimensionalRiftRoom)((FastMap)_rooms.get(Byte.valueOf(0))).get(Byte.valueOf(1))).checkIfInZone(x, y, z)) && (!((DimensionalRiftRoom)((FastMap)_rooms.get(Byte.valueOf(0))).get(Byte.valueOf(0))).checkIfInZone(x, y, z));
  }

  public boolean checkIfInPeaceZone(int x, int y, int z)
  {
    return ((DimensionalRiftRoom)((FastMap)_rooms.get(Byte.valueOf(0))).get(Byte.valueOf(0))).checkIfInZone(x, y, z);
  }

  public void teleportToWaitingRoom(L2PcInstance player)
  {
    int[] coords = getRoom(0, 0).getTeleportCoords();
    player.teleToLocation(coords[0], coords[1], coords[2]);
  }

  public void start(L2PcInstance player, byte type, L2NpcInstance npc)
  {
    boolean canPass = true;
    if (!player.isInParty())
    {
      showHtmlFile(player, "data/html/seven_signs/rift/NoParty.htm", npc);
      return;
    }

    if (player.getParty().getPartyLeaderOID() != player.getObjectId())
    {
      showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
      return;
    }

    if (player.getParty().isInDimensionalRift())
    {
      handleCheat(player, npc);
      return;
    }

    if (player.getParty().getMemberCount() < Config.RIFT_MIN_PARTY_SIZE)
    {
      NpcHtmlMessage html = NpcHtmlMessage.id(npc.getObjectId());
      html.setFile("data/html/seven_signs/rift/SmallParty.htm");
      html.replace("%npc_name%", npc.getName());
      html.replace("%count%", Integer.valueOf(Config.RIFT_MIN_PARTY_SIZE).toString());
      player.sendPacket(html);
      return;
    }

    for (L2PcInstance p : player.getParty().getPartyMembers()) {
      if (!checkIfInPeaceZone(p.getX(), p.getY(), p.getZ()))
        canPass = false;
    }
    if (!canPass)
    {
      showHtmlFile(player, "data/html/seven_signs/rift/NotInWaitingRoom.htm", npc);
      return;
    }

    for (L2PcInstance p : player.getParty().getPartyMembers())
    {
      L2ItemInstance i = p.getInventory().getItemByItemId(7079);

      if (i == null)
      {
        canPass = false;
        break;
      }

      if ((i.getCount() > 0) && 
        (i.getCount() < getNeededItems(type))) {
        canPass = false;
      }
    }
    if (!canPass)
    {
      NpcHtmlMessage html = NpcHtmlMessage.id(npc.getObjectId());
      html.setFile("data/html/seven_signs/rift/NoFragments.htm");
      html.replace("%npc_name%", npc.getName());
      html.replace("%count%", Integer.valueOf(getNeededItems(type)).toString());
      player.sendPacket(html);
      return;
    }

    for (L2PcInstance p : player.getParty().getPartyMembers())
    {
      L2ItemInstance i = p.getInventory().getItemByItemId(7079);
      p.destroyItem("RiftEntrance", i.getObjectId(), getNeededItems(type), null, false);
    }

    new DimensionalRift(player.getParty(), type, (byte)Rnd.get(1, 9));
  }

  public void killRift(DimensionalRift d)
  {
    if (d.getTeleportTimerTask() != null) d.getTeleportTimerTask().cancel();
    d.setTeleportTimerTask(null);

    if (d.getTeleportTimer() != null) d.getTeleportTimer().cancel();
    d.setTeleportTimer(null);

    if (d.getSpawnTimerTask() != null) d.getSpawnTimerTask().cancel();
    d.setSpawnTimerTask(null);

    if (d.getSpawnTimer() != null) d.getSpawnTimer().cancel();
    d.setSpawnTimer(null);
  }

  private int getNeededItems(byte type)
  {
    switch (type)
    {
    case 1:
      return Config.RIFT_ENTER_COST_RECRUIT;
    case 2:
      return Config.RIFT_ENTER_COST_SOLDIER;
    case 3:
      return Config.RIFT_ENTER_COST_OFFICER;
    case 4:
      return Config.RIFT_ENTER_COST_CAPTAIN;
    case 5:
      return Config.RIFT_ENTER_COST_COMMANDER;
    case 6:
      return Config.RIFT_ENTER_COST_HERO;
    }
    return 999999;
  }

  public void showHtmlFile(L2PcInstance player, String file, L2NpcInstance npc)
  {
    NpcHtmlMessage html = NpcHtmlMessage.id(npc.getObjectId());
    html.setFile(file);
    html.replace("%npc_name%", npc.getName());
    player.sendPacket(html);
  }

  public void handleCheat(L2PcInstance player, L2NpcInstance npc)
  {
    showHtmlFile(player, "data/html/seven_signs/rift/Cheater.htm", npc);
    if (!player.isGM())
    {
      _log.warn("Player " + player.getName() + "(" + player.getObjectId() + ") was cheating in dimension rift area!");
      Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " tried to cheat in dimensional rift.", Config.DEFAULT_PUNISH);
    }
  }

  public static class DimensionalRiftRoom
  {
    protected final byte _type;
    protected final byte _room;
    private final int _xMin;
    private final int _xMax;
    private final int _yMin;
    private final int _yMax;
    private final int _zMin;
    private final int _zMax;
    private final int[] _teleportCoords;
    private final Shape _s;
    private final boolean _isBossRoom;
    private final FastList<L2Spawn> _roomSpawns;
    protected final FastList<L2NpcInstance> _roomMobs;

    public DimensionalRiftRoom(byte type, byte room, int xMin, int xMax, int yMin, int yMax, int zMin, int zMax, int xT, int yT, int zT, boolean isBossRoom)
    {
      _type = type;
      _room = room;
      _xMin = (xMin + 128);
      _xMax = (xMax - 128);
      _yMin = (yMin + 128);
      _yMax = (yMax - 128);
      _zMin = zMin;
      _zMax = zMax;
      _teleportCoords = new int[] { xT, yT, zT };
      _isBossRoom = isBossRoom;
      _roomSpawns = new FastList();
      _roomMobs = new FastList();
      _s = new Polygon(new int[] { xMin, xMax, xMax, xMin }, new int[] { yMin, yMin, yMax, yMax }, 4);
    }

    public int getRandomX()
    {
      return Rnd.get(_xMin, _xMax);
    }

    public int getRandomY()
    {
      return Rnd.get(_yMin, _yMax);
    }

    public int[] getTeleportCoords()
    {
      return _teleportCoords;
    }

    public boolean checkIfInZone(int x, int y, int z)
    {
      return (_s.contains(x, y)) && (z >= _zMin) && (z <= _zMax);
    }

    public boolean isBossRoom()
    {
      return _isBossRoom;
    }

    public FastList<L2Spawn> getSpawns()
    {
      return _roomSpawns;
    }

    public void spawn()
    {
      for (L2Spawn spawn : _roomSpawns)
      {
        spawn.doSpawn();
        spawn.startRespawn();
      }
    }

    public void unspawn()
    {
      for (L2Spawn spawn : _roomSpawns)
      {
        spawn.stopRespawn();
        if (spawn.getLastSpawn() != null)
          spawn.getLastSpawn().deleteMe();
      }
    }
  }
}