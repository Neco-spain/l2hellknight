package net.sf.l2j.gameserver.model.actor.instance;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.Config.EventReward;
import net.sf.l2j.Config.PvpColor;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.CustomServerData;
import net.sf.l2j.gameserver.datatables.HelperBuffTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.instancemanager.games.Lottery;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2DropCategory;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.MobGroup;
import net.sf.l2j.gameserver.model.MobGroupTable;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.model.actor.stat.NpcStat;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.SpawnTerritory;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.RadarControl;
import net.sf.l2j.gameserver.network.serverpackets.ServerObjectInfo;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.templates.L2HelperBuff;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import scripts.zone.type.L2TownZone;

public class L2NpcInstance extends L2Character
{
  public static final int INTERACTION_DISTANCE = 150;
  private L2Spawn _spawn;
  private boolean _isBusy = false;

  private String _busyMessage = "";

  volatile boolean _isDecayed = false;

  private boolean _isSpoil = false;

  private int _castleIndex = -2;
  public boolean isEventMob = false;
  private boolean _isInTown = false;
  private int _isSpoiledBy = 0;
  protected RandomAnimationTask _rAniTask = null;
  private int _currentLHandId;
  private int _currentRHandId;
  private int _currentCollisionHeight;
  private int _currentCollisionRadius;
  private int _weaponEnch = 0;
  private static final FastList<Config.EventReward> _raidRewards = Config.NPC_RAID_REWARDS;
  private L2NpcTemplate _template;
  private int _showSpawnAnimation = 2;

  public void onRandomAnimation()
  {
    broadcastPacket(new SocialAction(getObjectId(), Rnd.get(2, 3)));
  }

  public void startRandomAnimationTimer()
  {
  }

  public boolean hasRandomAnimation()
  {
    return Config.MAX_NPC_ANIMATION > 0;
  }

  public L2NpcInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    getKnownList();
    getStat();
    getStatus();
    initCharStatusUpdateValues();

    _currentLHandId = template.lhand;
    _currentRHandId = template.rhand;

    _currentCollisionHeight = template.collisionHeight;
    _currentCollisionRadius = template.collisionRadius;

    if (template == null) {
      _log.severe("No template for Npc. Please check your datapack is setup correctly.");
      return;
    }

    setName(template.name);

