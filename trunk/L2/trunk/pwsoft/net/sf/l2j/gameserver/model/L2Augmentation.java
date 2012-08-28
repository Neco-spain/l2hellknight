package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.AugmentationData;
import net.sf.l2j.gameserver.datatables.AugmentationData.AugStat;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.funcs.FuncAdd;
import net.sf.l2j.gameserver.skills.funcs.LambdaConst;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public final class L2Augmentation
{
  private static final Logger _log = Logger.getLogger(L2Augmentation.class.getName());
  private L2ItemInstance _item;
  private int _effectsId = 0;
  private AugmentationStatBoni _boni = null;
  private L2Skill _skill = null;

  public L2Augmentation(L2ItemInstance item, int effects, L2Skill skill, boolean save) {
    _item = item;
    _effectsId = effects;
    _boni = new AugmentationStatBoni(_effectsId);
    _skill = skill;

    if (save)
      saveAugmentationData();
  }

  public L2Augmentation(L2ItemInstance item, int effects, int skill, int skillLevel, boolean save)
  {
    this(item, effects, SkillTable.getInstance().getInfo(skill, skillLevel), save);
  }

  private void saveAugmentationData()
  {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("UPDATE `items` SET `aug_id`=?,`aug_skill`=?,`aug_lvl`=? WHERE `object_id`=?");
      statement.setInt(1, _effectsId);
      if (_skill != null) {
        statement.setInt(2, _skill.getId());
        statement.setInt(3, _skill.getLevel());
      } else {
        statement.setInt(2, -1);
        statement.setInt(3, -1);
      }
      statement.setInt(4, _item.getObjectId());

      statement.executeUpdate();
    } catch (Exception e) {
      _log.log(Level.SEVERE, "Could not save augmentation for item: " + _item.getObjectId() + " from DB:", e);
    } finally {
      Close.CS(con, statement);
    }
  }

  public void deleteAugmentationData() {
    if (!_item.isAugmented()) {
      return;
    }

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `items` SET `aug_id`=?,`aug_skill`=?,`aug_lvl`=? WHERE `object_id`=?");
      statement.setInt(1, -1);
      statement.setInt(2, -1);
      statement.setInt(3, -1);
      statement.setInt(4, _item.getObjectId());
      statement.executeUpdate();
      statement.close();
    } catch (Exception e) {
      _log.log(Level.SEVERE, "Could not delete augmentation for item: " + _item.getObjectId() + " from DB:", e);
    } finally {
      Close.CS(con, statement);
    }
  }

  public int getAugmentationId()
  {
    return _effectsId;
  }

  public L2Skill getSkill() {
    return _skill;
  }

  public L2Skill getAugmentSkill() {
    return _skill;
  }

  public void applyBoni(L2PcInstance player)
  {
    _boni.applyBoni(player);

    if (_skill != null) {
      player.addSkill(_skill, false);
      player.sendSkillList();
    }
  }

  public void removeBoni(L2PcInstance player)
  {
    _boni.removeBoni(player);

    if (_skill != null)
    {
      if ((_skill.isActive()) || (_skill.isChance())) {
        player.setActiveAug(_skill.getId());
        if (Config.ONE_AUGMENT) {
          player.setActiveAug(0);
          player.stopSkillEffects(_skill.getId());
        }
      }

      player.removeSkill(_skill, false);
      player.sendSkillList();
    }
  }

  public static class AugmentationStatBoni
  {
    private Stats[] _stats;
    private float[] _values;
    private boolean _active;

    public AugmentationStatBoni(int augmentationId)
    {
      _active = false;
      FastList as = AugmentationData.getInstance().getAugStatsById(augmentationId);

      _stats = new Stats[as.size()];
      _values = new float[as.size()];

      int i = 0;
      for (AugmentationData.AugStat aStat : as) {
        _stats[i] = aStat.getStat();
        _values[i] = aStat.getValue();
        i++;
      }
    }

    public void applyBoni(L2PcInstance player)
    {
      if (_active) {
        return;
      }

      for (int i = 0; i < _stats.length; i++) {
        player.addStatFunc(new FuncAdd(_stats[i], 64, this, new LambdaConst(_values[i])));
      }

      _active = true;
    }

    public void removeBoni(L2PcInstance player)
    {
      if (!_active) {
        return;
      }

      player.removeStatsOwner(this);

      _active = false;
    }
  }
}