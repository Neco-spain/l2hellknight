/*
 * 
 */
package l2.hellknight.gameserver.instancemanager;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.itemcontainer.PcInventory;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance.ItemLocation;
/**
 * 
 * @author UnAfraid
 *
 */
public class BonusExpManager
{
	private final Logger _log = Logger.getLogger(getClass().getName());
	private Map<Integer, BonusItem> _bonusItems = new FastMap<Integer, BonusItem>().shared();
	
	public BonusExpManager()
	{
		load();
	}
	
	public static final BonusExpManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private void load()
	{
		try
		{
			int itemId = 0;
			double bonusExp = 0;
			double bonusSp = 0;
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = new File(Config.DATAPACK_ROOT + "/data/BonusExpItems.xml");
			if (!file.exists())
			{
				_log.log(Level.INFO, "[" + getClass().getSimpleName() + "]Missing" + Config.DATAPACK_ROOT + "/data/BonusExpItems.xml Thescriptwontworkwithoutit!");
				return;
			}
			
			Document doc = factory.newDocumentBuilder().parse(file);
			Node first = doc.getFirstChild();
			if (first != null && "list".equalsIgnoreCase(first.getNodeName()))
			{
				for (Node n = first.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("bonus".equalsIgnoreCase(n.getNodeName()))
					{
						Node att;
						
						for (Node cd = n.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("item".equalsIgnoreCase(cd.getNodeName()))
							{
								att = cd.getAttributes().getNamedItem("id");
								if (att != null)
									itemId = Integer.parseInt(att.getNodeValue());
								else
								{
									_log.severe("[" + getClass().getSimpleName() + "] Missing Itemid, skipping");
									continue;
								}
								
								att = cd.getAttributes().getNamedItem("exp");
								if (att != null)
									bonusExp = Double.parseDouble(att.getNodeValue());
								else
								{
									_log.severe("[" + getClass().getSimpleName() + "] Missing exp, skipping");
									continue;
								}
								
								att = cd.getAttributes().getNamedItem("sp");
								if (att != null)
									bonusSp = Double.parseDouble(att.getNodeValue());
								else
								{
									_log.severe("[" + getClass().getSimpleName() + "] Missing sp, skipping");
									continue;
								}
								
								_bonusItems.put(itemId, new BonusItem(bonusExp, bonusSp));
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		_log.info("[" + getClass().getSimpleName() + "]:Loaded:" + _bonusItems.size() + "Items");
	}
	
	public long[] getBonusExpAndSp(L2PcInstance player, long exp, long sp)
	{
		double bonusExp = 0.;
		double bonusSp = 0.;
		BonusItem bonus;
		if (player != null)
		{
			PcInventory inv = player.getInventory();
			
			for (int itemId : _bonusItems.keySet())
			{
				L2ItemInstance item = inv.getItemByItemId(itemId);
				if (item != null && item.getLocation() == ItemLocation.INVENTORY)
				{
					bonus = _bonusItems.get(itemId);
					bonusExp += bonus.getBonusExpMultiplyer();
					bonusSp += bonus.getBonusSpMultiplyer();
				}
			}
			return new long[] { (long) (exp * bonusExp / 1000), (long) (sp * bonusSp / 1000) };
		}
		return new long[] { 0L, 0L };
	}
	
	private final class BonusItem
	{
		private final double _bonusExp;
		private final double _bonusSp;
		
		public BonusItem(double exp, double sp)
		{
			_bonusExp = exp;
			_bonusSp = sp;
		}
		
		public double getBonusExpMultiplyer()
		{
			return _bonusExp;
		}
		
		public double getBonusSpMultiplyer()
		{
			return _bonusSp;
		}
	}
	
	private static class SingletonHolder
	{
		protected static final BonusExpManager _instance = new BonusExpManager();
	}
}