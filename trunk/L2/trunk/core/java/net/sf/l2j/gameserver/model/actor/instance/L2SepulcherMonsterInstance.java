package net.sf.l2j.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2SepulcherMonsterInstance extends L2MonsterInstance
{
  public int mysteriousBoxId = 0;

  protected Future<?> _victimSpawnKeyBoxTask = null;
  protected Future<?> _victimShout = null;
  protected Future<?> _changeImmortalTask = null;
  protected Future<?> _onDeadEventTask = null;

  public L2SepulcherMonsterInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onSpawn()
  {
    super.onSpawn();
    switch (getNpcId())
    {
    case 18150:
    case 18151:
    case 18152:
    case 18153:
    case 18154:
    case 18155:
    case 18156:
    case 18157:
      if (_victimSpawnKeyBoxTask != null)
        _victimSpawnKeyBoxTask.cancel(true);
      _victimSpawnKeyBoxTask = ThreadPoolManager.getInstance().scheduleEffect(new VictimSpawnKeyBox(this), 300000L);
      if (_victimShout != null)
        _victimShout.cancel(true);
      _victimShout = ThreadPoolManager.getInstance().scheduleEffect(new VictimShout(this), 5000L);
      break;
    case 18196:
    case 18197:
    case 18198:
    case 18199:
    case 18200:
    case 18201:
    case 18202:
    case 18203:
    case 18204:
    case 18205:
    case 18206:
    case 18207:
    case 18208:
    case 18209:
    case 18210:
    case 18211:
      break;
    case 18231:
    case 18232:
    case 18233:
    case 18234:
    case 18235:
    case 18236:
    case 18237:
    case 18238:
    case 18239:
    case 18240:
    case 18241:
    case 18242:
    case 18243:
      if (_changeImmortalTask != null)
        _changeImmortalTask.cancel(true);
      _changeImmortalTask = ThreadPoolManager.getInstance().scheduleEffect(new ChangeImmortal(this), 1600L);

      break;
    case 18158:
    case 18159:
    case 18160:
    case 18161:
    case 18162:
    case 18163:
    case 18164:
    case 18165:
    case 18166:
    case 18167:
    case 18168:
    case 18169:
    case 18170:
    case 18171:
    case 18172:
    case 18173:
    case 18174:
    case 18175:
    case 18176:
    case 18177:
    case 18178:
    case 18179:
    case 18180:
    case 18181:
    case 18182:
    case 18183:
    case 18184:
    case 18185:
    case 18186:
    case 18187:
    case 18188:
    case 18189:
    case 18190:
    case 18191:
    case 18192:
    case 18193:
    case 18194:
    case 18195:
    case 18212:
    case 18213:
    case 18214:
    case 18215:
    case 18216:
    case 18217:
    case 18218:
    case 18219:
    case 18220:
    case 18221:
    case 18222:
    case 18223:
    case 18224:
    case 18225:
    case 18226:
    case 18227:
    case 18228:
    case 18229:
    case 18230:
    case 18244:
    case 18245:
    case 18246:
    case 18247:
    case 18248:
    case 18249:
    case 18250:
    case 18251:
    case 18252:
    case 18253:
    case 18254:
    case 18255:
    case 18256: }  } 
  public boolean doDie(L2Character killer) { if (!super.doDie(killer)) {
      return false;
    }
    switch (getNpcId())
    {
    case 18120:
    case 18121:
    case 18122:
    case 18123:
    case 18124:
    case 18125:
    case 18126:
    case 18127:
    case 18128:
    case 18129:
    case 18130:
    case 18131:
    case 18149:
    case 18158:
    case 18159:
    case 18160:
    case 18161:
    case 18162:
    case 18163:
    case 18164:
    case 18165:
    case 18183:
    case 18184:
    case 18212:
    case 18213:
    case 18214:
    case 18215:
    case 18216:
    case 18217:
    case 18218:
    case 18219:
      if (_onDeadEventTask != null)
        _onDeadEventTask.cancel(true);
      _onDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500L);
      break;
    case 18150:
    case 18151:
    case 18152:
    case 18153:
    case 18154:
    case 18155:
    case 18156:
    case 18157:
      if (_victimSpawnKeyBoxTask != null)
      {
        _victimSpawnKeyBoxTask.cancel(true);
        _victimSpawnKeyBoxTask = null;
      }
      if (_victimShout != null)
      {
        _victimShout.cancel(true);
        _victimShout = null;
      }
      if (_onDeadEventTask != null)
        _onDeadEventTask.cancel(true);
      _onDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500L);
      break;
    case 18141:
    case 18142:
    case 18143:
    case 18144:
    case 18145:
    case 18146:
    case 18147:
    case 18148:
      if (!FourSepulchersManager.getInstance().isViscountMobsAnnihilated(mysteriousBoxId))
        break;
      if (_onDeadEventTask != null)
        _onDeadEventTask.cancel(true);
      _onDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500L); break;
    case 18220:
    case 18221:
    case 18222:
    case 18223:
    case 18224:
    case 18225:
    case 18226:
    case 18227:
    case 18228:
    case 18229:
    case 18230:
    case 18231:
    case 18232:
    case 18233:
    case 18234:
    case 18235:
    case 18236:
    case 18237:
    case 18238:
    case 18239:
    case 18240:
      if (!FourSepulchersManager.getInstance().isDukeMobsAnnihilated(mysteriousBoxId))
        break;
      if (_onDeadEventTask != null)
        _onDeadEventTask.cancel(true);
      _onDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500L); break;
    case 25339:
    case 25342:
    case 25346:
    case 25349:
      giveCup(killer);
      if (_onDeadEventTask != null)
        _onDeadEventTask.cancel(true);
      _onDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 8500L);
    }

    return true;
  }

  public void deleteMe()
  {
    if (_victimSpawnKeyBoxTask != null)
    {
      _victimSpawnKeyBoxTask.cancel(true);
      _victimSpawnKeyBoxTask = null;
    }
    if (_onDeadEventTask != null)
    {
      _onDeadEventTask.cancel(true);
      _onDeadEventTask = null;
    }

    super.deleteMe();
  }

  public boolean isRaid()
  {
    switch (getNpcId())
    {
    case 25339:
    case 25342:
    case 25346:
    case 25349:
      return true;
    }
    return false;
  }

  private void giveCup(L2Character killer)
  {
    String questId = "q620_FourGoblets";
    int cupId = 0;
    int oldBrooch = 7262;

    switch (getNpcId())
    {
    case 25339:
      cupId = 7256;
      break;
    case 25342:
      cupId = 7257;
      break;
    case 25346:
      cupId = 7258;
      break;
    case 25349:
      cupId = 7259;
    }

    L2PcInstance player = null;
    if ((killer instanceof L2PcInstance))
      player = (L2PcInstance)killer;
    else if ((killer instanceof L2Summon)) {
      player = ((L2Summon)killer).getOwner();
    }
    if (player == null) {
      return;
    }
    if (player.getParty() != null)
    {
      for (L2PcInstance mem : player.getParty().getPartyMembers())
      {
        QuestState qs = mem.getQuestState(questId);
        if ((qs != null) && ((qs.isStarted()) || (qs.isCompleted())) && (mem.getInventory().getItemByItemId(oldBrooch) == null))
        {
          mem.addItem("Quest", cupId, 1, mem, true);
        }
      }
    }
    else
    {
      QuestState qs = player.getQuestState(questId);
      if ((qs != null) && ((qs.isStarted()) || (qs.isCompleted())) && (player.getInventory().getItemByItemId(oldBrooch) == null))
      {
        player.addItem("Quest", cupId, 1, player, true);
      }
    }
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return true;
  }

  private class ChangeImmortal
    implements Runnable
  {
    L2SepulcherMonsterInstance activeChar;

    public ChangeImmortal(L2SepulcherMonsterInstance mob)
    {
      activeChar = mob;
    }

    public void run()
    {
      L2Skill fp = SkillTable.getInstance().getInfo(4616, 1);
      fp.getEffects(activeChar, activeChar);
    }
  }

  private class OnDeadEvent
    implements Runnable
  {
    L2SepulcherMonsterInstance _activeChar;

    public OnDeadEvent(L2SepulcherMonsterInstance activeChar)
    {
      _activeChar = activeChar;
    }

    public void run()
    {
      switch (_activeChar.getNpcId())
      {
      case 18120:
      case 18121:
      case 18122:
      case 18123:
      case 18124:
      case 18125:
      case 18126:
      case 18127:
      case 18128:
      case 18129:
      case 18130:
      case 18131:
      case 18149:
      case 18158:
      case 18159:
      case 18160:
      case 18161:
      case 18162:
      case 18163:
      case 18164:
      case 18165:
      case 18183:
      case 18184:
      case 18212:
      case 18213:
      case 18214:
      case 18215:
      case 18216:
      case 18217:
      case 18218:
      case 18219:
        FourSepulchersManager.getInstance().spawnKeyBox(_activeChar);
        break;
      case 18150:
      case 18151:
      case 18152:
      case 18153:
      case 18154:
      case 18155:
      case 18156:
      case 18157:
        FourSepulchersManager.getInstance().spawnExecutionerOfHalisha(_activeChar);
        break;
      case 18141:
      case 18142:
      case 18143:
      case 18144:
      case 18145:
      case 18146:
      case 18147:
      case 18148:
        FourSepulchersManager.getInstance().spawnMonster(_activeChar.mysteriousBoxId);
        break;
      case 18220:
      case 18221:
      case 18222:
      case 18223:
      case 18224:
      case 18225:
      case 18226:
      case 18227:
      case 18228:
      case 18229:
      case 18230:
      case 18231:
      case 18232:
      case 18233:
      case 18234:
      case 18235:
      case 18236:
      case 18237:
      case 18238:
      case 18239:
      case 18240:
        FourSepulchersManager.getInstance().spawnArchonOfHalisha(_activeChar.mysteriousBoxId);
        break;
      case 25339:
      case 25342:
      case 25346:
      case 25349:
        FourSepulchersManager.getInstance().spawnEmperorsGraveNpc(_activeChar.mysteriousBoxId);
      }
    }
  }

  private class VictimSpawnKeyBox
    implements Runnable
  {
    private L2SepulcherMonsterInstance _activeChar;

    public VictimSpawnKeyBox(L2SepulcherMonsterInstance activeChar)
    {
      _activeChar = activeChar;
    }

    public void run()
    {
      if (_activeChar.isDead()) {
        return;
      }
      if (!_activeChar.isVisible()) {
        return;
      }
      FourSepulchersManager.getInstance().spawnKeyBox(_activeChar);
      broadcastPacket(new CreatureSay(_activeChar.getObjectId(), 0, _activeChar.getName(), "Ѕольшое спасибо за спасение."));
      if (_victimShout != null)
      {
        _victimShout.cancel(true);
      }
    }
  }

  private class VictimShout
    implements Runnable
  {
    private L2SepulcherMonsterInstance _activeChar;

    public VictimShout(L2SepulcherMonsterInstance activeChar)
    {
      _activeChar = activeChar;
    }

    public void run()
    {
      if (_activeChar.isDead()) {
        return;
      }
      if (!_activeChar.isVisible()) {
        return;
      }
      broadcastPacket(new CreatureSay(_activeChar.getObjectId(), 0, _activeChar.getName(), "прости мен€!!"));
    }
  }
}