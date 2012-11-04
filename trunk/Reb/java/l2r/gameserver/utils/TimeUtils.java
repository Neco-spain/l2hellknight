package l2r.gameserver.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtils
{
	private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");

	public static String toSimpleFormat(Calendar cal)
	{
		return SIMPLE_FORMAT.format(cal.getTime());
	}

	public static String toSimpleFormat(long cal)
	{
		return SIMPLE_FORMAT.format(cal);
	}

	public static String minutesToFullString(int period)
	{
		StringBuilder sb = new StringBuilder();

		// парсим дни
		if(period > 1440) // больше 1 суток
		{
			sb.append((period - (period % 1440)) / 1440).append(" д.");
			period = period % 1440;
		}

		// парсим часы
		if(period > 60) // остаток более 1 часа
		{
			if(sb.length() > 0)
			{
				sb.append(", ");
			}

			sb.append((period - (period % 60)) / 60).append(" ч.");

			period = period % 60;
		}

		// парсим остаток
		if(period > 0) // есть остаток
		{
			if(sb.length() > 0)
			{
				sb.append(", ");
			}

			sb.append(period).append(" мин.");
		}
		if(sb.length() < 1)
		{
			sb.append("менее 1 мин.");
		}

		return sb.toString();
	}
}
