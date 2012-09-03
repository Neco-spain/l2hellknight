package l2rt.gameserver.xml;

import l2rt.Config;
import l2rt.extensions.scripts.Scripts;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.DocumentItem;
import l2rt.gameserver.templates.L2Armor;
import l2rt.gameserver.templates.L2EtcItem;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.xml.loader.XmlArmorLoader;
import l2rt.gameserver.xml.loader.XmlEtcItemLoader;
import l2rt.gameserver.xml.loader.XmlWeaponLoader;
import l2rt.util.GArray;
import l2rt.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author : Ragnarok
 * @date : 03.01.11    11:27
 */
public class ItemTemplates {
    private static ItemTemplates ourInstance;

    private Logger log = Logger.getLogger(ItemTemplates.class.getName());
    private ConcurrentHashMap<Integer, L2Item> allTemplates = new ConcurrentHashMap<Integer, L2Item>();
    private ArrayList<L2Armor> armors = new ArrayList<L2Armor>();
    private ArrayList<L2Weapon> weapons = new ArrayList<L2Weapon>();
    private ArrayList<L2EtcItem> etcItems = new ArrayList<L2EtcItem>();

    public static ItemTemplates getInstance() {
        if(ourInstance == null ) {
             ourInstance = new ItemTemplates();
        }
        return ourInstance;
    }

    private ItemTemplates() {
        load();
    }

    private void load() {
        armors.addAll(XmlArmorLoader.getInstance().getArmors().values());
        allTemplates.putAll(XmlArmorLoader.getInstance().getArmors());
        weapons.addAll(XmlWeaponLoader.getInstance().getWeapons().values());
        allTemplates.putAll(XmlWeaponLoader.getInstance().getWeapons());
        etcItems.addAll(XmlEtcItemLoader.getInstance().getEtcItems().values());
        allTemplates.putAll(XmlEtcItemLoader.getInstance().getEtcItems());

        // TODO: Перенести все эти статы в сами xml файлы соответствующих итемов
        new Thread(new Runnable(){
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {}
                for(File f : new File(Config.DATAPACK_ROOT + "/data/stats/items/").listFiles())
                    if(!f.isDirectory())
                        new DocumentItem(f);
            }
        }).start();
    }

    public L2Item getTemplate(int id) {
        if(allTemplates.get(id) == null) {
            log.warning("Not defined item_id=" + id + ", or out of range");
            Thread.dumpStack();
            return null;
		}
		return allTemplates.get(id);
	}
    public L2ItemInstance createItem(int itemId) {
		if(Config.DISABLE_CREATION_ID_LIST.contains(itemId)) {
			Log.displayStackTrace(new Throwable(), "Try creating DISABLE_CREATION item " + itemId);
			return null;
		}
		return new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
	}

    public Collection<L2Item> getAllTemplates() {
        return allTemplates.values();
    }

    public ArrayList<L2Armor> getAllArmors() {
        return armors;
    }

    public ArrayList<L2Weapon> getAllWeapons() {
        return weapons;
    }

    public ArrayList<L2EtcItem> getAllEtcItems() {
        return etcItems;
    }

    public static boolean useHandler(L2Playable self, L2ItemInstance item, Boolean ctrl) {
		L2Player player;
		if(self.isPlayer())
			player = (L2Player) self;
		else if(self.isPet())
			player = self.getPlayer();
		else
			return false;

		// Вызов всех определенных скриптовых итемхэндлеров
		GArray<Scripts.ScriptClassAndMethod> handlers = Scripts.itemHandlers.get(item.getItemId());
		if(handlers != null && handlers.size() > 0)
		{
			if(player.isInFlyingTransform())
			{
				player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
				return false;
			}

			Object[] script_args = new Object[] { player, ctrl };
			for(Scripts.ScriptClassAndMethod handler : handlers)
				player.callScripts(handler.scriptClass, handler.method, script_args);
			return true;
		}

		L2Skill[] skills = item.getItem().getAttachedSkills();
		if(skills != null && skills.length > 0)
		{
			for(L2Skill skill : skills)
			{
				L2Character aimingTarget = skill.getAimingTarget(player, player.getTarget());
				if(skill.checkCondition(player, aimingTarget, false, false, true))
					player.getAI().Cast(skill, aimingTarget, false, false);
			}
			return true;
		}
		return false;
	}
}
