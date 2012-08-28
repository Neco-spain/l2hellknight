package net.sf.l2j.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.AugmentationData;
import net.sf.l2j.gameserver.datatables.AugmentationData.AugStat;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.funcs.FuncAdd;
import net.sf.l2j.gameserver.skills.funcs.LambdaConst;

public final class L2Augmentation
{
  private static final Logger _log = Logger.getLogger(L2Augmentation.class.getName());
  private L2ItemInstance _item;
  private int _effectsId = 0;
  private augmentationStatBoni _boni = null;
  private L2Skill _skill = null;

  public L2Augmentation(L2ItemInstance item, int effects, L2Skill skill, boolean save)
  {
    _item = item;
    _effectsId = effects;
    _boni = new augmentationStatBoni(_effectsId);
    _skill = skill;
    if (save) saveAugmentationData();
  }

  public L2Augmentation(L2ItemInstance item, int effects, int skill, int skillLevel, boolean save)
  {
    this(item, effects, SkillTable.getInstance().getInfo(skill, skillLevel), save);
  }

  private void saveAugmentationData()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("INSERT INTO augmentations (item_id,attributes,skill,level) VALUES (?,?,?,?)");
      statement.setInt(1, _item.getObjectId());
      statement.setInt(2, _effectsId);
      if (_skill != null)
      {
        statement.setInt(3, _skill.getId());
        statement.setInt(4, _skill.getLevel());
      }
      else {
        statement.setInt(3, 0);
        statement.setInt(4, 0);
      }

      statement.executeUpdate();
      statement.close();
    } catch (Exception e) {
      _log.log(Level.SEVERE, "Could not save augmentation for item: " + _item.getObjectId() + " from DB:", e); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public void deleteAugmentationData() {
    if (!_item.isAugmented()) return;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM augmentations WHERE item_id=?");
      statement.setInt(1, _item.getObjectId());
      statement.executeUpdate();
      statement.close();
    } catch (Exception e) {
      _log.log(Level.SEVERE, "Could not delete augmentation for item: " + _item.getObjectId() + " from DB:", e); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public int getAugmentationId() {
    return _effectsId;
  }

  public L2Skill getSkill()
  {
    return _skill;
  }

  public void applyBoni(L2PcInstance player)
  {
    _boni.applyBoni(player);

    if (_skill != null)
    {
      player.addSkill(_skill);
      player.sendSkillList();
    }
  }

  public void removeBoni(L2PcInstance player)
  {
    _boni.removeBoni(player);

    if (_skill != null)
    {
      player.removeSkill(_skill);
      player.sendSkillList();
    }
  }

  public class augmentationStatBoni
  {
    private Stats[] _stats;
    private float[] _values;
    private boolean _active;

    public augmentationStatBoni(int augmentationId)
    {
      _active = false;
      FastList as = AugmentationData.getInstance().getAugStatsById(augmentationId);

      _stats = new Stats[as.size()];
      _values = new float[as.size()];

      int i = 0;
      for (AugmentationData.AugStat aStat : as)
      {
        _stats[i] = aStat.getStat();
        _values[i] = aStat.getValue();
        i++;
      }
    }

    public void applyBoni(L2PcInstance player)
    {
      if (_active) return;

      for (int i = 0; i < _stats.length; i++) {
        player.addStatFunc(new FuncAdd(_stats[i], 64, this, new LambdaConst(_values[i])));
      }
      _active = true;
    }

    public void removeBoni(L2PcInstance player)
    {
      if (!_active) return;

      player.removeStatsOwner(this);

      _active = false;
    }
  }
}