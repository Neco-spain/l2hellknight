package services.Auction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import javolution.text.TextBuilder;
import javolution.util.FastMap;
import l2rt.common.ThreadPoolManager;
import l2rt.config.ConfigSystem;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.database.mysql;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2EtcItem;
import l2rt.gameserver.templates.L2Item;
import l2rt.util.Files;

public class auction extends Functions implements ScriptFile
{

	public void givePay(int objId,int itemId,long count)
	{
		L2Player sender = L2ObjectsStorage.getPlayer(objId);
		if(sender != null) // цель в игре? отлично
			Functions.addItem(sender, itemId, count);
		else
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			ResultSet rs = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id = ? AND item_id = ? AND loc = 'INVENTORY' LIMIT 1"); // сперва пробуем найти в базе его адену в инвентаре
				statement.setInt(1, objId);
				statement.setInt(2, itemId);
				rs = statement.executeQuery();
				if(rs.next())
				{
					int id = rs.getInt("object_id");
					DatabaseUtils.closeStatement(statement);
					statement = con.prepareStatement("UPDATE items SET count=count+? WHERE object_id = ? LIMIT 1"); // если нашли увеличиваем ее количество
					statement.setLong(1, count);
					statement.setInt(2, id);
					statement.executeUpdate();
				}
				else
				{
					DatabaseUtils.closeStatement(statement);
					statement = con.prepareStatement("INSERT INTO items_delayed (owner_id,item_id,`count`,description) VALUES (?,?,?,'mail')"); // иначе используем items_delayed
					statement.setLong(1, objId);
					statement.setLong(2, itemId);
					statement.setLong(3, count);
					statement.executeUpdate();
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
			finally
			{
				DatabaseUtils.closeDatabaseCSR(con, statement, rs);
			}
		}
	}
	
	public void giveItemWiner(L2ItemInstance item,int objPlayer)
	{
		L2Player sender = L2ObjectsStorage.getPlayer(objPlayer);
		if(sender != null) // цель в игре? отлично
			sender.getInventory().addItem(item);
		else
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("REPLACE INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,shadow_life_time,name,class,flags) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?) ");
				statement.setInt(1, objPlayer);
				statement.setInt(2, item.getItemId());
				statement.setLong(3, 1);
				statement.setString(4, "INVENTORY");
				statement.setInt(5, item.getEquipSlot());
				statement.setInt(6, item.getEnchantLevel());
				statement.setInt(7, item.getObjectId());
				statement.setInt(8, 0);
				statement.setInt(9, 0);
				statement.setInt(10, -1);
				statement.setString(11, item.getName());
				statement.setString(12, item.getItemClass().name());
				statement.setInt(13, 0);
				statement.executeUpdate();

				DatabaseUtils.closeStatement(statement);
				statement = con.prepareStatement("REPLACE INTO item_attributes VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
				statement.setInt(1, item.getObjectId());
				if(item.getAugmentation() == null)
				{
					statement.setInt(2, -1);
					statement.setInt(3, -1);
					statement.setInt(4, -1);
				}
				else
				{
					statement.setInt(2, item.getAugmentationId());
					if(item.getAugmentation().getSkill() == null)
					{
						statement.setInt(3, 0);
						statement.setInt(4, 0);
					}
					else
					{
						statement.setInt(3, item.getAugmentation().getSkill().getId());
						statement.setInt(4, item.getAugmentation().getSkill().getLevel());
					}
				}
				statement.setByte(5, item.getAttackAttributeElement());
				statement.setInt(6, item.getAttackElementValue());
	            for(int i=0; i < item.getDeffAttr().length; i++)
	                statement.setInt(7+i, item.getDeffAttr()[i]);
				statement.executeUpdate();
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}
	
	public class FuncAuction implements Runnable
	{
		public int itemID = 0;
		
		@Override
		public void run()
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			ResultSet rs = null;
			try {
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("Id = " + itemID + ";");
				rs = statement.executeQuery();
				
				while (rs.next())
				{ 
					String clients = rs.getString("client");
					if (!clients.isEmpty())
					{
					int itemId = rs.getInt("IdItem");
					int price = rs.getInt("price");
					int priceId = rs.getInt("IdPrice");
					int owner = rs.getInt("char_id");
					int Winer = 0;
					//Участиков предмета заносим в маповую переменую
					StringTokenizer st = new StringTokenizer(clients, ";");
					FastMap<Integer,Long> TopPrice =  new FastMap <Integer,Long>().setShared(true);
					while (st.hasMoreTokens()) 
					{
						String client = st.nextToken();
						StringTokenizer kick = new StringTokenizer(client, ":");
						int objId = Integer.valueOf(kick.nextToken()).intValue();
						long priceKick = Long.valueOf(kick.nextToken()).longValue();
						TopPrice.put(objId, priceKick);
					}
					//Ищем победителя
					int winerprice = 0;
					for (Entry<Integer,Long> e : TopPrice.entrySet())
					{
						int objId = e.getKey();
						long pricefor = e.getValue();
				
						if (pricefor>winerprice)
							Winer = objId;
					}
					//Отдаем деньги проигравшим
					for (Entry<Integer,Long> e : TopPrice.entrySet())
					{
						int objId = e.getKey();
						long pricefor = e.getValue();
						if (objId!=Winer)
						{
							givePay(objId,priceId,pricefor);
						}
					}
			
					//Отдаем предмет победителю
					L2ItemInstance Lot = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
					Lot.setEnchantLevel(rs.getInt("Enchant"));
					int[] deffAttr = new int[]{0,0,0,0,0,0};
					for(int i=0; i<6; i++)
						deffAttr[i] = rs.getInt("att" + i);
					byte elemType = rs.getByte("elemType");
					int elemValue = rs.getInt("elemValue");
					Lot.setAttributeElement(elemType , elemValue, deffAttr, true);
					giveItemWiner(Lot,Winer);
			
					//Отдаем деньги продавцу
					givePay(owner,priceId,price);
					mysql.set("DELETE FROM auctionitem WHERE Item_Id = " + itemID + "");	
					}
					else
					{
						int itemId = rs.getInt("IdItem");
						int owner = rs.getInt("char_id");	
						//Отдаем предмет хозяину
						L2ItemInstance Lot = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
						Lot.setEnchantLevel(rs.getInt("Enchant"));
						int[] deffAttr = new int[]{0,0,0,0,0,0};
						for(int i=0; i<6; i++)
							deffAttr[i] = rs.getInt("att" + i);
						byte elemType = rs.getByte("elemType");
						int elemValue = rs.getInt("elemValue");
						Lot.setAttributeElement(elemType , elemValue, deffAttr, true);
						giveItemWiner(Lot,owner);
						mysql.set("DELETE FROM auctionitem WHERE Item_Id = " + itemID + "");	
					}
				}
				}
				catch (Exception e)
				 {
					e.printStackTrace();
			     }
				finally
			     {
					DatabaseUtils.closeDatabaseCSR(con, statement, rs);
			     }	
		}
		public FuncAuction(int itemID)
		{
			this.itemID = itemID;
		}
	}
	
