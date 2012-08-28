package net.sf.l2j.gameserver.model.actor.instance;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2EffectPointInstance extends L2NpcInstance
{
  private L2Character _owner;
  private L2PcInstance _player;
  private int _effectId;
  private int _skillId;
  private long _delay;
  private int _count = 0;
  private int _maxCount;

  public L2EffectPointInstance(int objectId, L2NpcTemplate template, L2Character owner)
  {
    super(objectId, template);
    _owner = owner;
  }

  public L2EffectPointInstance(int objectId, L2NpcTemplate template, L2PcInstance player, int effId, int skillId) {
    super(objectId, template);
    _player = player;
    _effectId = effId;
    _skillId = skillId;
    _maxCount = getCount(skillId);
    _delay = getDelay(skillId);
  }

  public void deleteMe()
  {
    super.deleteMe();
    FastList players = getKnownList().getKnownCharactersInRadius(1200);
    if ((players == null) || (players.isEmpty())) {
      return;
    }
    L2Character pc = null;
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2Character)n.getValue();
      if ((pc == null) || (pc.isL2Door())) {
        continue;
      }
      pc.stopSkillEffects(_effectId);
    }
    players.clear();
    players = null;
    pc = null;
  }

  public void onSpawn()
  {
    ThreadPoolManager.getInstance().scheduleAi(new EffectCycle(), 1000L, false);
  }

  private int getDelay(int sklId) {
    switch (sklId) {
    case 454:
    case 456:
    case 457:
    case 458:
    case 459:
    case 460:
      return 5000;
    case 455:
    }return 2000;
  }

  private int getCount(int sklId)
  {
    switch (sklId) {
    case 454:
    case 456:
    case 457:
    case 458:
    case 459:
    case 460:
      return 24;
    case 455:
    }return 15;
  }

  public void onAction(L2PcInstance player)
  {
    player.sendActionFailed();
  }

  public void onActionShift(L2GameClient client)
  {
    L2PcInstance player = client.getActiveChar();
    if (player == null) {
      return;
    }
    player.sendActionFailed();
  }

  public L2PcInstance getPlayer()
  {
    return _player;
  }

  private class EffectCycle
    implements Runnable
  {
    public EffectCycle()
    {
    }

    public void run()
    {
      L2EffectPointInstance.access$008(L2EffectPointInstance.this);
      if (_count > _maxCount) {
        decayMe();
        deleteMe();
        return;
      }

      FastList players = getKnownList().getKnownCharactersInRadius(180);
      if ((players == null) || (players.isEmpty())) {
        if (_delay == 5000L) {
          decayMe();
          deleteMe();
        } else {
          ThreadPoolManager.getInstance().scheduleAi(new EffectCycle(L2EffectPointInstance.this), _delay, false);
        }
        return;
      }

      if ((_delay == 5000L) && (!players.contains(_player))) {
        decayMe();
        deleteMe();
        return;
      }

      L2Skill skl = SkillTable.getInstance().getInfo(_effectId, 1);
      L2Character pc = null;
      FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
        pc = (L2Character)n.getValue();
        if ((pc == null) || (pc.isL2Door()))
        {
          continue;
        }
        if ((_delay != 5000L) && 
          (pc.isPlayer())) {
          L2PcInstance pl = (L2PcInstance)pc;
          if ((pl == _player) || (
            (_player.getParty() != null) && (_player.getParty().getPartyMembers().contains(pl))))
          {
            continue;
          }

        }

        pc.stopSkillEffects(_effectId);
        if ((_delay == 5000L) || (_effectId == 5145) || (_effectId == 5134) || (_effectId == 5124)) {
          skl.getEffects(pc, pc);
        } else {
          int damage = (int)Formulas.calcMagicDam(_player, pc, skl, false, true, false);
          pc.reduceCurrentHp(damage, _player);
        }

        pc.broadcastPacket(new MagicSkillUser(pc, pc, _effectId, 1, 0, 0));
      }
      players.clear();
      players = null;
      pc = null;
      ThreadPoolManager.getInstance().scheduleAi(new EffectCycle(L2EffectPointInstance.this), _delay, false);
    }
  }
}