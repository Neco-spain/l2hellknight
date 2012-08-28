package net.sf.l2j.gameserver.instancemanager;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.zone.type.L2ArenaZone;

public class ArenaManager
{
    private static ArenaManager _instance;
    public static final ArenaManager getInstance()
    {
        if (_instance == null)
        {
    		System.out.println("Initializing ArenaManager");
        	_instance = new ArenaManager();
        }
        return _instance;
    }
    private FastList<L2ArenaZone> _arenas;

    public ArenaManager()
    {
    }

    public void addArena(L2ArenaZone arena)
    {
    	if (_arenas == null)
    		_arenas = new FastList<L2ArenaZone>();

    	_arenas.add(arena);
    }

    public final L2ArenaZone getArena(L2Character character)
    {
    	for (L2ArenaZone temp : _arenas)
    		if (temp.isCharacterInZone(character)) return temp;

    	return null;
    }
}