	Thread startAuction = null;
	class StartEventTime extends Thread 
	{
		public void run()
		{while (startAuction != null)
		{
			try {
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			ResultSet rs = null;
			try {
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM auctionitem;");
			rs = statement.executeQuery();
			while (rs.next())
			{   
				long currentTime = System.currentTimeMillis() / 1000;
				long Time = rs.getLong("time");
				int ObjIdItem = rs.getInt("Item_Id");
				
				//проверка : если есть клиенты и время до окончания меньше часа
				if ((Time - currentTime)<3600)
				{
					long timer = (Time - currentTime) * 1000;
					if (timer<=0)
						timer = 100;
					ThreadPoolManager.getInstance().scheduleGeneral(new FuncAuction(ObjIdItem), timer);
				}
			}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				DatabaseUtils.closeDatabaseCSR(con, statement, rs);
			}	
			sleep(3600000);
		}catch(InterruptedException e){e.printStackTrace();}}}
	}
	
	public String DialogAppend_50010(Integer val)
	{
		if(val != 0)
			return "";
		String[] args = { "index" };
		return OutDia(args);
	}
	
	public static String[] rndColor = 
	{"161616","202020","242424","282828","323232"};
	
	public int returnCountItem()
	{
		int i = 0;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;	
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM auctionitem;");
			rs = statement.executeQuery();
			while (rs.next())
			{   
				i++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		return i;
			
	}
	
	public int getPriceStart(int itemobj)
	{
		int price = 0;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
		con = L2DatabaseFactory.getInstance().getConnection();
		statement = con.prepareStatement("SELECT * FROM auctionitem WHERE Item_Id = " + itemobj + ";");
		rs = statement.executeQuery();
		while (rs.next())
		{ 
			price = rs.getInt("Price");
		}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}	
		
		return price;
	}
	
	public String getClients(int itemobj)
	{
		String clients = "";
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
		con = L2DatabaseFactory.getInstance().getConnection();
		statement = con.prepareStatement("SELECT * FROM auctionitem WHERE Item_Id = " + itemobj + ";");
		rs = statement.executeQuery();
		while (rs.next())
		{ 
			clients = rs.getString("client");
		}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}	
		
