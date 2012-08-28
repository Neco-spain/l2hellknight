package scripts.zone.type;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import scripts.zone.L2ZoneType;

public class L2SignetZone extends L2ZoneType
{
  private L2WorldRegion _region;
  private L2Character _owner;
  private final boolean _isOffensive;
  private final int _toRemoveOnOwnerExit;
  private final L2Skill _skill;

  public L2SignetZone(L2WorldRegion region, L2Character owner, boolean isOffensive, int toRemoveOnOwnerExit, L2Skill skill)
  {
    super(-1);
    _region = region;
    _owner = owner;
    _isOffensive = isOffensive;
    _toRemoveOnOwnerExit = toRemoveOnOwnerExit;
    _skill = skill;
  }

  protected void onEnter(L2Character character)
  {
    if (!_isOffensive)
      _skill.getEffects(_owner, character);
    else if ((character != _owner) && ((!character.isInZonePeace()) || (!_owner.isInZonePeace())))
      _skill.getEffects(_owner, character);
  }

  protected void onExit(L2Character character)
  {
    if ((character == _owner) && (_toRemoveOnOwnerExit > 0))
    {
      _owner.stopSkillEffects(_toRemoveOnOwnerExit);
      return;
    }

    character.stopSkillEffects(_skill.getId());
  }

  public void remove()
  {
    _region.removeZone(this);

    for (L2Character member : _characterList.values()) {
      member.stopSkillEffects(_skill.getId());
    }
    if (!_isOffensive)
      _owner.stopSkillEffects(_skill.getId());
  }

  protected void onDieInside(L2Character character)
  {
    if ((character == _owner) && (_toRemoveOnOwnerExit > 0))
      _owner.stopSkillEffects(_toRemoveOnOwnerExit);
    else
      character.stopSkillEffects(_skill.getId());
  }

  protected void onReviveInside(L2Character character)
  {
    if (!_isOffensive)
      _skill.getEffects(_owner, character);
    else if ((character != _owner) && ((!character.isInZonePeace()) || (!_owner.isInZonePeace())))
      _skill.getEffects(_owner, character);
  }

  public L2Character[] getCharactersInZone()
  {
    FastList charsInZone = new FastList();
    for (L2Character character : _characterList.values())
    {
      if (!_isOffensive)
        charsInZone.add(character);
      else if ((character != _owner) && ((!character.isInZonePeace()) || (!_owner.isInZonePeace())))
        charsInZone.add(character);
    }
    return (L2Character[])charsInZone.toArray(new L2Character[_characterList.size()]);
  }
}