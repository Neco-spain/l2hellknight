package l2m.gameserver.data.xml.holder;

import java.util.Collection;
import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.model.petition.PetitionMainGroup;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public class PetitionGroupHolder extends AbstractHolder
{
  private static PetitionGroupHolder _instance = new PetitionGroupHolder();

  private IntObjectMap<PetitionMainGroup> _petitionGroups = new HashIntObjectMap();

  public static PetitionGroupHolder getInstance()
  {
    return _instance;
  }

  public void addPetitionGroup(PetitionMainGroup g)
  {
    _petitionGroups.put(g.getId(), g);
  }

  public PetitionMainGroup getPetitionGroup(int val)
  {
    return (PetitionMainGroup)_petitionGroups.get(val);
  }

  public Collection<PetitionMainGroup> getPetitionGroups()
  {
    return _petitionGroups.values();
  }

  public int size()
  {
    return _petitionGroups.size();
  }

  public void clear()
  {
    _petitionGroups.clear();
  }
}