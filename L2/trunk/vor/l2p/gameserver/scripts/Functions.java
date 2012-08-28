package l2p.gameserver.scripts;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.lang.reference.HardReferences;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.instancemanager.ServerVariables;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SimpleSpawner;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.World;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.ItemInstance.ItemLocation;
import l2p.gameserver.model.mail.Mail;
import l2p.gameserver.model.mail.Mail.SenderType;
import l2p.gameserver.serverpackets.ExNoticePostArrived;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.NpcSay;
import l2p.gameserver.serverpackets.components.ChatType;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.MapUtils;
import l2p.gameserver.utils.NpcUtils;
import l2p.gameserver.utils.Strings;

public class Functions
{
  public HardReference<Player> self = HardReferences.emptyRef();
  public HardReference<NpcInstance> npc = HardReferences.emptyRef();

  public static ScheduledFuture<?> executeTask(Player caller, String className, String methodName, Object[] args, Map<String, Object> variables, long delay)
  {
    return ThreadPoolManager.getInstance().schedule(new RunnableImpl(caller, className, methodName, args, variables)
    {
      public void runImpl() throws Exception
      {
        Functions.callScripts(val$caller, val$className, val$methodName, val$args, val$variables);
      }
    }
    , delay);
  }

  public static ScheduledFuture<?> executeTask(String className, String methodName, Object[] args, Map<String, Object> variables, long delay)
  {
    return executeTask(null, className, methodName, args, variables, delay);
  }

  public static ScheduledFuture<?> executeTask(Player player, String className, String methodName, Object[] args, long delay)
  {
    return executeTask(player, className, methodName, args, null, delay);
  }

  public static ScheduledFuture<?> executeTask(String className, String methodName, Object[] args, long delay)
  {
    return executeTask(className, methodName, args, null, delay);
  }

  public static Object callScripts(String className, String methodName, Object[] args)
  {
    return callScripts(className, methodName, args, null);
  }

  public static Object callScripts(String className, String methodName, Object[] args, Map<String, Object> variables)
  {
    return callScripts(null, className, methodName, args, variables);
  }

  public static Object callScripts(Player player, String className, String methodName, Object[] args, Map<String, Object> variables)
  {
    return Scripts.getInstance().callScripts(player, className, methodName, args, variables);
  }

  public void show(String text, Player self)
  {
    show(text, self, getNpc(), new Object[0]);
  }

  public static void show(String text, Player self, NpcInstance npc, Object[] arg)
  {
    if ((text == null) || (self == null)) {
      return;
    }
    NpcHtmlMessage msg = new NpcHtmlMessage(self, npc);

    if ((text.endsWith(".html")) || (text.endsWith(".htm")))
      msg.setFile(text);
    else {
      msg.setHtml(Strings.bbParse(text));
    }
    if ((arg != null) && (arg.length % 2 == 0))
    {
      for (int i = 0; i < arg.length; i = 2)
      {
        msg.replace(String.valueOf(arg[i]), String.valueOf(arg[(i + 1)]));
      }
    }

    self.sendPacket(msg);
  }

  public static void show(CustomMessage message, Player self)
  {
    show(message.toString(), self, null, new Object[0]);
  }

  public static void sendMessage(String text, Player self)
  {
    self.sendMessage(text);
  }

  public static void sendMessage(CustomMessage message, Player self)
  {
    self.sendMessage(message);
  }

  public static void npcSayInRange(NpcInstance npc, String text, int range)
  {
    npcSayInRange(npc, range, NpcString.NONE, new String[] { text });
  }

