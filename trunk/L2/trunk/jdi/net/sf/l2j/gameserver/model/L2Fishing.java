package net.sf.l2j.gameserver.model;

import java.util.concurrent.Future;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PenaltyMonsterInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExFishingHpRegen;
import net.sf.l2j.gameserver.network.serverpackets.ExFishingStartCombat;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class L2Fishing
  implements Runnable
{
  private L2PcInstance _fisher;
  private int _time;
  private int _stop = 0;
  private int _goodUse = 0;
  private int _anim = 0;
  private int _mode = 0;
  private int _deceptiveMode = 0;
  private Future _fishAiTask;
  private boolean _thinking;
  private int _fishId;
  private int _fishMaxHp;
  private int _fishCurHp;
  private double _regenHp;
  private boolean _isUpperGrade;
  private int _lureType;

  public void run()
  {
    if (_fishCurHp >= _fishMaxHp * 2)
    {
      _fisher.sendPacket(new SystemMessage(SystemMessageId.BAIT_STOLEN_BY_FISH));
      doDie(false);
    }
    else if (_time <= 0)
    {
      _fisher.sendPacket(new SystemMessage(SystemMessageId.FISH_SPIT_THE_HOOK));
      doDie(false);
    } else {
      aiTask();
    }
  }

  public L2Fishing(L2PcInstance Fisher, FishData fish, boolean isNoob, boolean isUpperGrade)
  {
    _fisher = Fisher;
    _fishMaxHp = fish.getHP();
    _fishCurHp = _fishMaxHp;
    _regenHp = fish.getHpRegen();
    _fishId = fish.getId();
    _time = (fish.getCombatTime() / 1000);
    _isUpperGrade = isUpperGrade;
    if (isUpperGrade) {
      _deceptiveMode = (Rnd.get(100) >= 90 ? 1 : 0);
      _lureType = 2;
    }
    else {
      _deceptiveMode = 0;
      _lureType = (isNoob ? 0 : 1);
    }
    _mode = (Rnd.get(100) >= 80 ? 1 : 0);

    ExFishingStartCombat efsc = new ExFishingStartCombat(_fisher, _time, _fishMaxHp, _mode, _lureType, _deceptiveMode);
    _fisher.broadcastPacket(efsc);

    _fisher.sendPacket(new SystemMessage(SystemMessageId.GOT_A_BITE));

    if (_fishAiTask == null)
    {
      _fishAiTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(this, 1000L, 1000L);
    }
  }

  public void changeHp(int hp, int pen)
  {
    _fishCurHp -= hp;
    if (_fishCurHp < 0) _fishCurHp = 0;

    ExFishingHpRegen efhr = new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, _goodUse, _anim, pen, _deceptiveMode);
    _fisher.broadcastPacket(efhr);
    _anim = 0;
    if (_fishCurHp > _fishMaxHp * 2)
    {
      _fishCurHp = (_fishMaxHp * 2);
      doDie(false);
      return;
    }
    if (_fishCurHp == 0)
    {
      doDie(true);
      return;
    }
  }

  public synchronized void doDie(boolean win)
  {
    _fishAiTask = null;

    if (_fisher == null) return;

    if (win)
    {
      int check = Rnd.get(100);
      if (check <= 5)
      {
        PenaltyMonster();
      }
      else
      {
        _fisher.sendPacket(new SystemMessage(SystemMessageId.YOU_CAUGHT_SOMETHING));
        _fisher.addItem("Fishing", _fishId, 1, null, true);
      }
    }
    _fisher.EndFishing(win);
    _fisher = null;
  }

  protected void aiTask()
  {
    if (_thinking) return;
    _thinking = true;
    _time -= 1;
    try
    {
      if (_mode == 1) {
        if (_deceptiveMode == 0) {
          _fishCurHp += (int)_regenHp;
        }
      }
      else if (_deceptiveMode == 1) {
        _fishCurHp += (int)_regenHp;
      }
      if (_stop == 0) {
        _stop = 1;
        int check = Rnd.get(100);
        if (check >= 70) {
          _mode = (_mode == 0 ? 1 : 0);
        }
        if (_isUpperGrade) {
          check = Rnd.get(100);
          if (check >= 90)
            _deceptiveMode = (_deceptiveMode == 0 ? 1 : 0);
        }
      }
      else {
        _stop -= 1;
      }
    }
    finally
    {
      ExFishingHpRegen efhr;
      _thinking = false;
      ExFishingHpRegen efhr = new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, 0, _anim, 0, _deceptiveMode);
      if (_anim != 0)
        _fisher.broadcastPacket(efhr);
      else
        _fisher.sendPacket(efhr);
    }
  }

  public void useRealing(int dmg, int pen)
  {
    _anim = 2;
    if (Rnd.get(100) > 90) {
      _fisher.sendPacket(new SystemMessage(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN));
      _goodUse = 0;
      changeHp(0, pen);
      return;
    }
    if (_fisher == null) return;
    if (_mode == 1)
    {
      if (_deceptiveMode == 0)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
        sm.addNumber(dmg);
        _fisher.sendPacket(sm);
        if (pen == 50) {
          sm = new SystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
          sm.addNumber(pen);
          _fisher.sendPacket(sm);
        }
        _goodUse = 1;
        changeHp(dmg, pen);
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
        sm.addNumber(dmg);
        _fisher.sendPacket(sm);
        _goodUse = 2;
        changeHp(-dmg, pen);
      }

    }
    else if (_deceptiveMode == 0)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
      sm.addNumber(dmg);
      _fisher.sendPacket(sm);
      _goodUse = 2;
      changeHp(-dmg, pen);
    }
    else
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
      sm.addNumber(dmg);
      _fisher.sendPacket(sm);
      if (pen == 50) {
        sm = new SystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
        sm.addNumber(pen);
        _fisher.sendPacket(sm);
      }
      _goodUse = 1;
      changeHp(dmg, pen);
    }
  }

  public void usePomping(int dmg, int pen)
  {
    _anim = 1;
    if (Rnd.get(100) > 90) {
      _fisher.sendPacket(new SystemMessage(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN));
      _goodUse = 0;
      changeHp(0, pen);
      return;
    }
    if (_fisher == null) return;
    if (_mode == 0)
    {
      if (_deceptiveMode == 0)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
        sm.addNumber(dmg);
        _fisher.sendPacket(sm);
        if (pen == 50) {
          sm = new SystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
          sm.addNumber(pen);
          _fisher.sendPacket(sm);
        }
        _goodUse = 1;
        changeHp(dmg, pen);
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
        sm.addNumber(dmg);
        _fisher.sendPacket(sm);
        _goodUse = 2;
        changeHp(-dmg, pen);
      }

    }
    else if (_deceptiveMode == 0)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
      sm.addNumber(dmg);
      _fisher.sendPacket(sm);
      _goodUse = 2;
      changeHp(-dmg, pen);
    }
    else
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
      sm.addNumber(dmg);
      _fisher.sendPacket(sm);
      if (pen == 50) {
        sm = new SystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
        sm.addNumber(pen);
        _fisher.sendPacket(sm);
      }
      _goodUse = 1;
      changeHp(dmg, pen);
    }
  }

  private void PenaltyMonster()
  {
    int lvl = (int)Math.round(_fisher.getLevel() * 0.1D);

    _fisher.sendPacket(new SystemMessage(SystemMessageId.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK));
    int npcid;
    switch (lvl)
    {
    case 0:
    case 1:
      npcid = 18319;
      break;
    case 2:
      npcid = 18320;
      break;
    case 3:
      npcid = 18321;
      break;
    case 4:
      npcid = 18322;
      break;
    case 5:
      npcid = 18323;
      break;
    case 6:
      npcid = 18324;
      break;
    case 7:
      npcid = 18325;
      break;
    case 8:
      npcid = 18326;
      break;
    default:
      npcid = 18319;
    }

    L2NpcTemplate temp = NpcTable.getInstance().getTemplate(npcid);
    if (temp != null)
    {
      try
      {
        L2Spawn spawn = new L2Spawn(temp);
        spawn.setLocx(_fisher.GetFishx());
        spawn.setLocy(_fisher.GetFishy());
        spawn.setLocz(_fisher.GetFishz());
        spawn.setAmount(1);
        spawn.setHeading(_fisher.getHeading());
        spawn.stopRespawn();
        ((L2PenaltyMonsterInstance)spawn.doSpawn()).setPlayerToKill(_fisher);
      }
      catch (Exception e)
      {
      }
    }
  }
}