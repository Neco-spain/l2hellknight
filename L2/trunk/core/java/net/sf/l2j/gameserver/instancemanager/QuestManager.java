package net.sf.l2j.gameserver.instancemanager;

import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.scripting.ScriptManager;

public class QuestManager extends ScriptManager<Quest>
{
    protected static final Logger _log = Logger.getLogger(QuestManager.class.getName());

    private static QuestManager _instance;
    public static final QuestManager getInstance()
    {
        if (_instance == null)
        {
    		System.out.println("Initializing QuestManager");
            _instance = new QuestManager();
        }
        return _instance;
    }
    private Map<String, Quest> _quests = new FastMap<String, Quest>();

    public QuestManager()
    {
    }

    public final boolean reload(String questFolder)
    {
    	Quest q = getQuest(questFolder);
    	if (q == null)
    	{
            return false;
    	}
    	return q.reload();
    }
    
    public final boolean reload(int questId)
    {
    	Quest q = this.getQuest(questId);
    	if (q == null)
    	{
    		return false;
    	}
    	return q.reload();
    }
    
    public final void report()
    {
        _log.info("Loaded: " + getQuests().size() + " quests");
    }
    
    public final void save()
    {
    	for (Quest q: getQuests().values())
        {
    		q.saveGlobalData();
        }
    }

    public final Quest getQuest(String name)
    {
		return getQuests().get(name);
    }

    public final Quest getQuest(int questId)
    {
    	for (Quest q: getQuests().values())
    	{
    		if (q.getQuestIntId() == questId)
    			return q;
    	}
    	return null;
    }
    

    public final void addQuest(Quest newQuest)
    {
        if (newQuest == null)
        {
            throw new IllegalArgumentException("Quest argument cannot be null");
        }
    	Quest old = this.getQuests().put(newQuest.getName(), newQuest);
        if (old != null)
        {
            _log.info("Replaced: ("+old.getName()+") with a new version ("+newQuest.getName()+")");
        }
    }
    
    public final boolean removeQuest(Quest q)
    {
        return this.getQuests().remove(q.getName()) != null;
    }
    
    public final FastMap<String, Quest> getQuests()
    {
        if (_quests == null) _quests = new FastMap<String, Quest>();
        return (FastMap<String, Quest>) _quests;
    }

    public Iterable<Quest> getAllManagedScripts()
    {
        return _quests.values();
    }

    public boolean unload(Quest ms)
    {
        ms.saveGlobalData();
        return this.removeQuest(ms);
    }

    @Override
    public String getScriptManagerName()
    {
        return "QuestManager";
    }
}