    if ((Config.ENCH_NPC_CAHNCE > 0) && (Rnd.get(100) < Config.ENCH_NPC_CAHNCE))
      _weaponEnch = Rnd.get(Config.ENCH_NPC_MINMAX.nick, Config.ENCH_NPC_MINMAX.title);
  }

  public NpcKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof NpcKnownList))) {
      setKnownList(new NpcKnownList(this));
    }
    return (NpcKnownList)super.getKnownList();
  }

  public NpcStat getStat()
  {
    if ((super.getStat() == null) || (!(super.getStat() instanceof NpcStat))) {
      setStat(new NpcStat(this));
    }
    return (NpcStat)super.getStat();
  }

  public NpcStatus getStatus()
  {
    if ((super.getStatus() == null) || (!(super.getStatus() instanceof NpcStatus))) {
      setStatus(new NpcStatus(this));
    }
    return (NpcStatus)super.getStatus();
  }

  public final L2NpcTemplate getTemplate()
  {
    if (_template == null) {
      _template = ((L2NpcTemplate)super.getTemplate());
    }
    return _template;
  }

  public boolean isAttackable()
  {
    return true;
  }

  public final String getFactionId()
  {
    return getTemplate().factionId;
  }

  public final int getLevel()
  {
    return getTemplate().level;
  }

  public boolean isAggressive()
  {
    return false;
  }

  public int getAggroRange()
  {
    if (fromMonastry()) {
      return 500;
    }

    return getTemplate().aggroRange;
  }

  public int getFactionRange()
  {
    return getTemplate().factionRange;
  }

  public boolean isUndead()
  {
    return getTemplate().isUndead;
  }

  public void updateAbnormalEffect()
  {
    FastList players = getKnownList().getListKnownPlayers();
    L2PcInstance pc = null;
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if (pc == null)
      {
        continue;
      }
      if (getRunSpeed() == 0) {
        pc.sendPacket(new ServerObjectInfo(this, pc)); continue;
      }
      pc.sendPacket(new NpcInfo(this, pc));
    }

    players.clear();
    players = null;
    pc = null;
  }

  public int getDistanceToWatchObject(L2Object object)
  {
    if ((object instanceof L2FestivalGuideInstance)) {
      return 10000;
    }

    if ((object.isL2Folk()) || (!object.isL2Character())) {
      return 0;
    }

    if (object.isL2Playable()) {
      return 1500;
    }

    return 500;
  }

  public int getDistanceToForgetObject(L2Object object)
  {
    return 2 * getDistanceToWatchObject(object);
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return false;
  }

  public int getLeftHandItem()
  {
    return _currentLHandId;
  }

  public int getRightHandItem()
  {
    return _currentRHandId;
  }

  public boolean isSpoil()
  {
    return _isSpoil;
  }

  public void setSpoil(boolean isSpoil)
  {
    _isSpoil = isSpoil;
  }

  public final int getIsSpoiledBy() {
    return _isSpoiledBy;
  }

  public final void setIsSpoiledBy(int value) {
    _isSpoiledBy = value;
  }

  public final boolean isBusy()
  {
    return _isBusy;
  }

  public void setBusy(boolean isBusy)
  {
    _isBusy = isBusy;
  }

  public final String getBusyMessage()
  {
    return _busyMessage;
  }

  public void setBusyMessage(String message)
  {
    _busyMessage = message;
  }

  protected boolean canTarget(L2PcInstance player) {
    if (player.isOutOfControl()) {
      player.sendActionFailed();
      return false;
    }

    if ((player.isDead()) || (player.isAlikeDead()) || (player.isFakeDeath())) {
      player.sendActionFailed();
      return false;
    }

    if (player.isSitting()) {
      player.sendActionFailed();
      return false;
    }

    if (getTemplate().npcId == 80008) {
      player.sendActionFailed();
      return false;
    }

    return true;
  }

  protected boolean canInteract(L2PcInstance player)
  {
    return isInsideRadius(player, 150, false, false);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) {
      return;
    }

    if (this != player.getTarget())
    {
      player.setTarget(this);

      if (isAutoAttackable(player))
      {
        player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

        StatusUpdate su = new StatusUpdate(getObjectId());
        su.addAttribute(9, (int)getCurrentHp());
        su.addAttribute(10, getMaxHp());
        player.sendPacket(su);
      }
      else {
        player.sendPacket(new MyTargetSelected(getObjectId(), 0));
      }

      player.sendPacket(new ValidateLocation(this));
      if (getTemplate().getEventQuests(Quest.QuestEventType.ONFOCUS) != null)
        for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ONFOCUS))
          quest.notifyFocus(this, player);
    }
    else
    {
      player.sendPacket(new ValidateLocation(this));

      if ((isAutoAttackable(player)) && (!isAlikeDead()))
      {
        if (Math.abs(player.getZ() - getZ()) < 200)
        {
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
        }
        else
        {
          player.sendActionFailed();
        }
      } else if (!isAutoAttackable(player))
      {
        if (!canInteract(player))
        {
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
        }
        else
        {
          broadcastPacket(new SocialAction(getObjectId(), Rnd.get(8)));

          if (isEventMob) {
            L2Event.showEventHtml(player, String.valueOf(getObjectId()));
          } else {
            Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.NPC_FIRST_TALK);
            if ((qlst != null) && (qlst.length == 1))
              qlst[0].notifyFirstTalk(this, player);
            else
              showChatWindow(player, 0);
          }
        }
      }
      else
        player.sendActionFailed();
    }
  }

  public void onActionShift(L2GameClient client)
  {
    L2PcInstance player = client.getActiveChar();
    if (player == null) {
      return;
    }
    if (!canTarget(player)) {
      return;
    }

    if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

      if (isAutoAttackable(player))
      {
        StatusUpdate su = new StatusUpdate(getObjectId());
        su.addAttribute(9, (int)getCurrentHp());
        su.addAttribute(10, getMaxHp());
        player.sendPacket(su);
      }

      NpcHtmlMessage html = NpcHtmlMessage.id(0);
      TextBuilder html1 = new TextBuilder("<html><body><center><font color=\"LEVEL\">NPC Information</font></center>");
      String className = "";
      try {
        className = getClass().getName().substring(43);
      } catch (Exception ignored) {
        try {
          className = getClass().getName().substring(11);
        } catch (Exception ignored2) {
          className = getName();
        }
      }
      html1.append("<br>");

      html1.append(new StringBuilder().append("Instance Type: ").append(className).append("<br1>Faction: ").append(getFactionId()).append("<br1>Location ID: ").append(getSpawn() != null ? getSpawn().getLocation() : 0).append("<br1>").toString());

      if ((this instanceof L2ControllableMobInstance))
        html1.append(new StringBuilder().append("Mob Group: ").append(MobGroupTable.getInstance().getGroupForMob((L2ControllableMobInstance)this).getGroupId()).append("<br>").toString());
      else {
        html1.append(new StringBuilder().append("Respawn Time: ").append(getSpawn() != null ? new StringBuilder().append(getSpawn().getRespawnDelay() / 1000).append("  Seconds<br>").toString() : "?  Seconds<br>").toString());
      }

      html1.append("<table border=\"0\" width=\"100%\">");
      html1.append(new StringBuilder().append("<tr><td>Object ID</td><td>").append(getObjectId()).append("</td><td>NPC ID</td><td>").append(getTemplate().npcId).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>Castle</td><td>").append(getCastle().getCastleId()).append("</td><td>Coords</td><td>").append(getX()).append(",").append(getY()).append(",").append(getZ()).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>Level</td><td>").append(getLevel()).append("</td><td>Aggro</td><td>").append(isL2Attackable() ? ((L2Attackable)this).getAggroRange() : 0).append("</td></tr>").toString());
      html1.append("</table><br>");

      html1.append("<font color=\"LEVEL\">Combat</font>");
      html1.append("<table border=\"0\" width=\"100%\">");
      html1.append(new StringBuilder().append("<tr><td>Current HP</td><td>").append(getCurrentHp()).append("</td><td>Current MP</td><td>").append(getCurrentMp()).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>Max.HP</td><td>").append((int)(getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1.0D, this, null))).append("*").append(getStat().calcStat(Stats.MAX_HP, 1.0D, this, null)).append("</td><td>Max.MP</td><td>").append(getMaxMp()).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>P.Atk.</td><td>").append(getPAtk(null)).append("</td><td>M.Atk.</td><td>").append(getMAtk(null, null)).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>P.Def.</td><td>").append(getPDef(null)).append("</td><td>M.Def.</td><td>").append(getMDef(null, null)).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>Accuracy</td><td>").append(getAccuracy()).append("</td><td>Evasion</td><td>").append(getEvasionRate(null)).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>Critical</td><td>").append(getCriticalHit(null, null)).append("</td><td>Speed</td><td>").append(getRunSpeed()).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>Atk.Speed</td><td>").append(getPAtkSpd()).append("</td><td>Cast.Speed</td><td>").append(getMAtkSpd()).append("</td></tr>").toString());
      html1.append("</table><br>");

      html1.append("<font color=\"LEVEL\">Basic Stats</font>");
      html1.append("<table border=\"0\" width=\"100%\">");
      html1.append(new StringBuilder().append("<tr><td>STR</td><td>").append(getSTR()).append("</td><td>DEX</td><td>").append(getDEX()).append("</td><td>CON</td><td>").append(getCON()).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>INT</td><td>").append(getINT()).append("</td><td>WIT</td><td>").append(getWIT()).append("</td><td>MEN</td><td>").append(getMEN()).append("</td></tr>").toString());
      html1.append("</table>");

      html1.append(new StringBuilder().append("<br><center><table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc ").append(getTemplate().npcId).append("\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1></td>").toString());
      html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><br1></tr>");
      html1.append(new StringBuilder().append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist ").append(getTemplate().npcId).append("\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>").toString());
      html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
      html1.append("</table></center><br>");
      html1.append("</body></html>");

      html.setHtml(html1.toString());
      player.sendPacket(html);
    } else if (Config.ALT_GAME_VIEWNPC)
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

      if (isAutoAttackable(player))
      {
        StatusUpdate su = new StatusUpdate(getObjectId());
        su.addAttribute(9, (int)getCurrentHp());
        su.addAttribute(10, getMaxHp());
        player.sendPacket(su);
      }

      NpcHtmlMessage html = NpcHtmlMessage.id(0);
      TextBuilder html1 = new TextBuilder("<html><body>");

      html1.append("<br><center><font color=\"LEVEL\">[Combat Stats]</font></center>");
      html1.append("<table border=0 width=\"100%\">");
      html1.append(new StringBuilder().append("<tr><td>Max.HP</td><td>").append((int)(getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1.0D, this, null))).append("*").append((int)getStat().calcStat(Stats.MAX_HP, 1.0D, this, null)).append("</td><td>Max.MP</td><td>").append(getMaxMp()).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>P.Atk.</td><td>").append(getPAtk(null)).append("</td><td>M.Atk.</td><td>").append(getMAtk(null, null)).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>P.Def.</td><td>").append(getPDef(null)).append("</td><td>M.Def.</td><td>").append(getMDef(null, null)).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>Accuracy</td><td>").append(getAccuracy()).append("</td><td>Evasion</td><td>").append(getEvasionRate(null)).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>Critical</td><td>").append(getCriticalHit(null, null)).append("</td><td>Speed</td><td>").append(getRunSpeed()).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>Atk.Speed</td><td>").append(getPAtkSpd()).append("</td><td>Cast.Speed</td><td>").append(getMAtkSpd()).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>Race</td><td>").append(getTemplate().race).append("</td><td></td><td></td></tr>").toString());
      html1.append("</table>");

      html1.append("<br><center><font color=\"LEVEL\">[Basic Stats]</font></center>");
      html1.append("<table border=0 width=\"100%\">");
      html1.append(new StringBuilder().append("<tr><td>STR</td><td>").append(getSTR()).append("</td><td>DEX</td><td>").append(getDEX()).append("</td><td>CON</td><td>").append(getCON()).append("</td></tr>").toString());
      html1.append(new StringBuilder().append("<tr><td>INT</td><td>").append(getINT()).append("</td><td>WIT</td><td>").append(getWIT()).append("</td><td>MEN</td><td>").append(getMEN()).append("</td></tr>").toString());
      html1.append("</table>");

      html1.append("<br><center><font color=\"LEVEL\">[Drop Info]</font></center>");
      html1.append("Rates legend: <font color=\"ff0000\">50%+</font> <font color=\"00ff00\">30%+</font> <font color=\"0000ff\">less than 30%</font>");
      html1.append("<table border=0 width=\"100%\">");

      for (Iterator i$ = getTemplate().getDropData().iterator(); i$.hasNext(); ) { cat = (L2DropCategory)i$.next();
        for (L2DropData drop : cat.getAllDrops()) {
          String name = ItemTable.getInstance().getTemplate(drop.getItemId()).getName();

          if (drop.getChance() >= 600000)
            html1.append(new StringBuilder().append("<tr><td><font color=\"ff0000\">").append(name).append("</font></td><td>").append(cat.isSweep() ? "Sweep" : drop.isQuestDrop() ? "Quest" : "Drop").append("</td></tr>").toString());
          else if (drop.getChance() >= 300000)
            html1.append(new StringBuilder().append("<tr><td><font color=\"00ff00\">").append(name).append("</font></td><td>").append(cat.isSweep() ? "Sweep" : drop.isQuestDrop() ? "Quest" : "Drop").append("</td></tr>").toString());
          else
            html1.append(new StringBuilder().append("<tr><td><font color=\"0000ff\">").append(name).append("</font></td><td>").append(cat.isSweep() ? "Sweep" : drop.isQuestDrop() ? "Quest" : "Drop").append("</td></tr>").toString());
        }
      }
      L2DropCategory cat;
      html1.append("</table>");
      html1.append("</body></html>");

      html.setHtml(html1.toString());
      player.sendPacket(html);
    }

    player.sendActionFailed();
  }

  public final Castle getCastle()
  {
    if (_castleIndex < 0) {
      TownManager.getInstance(); L2TownZone town = TownManager.getTown(getX(), getY(), getZ());

      if (town != null) {
        _castleIndex = CastleManager.getInstance().getCastleIndex(town.getTaxById());
      }

      if (_castleIndex < 0)
        _castleIndex = CastleManager.getInstance().findNearestCastleIndex(this);
      else {
        _isInTown = true;
      }
    }

    if (_castleIndex < 0) {
      return null;
    }

    return (Castle)CastleManager.getInstance().getCastles().get(_castleIndex);
  }

  public final boolean getIsInTown() {
    if (_castleIndex < 0) {
      getCastle();
    }
    return _isInTown;
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (!canTarget(player)) {
      return;
    }

    if ((isBusy()) && (getBusyMessage().length() > 0)) {
      player.sendActionFailed();

      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
      html.setFile("data/html/npcbusy.htm");
      html.replace("%busymessage%", getBusyMessage());
      html.replace("%npcname%", getName());
      html.replace("%playername%", player.getName());
      player.sendPacket(html);
    } else if (command.equalsIgnoreCase("TerritoryStatus")) {
      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());

      if (getCastle().getOwnerId() > 0) {
        html.setFile("data/html/territorystatus.htm");
        L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
        if (clan == null) {
          player.sendHtmlMessage("\u041E\u0448\u0438\u0431\u043A\u0430", new StringBuilder().append("\u0421\u043E\u043E\u0431\u0449\u0438\u0442\u0435 \u0430\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438; \u043A\u043E\u0434: ").append(getCastle().getOwnerId()).toString());
          player.sendActionFailed();
          return;
        }
        html.replace("%clanname%", Util.htmlSpecialChars(clan.getName()));
        html.replace("%clanleadername%", Util.htmlSpecialChars(clan.getLeaderName()));
      } else {
        html.setFile("data/html/territorynoclan.htm");
      }

      html.replace("%castlename%", getCastle().getName());
      html.replace("%taxpercent%", new StringBuilder().append("").append(getCastle().getTaxPercent()).toString());
      html.replace("%objectId%", String.valueOf(getObjectId()));

      if (getCastle().getCastleId() > 6)
        html.replace("%territory%", "The Kingdom of Elmore");
      else {
        html.replace("%territory%", "The Kingdom of Aden");
      }

      player.sendPacket(html);
    } else if (command.startsWith("Quest")) {
      String quest = "";
      try {
        quest = command.substring(5).trim();
      } catch (IndexOutOfBoundsException ioobe) {
      }
      if (quest.length() == 0)
        showQuestWindow(player);
      else
        showQuestWindow(player, quest);
    }
    else if (command.startsWith("Chat")) {
      int val = 0;
      try {
        val = Integer.parseInt(command.substring(5));
      } catch (IndexOutOfBoundsException ioobe) {
      } catch (NumberFormatException nfe) {
      }
      showChatWindow(player, val);
    } else if (command.startsWith("Link")) {
      String path = command.substring(5).trim();
      if (path.indexOf("..") != -1) {
        return;
      }
      String filename = new StringBuilder().append("data/html/").append(path).toString();
      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
      html.setFile(filename);
      html.replace("%objectId%", String.valueOf(getObjectId()));
      player.sendPacket(html);
    } else if (command.startsWith("NobleTeleport")) {
      if (!player.isNoble()) {
        String filename = "data/html/teleporter/nobleteleporter-no.htm";
        NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
        return;
      }
      int val = 0;
      try {
        val = Integer.parseInt(command.substring(5));
      } catch (IndexOutOfBoundsException ioobe) {
      } catch (NumberFormatException nfe) {
      }
      showChatWindow(player, val);
    } else if (command.startsWith("Loto")) {
      int val = 0;
      try {
        val = Integer.parseInt(command.substring(5));
      } catch (IndexOutOfBoundsException ioobe) {
      } catch (NumberFormatException nfe) {
      }
      if (val == 0)
      {
        for (int i = 0; i < 5; i++) {
          player.setLoto(i, 0);
        }
      }
      showLotoWindow(player, val);
    } else if (command.startsWith("CPRecovery")) {
      makeCPRecovery(player);
    } else if (command.startsWith("SupportMagic")) {
      makeSupportMagic(player);
    } else if (command.startsWith("GiveBlessing")) {
      giveBlessingSupport(player);
    } else if (command.startsWith("multisell")) {
      L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(command.substring(9).trim()), player, false, getCastle().getTaxRate());
    } else if (command.startsWith("exc_multisell")) {
      L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(command.substring(13).trim()), player, true, getCastle().getTaxRate());
    } else if (command.startsWith("Augment")) {
      int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
      switch (cmdChoice) {
      case 1:
        player.sendPacket(Static.SELECT_THE_ITEM_TO_BE_AUGMENTED);
        player.sendPacket(Static.ExShowVariationMakeWindow);
        player.setAugFlag(true);
        break;
      case 2:
        player.sendPacket(Static.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION);
        player.sendPacket(Static.ExShowVariationCancelWindow);
      }
    }
    else if (command.startsWith("npcfind_byid")) {
      try {
        L2Spawn spawn = SpawnTable.getInstance().getTemplate(Integer.parseInt(command.substring(12).trim()));

        if (spawn != null)
          player.sendPacket(new RadarControl(0, 1, spawn.getLocx(), spawn.getLocy(), spawn.getLocz()));
      }
      catch (NumberFormatException nfe) {
        player.sendMessage("Wrong command parameters");
      }
    } else if (command.startsWith("EnterRift")) {
      try {
        Byte b1 = Byte.valueOf(Byte.parseByte(command.substring(10)));
        DimensionalRiftManager.getInstance().start(player, b1.byteValue(), this);
      } catch (Exception e) {
      }
    } else if (command.startsWith("ChangeRiftRoom")) {
      if ((player.isInParty()) && (player.getParty().isInDimensionalRift()))
        player.getParty().getDimensionalRift().manualTeleport(player, this);
      else
        DimensionalRiftManager.getInstance().handleCheat(player, this);
    }
    else if (command.startsWith("ExitRift")) {
      if ((player.isInParty()) && (player.getParty().isInDimensionalRift()))
        player.getParty().getDimensionalRift().manualExitRift(player, this);
      else
        DimensionalRiftManager.getInstance().handleCheat(player, this);
    }
    else if (command.startsWith("Buff")) {
      if ((getTemplate().npcId != Config.BUFFER_ID) || (player.ignoreBuffer())) {
        return;
      }

      String[] opaopa = command.split(" ");
      int buff_type = Integer.parseInt(opaopa[1]);
      int buff_id = Integer.parseInt(opaopa[2]);
      int buff_level = Integer.parseInt(opaopa[3]);

      if ((opaopa.length == 7) && (!player.isPremium())) {
        int coin_id = Integer.parseInt(opaopa[4]);
        int coin_cnt = Math.max(Integer.parseInt(opaopa[5]), 1);
        if (player.getItemCount(coin_id) < coin_cnt) {
          player.sendHtmlMessage(new StringBuilder().append("\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u0431\u0430\u0444\u0444\u0430: ").append(coin_cnt).append(" ").append(opaopa[6]).toString());
          return;
        }
        if (Integer.parseInt(opaopa[5]) >= 1) {
          player.destroyItemByItemId("Buffer", coin_id, coin_cnt, this, true);
        }

      }

      addBuff(player.getBuffTarget(), buff_id, buff_level);
      if (buff_type == 0)
        showBufferWindow(player, new StringBuilder().append("data/html/default/").append(Config.BUFFER_ID).append(".htm").toString());
      else
        showBufferWindow(player, new StringBuilder().append("data/html/default/").append(Config.BUFFER_ID).append("-").append(buff_type).append(".htm").toString());
    }
    else if (command.startsWith("bDop")) {
      if ((getTemplate().npcId != Config.BUFFER_ID) || (player.ignoreBuffer())) {
        return;
      }

      int intdex = Integer.parseInt(command.substring(4).trim());
      switch (intdex) {
      case 1:
        player.getBuffTarget().stopAllEffectsB();
        break;
      case 2:
        player.getBuffTarget().fullRestore();
        break;
      case 3:
        player.getBuffTarget().doRebuff();
        break;
      case 4:
        player.getBuffTarget().doFullBuff(1);
        break;
      case 5:
        player.getBuffTarget().doFullBuff(2);
      }

      showBufferWindow(player, new StringBuilder().append("data/html/default/").append(Config.BUFFER_ID).append(".htm").toString());
    } else if (command.startsWith("profileBuff")) {
      if ((getTemplate().npcId != Config.BUFFER_ID) || (player.ignoreBuffer())) {
        return;
      }

      player.doBuffProfile(Integer.parseInt(command.substring(11).trim()));

      showBufferWindow(player, new StringBuilder().append("data/html/default/").append(Config.BUFFER_ID).append(".htm").toString());
    } else if (command.startsWith("sprofileBuff")) {
      if ((getTemplate().npcId != Config.BUFFER_ID) || (player.ignoreBuffer())) {
        return;
      }

      player.saveBuffProfile(Integer.parseInt(command.substring(12).trim()));

      showBufferWindow(player, new StringBuilder().append("data/html/default/").append(Config.BUFFER_ID).append("-4.htm").toString());
    } else if (command.equalsIgnoreCase("changeBuffTarget")) {
      if ((getTemplate().npcId != Config.BUFFER_ID) || (player.ignoreBuffer())) {
        return;
      }

      if ((player.getPet() == null) || (player.getBuffTarget() == player.getPet()))
        player.setBuffTarget(player);
      else {
        player.setBuffTarget(player.getPet());
      }

      showBufferWindow(player, new StringBuilder().append("data/html/default/").append(Config.BUFFER_ID).append(".htm").toString());
    }
  }

  private void showBufferWindow(L2PcInstance player, String htm)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    reply.setFile(htm);
    reply.replace("%objectId%", String.valueOf(getObjectId()));

    if (player.getPet() == null)
      reply.replace("%change_target%", "[\u0418\u0433\u0440\u043E\u043A]");
    else if (player.getBuffTarget() == player.getPet())
      reply.replace("%change_target%", new StringBuilder().append("[\u041F\u0435\u0442] <button value=\"\u0418\u0433\u0440\u043E\u043A\" action=\"bypass -h npc_").append(getObjectId()).append("_changeBuffTarget\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">").toString());
    else {
      reply.replace("%change_target%", new StringBuilder().append("<button value=\"\u041F\u0435\u0442\" action=\"bypass -h npc_").append(getObjectId()).append("_changeBuffTarget\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"> [\u0418\u0433\u0440\u043E\u043A]").toString());
    }

    player.sendUserPacket(reply);
    player.sendActionFailed();
    reply = null;
  }

  public L2ItemInstance getActiveWeaponInstance()
  {
    return null;
  }

  public L2Weapon getActiveWeaponItem()
  {
    int weaponId = getTemplate().rhand;

    if (weaponId < 1) {
      return null;
    }

    L2Item item = ItemTable.getInstance().getTemplate(weaponId);
    if (item == null) {
      return null;
    }

    if (!(item instanceof L2Weapon)) {
      return null;
    }

    return (L2Weapon)item;
  }

  public void giveBlessingSupport(L2PcInstance player) {
    if (player == null) {
      return;
    }

    int player_level = player.getLevel();

    setTarget(player);

    if ((player_level > 39) || (player.getClassId().level() >= 2)) {
      String content = "<html><body>Newbie Guide:<br>I'm sorry, but you are not eligible to receive the protection blessing.<br1>It can only be bestowed on <font color=\"LEVEL\">characters below level 39 who have not made a seccond transfer.</font></body></html>";
      insertObjectIdAndShowChatWindow(player, content);
      return;
    }
    L2Skill skill = SkillTable.getInstance().getInfo(5182, 1);
    doCast(skill);
  }

  public L2ItemInstance getSecondaryWeaponInstance()
  {
    return null;
  }

  public L2Weapon getSecondaryWeaponItem()
  {
    int weaponId = getTemplate().lhand;

    if (weaponId < 1) {
      return null;
    }

    L2Item item = ItemTable.getInstance().getTemplate(getTemplate().lhand);

    if (!(item instanceof L2Weapon)) {
      return null;
    }

    return (L2Weapon)item;
  }

  public void insertObjectIdAndShowChatWindow(L2PcInstance player, String content)
  {
    content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
    NpcHtmlMessage npcReply = NpcHtmlMessage.id(getObjectId());
    npcReply.setHtml(content);
    player.sendPacket(npcReply);
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";

    if (val == 0)
      pom = new StringBuilder().append("").append(npcId).toString();
    else {
      pom = new StringBuilder().append(npcId).append("-").append(val).toString();
    }

    String temp = new StringBuilder().append("data/html/default/").append(pom).append(".htm").toString();

    if (!Config.LAZY_CACHE)
    {
      if (HtmCache.getInstance().contains(temp)) {
        return temp;
      }
    }
    else if (HtmCache.getInstance().isLoadable(temp)) {
      return temp;
    }

    return "data/html/npcdefault.htm";
  }

  public void showQuestChooseWindow(L2PcInstance player, Quest[] quests)
  {
    TextBuilder sb = new TextBuilder();

    sb.append("<html><body><title>Talk about:</title><br>");

    for (Quest q : quests) {
      sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">").append(q.getDescr()).append("</a><br>");
    }

    sb.append("</body></html>");

    insertObjectIdAndShowChatWindow(player, sb.toString());
  }

  public void showQuestWindow(L2PcInstance player, String questId)
  {
    Quest q = QuestManager.getInstance().getQuest(questId);

    if ((player.getWeightPenalty() >= 3) && (q.getQuestIntId() >= 1) && (q.getQuestIntId() < 1000)) {
      player.sendPacket(Static.INVENTORY_LESS_THAN_80_PERCENT);
      return;
    }

    QuestState qs = player.getQuestState(questId);

    if (qs != null)
    {
      if (!qs.getQuest().notifyTalk(this, qs)) {
        return;
      }
    }
    else if (q != null)
    {
      Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);

      if ((qlst != null) && (qlst.length > 0))
        for (int i = 0; i < qlst.length; i++)
          if (qlst[i] == q) {
            qs = q.newQuestState(player);

            if (qs.getQuest().notifyTalk(this, qs)) break;
            return;
          }
    }
    String content;
    String content;
    if (qs == null)
    {
      content = "<html><body>\u0414\u043B\u044F \u0432\u0430\u0441 \u043D\u0430 \u0434\u0430\u043D\u043D\u044B\u0439 \u043C\u043E\u043C\u0435\u043D\u0442 \u0443 \u043C\u0435\u043D\u044F \u043D\u0438\u0447\u0435\u0433\u043E \u043D\u0435\u0442.</body></html>";
    } else {
      questId = qs.getQuest().getName();
      String stateId = qs.getStateId();
      content = HtmCache.getInstance().getHtm(new StringBuilder().append("data/jscript/quests/").append(questId).append("/").append(stateId).append(".htm").toString());
      if (content == null) {
        content = HtmCache.getInstance().getHtm(new StringBuilder().append("data/scripts/quests/").append(questId).append("/").append(stateId).append(".htm").toString());
      }

    }

    if (content != null) {
      insertObjectIdAndShowChatWindow(player, content);
    }

    player.sendActionFailed();
  }

  public void showQuestWindow(L2PcInstance player)
  {
    List options = new FastList();

    QuestState[] awaits = player.getQuestsForTalk(getTemplate().npcId);
    Quest[] starts = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);

    if (awaits != null) {
      for (QuestState x : awaits) {
        if ((options.contains(x)) || 
          (x.getQuest().getQuestIntId() <= 0) || (x.getQuest().getQuestIntId() >= 1000)) continue;
        options.add(x.getQuest());
      }

    }

    if (starts != null) {
      for (Quest x : starts) {
        if ((options.contains(x)) || 
          (x.getQuestIntId() <= 0) || (x.getQuestIntId() >= 1000)) continue;
        options.add(x);
      }

    }

    if (options.size() > 1)
      showQuestChooseWindow(player, (Quest[])options.toArray(new Quest[options.size()]));
    else if (options.size() == 1)
      showQuestWindow(player, ((Quest)options.get(0)).getName());
    else
      showQuestWindow(player, "");
  }

  public void showLotoWindow(L2PcInstance player, int val)
  {
    int npcId = getTemplate().npcId;

    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());

    if (val == 0)
    {
      String filename = getHtmlPath(npcId, 1);
      html.setFile(filename);
    } else if ((val >= 1) && (val <= 21))
    {
      if (!Lottery.getInstance().isStarted())
      {
        player.sendPacket(Static.NO_LOTTERY_TICKETS_CURRENT_SOLD);
        return;
      }
      if (!Lottery.getInstance().isSellableTickets())
      {
        player.sendPacket(Static.NO_LOTTERY_TICKETS_AVAILABLE);
        return;
      }

      String filename = getHtmlPath(npcId, 5);
      html.setFile(filename);

      int count = 0;
      int found = 0;

      for (int i = 0; i < 5; i++) {
        if (player.getLoto(i) == val)
        {
          player.setLoto(i, 0);
          found = 1;
        } else if (player.getLoto(i) > 0) {
          count++;
        }

      }

      if ((count < 5) && (found == 0) && (val <= 20)) {
        for (int i = 0; i < 5; i++) {
          if (player.getLoto(i) == 0) {
            player.setLoto(i, val);
            break;
          }
        }

      }

      count = 0;
      for (int i = 0; i < 5; i++) {
        if (player.getLoto(i) > 0) {
          count++;
          String button = String.valueOf(player.getLoto(i));
          if (player.getLoto(i) < 10) {
            button = new StringBuilder().append("0").append(button).toString();
          }
          String search = new StringBuilder().append("fore=\"L2UI.lottoNum").append(button).append("\" back=\"L2UI.lottoNum").append(button).append("a_check\"").toString();
          String replace = new StringBuilder().append("fore=\"L2UI.lottoNum").append(button).append("a_check\" back=\"L2UI.lottoNum").append(button).append("\"").toString();
          html.replace(search, replace);
        }
      }

      if (count == 5) {
        String search = "0\">Return";
        String replace = "22\">The winner selected the numbers above.";
        html.replace(search, replace);
      }
    } else if (val == 22)
    {
      if (!Lottery.getInstance().isStarted())
      {
        player.sendPacket(Static.NO_LOTTERY_TICKETS_CURRENT_SOLD);
        return;
      }
      if (!Lottery.getInstance().isSellableTickets())
      {
        player.sendPacket(Static.NO_LOTTERY_TICKETS_AVAILABLE);
        return;
      }

      int price = Config.ALT_LOTTERY_TICKET_PRICE;
      int lotonumber = Lottery.getInstance().getId();
      int enchant = 0;
      int type2 = 0;

      for (int i = 0; i < 5; i++) {
        if (player.getLoto(i) == 0) {
          return;
        }

        if (player.getLoto(i) < 17)
          enchant = (int)(enchant + Math.pow(2.0D, player.getLoto(i) - 1));
        else {
          type2 = (int)(type2 + Math.pow(2.0D, player.getLoto(i) - 17));
        }
      }
      if (player.getAdena() < price) {
        player.sendPacket(Static.YOU_NOT_ENOUGH_ADENA);
        return;
      }
      if (!player.reduceAdena("Loto", price, this, true)) {
        return;
      }
      Lottery.getInstance().increasePrize(price);

      player.sendPacket(SystemMessage.id(SystemMessageId.ACQUIRED).addNumber(lotonumber).addItemName(4442));

      L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), 4442);
      item.setCount(1);
      item.setCustomType1(lotonumber);
      item.setEnchantLevel(enchant);
      item.setCustomType2(type2);
      player.getInventory().addItem("Loto", item, player, this);

      InventoryUpdate iu = new InventoryUpdate();
      iu.addItem(item);
      L2ItemInstance adenaupdate = player.getInventory().getItemByItemId(57);
      iu.addModifiedItem(adenaupdate);
      player.sendPacket(iu);

      String filename = getHtmlPath(npcId, 3);
      html.setFile(filename);
    } else if (val == 23)
    {
      String filename = getHtmlPath(npcId, 3);
      html.setFile(filename);
    } else if (val == 24)
    {
      String filename = getHtmlPath(npcId, 4);
      html.setFile(filename);

      int lotonumber = Lottery.getInstance().getId();
      TextBuilder message = new TextBuilder();
      for (L2ItemInstance item : player.getInventory().getItems()) {
        if (item == null) {
          continue;
        }
        if ((item.getItemId() == 4442) && (item.getCustomType1() < lotonumber)) {
          message.append(new StringBuilder().append("<a action=\"bypass -h npc_%objectId%_Loto ").append(item.getObjectId()).append("\">").append(item.getCustomType1()).append(" Event Number ").toString());
          int[] numbers = Lottery.getInstance().decodeNumbers(item.getEnchantLevel(), item.getCustomType2());
          for (int i = 0; i < 5; i++) {
            message.append(new StringBuilder().append(numbers[i]).append(" ").toString());
          }
          int[] check = Lottery.getInstance().checkTicket(item);
          if (check[0] > 0) {
            switch (check[0]) {
            case 1:
              message.append("- 1st Prize");
              break;
            case 2:
              message.append("- 2nd Prize");
              break;
            case 3:
              message.append("- 3th Prize");
              break;
            case 4:
              message.append("- 4th Prize");
            }

            message.append(new StringBuilder().append(" ").append(check[1]).append("a.").toString());
          }
          message.append("</a><br>");
        }
      }
      if (message.toString().equals("")) {
        message.append("There is no winning lottery ticket...<br>");
      }
      html.replace("%result%", message.toString());
    } else if (val > 24)
    {
      int lotonumber = Lottery.getInstance().getId();
      L2ItemInstance item = player.getInventory().getItemByObjectId(val);
      if ((item == null) || (item.getItemId() != 4442) || (item.getCustomType1() >= lotonumber)) {
        return;
      }
      int[] check = Lottery.getInstance().checkTicket(item);

      player.sendPacket(SystemMessage.id(SystemMessageId.DISSAPEARED_ITEM).addItemName(4442));
      int adena = check[1];
      if (adena > 0) {
        player.addAdena("Loto", adena, this, true);
      }
      player.destroyItem("Loto", item, this, false);
      return;
    }
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%race%", new StringBuilder().append("").append(Lottery.getInstance().getId()).toString());
    html.replace("%adena%", new StringBuilder().append("").append(Lottery.getInstance().getPrize()).toString());
    html.replace("%ticket_price%", new StringBuilder().append("").append(Config.ALT_LOTTERY_TICKET_PRICE).toString());
    html.replace("%prize5%", new StringBuilder().append("").append(Config.ALT_LOTTERY_5_NUMBER_RATE * 100.0F).toString());
    html.replace("%prize4%", new StringBuilder().append("").append(Config.ALT_LOTTERY_4_NUMBER_RATE * 100.0F).toString());
    html.replace("%prize3%", new StringBuilder().append("").append(Config.ALT_LOTTERY_3_NUMBER_RATE * 100.0F).toString());
    html.replace("%prize2%", new StringBuilder().append("").append(Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE).toString());
    html.replace("%enddate%", new StringBuilder().append("").append(DateFormat.getDateInstance().format(Long.valueOf(Lottery.getInstance().getEndDate()))).toString());
    player.sendPacket(html);

    player.sendActionFailed();
  }

  public void makeCPRecovery(L2PcInstance player) {
    if ((getNpcId() != 31225) && (getNpcId() != 31226)) {
      return;
    }

    if (player.isCursedWeaponEquiped()) {
      player.sendHtmlMessage("Go away, you're not welcome here.");
      return;
    }

    int neededmoney = 100;

    if (!player.reduceAdena("RestoreCP", neededmoney, player.getLastFolkNPC(), true)) {
      return;
    }

    player.setCurrentCp(player.getMaxCp());

    player.sendPacket(SystemMessage.id(SystemMessageId.S1_CP_WILL_BE_RESTORED).addString(player.getName()));
  }

  public void makeSupportMagic(L2PcInstance player)
  {
    if (player == null) {
      return;
    }

    if (player.isCursedWeaponEquiped()) {
      return;
    }

    int player_level = player.getLevel();
    int lowestLevel = 0;
    int higestLevel = 0;

    setTarget(player);

    if (player.isMageClass()) {
      lowestLevel = HelperBuffTable.getInstance().getMagicClassLowestLevel();
      higestLevel = HelperBuffTable.getInstance().getMagicClassHighestLevel();
    } else {
      lowestLevel = HelperBuffTable.getInstance().getPhysicClassLowestLevel();
      higestLevel = HelperBuffTable.getInstance().getPhysicClassHighestLevel();
    }

    if ((player_level > higestLevel) || (!player.isNewbie())) {
      String content = new StringBuilder().append("<html><body>Newbie Guide:<br>Only a <font color=\"LEVEL\">novice character of level ").append(higestLevel).append(" or less</font> can receive my support magic.<br>Your novice character is the first one that you created and raised in this world.</body></html>").toString();
      insertObjectIdAndShowChatWindow(player, content);
      return;
    }

    if (player_level < lowestLevel) {
      String content = new StringBuilder().append("<html><body>Come back here when you have reached level ").append(lowestLevel).append(". I will give you support magic then.</body></html>").toString();
      insertObjectIdAndShowChatWindow(player, content);
      return;
    }

    L2Skill skill = null;

    for (L2HelperBuff helperBuffItem : HelperBuffTable.getInstance().getHelperBuffTable())
      if ((helperBuffItem.isMagicClassBuff() == player.isMageClass()) && 
        (player_level >= helperBuffItem.getLowerLevel()) && (player_level <= helperBuffItem.getUpperLevel())) {
        skill = SkillTable.getInstance().getInfo(helperBuffItem.getSkillID(), helperBuffItem.getSkillLevel());
        if (skill.getSkillType() == L2Skill.SkillType.SUMMON)
          player.doCast(skill);
        else
          doCast(skill);
      }
  }

  public void showChatWindow(L2PcInstance player)
  {
    showChatWindow(player, 0);
  }

  private boolean showPkDenyChatWindow(L2PcInstance player, String type)
  {
    String html = HtmCache.getInstance().getHtm(new StringBuilder().append("data/html/").append(type).append("/").append(getNpcId()).append("-pk.htm").toString());

    if (html != null) {
      NpcHtmlMessage pkDenyMsg = NpcHtmlMessage.id(getObjectId());
      pkDenyMsg.setHtml(html);
      player.sendPacket(pkDenyMsg);
      player.sendActionFailed();
      return true;
    }

    return false;
  }

  public void showChatWindow(L2PcInstance player, int val)
  {
    if (player.getKarma() > 0) {
      if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP) && ((this instanceof L2MerchantInstance))) {
        if (showPkDenyChatWindow(player, "merchant"))
          return;
      }
      else if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK) && ((this instanceof L2TeleporterInstance))) {
        if (showPkDenyChatWindow(player, "teleporter"))
          return;
      }
      else if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE) && ((this instanceof L2WarehouseInstance))) {
        if (showPkDenyChatWindow(player, "warehouse"))
          return;
      }
      else if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP) && ((this instanceof L2FishermanInstance)) && 
        (showPkDenyChatWindow(player, "fisherman"))) {
        return;
      }

    }

    if (("L2Auctioneer".equals(getTemplate().type)) && (val == 0)) {
      return;
    }

    int npcId = getTemplate().npcId;
    if ((npcId == Config.BUFFER_ID) && (player.ignoreBuffer())) {
      return;
    }

    String filename = "data/html/seven_signs/";
    int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(1);
    int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(2);
    int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
    boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
    int compWinner = SevenSigns.getInstance().getCabalHighestScore();

    switch (npcId) {
    case 31078:
    case 31079:
    case 31080:
    case 31081:
    case 31082:
    case 31083:
    case 31084:
    case 31168:
    case 31692:
    case 31694:
    case 31997:
      switch (playerCabal) {
      case 2:
        if (isSealValidationPeriod) {
          if (compWinner == 2) {
            if (compWinner != sealGnosisOwner)
              filename = new StringBuilder().append(filename).append("dawn_priest_2c.htm").toString();
            else
              filename = new StringBuilder().append(filename).append("dawn_priest_2a.htm").toString();
          }
          else
            filename = new StringBuilder().append(filename).append("dawn_priest_2b.htm").toString();
        }
        else {
          filename = new StringBuilder().append(filename).append("dawn_priest_1b.htm").toString();
        }
        break;
      case 1:
        if (isSealValidationPeriod)
          filename = new StringBuilder().append(filename).append("dawn_priest_3b.htm").toString();
        else {
          filename = new StringBuilder().append(filename).append("dawn_priest_3a.htm").toString();
        }
        break;
      default:
        if (isSealValidationPeriod) {
          if (compWinner == 2)
            filename = new StringBuilder().append(filename).append("dawn_priest_4.htm").toString();
          else
            filename = new StringBuilder().append(filename).append("dawn_priest_2b.htm").toString();
        }
        else
          filename = new StringBuilder().append(filename).append("dawn_priest_1a.htm").toString();
      }
      break;
    case 31085:
    case 31086:
    case 31087:
    case 31088:
    case 31089:
    case 31090:
    case 31091:
    case 31169:
    case 31693:
    case 31695:
    case 31998:
      switch (playerCabal) {
      case 1:
        if (isSealValidationPeriod) {
          if (compWinner == 1) {
            if (compWinner != sealGnosisOwner)
              filename = new StringBuilder().append(filename).append("dusk_priest_2c.htm").toString();
            else
              filename = new StringBuilder().append(filename).append("dusk_priest_2a.htm").toString();
          }
          else
            filename = new StringBuilder().append(filename).append("dusk_priest_2b.htm").toString();
        }
        else {
          filename = new StringBuilder().append(filename).append("dusk_priest_1b.htm").toString();
        }
        break;
      case 2:
        if (isSealValidationPeriod)
          filename = new StringBuilder().append(filename).append("dusk_priest_3b.htm").toString();
        else {
          filename = new StringBuilder().append(filename).append("dusk_priest_3a.htm").toString();
        }
        break;
      default:
        if (isSealValidationPeriod) {
          if (compWinner == 1)
            filename = new StringBuilder().append(filename).append("dusk_priest_4.htm").toString();
          else
            filename = new StringBuilder().append(filename).append("dusk_priest_2b.htm").toString();
        }
        else
          filename = new StringBuilder().append(filename).append("dusk_priest_1a.htm").toString();
      }
      break;
    case 31095:
    case 31096:
    case 31097:
    case 31098:
    case 31099:
    case 31100:
    case 31101:
    case 31102:
      if (isSealValidationPeriod) {
        if ((playerCabal != compWinner) || (sealAvariceOwner != compWinner)) {
          switch (compWinner) {
          case 2:
            player.sendPacket(Static.CAN_BE_USED_BY_DAWN);
            filename = new StringBuilder().append(filename).append("necro_no.htm").toString();
            break;
          case 1:
            player.sendPacket(Static.CAN_BE_USED_BY_DUSK);
            filename = new StringBuilder().append(filename).append("necro_no.htm").toString();
            break;
          case 0:
            filename = getHtmlPath(npcId, val);
          }
        }
        else {
          filename = getHtmlPath(npcId, val);
        }
      }
      else if (playerCabal == 0)
        filename = new StringBuilder().append(filename).append("necro_no.htm").toString();
      else {
        filename = getHtmlPath(npcId, val);
      }

      break;
    case 31114:
    case 31115:
    case 31116:
    case 31117:
    case 31118:
    case 31119:
      if (isSealValidationPeriod) {
        if ((playerCabal != compWinner) || (sealGnosisOwner != compWinner)) {
          switch (compWinner) {
          case 2:
            player.sendPacket(Static.CAN_BE_USED_BY_DAWN);
            filename = new StringBuilder().append(filename).append("cata_no.htm").toString();
            break;
          case 1:
            player.sendPacket(Static.CAN_BE_USED_BY_DUSK);
            filename = new StringBuilder().append(filename).append("cata_no.htm").toString();
            break;
          case 0:
            filename = getHtmlPath(npcId, val);
          }
        }
        else {
          filename = getHtmlPath(npcId, val);
        }
      }
      else if (playerCabal == 0)
        filename = new StringBuilder().append(filename).append("cata_no.htm").toString();
      else {
        filename = getHtmlPath(npcId, val);
      }

      break;
    case 31111:
      if ((playerCabal == sealAvariceOwner) && (playerCabal == compWinner)) {
        switch (sealAvariceOwner) {
        case 2:
          filename = new StringBuilder().append(filename).append("spirit_dawn.htm").toString();
          break;
        case 1:
          filename = new StringBuilder().append(filename).append("spirit_dusk.htm").toString();
          break;
        case 0:
          filename = new StringBuilder().append(filename).append("spirit_null.htm").toString();
        }
      }
      else {
        filename = new StringBuilder().append(filename).append("spirit_null.htm").toString();
      }
      break;
    case 31112:
      filename = new StringBuilder().append(filename).append("spirit_exit.htm").toString();
      break;
    case 31127:
    case 31128:
    case 31129:
    case 31130:
    case 31131:
      filename = new StringBuilder().append(filename).append("festival/dawn_guide.htm").toString();
      break;
    case 31137:
    case 31138:
    case 31139:
    case 31140:
    case 31141:
      filename = new StringBuilder().append(filename).append("festival/dusk_guide.htm").toString();
      break;
    case 31092:
      filename = new StringBuilder().append(filename).append("blkmrkt_1.htm").toString();
      break;
    case 31113:
      filename = new StringBuilder().append(filename).append("mammmerch_1.htm").toString();
      break;
    case 31126:
      filename = new StringBuilder().append(filename).append("mammblack_1.htm").toString();
      break;
    case 31132:
    case 31133:
    case 31134:
    case 31135:
    case 31136:
    case 31142:
    case 31143:
    case 31144:
    case 31145:
    case 31146:
      filename = new StringBuilder().append(filename).append("festival/festival_witch.htm").toString();
      break;
    case 31688:
      if (player.isNoble())
        filename = "data/html/olympiad/noble_main.htm";
      else {
        filename = getHtmlPath(npcId, val);
      }
      break;
    case 31690:
    case 31769:
    case 31770:
    case 31771:
    case 31772:
      if ((player.isNoble()) || (player.isHero()))
        filename = "data/html/olympiad/obelisk001.htm";
      else {
        filename = "data/html/olympiad/obelisk001a.htm";
      }
      break;
    case 40001:
      showBufferWindow(player, getHtmlPath(npcId, val));
      return;
    default:
      if ((npcId >= 31865) && (npcId <= 31918)) {
        filename = new StringBuilder().append(filename).append("rift/GuardianOfBorder.htm").toString();
      }
      else {
        if (((npcId >= 31093) && (npcId <= 31094)) || ((npcId >= 31172) && (npcId <= 31201)) || ((npcId >= 31239) && (npcId <= 31254))) {
          return;
        }

        filename = getHtmlPath(npcId, val);
      }

    }

    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    html.setFile(filename);

    if (((this instanceof L2MerchantInstance)) && 
      (Config.LIST_PET_RENT_NPC.contains(Integer.valueOf(npcId)))) {
      html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");
    }

    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
    player.sendPacket(html);

    player.sendActionFailed();
  }

  public void showChatWindow(L2PcInstance player, String filename)
  {
    if ((getTemplate().npcId == Config.BUFFER_ID) && (player.ignoreBuffer())) {
      return;
    }

    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    player.sendPacket(html);

    player.sendActionFailed();
  }

  public int getExpReward()
  {
    double rateXp = getStat().calcStat(Stats.MAX_HP, 1.0D, this, null);
    return (int)(getTemplate().rewardExp * rateXp * Config.RATE_XP);
  }

  public int getSpReward()
  {
    double rateSp = getStat().calcStat(Stats.MAX_HP, 1.0D, this, null);
    return (int)(getTemplate().rewardSp * rateSp * Config.RATE_SP);
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }

    _currentLHandId = getTemplate().lhand;
    _currentRHandId = getTemplate().rhand;
    _currentCollisionHeight = getTemplate().collisionHeight;
    _currentCollisionRadius = getTemplate().collisionRadius;
    DecayTaskManager.getInstance().addDecayTask(this);
    return true;
  }

  public void setSpawn(L2Spawn spawn)
  {
    _spawn = spawn;
  }

  public void onSpawn()
  {
    super.onSpawn();
  }

  public void onDecay()
  {
    if (isDecayed()) {
      return;
    }

    setDecayed(true);

    if ((this instanceof L2ControlTowerInstance)) {
      ((L2ControlTowerInstance)this).onDeath();
    }

    if (_spawn != null) {
      _spawn.decreaseCount(this);
      if (_spawn.getTerritory() != null) {
        _spawn.getTerritory().notifyDeath();
        _spawn.setLastKill(System.currentTimeMillis());
      }

    }

    super.onDecay();
  }

  public void deleteMe()
  {
    if (getWorldRegion() != null) {
      getWorldRegion().removeFromZones(this);
    }

    try
    {
      decayMe();
    } catch (Throwable t) {
      _log.severe(new StringBuilder().append("deletedMe(): ").append(t).toString());
    }

    try
    {
      getKnownList().removeAllKnownObjects();
    } catch (Throwable t) {
      _log.severe(new StringBuilder().append("deletedMe(): ").append(t).toString());
    }

    L2World.getInstance().removeObject(this);
  }

  public L2Spawn getSpawn()
  {
    return _spawn;
  }

  public String toString()
  {
    return getTemplate().name;
  }

  public boolean isDecayed() {
    return _isDecayed;
  }

  public void setDecayed(boolean decayed) {
    _isDecayed = decayed;
  }

  public void endDecayTask() {
    if (!isDecayed()) {
      DecayTaskManager.getInstance().cancelDecayTask(this);
      onDecay();
    }
  }

  public boolean isMob()
  {
    return false;
  }

  public void setLHandId(int newWeaponId)
  {
    _currentLHandId = newWeaponId;
  }

  public void setRHandId(int newWeaponId) {
    _currentRHandId = newWeaponId;
  }

  public void setCollisionHeight(int height) {
    _currentCollisionHeight = height;
  }

  public void setCollisionRadius(int radius) {
    _currentCollisionRadius = radius;
  }

  public int getCollisionHeight() {
    return _currentCollisionHeight;
  }

  public int getCollisionRadius() {
    return _currentCollisionRadius;
  }

  public void addUseSkillDesire(int skillId, int skillLvl)
  {
    doCast(SkillTable.getInstance().getInfo(skillId, skillLvl));
  }

  public void dropItem(int item, int count, L2PcInstance pickuper) {
    L2ItemInstance ditem = ItemTable.getInstance().createItem("MonsterDrop", item, count, null, this);
    ditem.dropMe(this, getX() + Rnd.get(40), getY() + Rnd.get(40), getZ());

    if (pickuper != null)
      ditem.setPickuper(pickuper);
  }

  public void dropItem(int item, int count)
  {
    dropItem(item, count, null);
  }

  public void sayString(String text, int type) {
    broadcastPacket(new CreatureSay(getObjectId(), type, getName(), text));
  }

  public void sayString(String text) {
    sayString(text, 0);
  }

  public boolean fromMonastry()
  {
    return getTemplate().fromMonastry();
  }

  public boolean isEnemyForMob(L2Attackable mob)
  {
    return false;
  }

  public void dropRaidCustom(L2PcInstance killer)
  {
    if (_raidRewards.isEmpty()) {
      return;
    }

    FastList.Node k = _raidRewards.head(); for (FastList.Node endk = _raidRewards.tail(); (k = k.getNext()) != endk; ) {
      Config.EventReward reward = (Config.EventReward)k.getValue();
      if (reward == null)
      {
        continue;
      }
      if (Rnd.get(100) < reward.chance)
        killer.addItem("RaidDrop", reward.id, reward.count, killer, true);
    }
  }

  public int getItemCount(L2PcInstance player, int itemId)
  {
    return player.getItemCount(itemId);
  }

  public void giveItem(L2PcInstance player, int itemId, int count) {
    player.addItem("Npc.giveItem", itemId, count, player, true);
  }

  public void deleteItem(L2PcInstance player, int itemId, int count) {
    player.destroyItemByItemId("Npc.deleteItem", itemId, count, player, true);
  }

  public void addBuff(L2Character target, int id, int lvl) {
    if (CustomServerData.getInstance().isWhiteBuff(id)) {
      target.stopSkillEffects(id);
      SkillTable.getInstance().getInfo(id, lvl).getEffects(target, target);
    }
  }

  public void soundEffect(L2PcInstance player, String sound) {
    player.sendPacket(new PlaySound(sound));
  }

  public int isShowSpawnAnimation()
  {
    return _showSpawnAnimation;
  }

  public void setShowSpawnAnimation(int value)
  {
    _showSpawnAnimation = value;
  }

  public int getWeaponEnchant()
  {
    return _weaponEnch;
  }

  public void doNpcChat(int type, String name)
  {
    _template.doNpcChat(this, type, name);
  }

  public boolean isEventMob()
  {
    return isEventMob;
  }

  public int getNpcId()
  {
    return getTemplate().npcId;
  }

  public boolean isL2Npc()
  {
    return true;
  }

  public FastList<Integer> getPenaltyItems() {
    return _template.getPenaltyItems();
  }

  public Location getPenaltyLoc() {
    return _template.getPenaltyLoc();
  }

  public static class DestroyTemporalSummon
    implements Runnable
  {
    L2Summon _summon;
    L2PcInstance _player;

    public DestroyTemporalSummon(L2Summon summon, L2PcInstance player)
    {
      _summon = summon;
      _player = player;
    }

    public void run() {
      _summon.unSummon(_player);
    }
  }

  public static class DestroyTemporalNPC
    implements Runnable
  {
    private L2Spawn _oldSpawn;

    public DestroyTemporalNPC(L2Spawn spawn)
    {
      _oldSpawn = spawn;
    }

    public void run() {
      try {
        _oldSpawn.getLastSpawn().deleteMe();
        _oldSpawn.stopRespawn();
        SpawnTable.getInstance().deleteSpawn(_oldSpawn, false);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  protected class RandomAnimationTask
    implements Runnable
  {
    protected RandomAnimationTask()
    {
    }

    public void run()
    {
      try
      {
        if (this != _rAniTask) {
          return;
        }
        if (isMob())
        {
          if (getAI().getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
            return;
        }
        else {
          if (!isInActiveRegion().booleanValue())
          {
            return;
          }

          getKnownList().updateKnownObjects();
        }

        if ((!isDead()) && (!isStunned()) && (!isSleeping()) && (!isParalyzed())) {
          onRandomAnimation();
        }

        startRandomAnimationTimer();
      }
      catch (Throwable t)
      {
      }
    }
  }
}