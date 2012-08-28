package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.WorldRegionTable;
import net.sf.l2j.gameserver.datatables.WorldRegionTable.Region;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.log.AbstractLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import scripts.zone.L2ZoneForm;
import scripts.zone.L2ZoneType;
import scripts.zone.form.ZoneCuboid;
import scripts.zone.form.ZoneCylinder;
import scripts.zone.form.ZoneNPoly;
import scripts.zone.type.L2AqZone;
import scripts.zone.type.L2ArenaZone;
import scripts.zone.type.L2BigheadZone;
import scripts.zone.type.L2BossZone;
import scripts.zone.type.L2CastleZone;
import scripts.zone.type.L2ClanHallZone;
import scripts.zone.type.L2ColiseumZone;
import scripts.zone.type.L2DamageZone;
import scripts.zone.type.L2DerbyTrackZone;
import scripts.zone.type.L2DismountZone;
import scripts.zone.type.L2ElfTreeZone;
import scripts.zone.type.L2FishingZone;
import scripts.zone.type.L2HotSpaZone;
import scripts.zone.type.L2JailZone;
import scripts.zone.type.L2MotherTreeZone;
import scripts.zone.type.L2NoLandingZone;
import scripts.zone.type.L2NoblessRbZone;
import scripts.zone.type.L2OlympiadStadiumZone;
import scripts.zone.type.L2OlympiadTexture;
import scripts.zone.type.L2PeaceZone;
import scripts.zone.type.L2PoisonZone;
import scripts.zone.type.L2PvpFarmZone;
import scripts.zone.type.L2S400Zone;
import scripts.zone.type.L2SafeDismountZone;
import scripts.zone.type.L2SiegeFlagZone;
import scripts.zone.type.L2SiegeRuleZone;
import scripts.zone.type.L2SiegeWaitZone;
import scripts.zone.type.L2TownZone;
import scripts.zone.type.L2TvtZone;
import scripts.zone.type.L2WaterZone;
import scripts.zone.type.L2ZakenWelcomeZone;
import scripts.zone.type.L2ZakenZone;

public class ZoneManager
{
  private static final Logger _log = AbstractLogger.getLogger(ZoneManager.class.getName());
  private static ZoneManager _instance;
  private static final FastList<L2ZoneType> _zones = new FastList();
  private static final FastList<L2ZoneType> _safeZones = new FastList();
  private int _update = 0;

  public static ZoneManager getInstance() {
    return _instance;
  }

  public static void init()
  {
    _instance = new ZoneManager();
    _instance.load();
  }

  public void reload()
  {
    int count = 0;
    L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
    for (int x = 0; x < worldRegions.length; x++) {
      for (int y = 0; y < worldRegions[x].length; y++) {
        worldRegions[x][y].getZones().clear();
        count++;
      }
    }
    GrandBossManager.getInstance().getZones().clear();
    _log.info("ZoneManager: Removed zones in " + count + " regions.");

    load();
  }

  private void load()
  {
    long start = System.currentTimeMillis();

    int zoneCount = 0;
    _zones.clear();
    _safeZones.clear();

    L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
    FastMap _regions = WorldRegionTable.getInstance().getRegions();

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);

