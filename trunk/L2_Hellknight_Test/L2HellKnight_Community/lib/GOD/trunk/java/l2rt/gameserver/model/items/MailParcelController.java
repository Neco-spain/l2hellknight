package l2rt.gameserver.model.items;

import l2rt.common.ThreadPoolManager;
import l2rt.database.*;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.base.Transaction.TransactionType;
import l2rt.gameserver.model.items.L2ItemInstance.ItemLocation;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.util.GArray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Это общий контейнер для методов работы с почтой.
 */
public class MailParcelController
{
	private static ReentrantLock lock = new ReentrantLock(); // вся работа с базой ведется в один поток, во избежание
	private static final ReentrantLock sendLock = new ReentrantLock();
	private static MailParcelController _instance;

	private HashMap<Integer, Letter> lettersByIdCache;
	private HashMap<Integer, GArray<Letter>> lettersByReceiverCache;
	private HashMap<Integer, GArray<Letter>> lettersBySenderCache;
	@SuppressWarnings("unused")
	private RunnableScheduledFuture<TimeoutChecker> _scheduled;

	public static MailParcelController getInstance()
	{
		if(_instance == null)
			_instance = new MailParcelController();
		return _instance;
	}

	@SuppressWarnings("unchecked")
	private MailParcelController()
	{
		cleanupBD();
		loadCache();
		_scheduled = (RunnableScheduledFuture<TimeoutChecker>) ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TimeoutChecker(), 60000, 60000);
	}

	private class TimeoutChecker implements Runnable
	{
		@Override
		public void run()
		{
			lock.lock();
			try
			{
				GArray<Letter> toRemove = new GArray<Letter>();
				for(Letter letter : lettersByIdCache.values())
					if(isExpired(letter))
						toRemove.add(letter);

				for(Letter letter : toRemove)
					if(letter.attachments > 0)
						returnLetter(letter.id);
					else
						deleteLetter(letter.id);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				lock.unlock();
			}
		}
	}

	public boolean isExpired(Letter letter)
	{
		return letter.validtime < System.currentTimeMillis() / 1000;
	}

	/**
	 * Помечает сообщение прочитанным.
	 */
	public void markMailRead(int mailId)
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("UPDATE mail SET mail.unread=0 WHERE mail.messageId=" + mailId + " LIMIT 1");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void sendLetter(Letter letter)
	{
		sendLetter(letter, null, null, null);
	}

	/**
	 * Отправляет письмо. attachments может быть null. sender может быть null если attachments == null или пуст.
	 * 
	 * Для письма обязательно должны быть определены поля validtime (unixtime просрочки), topic и body. Остальные поля можно опустить, если установить флаг system.
	 */
	public void sendLetter(Letter letter, int[] attachments, long[] attItemsQ, L2Player sender)
	{
		if(letter == null) 
		{
		return;
		}
	
		if(letter != null) 
		{
		return;
		}
		
		ThreadConnection con = null;
		FiltredPreparedStatement stmnt = null;
		ResultSet rs = null;
		try
		{
			sendLock.lock();
			con = L2DatabaseFactory.getInstance().getConnection();
			stmnt = con.prepareStatement("INSERT INTO `mail` (`sender`, `receiver`, `topic`, `body`, `attachments`, `price`, `expire`, `system`) VALUES (?,?,?,?,?,?,FROM_UNIXTIME(?),?)");
			stmnt.setInt(1, letter.senderId);
			stmnt.setInt(2, letter.receiverId);
			stmnt.setString(3, letter.topic);
			stmnt.setString(4, letter.body);
			stmnt.setInt(5, attachments == null ? 0 : attachments.length);
			stmnt.setLong(6, letter.price);
			stmnt.setLong(7, letter.validtime);
			stmnt.setInt(8, letter.system);
			stmnt.executeUpdate();

			DatabaseUtils.closeStatement(stmnt);
			stmnt = con.prepareStatement("SELECT LAST_INSERT_ID()");
			rs = stmnt.executeQuery();

			if(rs.next())
				letter.id = rs.getInt(1);

			if(letter.id == 0) // письмо не добавилось в базу?
				return;

			// аттачи для письма
			if(attachments != null && attachments.length > 0)
			{
				L2ItemInstance[] att = new L2ItemInstance[attachments.length];
				for(int i = 0; i < attachments.length; i++)
					att[i] = sender.getInventory().dropItem(attachments[i], attItemsQ[i], false);
				attach(letter, att);
			}
			MailParcelController.getInstance().cache(letter);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			sendLock.unlock();
			DatabaseUtils.closeDatabaseCSR(con, stmnt, rs);
		}
	}

	public void attach(Letter letter, L2ItemInstance... items)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement stmnt = null;
		try
		{
			sendLock.lock();
			if(items != null && items.length > 0)
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				stmnt = con.prepareStatement("INSERT INTO `mail_attachments` (`messageId`, `itemId`) VALUES (?,?)");

				letter.attached = new GArray<TradeItem>(items.length);
				for(int i = 0; i < items.length; i++)
				{
					L2ItemInstance item = items[i];
					item.setOwnerId(letter.senderId);
					item.setLocation(ItemLocation.LEASE);
					item.updateDatabase(true, false);
					letter.attached.add(new TradeItem(item));
					stmnt.setInt(1, letter.id);
					stmnt.setInt(2, item.getObjectId());
					stmnt.executeUpdate();
				}
				letter.attachments = letter.attached.size();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			sendLock.unlock();
			DatabaseUtils.closeDatabaseCS(con, stmnt);
		}
	}

	/**
	 * Возвращает письмо из кеша.
	 */
	public Letter getLetter(int mailId)
	{
		return lettersByIdCache.get(mailId);
	}

	/**
	 * Возвращает из кеша все входящие для чара.
	 */
	public GArray<Letter> getReceived(int charId)
	{
		return lettersByReceiverCache.get(charId);
	}

	/**
	 * Возвращает из кеша все исходящие для чара.
	 */
	public GArray<Letter> getSent(int charId)
	{
		return lettersBySenderCache.get(charId);
	}

	/**
	 * Вовзращает список прикрепленных вещей.
	 */
	public GArray<L2ItemInstance> listAttachedItems(int mailId)
	{
		GArray<L2ItemInstance> ret = new GArray<L2ItemInstance>(8);
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		ThreadConnection con = null;
		lock.lock();
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT itemId FROM mail_attachments WHERE messageId = ?");
			statement.setInt(1, mailId);
			rs = statement.executeQuery();
			while(rs.next())
				ret.add(L2ItemInstance.restoreFromDb(rs.getInt("itemId"), false));
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			lock.unlock();
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		return ret;
	}

	/**
	 * Возвращает письмо с приложениями написавшему. Вызывается при нажатии кнопки возврата написавшим, при отказе от письма адресатом либо по таймауту. Вещи попадают на склад.
	 */
	public void returnLetter(int mailId)
	{	
		lock.lock();
		try
		{
			Letter letter = MailParcelController.getInstance().getLetter(mailId);

			if(letter == null)
				return;
				
			if(letter != null)
				return;

			if(letter.attachments == 0)
			{
				MailParcelController.getInstance().deleteLetter(mailId);
				return;
			}

			GArray<L2ItemInstance> templist = listAttachedItems(mailId);
			if(templist.isEmpty())
			{
				MailParcelController.getInstance().deleteLetter(mailId);
				return;
			}

			for(L2ItemInstance item : templist)
				if(item.getLocation() == ItemLocation.LEASE)
					returnItem(item);

			MailParcelController.getInstance().deleteLetter(mailId);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Удаляет письмо. Перед этим следует вернуть приложенные вещи.
	 */
	public void deleteLetter(int... mailIds)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement stmnt = null;
		lock.lock();
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			stmnt = con.prepareStatement("DELETE FROM mail WHERE messageId=? LIMIT 1");
			GArray<Letter> removed = new GArray<Letter>(mailIds.length);
			for(int id : mailIds)
			{
				Letter letter = lettersByIdCache.remove(id);
				if(letter == null)
					continue;

				GArray<Letter> cached = lettersBySenderCache.get(letter.senderId);
				if(cached != null)
				{
					cached.remove(letter);
					if(cached.isEmpty())
						lettersBySenderCache.remove(letter.senderId);
				}
				cached = lettersByReceiverCache.get(letter.receiverId);
				if(cached != null)
				{
					cached.remove(letter);
					if(cached.isEmpty())
						lettersByReceiverCache.remove(letter.receiverId);
				}

				removed.add(letter);
				stmnt.setInt(1, id);
				stmnt.executeUpdate();
			}
			DatabaseUtils.closeStatement(stmnt);
			stmnt = con.prepareStatement("DELETE FROM mail_attachments WHERE messageId=? LIMIT ?");
			for(Letter letter : removed)
			{
				stmnt.setInt(1, letter.id);
				stmnt.setInt(2, letter.attachments);
				stmnt.executeUpdate();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stmnt);
			lock.unlock();
		}
	}

	/**
	 * Игрок забирает предметы из приложений, платит деньги если письмо платное и удаляет после этого письмо. Подразумевается что игрок онлайн.
	 */
	public void receivePost(int postId, L2Player cha)
	{
	
		if(cha.isInStoreMode())
		{
			cha.sendPacket(Msg.YOU_CANNOT_RECEIVE_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
			return;
		}

		if(cha.isInTransaction() && cha.getTransaction().isTypeOf(TransactionType.TRADE))
		{
			cha.sendPacket(Msg.YOU_CANNOT_RECEIVE_DURING_AN_EXCHANGE);
			return;
		}

		if(!cha.isInPeaceZone())
		{
			cha.sendPacket(Msg.YOU_CANNOT_RECEIVE_IN_A_NON_PEACE_ZONE_LOCATION);
			return;
		}

		if(cha.getEnchantScroll() != null)
		{
			cha.sendPacket(Msg.YOU_CANNOT_RECEIVE_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
			return;
		}

		Letter letter = MailParcelController.getInstance().getLetter(postId);

		if(letter == null)
			return;
			
		if(letter != null)
			return;

		try
		{
			lock.lock();
			if(cha.getAdena() < letter.price)
			{
				cha.sendPacket(Msg.YOU_CANNOT_RECEIVE_BECAUSE_YOU_DON_T_HAVE_ENOUGH_ADENA);
				return;
			}

			if(letter.attached.isEmpty()) // приложений нет?
				return;

			Inventory inv = cha.getInventory();

			// проверяем слоты
			int slots = inv.getSize();
			for(TradeItem item : letter.attached)
				if(!(item.getItem().isStackable() && inv.getItemByItemId(item.getItemId()) != null))
					slots++;

			if(cha.getInventoryLimit() < slots)
			{
				cha.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
				return;
			}

			// проверяем вес
			long weight = 0;
			for(TradeItem item : letter.attached)
				weight += item.getItem().getWeight() * item.getCount();
			if(inv.getTotalWeight() + weight > cha.getMaxLoad())
			{
				cha.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}

			if(letter.price > 0)
			{
				cha.reduceAdena(letter.price, true);
				givePayPrice(letter.senderId, 57, letter.price);
			}

			for(TradeItem item : letter.attached)
			{
				L2ItemInstance TransferItem = L2ItemInstance.restoreFromDb(item.getObjectId(), true);
				if(TransferItem != null && TransferItem.getLocation() == ItemLocation.LEASE)
				{
					cha.sendPacket(SystemMessage.obtainItems(TransferItem));
					cha.getInventory().addItem(TransferItem).updateDatabase(true, false);
				}
			}

			MailParcelController.getInstance().deleteLetter(postId);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Дает игроку адену. Сперва пытаемся найти его в игре, потом добавить через items, в крайнем случае использум items_delayed.
	 */
	public static void givePayPrice(int player, int item, long count)
	{
		L2Player sender = L2ObjectsStorage.getPlayer(player);
		if(sender != null) // цель в игре? отлично
		{
			Functions.addItem(sender, item, count);
			sender.sendPacket(new SystemMessage(SystemMessage.S1_ACQUIRED_THE_ATTACHED_ITEM_TO_YOUR_MAIL));
		}
		else
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			ResultSet rs = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id = ? AND item_id = ? AND loc = 'INVENTORY' LIMIT 1"); // сперва пробуем найти в базе его адену в инвентаре
				statement.setInt(1, player);
				statement.setInt(2, item);
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
					statement.setLong(1, player);
					statement.setLong(2, item);
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

	/**
	 * Возвращает вещь игроку на склад. Игрок может быть оффлайн.
	 */
	public static void returnItem(L2ItemInstance item)
	{
		L2Player sender = L2ObjectsStorage.getPlayer(item.getOwnerId());
		if(sender != null) // цель в игре? отлично, используем стандартный механизм
		{
			item = L2ItemInstance.restoreFromDb(item.getObjectId(), true);
			sender.getWarehouse().addItem(item, "mail returned");
		}
		else if(!item.isStackable()) // нестекуемые вещи можно возвращать без проблем
		{
			item.setLocation(ItemLocation.WAREHOUSE);
			item.updateDatabase(true, false);
		}
		else
		{ // стекуемые проверяем на коллизии, хотя обрабатывает он корректно и несколько одинаковых но это некрасиво
			FiltredPreparedStatement statement = null;
			ResultSet rs = null;
			ThreadConnection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id = ? AND item_id = ? AND loc = 'WAREHOUSE' LIMIT 1"); // сперва пробуем найти в базе его вещь на складе
				statement.setInt(1, item.getOwnerId());
				statement.setInt(2, item.getItemId());
				rs = statement.executeQuery();
				if(rs.next())
				{
					int id = rs.getInt("object_id");
					DatabaseUtils.closeStatement(statement);
					statement = con.prepareStatement("UPDATE items SET count=count+? WHERE object_id = ? LIMIT 1"); // если нашли увеличиваем ее количество
					statement.setLong(1, item.getCount());
					statement.setInt(2, id);
					statement.executeUpdate();
				}
				else
				{
					item.setLocation(ItemLocation.WAREHOUSE);
					item.updateDatabase(true, false);
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

	/**
	 * Вызывается при старте сервера, чистит базу от фигни
	 */
	public void cleanupBD()
	{
		ThreadConnection con = null;
		FiltredStatement stmt = null;
		ResultSet rs = null;
		try
		{
			lock.lock();
			con = L2DatabaseFactory.getInstance().getConnection();

			// удаляем почту у удаленных чаров
			stmt = con.createStatement();
			stmt.executeUpdate("DELETE mail FROM mail LEFT JOIN characters ON mail.sender = characters.obj_Id WHERE characters.obj_Id IS NULL");
			DatabaseUtils.closeStatement(stmt);
			stmt = con.createStatement();
			stmt.executeUpdate("DELETE mail FROM mail LEFT JOIN characters ON mail.receiver = characters.obj_Id WHERE characters.obj_Id IS NULL");
			DatabaseUtils.closeStatement(stmt);

			// удаляем протухшие письма
			stmt = con.createStatement();
			stmt.executeUpdate("DELETE FROM mail WHERE UNIX_TIMESTAMP(expire) < UNIX_TIMESTAMP()");
			DatabaseUtils.closeStatement(stmt);

			// удаляем некорректные аттачи
			stmt = con.createStatement();
			stmt.executeUpdate("DELETE mail_attachments FROM mail_attachments LEFT JOIN items ON mail_attachments.itemId = items.object_id WHERE items.object_id IS NULL");
			DatabaseUtils.closeStatement(stmt);

			// чистим письма с потерянными аттачами
			stmt = con.createStatement();
			stmt.executeUpdate("UPDATE mail LEFT JOIN mail_attachments ON mail.messageId = mail_attachments.messageId SET price=0,attachments=0 WHERE mail_attachments.messageId IS NULL");
			DatabaseUtils.closeStatement(stmt);

			// чистим от мусора в mail_attachments, возвращая вещи владельцам
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT itemId FROM mail_attachments LEFT JOIN mail ON mail.messageId = mail_attachments.messageId WHERE mail.messageId IS NULL");
			while(rs.next())
			{
				L2ItemInstance item = L2ItemInstance.restoreFromDb(rs.getInt("itemId"), false);
				if(item.getOwnerId() == 0)
					item.removeFromDb(true);
				else
					returnItem(item);
			}
			DatabaseUtils.closeDatabaseSR(stmt, rs);

			stmt = con.createStatement();
			stmt.executeUpdate("DELETE mail_attachments FROM mail_attachments LEFT JOIN mail ON mail.messageId = mail_attachments.messageId WHERE mail.messageId IS NULL");
			DatabaseUtils.closeStatement(stmt);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, stmt, rs);
			lock.unlock();
		}
	}

	private void loadCache()
	{
		lettersByIdCache = new HashMap<Integer, Letter>();
		lettersByReceiverCache = new HashMap<Integer, GArray<Letter>>();
		lettersBySenderCache = new HashMap<Integer, GArray<Letter>>();

		ThreadConnection con = null;
		FiltredStatement stmt = null;
		FiltredStatement stmt2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		lock.lock();
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT m.messageId, m.topic, UNIX_TIMESTAMP(m.expire) AS lifetime, m.body, m.price, m.attachments, m.unread, m.system, m.sender, m.receiver, cs.char_name, cr.char_name FROM mail m LEFT JOIN characters cs ON ( m.sender = cs.obj_Id ) LEFT JOIN characters cr ON ( m.receiver = cr.obj_Id )");
			while(rs.next())
			{
				Letter ret = new Letter();
				ret.id = rs.getInt("m.messageId");
				ret.system = rs.getInt("m.system");
				ret.attachments = rs.getInt("m.attachments");
				ret.unread = rs.getInt("m.unread");
				ret.senderId = rs.getInt("m.sender");
				ret.receiverId = rs.getInt("m.receiver");
				ret.validtime = rs.getInt("lifetime");
				ret.senderName = rs.getString("cs.char_name");
				ret.receiverName = rs.getString("cr.char_name");
				ret.topic = rs.getString("m.topic");
				ret.body = rs.getString("m.body");
				ret.price = rs.getLong("m.price");

				if(ret.attachments > 0)
				{
					stmt2 = con.createStatement();
					rs2 = stmt2.executeQuery("SELECT itemId FROM mail_attachments WHERE messageId=" + ret.id);
					GArray<TradeItem> items = new GArray<TradeItem>(ret.attachments);
					while(rs2.next())
					{
						TradeItem ti = TradeItem.restoreFromDb(rs2.getInt("itemId"), ItemLocation.LEASE);
						if(ti != null)
							items.add(ti);
					}
					ret.attached = items;
					DatabaseUtils.closeDatabaseSR(stmt2, rs2);
				}

				cache(ret);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, stmt, rs);
			DatabaseUtils.closeDatabaseSR(stmt2, rs2);
			lock.unlock();
		}
	}

	public void cache(Letter letter)
	{
		try
		{
			lock.lock();
			lettersByIdCache.put(letter.id, letter);

			GArray<Letter> arr = lettersByReceiverCache.get(letter.receiverId);
			if(arr == null)
			{
				arr = new GArray<Letter>();
				lettersByReceiverCache.put(letter.receiverId, arr);
			}
			arr.add(letter);

			arr = lettersBySenderCache.get(letter.senderId);
			if(arr == null)
			{
				arr = new GArray<Letter>();
				lettersBySenderCache.put(letter.senderId, arr);
			}
			arr.add(letter);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			lock.unlock();
		}
	}

	public static class Letter
	{
		private static final GArray<TradeItem> EMPTY_LIST = new GArray<TradeItem>(0);

		public int id, system, unread, attachments, validtime, senderId, receiverId;
		public String senderName, receiverName, topic, body;
		public long price;
		public GArray<TradeItem> attached = EMPTY_LIST;
	}
}