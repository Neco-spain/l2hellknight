package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class L2SepulcherNpcInstance extends L2NpcInstance
{
  protected static Map<Integer, Integer> _hallGateKeepers = new FastMap<Integer, Integer>();

  protected Future<?> _closeTask = null;
  protected Future<?> _spawnNextMysteriousBoxTask = null;
  protected Future<?> _spawnMonsterTask = null;
  private String HTML_FILE_PATH;
  public L2SepulcherNpcInstance(int objectID, L2NpcTemplate template)
  {
    super(objectID, template);

    if (this._closeTask != null)
      this._closeTask.cancel(true);
    if (this._spawnNextMysteriousBoxTask != null)
      this._spawnNextMysteriousBoxTask.cancel(true);
    if (this._spawnMonsterTask != null)
      this._spawnMonsterTask.cancel(true);
    this._closeTask = null;
    this._spawnNextMysteriousBoxTask = null;
    this._spawnMonsterTask = null;
  }

  public void onSpawn()
  {
    super.onSpawn();
  }

  public void deleteMe()
  {
    if (this._closeTask != null)
    {
      this._closeTask.cancel(true);
      this._closeTask = null;
    }
    if (this._spawnNextMysteriousBoxTask != null)
    {
      this._spawnNextMysteriousBoxTask.cancel(true);
      this._spawnNextMysteriousBoxTask = null;
    }
    if (this._spawnMonsterTask != null)
    {
      this._spawnMonsterTask.cancel(true);
      this._spawnMonsterTask = null;
    }
    super.deleteMe();
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) {
      return;
    }

    if (this != player.getTarget())
    {
      if (Config.DEBUG) {
        _log.info("new target selected:" + getObjectId());
      }

      player.setTarget(this);

      if (isAutoAttackable(player))
      {
        MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
        player.sendPacket(my);

        StatusUpdate su = new StatusUpdate(getObjectId());
        su.addAttribute(9, (int)getStatus().getCurrentHp());
        su.addAttribute(10, getMaxHp());
        player.sendPacket(su);
      }
      else
      {
        MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
        player.sendPacket(my);
      }

      player.sendPacket(new ValidateLocation(this));
    }
    else
    {
      if ((isAutoAttackable(player)) && (!isAlikeDead()))
      {
        if (Math.abs(player.getZ() - getZ()) < 400)
        {
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
        }
        else
        {
          player.sendPacket(new ActionFailed());
        }
      }

      if (!isAutoAttackable(player))
      {
        if (!canInteract(player))
        {
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
        }
        else
        {
          SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
          broadcastPacket(sa);

          doAction(player);
        }

      }

      player.sendPacket(new ActionFailed());
    }
  }

  private void doAction(L2PcInstance player)
  {
    if (isDead())
    {
      player.sendPacket(new ActionFailed());
      return;
    }

    switch (getNpcId())
    {
    case 31468:
    case 31469:
    case 31470:
    case 31471:
    case 31472:
    case 31473:
    case 31474:
    case 31475:
    case 31476:
    case 31477:
    case 31478:
    case 31479:
    case 31480:
    case 31481:
    case 31482:
    case 31483:
    case 31484:
    case 31485:
    case 31486:
    case 31487:
      setIsInvul(false);
      reduceCurrentHp(getMaxHp() + 1, player);
      if (this._spawnMonsterTask != null)
        this._spawnMonsterTask.cancel(true);
      this._spawnMonsterTask = ThreadPoolManager.getInstance().scheduleEffect(new SpawnMonster(getNpcId()), 3500L);
      break;
    case 31455:
    case 31456:
    case 31457:
    case 31458:
    case 31459:
    case 31460:
    case 31461:
    case 31462:
    case 31463:
    case 31464:
    case 31465:
    case 31466:
    case 31467:
      setIsInvul(false);
      reduceCurrentHp(getMaxHp() + 1, player);
      if ((player.getParty() != null) && (!player.getParty().isLeader(player)))
        player = player.getParty().getLeader();
      player.addItem("Quest", 7260, 1, player, true);
      break;
    default:
      Quest[] qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
      if ((qlsa != null) && (qlsa.length > 0))
        player.setLastQuestNpcObject(getObjectId());
      Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
      if ((qlst != null) && (qlst.length == 1))
        qlst[0].notifyFirstTalk(this, player);
      else {
        showChatWindow(player, 0);
      }
    }
    player.sendPacket(new ActionFailed());
  }

  public String getHtmlPath(int npcId, int val)
  {
    
      this.HTML_FILE_PATH = "data/html/SepulcherNpc/";
    String pom = "";
    if (val == 0)
    {
      pom = "" + npcId;
    }
    else
    {
      pom = npcId + "-" + val;
    }

    return this.HTML_FILE_PATH + pom + ".htm";
  }

  public void showChatWindow(L2PcInstance player, int val)
  {
    String filename = getHtmlPath(getNpcId(), val);
    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    player.sendPacket(html);
    player.sendPacket(new ActionFailed());
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (isBusy())
    {
      NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
      
        html.setFile("data/html/npcbusy.htm");
      
      html.replace("%busymessage%", getBusyMessage());
      html.replace("%npcname%", getName());
      html.replace("%playername%", player.getName());
      player.sendPacket(html);
    }
    else if (command.startsWith("Chat"))
    {
      int val = 0;
      try
      {
        val = Integer.parseInt(command.substring(5));
      }
      catch (IndexOutOfBoundsException ioobe)
      {
      }
      catch (NumberFormatException nfe)
      {
      }
      showChatWindow(player, val);
    }
    else if (command.startsWith("open_gate"))
    {
      L2ItemInstance hallsKey = player.getInventory().getItemByItemId(7260);
      if (hallsKey == null)
      {
        showHtmlFile(player, "Gatekeeper-no.htm");
      }
      else if (FourSepulchersManager.getInstance().isAttackTime())
      {
        switch (getNpcId())
        {
        case 31929:
        case 31934:
        case 31939:
        case 31944:
          FourSepulchersManager.getInstance().spawnShadow(getNpcId());
        }

        openNextDoor(getNpcId());
        if (player.getParty() != null)
        {
          for (L2PcInstance mem : player.getParty().getPartyMembers())
          {
            if (mem.getInventory().getItemByItemId(7260) != null)
              mem.destroyItemByItemId("Quest", 7260, mem.getInventory().getItemByItemId(7260).getCount(), mem, true);
          }
        }
        else
          player.destroyItemByItemId("Quest", 7260, hallsKey.getCount(), player, true);
      }
    }
    else
    {
      super.onBypassFeedback(player, command);
    }
  }

  public void openNextDoor(int npcId)
  {
    int doorId = ((Integer)FourSepulchersManager.getInstance().getHallGateKeepers().get(Integer.valueOf(npcId))).intValue();
    DoorTable.getInstance().getDoor(Integer.valueOf(doorId)).openMe();

    if (this._closeTask != null)
      this._closeTask.cancel(true);
    this._closeTask = ThreadPoolManager.getInstance().scheduleEffect(new CloseNextDoor(doorId), 10000L);
    if (this._spawnNextMysteriousBoxTask != null)
      this._spawnNextMysteriousBoxTask.cancel(true);
    this._spawnNextMysteriousBoxTask = ThreadPoolManager.getInstance().scheduleEffect(new SpawnNextMysteriousBox(npcId), 0L);
  }

  public void sayInShout(String msg)
  {
    if ((msg == null) || (msg.isEmpty()))
      return;
    Collection<L2PcInstance> knownPlayers = L2World.getInstance().getAllPlayers();
    if ((knownPlayers == null) || (knownPlayers.isEmpty()))
      return;
    CreatureSay sm = new CreatureSay(0, 1, getName(), msg);
    for (L2PcInstance player : knownPlayers)
    {
      if (player == null)
        continue;
      if (Util.checkIfInRange(15000, player, this, true))
        player.sendPacket(sm);
    }
  }

  public void showHtmlFile(L2PcInstance player, String file)
  {
    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
    
      html.setFile("data/html/SepulcherNpc/" + file);
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }

  private class SpawnMonster
    implements Runnable
  {
    private int _NpcId;

    public SpawnMonster(int npcId)
    {
      this._NpcId = npcId;
    }

    public void run()
    {
      FourSepulchersManager.getInstance().spawnMonster(this._NpcId);
    }
  }

  private class SpawnNextMysteriousBox
    implements Runnable
  {
    private int _NpcId;

    public SpawnNextMysteriousBox(int npcId)
    {
      this._NpcId = npcId;
    }

    public void run()
    {
      FourSepulchersManager.getInstance().spawnMysteriousBox(this._NpcId);
    }
  }

  private class CloseNextDoor
    implements Runnable
  {
    final DoorTable _DoorTable = DoorTable.getInstance();
    private int _DoorId;

    public CloseNextDoor(int doorId)
    {
      this._DoorId = doorId;
    }

    public void run()
    {
      try
      {
        this._DoorTable.getDoor(Integer.valueOf(this._DoorId)).closeMe();
      }
      catch (Exception e)
      {
        L2SepulcherNpcInstance._log.warning(e.getMessage());
      }
    }
  }
}