		return clients;
	}
		
	
	public void addPriceItem(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		int ID = 0;
		long price = 0;
		int priceId = 0;
		try
		{
		ID = Integer.valueOf(args[0]);
		price = Integer.valueOf(args[1]);
		priceId = Integer.valueOf(args[2]);
		}
		catch (Exception e)
		{
			player.sendMessage("Некорректные данные");
			return;
		}
		if (player.getInventory().getCountOf(priceId)<price)
		{
			player.sendMessage("Недостаточно средств.");
			return;
		}
		String getClients = getClients(ID);
		if (price<=getPriceStart(ID))
		{
			player.sendMessage("Вы не привысили минимальную ставку");
			return;
		}
		long PriceMAX = 0;
		if (getClients.contains(player.getObjectId() + ":"))
		{
		StringTokenizer st = new StringTokenizer(getClients, ";");
		while (st.hasMoreTokens()) 
		{
			String client = st.nextToken();
			StringTokenizer kick = new StringTokenizer(client, ":");
			int objId = Integer.valueOf(kick.nextToken()).intValue();
			int priceKick = Integer.valueOf(kick.nextToken()).intValue();
			if ((priceKick)>PriceMAX)
				PriceMAX = priceKick;
			if (objId == player.getObjectId())
			{
				if ((priceKick + price)>PriceMAX)
					PriceMAX = priceKick  + price;
			String oldString = ("" + objId + ":" + priceKick + ";");
			String newString = ("" + objId + ":" + (priceKick + price) + ";");
			getClients = getClients.replaceAll(oldString,newString);
			}
		}
		}
		else
		{
			if (price>PriceMAX)
				PriceMAX = price;
			getClients += ("" + player.getObjectId() + ":" + price + ";");
		}

		mysql.set("UPDATE auctionitem SET client='" + getClients + "' WHERE Item_Id=" + ID + ";");
		mysql.set("UPDATE auctionitem SET price=" + PriceMAX + " WHERE Item_Id=" + ID + ";");
		removeItem(player, priceId, price);
	}
	
	public void showItem(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		int ID = Integer.valueOf(args[0]);
		String html = "";
		
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
		con = L2DatabaseFactory.getInstance().getConnection();
		statement = con.prepareStatement("SELECT * FROM auctionitem WHERE Item_Id = " + ID + ";");
		rs = statement.executeQuery();
		html+=("");
		html+=("");
		while (rs.next())
		{ 
			
		  L2ItemInstance Lot = new L2ItemInstance(IdFactory.getInstance().getNextId(), rs.getInt("IdItem"));
		  
		  //Востонавление заточки
		  Lot.setEnchantLevel(rs.getInt("Enchant"));
		  html+=("<table width=300 height=32\"><tr><td valing=top><img src=icon." + Lot.getItem().getIcon() + " width=32 height=32></td>");
		  html+=("<td width=140 valing=top><a action=\"bypass -h scripts_services.Auction.auction:showItem \"><font color=\"LEVEL\">" + Lot.getItem().getName() + " " + (Lot.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font></a><br1><font color=3293F3>Заточено на: +" + Lot.getEnchantLevel())) + "</font><br1></td></tr></table>");
	    	
		  //Востонавление атрибута
		  int[] deffAttr = new int[]{0,0,0,0,0,0};
		  for(int i=0; i<6; i++)
              deffAttr[i] = rs.getInt("att" + i);
		  byte elemType = rs.getByte("elemType");
		  int elemValue = rs.getInt("elemValue");
		  Lot.setAttributeElement(elemType , elemValue, deffAttr, true);
		  if (Lot.isWeapon())
	    	{
	    		//0 - Fire, 1 - Water, 2 - Wind, 3 - Earth, 4 - Holy, 5 - Dark, -2 - None
	    		int attEl = Lot.getAttackAttributeElement();
	    		int attValue = Lot.getAttackElementValue();
	    		 html+=("<br>Атрибут: ");
	    		if (attEl == 0)
	    			 html+=("<font color=\"B22222\">Огонь");
	    		else if (attEl == 1)
	    			 html+=("<font color=\"4876FF\">Вода");
	    		else if (attEl == 2)
	    			 html+=("<font color=\"E0EEEE\">Ветер");
	    		else if (attEl == 3)
	    			 html+=("<font color=\"8B4513\">Земля");
	    		else if (attEl == 4)
	    			 html+=("<font color=\"D3D3D3\">Святость");
	    		else if (attEl == 5)
	    			 html+=("<font color=\"551A8B\">Тьма");
	    		else
	    			 html+=("<font color=\"FFFFFF\">Нет");
	    		if (attEl!=-2)
	    			 html+=(" " + attValue + "<br></center></font>");	
	    	}
	    	else if (Lot.isArmor())
	    	{
	    		 html+=("<center>Атрибут</center>");
	    		int[] armorAtt =  Lot.getDeffAttr();
	    		 html+=("<table width=150><tr>");
	    		if (armorAtt[0]!=0)
	    		{
	    			 html+=("<td><font color=\"B22222\">Огонь " + armorAtt[0] + "<br></font></td>");	
	    		}
	    		if (armorAtt[1]!=0)
	    		{
	    			 html+=("<td><font color=\"4876FF\">Вода " + armorAtt[1] + "<br></font></td>");	
	        	}
	    		if (armorAtt[2]!=0)
	    		{
	    			 html+=("<td><font color=\"E0EEEE\">Ветер " + armorAtt[2] + "<br></font></td>");	
	        	}
	    		if (armorAtt[3]!=0)
	    		{
	    			 html+=("<td><font color=\"8B4513\">Земля " + armorAtt[3] + "<br></font></td>");	
	        	}
	    		if (armorAtt[4]!=0)
	    		{
	    			 html+=("<td><font color=\"D3D3D3\">Святость " + armorAtt[4] + "<br></font></td>");	
	        	}
	    		if (armorAtt[5]!=0)
	    		{
	    			 html+=("<td><font color=\"551A8B\">Тьма " + armorAtt[5] + "<br></font></td>");	
	        	}
	    		 html+=("</tr></table>");
	    	}

		  html+=("<br><br>");
		  int price  = rs.getInt("Price");
		  int priceId  = rs.getInt("IdPrice");
		  long currentTime = System.currentTimeMillis()/1000;
		  long time =  rs.getLong("time");
		  html+=("Цена: " + price + " <br>");
		  if(priceId == 57)
		  html+=("Вид валюты: Адена <br>");
		  else if(priceId == 21002)
		  html+=("Вид валюты: Донэйт Монета <br>");
		  else if(priceId == 21007)
		  html+=("Вид валюты: Специальная монета <br>");
		  else if(priceId == 21008)
		  html+=("Вид валюты: Редкий Кристалл <br>");
		  else if(priceId == 21000)
		  html+=("Вид валюты: Монета за Голосование <br>");
		  else
		  html+=("Вид валюты: Ивент Монета <br>");
		  html+=("Время до окончания аукциона: " + (( time - currentTime) / 60) + " минут<br><br>");
		  if (rs.getInt("char_id") != player.getObjectId())
		  {
			  html+= Files.read("data/scripts/services/Auction/showItem.htm");
			  html = html.replaceAll("%objItem%", "" + ID);
			  html = html.replaceAll("%IdPrice%", "" + rs.getInt("IdPrice"));
			  String getClients = getClients(ID);
			  StringTokenizer st = new StringTokenizer(getClients, ";");
			  int priceKick = 0;
			  while (st.hasMoreTokens()) 
			    {
					String client = st.nextToken();
					StringTokenizer kick = new StringTokenizer(client, ":");
					int objId = Integer.valueOf(kick.nextToken()).intValue();
					int priceTime = Integer.valueOf(kick.nextToken()).intValue();
					if (objId == player.getObjectId())
					{
						priceKick = priceTime;
					}
				}
			  html = html.replaceAll("%mySta%", "" + priceKick);
			  if (price == priceKick)
				  html+= ("<br><font color=\"LEVEL\">Вы являетесь фаворитом.</font>");
		  }
		}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}	
		show(html,player);
		
	}
	
	public void showAllItems(String[] args)
	{
		String html = "";
		String type = args[0];
		int page = Integer.valueOf(args[1]);
		String append = Files.read("data/scripts/services/Auction/allItem.htm");
		L2Player player = (L2Player) getSelf();
		L2NpcInstance lastNpc = player.getLastNpc();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		int i = 0;
		i = (page)*7-7;
		int len = (page)*7;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			
			if (args[0].equalsIgnoreCase("src") && args.length==3 && !args[2].equalsIgnoreCase("все"))
			{
				statement = con.prepareStatement("SELECT * FROM auctionitem;");
				rs = statement.executeQuery();
				String typeItem = args[2].toLowerCase();
				int count = 0;
				while (rs.next())
				{   
					L2ItemInstance Item = new L2ItemInstance(lastNpc.getObjectId(), rs.getInt("IdItem"));
					String typeNameItem = rs.getString("TypeItem");
					if (typeItem.equalsIgnoreCase(typeNameItem))
					{
					
					if (i<=count && len>count)
					{
					Item.setEnchantLevel(rs.getInt("Enchant"));
					html+= "<img src=L2UI.SquareWhite width=300 height=1><table width=300 height=40><tr><td valing=top><img src=icon." + Item.getItem().getIcon() + " width=32 height=32></td>";
					html+= ("<td width=268 valing=top><a action=\"bypass -h scripts_services.Auction.auction:showItem " + rs.getInt("Item_Id") + "\"><font color=\"LEVEL\">" + Item.getItem().getName() + " " + (Item.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font></a><br1><font color=3293F3>Заточено на: +" + Item.getEnchantLevel())) + "</font><br1></td></tr></table>");
					}
					count++;
					}
					
				}
				if (count==0)
					html+= ("<br><br>К сожалению, поиск по аукциону не дал никаких результатов.");
				int pages = Math.max(1, count / 7 + 1);
				html+=("<table><tr>");
				if (pages!=1)
		         for (int ii = 1; ii <= pages; ii++)
		         {
		         if (ii != page)
		        	 html+=("<td width=20 height=20><a action=\"bypass -h scripts_services.Auction.auction:showAllItems src " + ii + " " + args[2] + "\"><font color=F2C202>[" + ii + "]</font></a></td>");
		         else
		        	 html+=("<td width=10 height=20><font color=FFFFFF>" + ii + "</font></a></td>");
		         }
				html+=("</tr></table>");
			}
			else if (type.equalsIgnoreCase("all") || args.length==3)
			{
			statement = con.prepareStatement("SELECT * FROM auctionitem;");
			rs = statement.executeQuery();
			int count = 0;
			while (rs.next())
			{   
				
				if (i<=count && len>count)
				{
				L2ItemInstance Item = new L2ItemInstance(lastNpc.getObjectId(), rs.getInt("IdItem"));
				Item.setEnchantLevel(rs.getInt("Enchant"));
				html+= "<img src=L2UI.SquareWhite width=300 height=1><table width=300 height=40><tr><td valing=top><img src=icon." + Item.getItem().getIcon() + " width=32 height=32></td>";
				html+= ("<td width=268 valing=top><a action=\"bypass -h scripts_services.Auction.auction:showItem " + rs.getInt("Item_Id") + "\"><font color=\"LEVEL\">" + Item.getItem().getName() + " " + (Item.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font></a><br1><font color=3293F3>Заточено на: +" + Item.getEnchantLevel())) + "</font><br1></td></tr></table>");
				}
				count++;
				}
			if (count==0)
				html+= ("<br><br>К сожалению, поиск по аукциону не дал никаких результатов.");
			int pages = Math.max(1, count / 7 + 1);
			html+=("<table><tr>");
			if (pages!=1)
	         for (int ii = 1; ii <= pages; ii++)
	         {
	         if (ii != page)
	        	 html+=("<td width=20 height=20><a action=\"bypass -h scripts_services.Auction.auction:showAllItems all " + ii + "\"><font color=F2C202>[" + ii + "]</font></a></td>");
	         else
	        	 html+=("<td width=10 height=20><font color=FFFFFF>" + ii + "</font></a></td>");
	         }
			html+=("</tr></table>");
			}
			else if (args[0].equalsIgnoreCase("src") && args[3].equalsIgnoreCase("все"))
			{
				statement = con.prepareStatement("SELECT * FROM auctionitem;");
				rs = statement.executeQuery();
				String search = args[2].toLowerCase();
				int count = 0;
				while (rs.next())
				{   
					L2ItemInstance Item = new L2ItemInstance(lastNpc.getObjectId(), rs.getInt("IdItem"));
					String itemName = Item.getName().toLowerCase();
					if (itemName.startsWith(search))
					{
					
					if (i<=count && len>count)
					{
					Item.setEnchantLevel(rs.getInt("Enchant"));
					html+= "<img src=L2UI.SquareWhite width=300 height=1><table width=300 height=40><tr><td valing=top><img src=icon." + Item.getItem().getIcon() + " width=32 height=32></td>";
					html+= ("<td width=268 valing=top><a action=\"bypass -h scripts_services.Auction.auction:showItem " + rs.getInt("Item_Id") + "\"><font color=\"LEVEL\">" + Item.getItem().getName() + " " + (Item.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font></a><br1><font color=3293F3>Заточено на: +" + Item.getEnchantLevel())) + "</font><br1></td></tr></table>");
					}
					count++;
					}
					
				}
				if (count==0)
					html+= ("<br><br>К сожалению, поиск по аукциону не дал никаких результатов.");
				int pages = Math.max(1, count / 7 + 1);
				html+=("<table><tr>");
				if (pages!=1)
		         for (int ii = 1; ii <= pages; ii++)
		         {
		         if (ii != page)
		        	 html+=("<td width=20 height=20><a action=\"bypass -h scripts_services.Auction.auction:showAllItems src " + ii + " " + search + " " + args[3] + "\"><font color=F2C202>[" + ii + "]</font></a></td>");
		         else
		        	 html+=("<td width=10 height=20><font color=FFFFFF>" + ii + "</font></a></td>");
		         }
				html+=("</tr></table>");
			}
			else if (args[0].equalsIgnoreCase("src") && args.length == 4)
			{
				statement = con.prepareStatement("SELECT * FROM auctionitem;");
				rs = statement.executeQuery();
				String search = args[2].toLowerCase();
				String typeItem = args[3].toLowerCase();
				int count = 0;
				while (rs.next())
				{   
					L2ItemInstance Item = new L2ItemInstance(lastNpc.getObjectId(), rs.getInt("IdItem"));
					String itemName = Item.getName().toLowerCase();
					String typeNameItem = rs.getString("TypeItem");
					if (itemName.startsWith(search) && typeItem.equalsIgnoreCase(typeNameItem))
					{
					
					if (i<=count && len>count)
					{
					Item.setEnchantLevel(rs.getInt("Enchant"));
					html+= "<img src=L2UI.SquareWhite width=300 height=1><table width=300 height=40><tr><td valing=top><img src=icon." + Item.getItem().getIcon() + " width=32 height=32></td>";
					html+= ("<td width=268 valing=top><a action=\"bypass -h scripts_services.Auction.auction:showItem " + rs.getInt("Item_Id") + "\"><font color=\"LEVEL\">" + Item.getItem().getName() + " " + (Item.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font></a><br1><font color=3293F3>Заточено на: +" + Item.getEnchantLevel())) + "</font><br1></td></tr></table>");
					}
					count++;
					}
					
				}
				if (count==0)
					html+= ("<br><br>К сожалению, поиск по аукциону не дал никаких результатов.");
				int pages = Math.max(1, count / 7 + 1);
				 html+=("<table><tr>");
				if (pages!=1)
		         for (int ii = 1; ii <= pages; ii++)
		         {
		         if (ii != page)
		        	 html+=("<td width=20 height=20><a action=\"bypass -h scripts_services.Auction.auction:showAllItems src " + ii + " " + search + " " + args[3] + "\"><font color=F2C202>[" + ii + "]</font></a></td>");
		         else
		        	 html+=("<td width=10 height=20><font color=FFFFFF>" + ii + "</font></a></td>");
		         }
				html+=("</tr></table>");

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}	
		append = append.replaceFirst("%all%", html);
		show(append,player);
	}
	
	public void showMyLots(String args[])
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance lastNpc = player.getLastNpc();
		int page = Integer.valueOf(args[0]);
		String html = "";
		int i = 0;
		i = (page)*4-4;
		int len = (page)*4;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM auctionitem WHERE char_id=" + player.getObjectId());
			rs = statement.executeQuery();
			int count = 0;
			html+=("<center>Ваши предметы<br>");
			while (rs.next())
			{   
				
				if (i<=count && len>count)
				{
				L2ItemInstance Item = new L2ItemInstance(lastNpc.getObjectId(), rs.getInt("IdItem"));
				Item.setEnchantLevel(rs.getInt("Enchant"));
				html+= "<img src=L2UI.SquareWhite width=300 height=1><center><table width=200 height=40\"><tr><td valing=top><img src=icon." + Item.getItem().getIcon() + " width=32 height=32></td>";
				html+= ("<td width=200 valing=top><center><a action=\"bypass -h scripts_services.Auction.auction:showItem " + rs.getInt("Item_Id") + "\"><font color=\"LEVEL\">" + Item.getItem().getName() + " " + (Item.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font></a><br1><font color=3293F3>Заточено на: +" + Item.getEnchantLevel())) + "</font><br1></td></tr></table>");
				}
				count++;
			}
			int pages = Math.max(1, count / 4 + 1);
			html+= ("<table><tr>");
			if (pages!=1)
			for (int ii = 1; ii <= pages; ii++)
	         {
				if (ii != page)
					html+=("<td width=20 height=20><a action=\"bypass -h scripts_services.Auction.auction:showMyLots " + ii + "\"><font color=F2C202>[" + ii + "]</font></a></td>");
				else
					html+=("<td width=10 height=20><font color=FFFFFF>" + ii + "</font></a></td>");
	         }
			html+= ("</tr></table>");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		html+=("<center><table bgcolor=656565>");
		html+=("<tr><td><button action=\"bypass -h scripts_services.Auction.auction:addLot 1 0\" value=\"Выставить оружие\" width=180 height=30 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		html+=("<tr><td><button action=\"bypass -h scripts_services.Auction.auction:addLot 1 1\" value=\"Выставить броню\" width=180 height=30 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		html+=("<tr><td><button action=\"bypass -h scripts_services.Auction.auction:addLot 1 2\" value=\"Выставить бижутрию\" width=180 height=30 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		html+=("<tr><td><button action=\"bypass -h scripts_services.Auction.auction:addLot 1 3\" value=\"Выставить предметы бонуса\" width=180 height=30 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		html+=("</table>");
		show(html,player);
	}
	
	public void showMyTrade(String args[])
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance lastNpc = player.getLastNpc();
		int page = Integer.valueOf(args[0]);
		String html = "";
		int i = 0;
		i = (page)*4-4;
		int len = (page)*4;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM auctionitem;");
			rs = statement.executeQuery();
			int count = 0;
			html+=("<center>Предметы<br>");
			while (rs.next())
			{   
				String clients = rs.getString("client");
				StringTokenizer st = new StringTokenizer(clients, ";");
				while (st.hasMoreTokens()) 
				{
					String client = st.nextToken();
					StringTokenizer kick = new StringTokenizer(client, ":");
					int objId = Integer.valueOf(kick.nextToken()).intValue();
					if (player.getObjectId()==objId)
					{
						if (i<=count && len>count)
						{
						L2ItemInstance Item = new L2ItemInstance(lastNpc.getObjectId(), rs.getInt("IdItem"));
						Item.setEnchantLevel(rs.getInt("Enchant"));
						html+= "<img src=L2UI.SquareWhite width=300 height=1><center><table width=200 height=40\"><tr><td valing=top><img src=icon." + Item.getItem().getIcon() + " width=32 height=32></td>";
						html+= ("<td width=200 valing=top><center><a action=\"bypass -h scripts_services.Auction.auction:showItem " + rs.getInt("Item_Id") + "\"><font color=\"LEVEL\">" + Item.getItem().getName() + " " + (Item.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font></a><br1><font color=3293F3>Заточено на: +" + Item.getEnchantLevel())) + "</font><br1></td></tr></table>");
						}
						count++;	
					}
				}
				
			}
			if (count==0)
			{
				html+= ("<center><br>Вы не где не участвуете.");
				html+= ("<br><button value=\"Ознакомьтесь со списком лотов.\" action=\"bypass -h scripts_services.Auction.auction:showAllItems all 1\" width=180 height=30 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\">");
			}
			int pages = Math.max(1, count / 4 + 1);
			html+= ("<table><tr>");
			if (pages!=1)
			for (int ii = 1; ii <= pages; ii++)
	         {
	         if (ii != page)
	        	 html+=("<td width=20 height=20><a action=\"bypass -h scripts_services.Auction.auction:Trade " + ii + "\"><font color=F2C202>[" + ii + "]</font></a></td>");
	         else
	        	 html+=("<td width=10 height=20><font color=FFFFFF>" + ii + "</font></a></td>");
	         }
			html+= ("</tr></table>");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		show(html,player);
	}
	
	public L2ItemInstance[] returnAllWeapon (L2Player player)
	{
		L2ItemInstance[] weapon;
		ConcurrentLinkedQueue<L2ItemInstance> _items = new ConcurrentLinkedQueue<L2ItemInstance>();;
		L2ItemInstance[] arr = player.getInventory().getItems();
		int len = arr.length;
		for (int i = 0; i < len; i++)
        {
			L2ItemInstance _item = arr[i];
            L2ItemInstance EnchantItem = player.getInventory().getItemByObjectId(_item.getObjectId());  
            if (_item == null || _item.getItem() instanceof L2EtcItem || !EnchantItem.isWeapon() || _item.isEquipped() || _item.isAugmented() || EnchantItem.getItem().isCloak() || _item.isHeroWeapon() || _item.getItem().getCrystalType() == L2Item.Grade.NONE || _item.getItemId() >= 7816 && _item.getItemId() <= 7831 || _item.isShadowItem() || _item.isCommonItem() || _item.isWear() || _item.getEnchantLevel() >= (ConfigSystem.getInt("CBMaxEnchant") + 1))
            { 
            continue;
            }
            else
            _items.add(EnchantItem);
        }
		weapon = _items.toArray(new L2ItemInstance[_items.size()]);
        return weapon;
	}
	
	public L2ItemInstance[] returnAllAccessory (L2Player player)
	{
		L2ItemInstance[] Accessory;
		ConcurrentLinkedQueue<L2ItemInstance> _items = new ConcurrentLinkedQueue<L2ItemInstance>();;
		L2ItemInstance[] arr = player.getInventory().getItems();
		int len = arr.length;
		for (int i = 0; i < len; i++)
        {
			L2ItemInstance _item = arr[i];
            L2ItemInstance EnchantItem = player.getInventory().getItemByObjectId(_item.getObjectId());  
            if (_item == null || _item.getItem() instanceof L2EtcItem || !EnchantItem.getItem().isAccessory() || _item.isEquipped() ||  _item.isHeroWeapon() || _item.getItem().getCrystalType() == L2Item.Grade.NONE || _item.getItemId() >= 7816 && _item.getItemId() <= 7831 || _item.isShadowItem() || _item.isCommonItem() || _item.isWear() || _item.getEnchantLevel() >= (ConfigSystem.getInt("CBMaxEnchant") + 1))
            { 
            continue;
            }
            else
            _items.add(EnchantItem);
        }
		Accessory = _items.toArray(new L2ItemInstance[_items.size()]);
        return Accessory;
	}
	
	public L2ItemInstance[] returnAllBonus (L2Player player)
	{
		L2ItemInstance[] Accessory;
		ConcurrentLinkedQueue<L2ItemInstance> _items = new ConcurrentLinkedQueue<L2ItemInstance>();;
		L2ItemInstance[] arr = player.getInventory().getItems();
		int len = arr.length;
		for (int i = 0; i < len; i++)
        {
			L2ItemInstance _item = arr[i];
            L2ItemInstance EnchantItem = player.getInventory().getItemByObjectId(_item.getObjectId());
            if (_item == null)
            	continue;
            if (EnchantItem.getItem().isCloak())
            	 _items.add(EnchantItem);
            if (EnchantItem.getItem().isBelt())
            	 _items.add(EnchantItem);
            if (EnchantItem.getItem().isBracelet())
           	 _items.add(EnchantItem);
            if (EnchantItem.getItem().isArmor() && (EnchantItem.getBodyPart() == L2Item.SLOT_R_BRACELET || EnchantItem.getBodyPart() == L2Item.SLOT_L_BRACELET))
              	 _items.add(EnchantItem);
           
        }
		Accessory = _items.toArray(new L2ItemInstance[_items.size()]);
        return Accessory;
	}
	
	public L2ItemInstance[] returnAllArmor (L2Player player)
	{
		L2ItemInstance[] armor;
		ConcurrentLinkedQueue<L2ItemInstance> _items = new ConcurrentLinkedQueue<L2ItemInstance>();;
		L2ItemInstance[] arr = player.getInventory().getItems();
		int len = arr.length;
		for (int i = 0; i < len; i++)
        {
			L2ItemInstance _item = arr[i];
            L2ItemInstance EnchantItem = player.getInventory().getItemByObjectId(_item.getObjectId());  
            if (_item == null || _item.getItem() instanceof L2EtcItem || !EnchantItem.isArmor() || EnchantItem.getItem().isBracelet() || EnchantItem.getItem().isBelt() || EnchantItem.getItem().isCloak() || _item.isEquipped() ||  _item.isHeroWeapon() || _item.getItem().getCrystalType() == L2Item.Grade.NONE || _item.getItemId() >= 7816 && _item.getItemId() <= 7831 || _item.isShadowItem() || _item.isCommonItem() || _item.isWear() || _item.getEnchantLevel() >= (ConfigSystem.getInt("CBMaxEnchant") + 1))
            { 
            continue;
            }
            else
            _items.add(EnchantItem);
        }
		armor = _items.toArray(new L2ItemInstance[_items.size()]);
        return armor;
	}
	
	/*
	 * CREATE TABLE `auctionitem` (
  `char_id` int(11) NOT NULL DEFAULT '0',
  `Item_Id` int(11) NOT NULL DEFAULT '0',
  `TypeItem` varchar(20) DEFAULT NULL,
  `NameItem` varchar(45) DEFAULT NULL,
  `price` int(11) NOT NULL DEFAULT '0',
  `IdItem` int(6) NOT NULL DEFAULT '0',
  `time` bigint(16) NOT NULL DEFAULT '0',
  `elemValue` int(4) NOT NULL DEFAULT '0',
  `elemType` int(4) NOT NULL DEFAULT '0',
  `att0` int(4) NOT NULL DEFAULT '0',
  `att1` int(4) NOT NULL DEFAULT '0',
  `att2` int(4) NOT NULL DEFAULT '0',
  `att3` int(4) NOT NULL DEFAULT '0',
  `att4` int(4) NOT NULL DEFAULT '0',
  `att5` int(4) NOT NULL DEFAULT '0',
  `Enchant` int(6) NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
	 */
	
	public void addSQLAuction(String args[])
	{
		int objItem = Integer.valueOf(args[0]);
		int price = Integer.valueOf(args[1]);
		long Time = System.currentTimeMillis() / 1000;
		String timeSend = args[2];
		if (timeSend.startsWith("1"))
			Time+= 1 * 24 * 60 * 60;
		else if (timeSend.startsWith("3"))
			Time+= 3 * 24 * 60 * 60;
		else if (timeSend.startsWith("7"))
			Time+= 7 * 24 * 60 * 60;
		
		int priceId = 57;
		
		if (Integer.valueOf(args[4]) == 2)
			priceId = 21002;
		else if (Integer.valueOf(args[4]) == 3)
			priceId = 21007;
		else if (Integer.valueOf(args[4]) == 4)
			priceId = 21008;
		else if (Integer.valueOf(args[4]) == 5)
			priceId = 21000;
		else if (Integer.valueOf(args[4]) == 6)
			priceId = 21001;
			

			

		L2Player player = (L2Player) getSelf();
		L2ItemInstance Lot = player.getInventory().getItemByObjectId(objItem);
		if (Lot==null)
			return;
        int charId = player.getObjectId();
        String TypeItem = "Бонус";
        if (Lot.isWeapon())
        	TypeItem = "Оружие";
        else if (Lot.isArmor())
        	TypeItem = "Броня";
        else if (Lot.getItem().isAccessory())
        	TypeItem = "Бижутерия";
        String nameItem = Lot.getName();
        
        int idItem = Lot.getItemId();
        int attEl = Lot.getAttackAttributeElement();
		int attValue = Lot.getAttackElementValue();
		int Enchant = Lot.getEnchantLevel();
		int[] armorAtt =  Lot.getDeffAttr();
		if (Lot==null)
			return;

		
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO auctionitem (char_id,Item_Id,TypeItem,NameItem,price,IdItem,time,elemValue,elemType,att0,att1,att2,att3,att4,att5,Enchant,IdPrice,client,startPrice) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, charId);
			statement.setInt(2, objItem);
			statement.setString(3, TypeItem);
			statement.setString(4, nameItem);
			statement.setInt(5, price);
			statement.setInt(6, idItem);
			statement.setLong(7, Time);
			statement.setInt(8, attValue);
			statement.setInt(9, attEl);
			statement.setInt(10, armorAtt[0]);
			statement.setInt(11, armorAtt[1]);
			statement.setInt(12, armorAtt[2]);
			statement.setInt(13, armorAtt[3]);
			statement.setInt(14, armorAtt[4]);
			statement.setInt(15, armorAtt[5]);
			statement.setInt(16, Enchant);
			statement.setInt(17, priceId);
			statement.setString(18, "");
			statement.setInt(19, price);
			statement.executeUpdate();

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		player.getInventory().dropItem(Lot, 1, true);
		
	}
	
	public void addAuction(String args[])
	{
		L2Player player = (L2Player) getSelf();
		int objItem = Integer.valueOf(args[0]);
		
		TextBuilder sb = new TextBuilder();
		sb.append(Files.read("data/scripts/services/Auction/addLot.htm"));
		L2ItemInstance Lot = player.getInventory().getItemByObjectId(objItem);

		sb.append("<table width=300 height=32\"><tr><td valing=top><img src=icon." + Lot.getItem().getIcon() + " width=32 height=32></td>");
    	sb.append("<td width=140 valing=top><font color=\"LEVEL\">" + Lot.getItem().getName() + " " + (Lot.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font><br1><font color=3293F3>Заточено на: +" + Lot.getEnchantLevel())) + "</font><br1></td></tr></table>");
    	if (Lot.isWeapon())
    	{
    		//0 - Fire, 1 - Water, 2 - Wind, 3 - Earth, 4 - Holy, 5 - Dark, -2 - None
    		int attEl = Lot.getAttackAttributeElement();
    		int attValue = Lot.getAttackElementValue();
    		sb.append("<br>Атрибут: ");
    		if (attEl == 0)
    		sb.append("<font color=\"B22222\">Огонь");
    		else if (attEl == 1)
    		    sb.append("<font color=\"4876FF\">Вода");
    		else if (attEl == 2)
        		sb.append("<font color=\"E0EEEE\">Ветер");
    		else if (attEl == 3)
        		sb.append("<font color=\"8B4513\">Земля");
    		else if (attEl == 4)
        		sb.append("<font color=\"D3D3D3\">Святость");
    		else if (attEl == 5)
        		sb.append("<font color=\"551A8B\">Тьма");
    		else
    			sb.append("<font color=\"FFFFFF\">Нету");
    		if (attEl!=-2)
    		sb.append(" " + attValue + "<br></center></font>");	
    	}
    	else if (Lot.isArmor())
    	{
    		sb.append("<center>Атрибут</center>");
    		int[] armorAtt =  Lot.getDeffAttr();
    		sb.append("<table width=150><tr>");
    		if (armorAtt[0]!=0)
    		{
    			sb.append("<td><font color=\"B22222\">Огонь " + armorAtt[0] + "<br></td>");	
    		}
    		if (armorAtt[1]!=0)
    		{
    			 sb.append("<td><font color=\"4876FF\">Вода " + armorAtt[1] + "<br></td>");	
        	}
    		if (armorAtt[2]!=0)
    		{
    			sb.append("<td><font color=\"E0EEEE\">Ветер " + armorAtt[2] + "<br></td>");	
        	}
    		if (armorAtt[3]!=0)
    		{
    			sb.append("<td><font color=\"8B4513\">Земля " + armorAtt[3] + "<br></td>");	
        	}
    		if (armorAtt[4]!=0)
    		{
    			sb.append("<td><font color=\"D3D3D3\">Святость " + armorAtt[4] + "<br></td>");	
        	}
    		if (armorAtt[5]!=0)
    		{
    			sb.append("<td><font color=\"551A8B\">Тьма " + armorAtt[5] + "<br></td>");	
        	}
    		sb.append("</tr></table>");
    	}
		
		String html = sb.toString();
		html = html.replaceFirst("%objid%", "" + objItem);
		show(html,player);
	}
	
	public void addLot(String args[])
	{
		L2Player player = (L2Player) getSelf();
		int page = Integer.valueOf(args[0]);
		int type = Integer.valueOf(args[1]);
		String html = "";
		
		
		TextBuilder sb = new TextBuilder();
		L2ItemInstance[] arr = null;
		if (type == 0)
		arr = returnAllWeapon(player);
		else if(type == 1)
		arr = returnAllArmor(player);
		else if (type==2)
		arr = returnAllAccessory(player);
		else if (type==3)
		arr = returnAllBonus(player);

		int lenghtarray = arr.length;
		 int len = 0;
		 int i = 0;
		 i = (page)*8-8;
		 len = (page)*8;
        
		 for (; i < len; i++)
          {
       	   if (lenghtarray == i)
           	 break;
       	   L2ItemInstance _item = arr[i];
       	    sb.append("<table width=300 height=32\"><tr><td valing=top><img src=icon." + _item.getItem().getIcon() + " width=32 height=32></td>");
       	    sb.append("<td width=140 valing=top><font color=\"LEVEL\">" + _item.getItem().getName() + " " + (_item.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font><br1><font color=3293F3>Заточено на: +" + _item.getEnchantLevel())) + "</font><br1></td>");
       	    sb.append("<td width=128 valing=top><button action=\"bypass -h scripts_services.Auction.auction:addAuction " + _item.getObjectId() + "\" value=\"Выставить\" width=118 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
          }
		 if (lenghtarray==0)
		 {
			  sb.append("<br><br><center>У вас нету предмета данной категории."); 
		 }
		 int pages = Math.max(1, arr.length / 8 + 1);
		 sb.append("<table><tr>");
		 if (pages!=1)
			for (int ii = 1; ii <= pages; ii++)
	         {
	         if (ii != page)
	        	 sb.append("<td width=20 height=20><a action=\"bypass -h scripts_services.Auction.auction:addLot " + ii + " " + type + "\"><font color=F2C202>[" + ii + "]</font></a></td>");
	         else
	        	 sb.append("<td width=10 height=20><font color=FFFFFF>" + ii + "</font></a></td>");
	         }
			sb.append("</tr></table>");
        html += sb.toString();
		
		show(html,player);
	}
	
	public String OutDia(String[] args)
	{
		String html = "";
		L2Player player = (L2Player) getSelf();
		L2NpcInstance lastNpc = player.getLastNpc();
		String append = Files.read("data/scripts/services/Auction/" + args[0] + ".htm");
		html+=("");
		html+=("");
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		int i = -1;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM auctionitem ORDER BY time DESC LIMIT 5;");
			rs = statement.executeQuery();
			while (rs.next())
			{   i++;
				L2ItemInstance Item = new L2ItemInstance(lastNpc.getObjectId(), rs.getInt("IdItem"));
				Item.setEnchantLevel(rs.getInt("Enchant"));
				html+= "<img src=L2UI.SquareWhite width=300 height=1><table width=300 height=40 bgcolor=\"" + rndColor[i] + "\"><tr><td valing=top><img src=icon." + Item.getItem().getIcon() + " width=32 height=32></td>";
				html+= ("<td width=268 valing=top><a action=\"bypass -h scripts_services.Auction.auction:showItem " + rs.getInt("Item_Id") + "\"><font color=\"LEVEL\">" + Item.getItem().getName() + " " + (Item.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font></a><br1><font color=3293F3>Заточено на: +" + Item.getEnchantLevel())) + "</font><br1></td></tr></table>");
				if (i==4)
			    	html+="<img src=L2UI.SquareWhite width=300 height=1>";
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}	
		append = append.replaceFirst("%top%", html);
		return append;
	}
	
	public void showHtml(String[] args)
	{
		
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		String page = args[0];
		show(Files.read("data/scripts/services/Auction/" + page + ".htm", player), player, npc);
	}
	
	
	
	@Override
	public void onLoad() {
		if (startAuction==null)
		{
		startAuction =  new StartEventTime();
		startAuction.start();
		}
	}

	@Override
	public void onReload() {
		startAuction = null;
	}

	@Override
	public void onShutdown() {
		onReload();
	}
	
}