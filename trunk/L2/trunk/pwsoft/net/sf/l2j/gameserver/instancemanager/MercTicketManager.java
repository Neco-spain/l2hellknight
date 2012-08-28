package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class MercTicketManager
{
  protected static Logger _log = AbstractLogger.getLogger(CastleManager.class.getName());
  private static MercTicketManager _instance;
  private List<L2ItemInstance> _droppedTickets;
  private static final int[] MAX_MERC_PER_TYPE = { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20 };

  private static final int[] MERCS_MAX_PER_CASTLE = { 100, 150, 200, 300, 400, 400, 400, 400, 400 };

  private static final int[] ITEM_IDS = { 3960, 3961, 3962, 3963, 3964, 3965, 3966, 3967, 3968, 3969, 6038, 6039, 6040, 6041, 6042, 6043, 6044, 6045, 6046, 6047, 3973, 3974, 3975, 3976, 3977, 3978, 3979, 3980, 3981, 3982, 6051, 6052, 6053, 6054, 6055, 6056, 6057, 6058, 6059, 6060, 3986, 3987, 3988, 3989, 3990, 3991, 3992, 3993, 3994, 3995, 6064, 6065, 6066, 6067, 6068, 6069, 6070, 6071, 6072, 6073, 3999, 4000, 4001, 4002, 4003, 4004, 4005, 4006, 4007, 4008, 6077, 6078, 6079, 6080, 6081, 6082, 6083, 6084, 6085, 6086, 4012, 4013, 4014, 4015, 4016, 4017, 4018, 4019, 4020, 4021, 6090, 6091, 6092, 6093, 6094, 6095, 6096, 6097, 6098, 6099, 5205, 5206, 5207, 5208, 5209, 5210, 5211, 5212, 5213, 5214, 6105, 6106, 6107, 6108, 6109, 6110, 6111, 6112, 6113, 6114, 6779, 6780, 6781, 6782, 6783, 6784, 6785, 6786, 6787, 6788, 6792, 6793, 6794, 6795, 6796, 6797, 6798, 6799, 6800, 6801, 7973, 7974, 7975, 7976, 7977, 7978, 7979, 7980, 7981, 7982, 7988, 7989, 7990, 7991, 7992, 7993, 7994, 7995, 7996, 7997, 7918, 7919, 7920, 7921, 7922, 7923, 7924, 7925, 7926, 7927, 7931, 7932, 7933, 7934, 7935, 7936, 7937, 7938, 7939, 7940 };

  private static final int[] NPC_IDS = { 35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, 35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039, 35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, 35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039, 35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, 35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039, 35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, 35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039, 35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, 35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039, 35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, 35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039, 35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, 35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039, 35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, 35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039, 35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, 35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039 };

  public static final MercTicketManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new MercTicketManager();
    _instance.load();
  }

  public int getTicketCastleId(int itemId)
  {
    if (((itemId >= ITEM_IDS[0]) && (itemId <= ITEM_IDS[9])) || ((itemId >= ITEM_IDS[10]) && (itemId <= ITEM_IDS[19])))
      return 1;
    if (((itemId >= ITEM_IDS[20]) && (itemId <= ITEM_IDS[29])) || ((itemId >= ITEM_IDS[30]) && (itemId <= ITEM_IDS[39])))
      return 2;
    if (((itemId >= ITEM_IDS[40]) && (itemId <= ITEM_IDS[49])) || ((itemId >= ITEM_IDS[50]) && (itemId <= ITEM_IDS[59])))
      return 3;
    if (((itemId >= ITEM_IDS[60]) && (itemId <= ITEM_IDS[69])) || ((itemId >= ITEM_IDS[70]) && (itemId <= ITEM_IDS[79])))
      return 4;
    if (((itemId >= ITEM_IDS[80]) && (itemId <= ITEM_IDS[89])) || ((itemId >= ITEM_IDS[90]) && (itemId <= ITEM_IDS[99])))
      return 5;
    if (((itemId >= ITEM_IDS[100]) && (itemId <= ITEM_IDS[109])) || ((itemId >= ITEM_IDS[110]) && (itemId <= ITEM_IDS[119])))
      return 6;
    if (((itemId >= ITEM_IDS[120]) && (itemId <= ITEM_IDS['\u0081'])) || ((itemId >= ITEM_IDS['\u0082']) && (itemId <= ITEM_IDS['\u008B'])))
      return 7;
    if (((itemId >= ITEM_IDS['\u008C']) && (itemId <= ITEM_IDS['\u0095'])) || ((itemId >= ITEM_IDS['\u0096']) && (itemId <= ITEM_IDS['\u009F'])))
      return 8;
    if (((itemId >= ITEM_IDS['\u00A0']) && (itemId <= ITEM_IDS['\u00A9'])) || ((itemId >= ITEM_IDS['\u00AA']) && (itemId <= ITEM_IDS['\u00B3'])))
      return 9;
    return -1;
  }

  public void reload()
  {
    getDroppedTickets().clear();
    load();
  }

  private final void load()
  {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("SELECT * FROM castle_siege_guards Where isHired = 1");
      rs = st.executeQuery();

      int startindex = 0;

      while (rs.next())
      {
        int npcId = rs.getInt("npcId");
        int x = rs.getInt("x");
        int y = rs.getInt("y");
        int z = rs.getInt("z");
        Castle castle = CastleManager.getInstance().getCastle(x, y, z);
        if (castle != null) {
          startindex = 10 * (castle.getCastleId() - 1);
        }

        for (int i = startindex; i < NPC_IDS.length; i++) {
          if (NPC_IDS[i] != npcId)
          {
            continue;
          }
          if ((castle == null) || (castle.getSiege().getIsInProgress()))
            break;
          int itemId = ITEM_IDS[i];

          L2ItemInstance dropticket = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
          dropticket.setLocation(L2ItemInstance.ItemLocation.INVENTORY);
          dropticket.dropMe(null, x, y, z);
          dropticket.setDropTime(0L);
          L2World.getInstance().storeObject(dropticket);
          getDroppedTickets().add(dropticket);
          break;
        }
      }

    }
    catch (Exception e)
    {
      _log.warning("Exception: loadMercenaryData(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      Close.CSR(con, st, rs);
    }
    _log.info("MercTicketManager: Loaded: " + getDroppedTickets().size() + " Mercenary Tickets");
  }

  public boolean isAtTypeLimit(int itemId)
  {
    int limit = -1;

    for (int i = 0; i < ITEM_IDS.length; i++) {
      if (ITEM_IDS[i] != itemId)
        continue;
      limit = MAX_MERC_PER_TYPE[i];
      break;
    }

    if (limit <= 0) {
      return true;
    }
    int count = 0;

    for (int i = 0; i < getDroppedTickets().size(); i++)
    {
      L2ItemInstance ticket = (L2ItemInstance)getDroppedTickets().get(i);
      if ((ticket != null) && (ticket.getItemId() == itemId)) {
        count++;
      }
    }
    return count >= limit;
  }

  public boolean isAtCasleLimit(int itemId)
  {
    int castleId = getTicketCastleId(itemId);
    if (castleId <= 0)
      return true;
    int limit = MERCS_MAX_PER_CASTLE[(castleId - 1)];
    if (limit <= 0) {
      return true;
    }
    int count = 0;

    for (int i = 0; i < getDroppedTickets().size(); i++)
    {
      L2ItemInstance ticket = (L2ItemInstance)getDroppedTickets().get(i);
      if ((ticket != null) && (getTicketCastleId(ticket.getItemId()) == castleId)) {
        count++;
      }
    }
    return count >= limit;
  }

  public int addTicket(int itemId, L2PcInstance activeChar, String[] messages)
  {
    int x = activeChar.getX();
    int y = activeChar.getY();
    int z = activeChar.getZ();
    int heading = activeChar.getHeading();

    Castle castle = CastleManager.getInstance().getCastle(activeChar);
    if (castle == null) {
      return -1;
    }

    for (int i = 0; i < ITEM_IDS.length; i++)
    {
      if (ITEM_IDS[i] != itemId)
        continue;
      spawnMercenary(NPC_IDS[i], x, y, z, 3000, messages, 0);

      castle.getSiege().getSiegeGuardManager().hireMerc(x, y, z, heading, NPC_IDS[i]);

      L2ItemInstance dropticket = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
      dropticket.setLocation(L2ItemInstance.ItemLocation.INVENTORY);
      dropticket.dropMe(null, x, y, z);
      dropticket.setDropTime(0L);
      L2World.getInstance().storeObject(dropticket);

      _droppedTickets.add(dropticket);

      return NPC_IDS[i];
    }

    return -1;
  }

  private void spawnMercenary(int npcId, int x, int y, int z, int despawnDelay, String[] messages, int chatDelay)
  {
    L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
    if (template != null)
    {
      L2SiegeGuardInstance npc = new L2SiegeGuardInstance(IdFactory.getInstance().getNextId(), template);
      npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
      npc.setDecayed(false);
      npc.spawnMe(x, y, z + 20);

      if ((messages != null) && (messages.length > 0)) {
        AutoChatHandler.getInstance().registerChat(npc, messages, chatDelay);
      }
      if (despawnDelay > 0)
      {
        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(npc)
        {
          public void run() {
            val$npc.deleteMe();
          }
        }
        , despawnDelay);
      }
    }
  }

  public void deleteTickets(int castleId)
  {
    int i = 0;
    while (i < getDroppedTickets().size())
    {
      L2ItemInstance item = (L2ItemInstance)getDroppedTickets().get(i);
      if ((item != null) && (getTicketCastleId(item.getItemId()) == castleId))
      {
        item.decayMe();
        L2World.getInstance().removeObject(item);

        getDroppedTickets().remove(i);
      }
      else {
        i++;
      }
    }
  }

  public void removeTicket(L2ItemInstance item)
  {
    int itemId = item.getItemId();
    int npcId = -1;

    for (int i = 0; i < ITEM_IDS.length; i++) {
      if (ITEM_IDS[i] != itemId)
        continue;
      npcId = NPC_IDS[i];
      break;
    }

    Castle castle = CastleManager.getInstance().getCastleById(getTicketCastleId(itemId));

    if ((npcId > 0) && (castle != null))
    {
      new SiegeGuardManager(castle).removeMerc(npcId, item.getX(), item.getY(), item.getZ());
    }

    getDroppedTickets().remove(item);
  }

  public int[] getItemIds() {
    return ITEM_IDS;
  }

  public final List<L2ItemInstance> getDroppedTickets()
  {
    if (_droppedTickets == null) _droppedTickets = new FastList();
    return _droppedTickets;
  }
}