  public static void npcSayInRange(NpcInstance npc, int range, NpcString fStringId, String[] params)
  {
    if (npc == null)
      return;
    NpcSay cs = new NpcSay(npc, ChatType.ALL, fStringId, params);
    for (Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
      if (npc.getReflection() == player.getReflection())
        player.sendPacket(cs);
  }

  public static void npcSay(NpcInstance npc, String text)
  {
    npcSayInRange(npc, text, 1500);
  }

  public static void npcSay(NpcInstance npc, NpcString npcString, String[] params)
  {
    npcSayInRange(npc, 1500, npcString, params);
  }

  public static void npcSayInRangeCustomMessage(NpcInstance npc, int range, String address, Object[] replacements)
  {
    if (npc == null)
      return;
    for (Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
      if (npc.getReflection() == player.getReflection())
        player.sendPacket(new NpcSay(npc, ChatType.ALL, new CustomMessage(address, player, replacements).toString()));
  }

  public static void npcSayCustomMessage(NpcInstance npc, String address, Object[] replacements)
  {
    npcSayInRangeCustomMessage(npc, 1500, address, replacements);
  }

  public static void npcSayToPlayer(NpcInstance npc, Player player, String text)
  {
    npcSayToPlayer(npc, player, NpcString.NONE, new String[] { text });
  }

  public static void npcSayToPlayer(NpcInstance npc, Player player, NpcString npcString, String[] params)
  {
    if (npc == null)
      return;
    player.sendPacket(new NpcSay(npc, ChatType.TELL, npcString, params));
  }

  public static void npcShout(NpcInstance npc, String text)
  {
    npcShout(npc, NpcString.NONE, new String[] { text });
  }

  public static void npcShout(NpcInstance npc, NpcString npcString, String[] params)
  {
    if (npc == null)
      return;
    NpcSay cs = new NpcSay(npc, ChatType.SHOUT, npcString, params);

    int rx = MapUtils.regionX(npc);
    int ry = MapUtils.regionY(npc);
    int offset = Config.SHOUT_OFFSET;

    for (Player player : GameObjectsStorage.getAllPlayersForIterate())
    {
      if (player.getReflection() != npc.getReflection()) {
        continue;
      }
      int tx = MapUtils.regionX(player);
      int ty = MapUtils.regionY(player);

      if ((tx >= rx - offset) && (tx <= rx + offset) && (ty >= ry - offset) && (ty <= ry + offset))
        player.sendPacket(cs);
    }
  }

  public static void npcShoutCustomMessage(NpcInstance npc, String address, Object[] replacements)
  {
    if (npc == null) {
      return;
    }
    int rx = MapUtils.regionX(npc);
    int ry = MapUtils.regionY(npc);
    int offset = Config.SHOUT_OFFSET;

    for (Player player : GameObjectsStorage.getAllPlayersForIterate())
    {
      if (player.getReflection() != npc.getReflection()) {
        continue;
      }
      int tx = MapUtils.regionX(player);
      int ty = MapUtils.regionY(player);

      if (((tx >= rx - offset) && (tx <= rx + offset) && (ty >= ry - offset) && (ty <= ry + offset)) || (npc.isInRange(player, Config.CHAT_RANGE)))
        player.sendPacket(new NpcSay(npc, ChatType.SHOUT, new CustomMessage(address, player, replacements).toString()));
    }
  }

  public static void npcSay(NpcInstance npc, NpcString address, ChatType type, int range, String[] replacements)
  {
    if (npc == null)
      return;
    for (Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
    {
      if (player.getReflection() == npc.getReflection())
        player.sendPacket(new NpcSay(npc, type, address, replacements));
    }
  }

  public static void addItem(Playable playable, int itemId, long count)
  {
    ItemFunctions.addItem(playable, itemId, count, true);
  }

  public static long getItemCount(Playable playable, int itemId)
  {
    return ItemFunctions.getItemCount(playable, itemId);
  }

  public static long removeItem(Playable playable, int itemId, long count)
  {
    return ItemFunctions.removeItem(playable, itemId, count, true);
  }

  public static boolean ride(Player player, int pet)
  {
    if (player.isMounted()) {
      player.setMount(0, 0, 0);
    }
    if (player.getPet() != null)
    {
      player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
      return false;
    }

    player.setMount(pet, 0, 0);
    return true;
  }

  public static void unRide(Player player)
  {
    if (player.isMounted())
      player.setMount(0, 0, 0);
  }

  public static void unSummonPet(Player player, boolean onlyPets)
  {
    Summon pet = player.getPet();
    if (pet == null)
      return;
    if ((pet.isPet()) || (!onlyPets))
      pet.unSummon();
  }

  @Deprecated
  public static NpcInstance spawn(Location loc, int npcId)
  {
    return spawn(loc, npcId, ReflectionManager.DEFAULT);
  }

  @Deprecated
  public static NpcInstance spawn(Location loc, int npcId, Reflection reflection) {
    return NpcUtils.spawnSingle(npcId, loc, reflection, 0L);
  }

  public Player getSelf()
  {
    return (Player)self.get();
  }

  public NpcInstance getNpc()
  {
    return (NpcInstance)npc.get();
  }

  @Deprecated
  public static void SpawnNPCs(int npcId, int[][] locations, List<SimpleSpawner> list)
  {
    NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
    if (template == null)
    {
      System.out.println("WARNING! Functions.SpawnNPCs template is null for npc: " + npcId);
      Thread.dumpStack();
      return;
    }
    for (int[] location : locations)
    {
      SimpleSpawner sp = new SimpleSpawner(template);
      sp.setLoc(new Location(location[0], location[1], location[2]));
      sp.setAmount(1);
      sp.setRespawnDelay(0);
      sp.init();
      if (list != null)
        list.add(sp);
    }
  }

  public static void deSpawnNPCs(List<SimpleSpawner> list)
  {
    for (SimpleSpawner sp : list) {
      sp.deleteAll();
    }
    list.clear();
  }

  public static boolean IsActive(String name)
  {
    return ServerVariables.getString(name, "off").equalsIgnoreCase("on");
  }

  public static boolean SetActive(String name, boolean active)
  {
    if (active == IsActive(name))
      return false;
    if (active)
      ServerVariables.set(name, "on");
    else
      ServerVariables.unset(name);
    return true;
  }

  public static boolean SimpleCheckDrop(Creature mob, Creature killer)
  {
    return (mob != null) && (mob.isMonster()) && (!mob.isRaid()) && (killer != null) && (killer.getPlayer() != null) && (killer.getLevel() - mob.getLevel() < 9);
  }

  public static boolean isPvPEventStarted()
  {
    if (((Boolean)callScripts("events.TvT.TvT", "isRunned", new Object[0])).booleanValue()) {
      return true;
    }
    return ((Boolean)callScripts("events.lastHero.LastHero", "isRunned", new Object[0])).booleanValue();
  }

  public static void sendDebugMessage(Player player, String message)
  {
    if (!player.isGM())
      return;
    player.sendMessage(message);
  }

  public static void sendSystemMail(Player receiver, String title, String body, Map<Integer, Long> items)
  {
    if ((receiver == null) || (!receiver.isOnline()))
      return;
    if (title == null)
      return;
    if (items.keySet().size() > 8) {
      return;
    }
    Mail mail = new Mail();
    mail.setSenderId(1);
    mail.setSenderName("Admin");
    mail.setReceiverId(receiver.getObjectId());
    mail.setReceiverName(receiver.getName());
    mail.setTopic(title);
    mail.setBody(body);
    for (Map.Entry itm : items.entrySet())
    {
      ItemInstance item = ItemFunctions.createItem(((Integer)itm.getKey()).intValue());
      item.setLocation(ItemInstance.ItemLocation.MAIL);
      item.setCount(((Long)itm.getValue()).longValue());
      item.save();
      mail.addAttachment(item);
    }
    mail.setType(Mail.SenderType.NEWS_INFORMER);
    mail.setUnread(true);
    mail.setExpireTime(2592000 + (int)(System.currentTimeMillis() / 1000L));
    mail.save();

    receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
    receiver.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
  }
}