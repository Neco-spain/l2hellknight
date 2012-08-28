package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class L2SepulcherNpcInstance extends L2NpcInstance
{
  protected static Map<Integer, Integer> _hallGateKeepers = new FastMap();
  protected Future<?> _closeTask = null;
  protected Future<?> _spawnNextMysteriousBoxTask = null;
  protected Future<?> _spawnMonsterTask = null;
  private static final String HTML_FILE_PATH = "data/html/SepulcherNpc/";
  private static final int HALLS_KEY = 7260;

  public L2SepulcherNpcInstance(int objectID, L2NpcTemplate template)
  {
    super(objectID, template);
    setShowSummonAnimation(true);

    if (_closeTask != null) {
      _closeTask.cancel(true);
    }
    if (_spawnNextMysteriousBoxTask != null) {
      _spawnNextMysteriousBoxTask.cancel(true);
    }
    if (_spawnMonsterTask != null) {
      _spawnMonsterTask.cancel(true);
    }
    _closeTask = null;
    _spawnNextMysteriousBoxTask = null;
    _spawnMonsterTask = null;
  }

  public void onSpawn()
  {
    super.onSpawn();
    setShowSummonAnimation(false);
  }

  public void deleteMe()
  {
    if (_closeTask != null) {
      _closeTask.cancel(true);
      _closeTask = null;
    }
    if (_spawnNextMysteriousBoxTask != null) {
      _spawnNextMysteriousBoxTask.cancel(true);
      _spawnNextMysteriousBoxTask = null;
    }
    if (_spawnMonsterTask != null) {
      _spawnMonsterTask.cancel(true);
      _spawnMonsterTask = null;
    }
    super.deleteMe();
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) {
      return;
    }

    if (this != player.getTarget()) {
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
          player.sendActionFailed();
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

      player.sendActionFailed();
    }
  }

  private void doAction(L2PcInstance player) {
    if (isDead()) {
      player.sendActionFailed();
      return;
    }

    switch (getNpcId()) {
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
      reduceNpcHp(getMaxHp() + 1, player);
      if (_spawnMonsterTask != null) {
        _spawnMonsterTask.cancel(true);
      }
      _spawnMonsterTask = ThreadPoolManager.getInstance().scheduleEffect(new SpawnMonster(getNpcId()), 3500L);
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
      reduceNpcHp(getMaxHp() + 1, player);
      if ((player.getParty() != null) && (!player.getParty().isLeader(player))) {
        player = player.getParty().getLeader();
      }
      player.addItem("Quest", 7260, 1, player, true);
      break;
    default:
      Quest[] qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
      if ((qlsa != null) && (qlsa.length > 0)) {
        player.setLastQuestNpcObject(getObjectId());
      }
      Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.NPC_FIRST_TALK);
      if ((qlst != null) && (qlst.length == 1))
        qlst[0].notifyFirstTalk(this, player);
      else {
        showChatWindow(player, 0);
      }
    }

    player.sendActionFailed();
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";
    if (val == 0)
      pom = "" + npcId;
    else {
      pom = npcId + "-" + val;
    }

    return "data/html/SepulcherNpc/" + pom + ".htm";
  }

  public void showChatWindow(L2PcInstance player, int val)
  {
    String filename = getHtmlPath(getNpcId(), val);
    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    player.sendPacket(html);
    player.sendActionFailed();
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (isBusy()) {
      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
      html.setFile("data/html/npcbusy.htm");
      html.replace("%busymessage%", getBusyMessage());
      html.replace("%npcname%", getName());
      html.replace("%playername%", player.getName());
      player.sendPacket(html);
    } else if (command.startsWith("Chat")) {
      int val = 0;
      try {
        val = Integer.parseInt(command.substring(5));
      } catch (IndexOutOfBoundsException ioobe) {
      } catch (NumberFormatException nfe) {
      }
      showChatWindow(player, val);
    } else if (command.startsWith("open_gate")) {
      L2ItemInstance hallsKey = player.getInventory().getItemByItemId(7260);
      if (hallsKey == null) {
        showHtmlFile(player, "Gatekeeper-no.htm");
      } else if (FourSepulchersManager.getInstance().isAttackTime()) {
        switch (getNpcId()) {
        case 31929:
        case 31934:
        case 31939:
        case 31944:
          FourSepulchersManager.getInstance().spawnShadow(getNpcId());
        }
        openNextDoor(getNpcId());
        if (player.getParty() != null) {
          for (L2PcInstance mem : player.getParty().getPartyMembers()) {
            if (mem.getInventory().getItemByItemId(7260) != null)
              mem.destroyItemByItemId("Quest", 7260, mem.getInventory().getItemByItemId(7260).getCount(), mem, true);
          }
        }
        else {
          player.destroyItemByItemId("Quest", 7260, hallsKey.getCount(), player, true);
        }
      }
    }
    else
    {
      super.onBypassFeedback(player, command);
    }
  }

  public void openNextDoor(int npcId) {
    int doorId = ((Integer)FourSepulchersManager.getInstance().getHallGateKeepers().get(Integer.valueOf(npcId))).intValue();
    DoorTable _doorTable = DoorTable.getInstance();
    _doorTable.getDoor(Integer.valueOf(doorId)).openMe();

    if (_closeTask != null) {
      _closeTask.cancel(true);
    }
    _closeTask = ThreadPoolManager.getInstance().scheduleEffect(new CloseNextDoor(doorId), 10000L);
    if (_spawnNextMysteriousBoxTask != null) {
      _spawnNextMysteriousBoxTask.cancel(true);
    }
    _spawnNextMysteriousBoxTask = ThreadPoolManager.getInstance().scheduleEffect(new SpawnNextMysteriousBox(npcId), 0L);
  }

  public void showHtmlFile(L2PcInstance player, String file)
  {
    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
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
      _NpcId = npcId;
    }

    public void run() {
      FourSepulchersManager.getInstance().spawnMonster(_NpcId);
    }
  }

  private class SpawnNextMysteriousBox
    implements Runnable
  {
    private int _NpcId;

    public SpawnNextMysteriousBox(int npcId)
    {
      _NpcId = npcId;
    }

    public void run() {
      FourSepulchersManager.getInstance().spawnMysteriousBox(_NpcId);
    }
  }

  private class CloseNextDoor
    implements Runnable
  {
    final DoorTable _DoorTable = DoorTable.getInstance();
    private int _DoorId;

    public CloseNextDoor(int doorId)
    {
      _DoorId = doorId;
    }

    public void run() {
      try {
        _DoorTable.getDoor(Integer.valueOf(_DoorId)).closeMe();
      } catch (Exception e) {
        L2SepulcherNpcInstance._log.warning(e.getMessage());
      }
    }
  }
}