      File file = new File(Config.DATAPACK_ROOT + "/data/zones/zone.xml");
      if (!file.exists()) { _log.info("ZoneManager [ERROR]: /data/zones/zone.xml file is missing.");
        return;
      }
      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        if ("list".equalsIgnoreCase(n.getNodeName()))
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            if ("zone".equalsIgnoreCase(d.getNodeName())) {
              NamedNodeMap attrs = d.getAttributes();
              int zoneId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
              int minZ = Integer.parseInt(attrs.getNamedItem("minZ").getNodeValue());
              int maxZ = Integer.parseInt(attrs.getNamedItem("maxZ").getNodeValue());
              String zoneType = attrs.getNamedItem("type").getNodeValue();
              String zoneShape = attrs.getNamedItem("shape").getNodeValue();

              if (zoneType.equals("Fishing"))
              {
                continue;
              }
              if (zoneType.equals("Water")) {
                minZ -= 20;
              }

              L2ZoneType temp = getZoneByType(zoneType, zoneId);

              if (temp == null) {
                _log.warning("ZoneManager [ERROR]: No such zone type: " + zoneType);
              }
              else
              {
                try
                {
                  st = con.prepareStatement("SELECT x,y FROM zone_vertices WHERE id=? ORDER BY 'order' ASC ", 1000, 1007);
                  st.setInt(1, zoneId);
                  st.setFetchSize(30);
                  rs = st.executeQuery();
                  rs.setFetchSize(50);

                  if (zoneShape.equalsIgnoreCase("Cuboid")) {
                    int[] x = { 0, 0 };
                    int[] y = { 0, 0 };
                    boolean successfulLoad = true;

                    for (int i = 0; i < 2; i++) {
                      if (rs.next()) {
                        x[i] = rs.getInt("x");
                        y[i] = rs.getInt("y");
                      } else {
                        _log.warning("ZoneManager: Missing cuboid vertex in sql data for zone: " + zoneId);
                        Close.SR(st, rs);
                        successfulLoad = false;
                        break;
                      }
                    }

                    if (successfulLoad) {
                      temp.setZone(new ZoneCuboid(x[0], x[1], y[0], y[1], minZ, maxZ));
                    }
                    else
                    {
                      Close.SR(st, rs); continue;
                    }
                  }
                  else if (zoneShape.equalsIgnoreCase("NPoly")) {
                    FastList fl_x = new FastList(); FastList fl_y = new FastList();

                    while (rs.next()) {
                      fl_x.add(Integer.valueOf(rs.getInt("x")));
                      fl_y.add(Integer.valueOf(rs.getInt("y")));
                    }

                    if ((fl_x.size() == fl_y.size()) && (fl_x.size() > 2))
                    {
                      int[] aX = new int[fl_x.size()];
                      int[] aY = new int[fl_y.size()];

                      for (int i = 0; i < fl_x.size(); i++) {
                        aX[i] = ((Integer)fl_x.get(i)).intValue();
                        aY[i] = ((Integer)fl_y.get(i)).intValue();
                      }

                      temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
                    } else {
                      _log.warning("ZoneManager [ERROR]: Bad sql data for zone: " + zoneId);
                      Close.SR(st, rs);

                      Close.SR(st, rs); continue;
                    }
                  }
                  else if (zoneShape.equalsIgnoreCase("Cylinder"))
                  {
                    int zoneRad = Integer.parseInt(attrs.getNamedItem("rad").getNodeValue());
                    if ((rs.next()) && (zoneRad > 0)) {
                      int zoneX = rs.getInt("x");
                      int zoneY = rs.getInt("y");

                      temp.setZone(new ZoneCylinder(zoneX, zoneY, minZ, maxZ, zoneRad));
                    } else {
                      _log.warning("ZoneManager [ERROR]: Bad sql data for zone: " + zoneId);
                      Close.SR(st, rs);

                      Close.SR(st, rs); continue;
                    }
                  }
                  else
                  {
                    _log.warning("ZoneManager [ERROR]: Unknown shape: " + zoneShape);
                    Close.SR(st, rs);

                    Close.SR(st, rs); continue;
                  }
                  Close.SR(st, rs);
                } catch (Exception e) {
                  _log.warning("ZoneManager [ERROR]: Failed to load zone coordinates: " + e);
                } finally {
                  Close.SR(st, rs);
                }

                for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                  if ("stat".equalsIgnoreCase(cd.getNodeName())) {
                    attrs = cd.getAttributes();
                    String name = attrs.getNamedItem("name").getNodeValue();
                    String val = attrs.getNamedItem("val").getNodeValue();

                    temp.setParameter(name, val);
                  }

                }

                if (((temp instanceof L2TownZone)) || ((temp instanceof L2PeaceZone)) || ((temp instanceof L2SafeDismountZone))) {
                  addSafe(temp);
                }

                addZone(temp);

                if ((temp instanceof L2FishingZone)) {
                  zoneCount++;
                }
                else
                {
                  FastList.Node k;
                  if (_regions.containsKey(Integer.valueOf(zoneId)))
                  {
                    FastList rgs = (FastList)_regions.get(Integer.valueOf(zoneId));

                    k = rgs.head(); for (FastList.Node end = rgs.tail(); (k = k.getNext()) != end; )
                    {
                      WorldRegionTable.Region value = (WorldRegionTable.Region)k.getValue();
                      if (value == null)
                      {
                        continue;
                      }
                      worldRegions[value.x][value.y].addZone(temp);
                    }

                  }
                  else if (_update == 2) {
                    File wr = new File("./data/world_regions.txt");
                    wr.createNewFile();

                    TextBuilder tb = new TextBuilder();
                    for (int x = 0; x < worldRegions.length; x++) {
                      for (int y = 0; y < worldRegions[x].length; y++) {
                        int ax = x - L2World.OFFSET_X << 12;
                        int bx = x + 1 - L2World.OFFSET_X << 12;
                        int ay = y - L2World.OFFSET_Y << 12;
                        int by = y + 1 - L2World.OFFSET_Y << 12;

                        if (temp.getZone().intersectsRectangle(ax, bx, ay, by)) {
                          worldRegions[x][y].addZone(temp);
                          tb.append(x + "," + y + ";");
                        }
                      }
                    }

                    Log.addToPath(wr, zoneId + "=" + tb.toString());
                    tb.clear();
                    tb = null;
                  } else {
                    _update = 1;
                    break label1620;
                  }

                  if ((temp instanceof L2BossZone)) {
                    GrandBossManager.getInstance().addZone((L2BossZone)temp);
                  }

                  zoneCount++;
                }
              }
            }
    } catch (Exception e) { label1620: _log.log(Level.SEVERE, "ZoneManager [ERROR]: loading failed: ", e);
      return;
    } finally {
      Close.CSR(con, st, rs);
    }

    if (_update > 0) {
      switch (_update) {
      case 1:
        _update = 2;
        System.out.println("ZoneManager [INFO]: ####Updating zone cache, please wait...");
        File wr = new File("./data/world_regions.txt");
        if (wr.exists()) {
          wr.delete();
        }
        WorldRegionTable.getInstance().clearRegions();
        load();
        break;
      case 2:
        System.out.println("ZoneManager [INFO]: ####Updating zone cache, done; restarting now...");
        System.exit(2);
      }
    }
    else {
      GrandBossManager.getInstance().initZones();

      long time = System.currentTimeMillis() - start;
      _log.info("ZoneManager: Loaded " + zoneCount + " zones; Time: " + time + "ms.");
    }
  }

  private L2ZoneType getZoneByType(String zoneType, int zoneId) {
    if (zoneType.equals("Fishing"))
      return new L2FishingZone(zoneId);
    if (zoneType.equals("ClanHall"))
      return new L2ClanHallZone(zoneId);
    if (zoneType.equals("Peace"))
      return new L2PeaceZone(zoneId);
    if (zoneType.equals("Town"))
      return new L2TownZone(zoneId);
    if (zoneType.equals("OlympiadStadium"))
      return new L2OlympiadStadiumZone(zoneId);
    if (zoneType.equals("Castle"))
      return new L2CastleZone(zoneId);
    if (zoneType.equals("Damage"))
      return new L2DamageZone(zoneId);
    if (zoneType.equals("Skill"))
      return new L2PoisonZone(zoneId);
    if (zoneType.equals("Arena"))
      return new L2ArenaZone(zoneId);
    if (zoneType.equals("MotherTree"))
      return new L2MotherTreeZone(zoneId);
    if (zoneType.equals("Bighead"))
      return new L2BigheadZone(zoneId);
    if (zoneType.equals("NoLanding"))
      return new L2NoLandingZone(zoneId);
    if (zoneType.equals("Jail"))
      return new L2JailZone(zoneId);
    if (zoneType.equals("DerbyTrack"))
      return new L2DerbyTrackZone(zoneId);
    if (zoneType.equals("Boss"))
      return new L2BossZone(zoneId);
    if (zoneType.equals("Water"))
      return new L2WaterZone(zoneId);
    if (zoneType.equals("SiegeFlag"))
      return new L2SiegeFlagZone(zoneId);
    if (zoneType.equals("SiegeRule"))
      return new L2SiegeRuleZone(zoneId);
    if (zoneType.equals("SiegeWait"))
      return new L2SiegeWaitZone(zoneId);
    if (zoneType.equals("Tvt"))
      return new L2TvtZone(zoneId);
    if (zoneType.equals("Aq"))
      return new L2AqZone(zoneId);
    if (zoneType.equals("S400"))
      return new L2S400Zone(zoneId);
    if (zoneType.equals("Dismount"))
      return new L2DismountZone(zoneId);
    if (zoneType.equals("Coliseum"))
      return new L2ColiseumZone(zoneId);
    if (zoneType.equals("ElfTree"))
      return new L2ElfTreeZone(zoneId);
    if (zoneType.equals("SafeDismount"))
      return new L2SafeDismountZone(zoneId);
    if (zoneType.equals("NobleRb"))
      return new L2NoblessRbZone(zoneId);
    if (zoneType.equals("Zaken"))
      return new L2ZakenZone(zoneId);
    if (zoneType.equals("OlympTex"))
      return new L2OlympiadTexture(zoneId);
    if (zoneType.equals("PvpFarm"))
      return new L2PvpFarmZone(zoneId);
    if (zoneType.equals("HotSpa"))
      return new L2HotSpaZone(zoneId);
    if (zoneType.equals("ZakenWelcome")) {
      return new L2ZakenWelcomeZone(zoneId);
    }
    return null;
  }

  public void addZone(L2ZoneType zone)
  {
    _zones.add(zone);
  }

  public void addSafe(L2ZoneType zone) {
    _safeZones.add(zone);
  }

  public FastList<L2ZoneType> getAllZones()
  {
    return _zones;
  }

  public FastList<L2ZoneType> getSafeZones() {
    return _safeZones;
  }

  public FastList<L2ZoneType> getZones(L2Object object)
  {
    return getZones(object.getX(), object.getY(), object.getZ());
  }

  public FastList<L2ZoneType> getZones(int x, int y)
  {
    L2WorldRegion region = L2World.getInstance().getRegion(x, y);
    FastList temp = new FastList();
    for (L2ZoneType zone : region.getZones()) {
      if (zone.isInsideZone(x, y)) {
        temp.add(zone);
      }
    }
    return temp;
  }

  public FastList<L2ZoneType> getZones(int x, int y, int z)
  {
    L2WorldRegion region = L2World.getInstance().getRegion(x, y);
    FastList temp = new FastList();
    for (L2ZoneType zone : region.getZones()) {
      if (zone.isInsideZone(x, y, z)) {
        temp.add(zone);
      }
    }
    return temp;
  }

  public final L2ArenaZone getArena(L2Character ch) {
    L2ZoneType zn = null;
    L2ArenaZone a = null;
    FastList all = new FastList();
    all.addAll(getZones(ch.getX(), ch.getY(), ch.getZ()));
    FastList.Node n = all.head(); for (FastList.Node end = all.tail(); (n = n.getNext()) != end; ) {
      zn = (L2ZoneType)n.getValue();
      if (((zn instanceof L2ArenaZone)) && (zn.isCharacterInZone(ch))) {
        a = (L2ArenaZone)zn;
      }
    }

    all.clear();
    all = null;
    return a;
  }

  public final L2FishingZone getFishingZone(int x, int y, int z)
  {
    L2ZoneType zn = null;
    L2FishingZone f = null;
    FastList.Node n = _zones.head(); for (FastList.Node end = _zones.tail(); (n = n.getNext()) != end; ) {
      zn = (L2ZoneType)n.getValue();
      if (((zn instanceof L2FishingZone)) && (zn.isInsideZone(x, y, ((L2FishingZone)zn).getWaterZ()))) {
        f = (L2FishingZone)zn;
      }
    }

    return f;
  }

  public final L2WaterZone getWaterZone(int x, int y, int z) {
    L2ZoneType zn = null;
    L2WaterZone w = null;
    FastList all = new FastList();
    all.addAll(getZones(x, y, z));
    FastList.Node n = all.head(); for (FastList.Node end = all.tail(); (n = n.getNext()) != end; ) {
      zn = (L2ZoneType)n.getValue();
      if (((zn instanceof L2WaterZone)) && (zn.isInsideZone(x, y, ((L2WaterZone)zn).getWaterZ()))) {
        w = (L2WaterZone)zn;
      }
    }

    all.clear();
    all = null;
    return w;
  }

  public final L2OlympiadStadiumZone getOlympiadStadium(L2Character ch) {
    L2ZoneType zn = null;
    L2OlympiadStadiumZone o = null;
    FastList all = new FastList();
    all.addAll(getZones(ch.getX(), ch.getY(), ch.getZ()));
    FastList.Node n = all.head(); for (FastList.Node end = all.tail(); (n = n.getNext()) != end; ) {
      zn = (L2ZoneType)n.getValue();
      if (((zn instanceof L2OlympiadStadiumZone)) && (zn.isCharacterInZone(ch))) {
        o = (L2OlympiadStadiumZone)zn;
      }
    }

    all.clear();
    all = null;
    return o;
  }

  public final L2AqZone getAqZone(int x, int y, int z) {
    L2AqZone a = null;
    L2ZoneType zn = null;
    FastList all = new FastList();
    all.addAll(getZones(x, y, z));
    FastList.Node n = all.head(); for (FastList.Node end = all.tail(); (n = n.getNext()) != end; ) {
      zn = (L2ZoneType)n.getValue();
      if ((zn instanceof L2AqZone)) {
        a = (L2AqZone)zn;
      }
    }

    all.clear();
    all = null;
    return a;
  }

  public boolean inSafe(L2Object obj)
  {
    L2ZoneType temp = null;
    FastList.Node n = _safeZones.head(); for (FastList.Node end = _safeZones.tail(); (n = n.getNext()) != end; ) {
      temp = (L2ZoneType)n.getValue();
      if ((temp != null) && 
        (temp.isInsideZone(obj)))
      {
        return !temp.isPvP(obj.getX(), obj.getY());
      }

    }

    temp = null;
    return false;
  }

  public boolean inTradeZone(L2PcInstance pc)
  {
    L2ZoneType temp = null;
    FastList.Node n = _safeZones.head(); for (FastList.Node end = _safeZones.tail(); (n = n.getNext()) != end; ) {
      temp = (L2ZoneType)n.getValue();
      if ((temp != null) && 
        ((temp instanceof L2TownZone)) && 
        (temp.isInsideZone(pc)) && (!temp.isInsideTradeZone(pc.getX(), pc.getY()))) {
        return false;
      }
    }
    temp = null;
    return true;
  }

  public boolean inPvpZone(L2Object obj)
  {
    L2ZoneType temp = null;
    FastList.Node n = _safeZones.head(); for (FastList.Node end = _safeZones.tail(); (n = n.getNext()) != end; ) {
      temp = (L2ZoneType)n.getValue();
      if ((temp != null) && 
        ((temp instanceof L2TownZone)) && 
        (temp.isArena()) && (temp.isInsideZone(obj)) && (temp.isPvP(obj.getX(), obj.getY()))) {
        return true;
      }
    }
    temp = null;
    return false;
  }
}