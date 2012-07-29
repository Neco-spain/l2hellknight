package l2p.gameserver.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import l2p.gameserver.Config;
import l2p.gameserver.loginservercon.LoginServerCommunication;
import l2p.gameserver.loginservercon.gspackets.*;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.Ex2ndPasswordAck;
import l2p.gameserver.serverpackets.Ex2ndPasswordCheck;
import l2p.gameserver.serverpackets.Ex2ndPasswordVerify;
import l2p.gameserver.serverpackets.ServerClose;

/**
 * @author Cain
 */
public class SecondaryPasswordAuth
{
	private final Logger _log = Logger.getLogger(SecondaryPasswordAuth.class.getName());
	private final GameClient _activeClient;

    private String _password;
    private int _wrongAttempts;
    private boolean _authed;
    private long _unBanTime;

	public SecondaryPasswordAuth(GameClient activeClient, String pwd, int wrongAttempts, long unbanTime)
	{
        _activeClient = activeClient;
		_password = pwd;
		_wrongAttempts = wrongAttempts;
		_authed = false;
        _unBanTime = unbanTime;
	}

    public void setPassword(String password)
    {
        _password = password;
    }
    
    public void setUnbanTime(long unbanTime)
    {
        _unBanTime = unbanTime;
    }
    
    public void setWrongAttempts(int wrongAttempts)
    {
        _wrongAttempts = wrongAttempts;
    }
	
	public boolean savePassword(String password)
	{
		if (passwordExist())
		{
			_log.warning("[SecondaryPasswordAuth]"+_activeClient.getLogin()+" forced savePassword");

            _activeClient.closeNow(false);
			return false;
		}
		
		if (!validatePassword(password))
		{
			_activeClient.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.WRONG_PATTERN));
			return false;
		}
		
		password = cryptPassword(password);

        LoginServerCommunication.getInstance().sendPacket(new Player2ndAuthSetPassword(_activeClient.getLogin(), password));
		
		_password = password;
		return true;
	}
	
	public boolean insertWrongAttempt(int attempts)
	{
        LoginServerCommunication.getInstance().sendPacket(new Player2ndAuthSetAttempts(_activeClient.getLogin(), attempts));
		return true;
	}
	
	public boolean changePassword(String oldPassword, String newPassword)
	{
		if (!passwordExist())
		{
			_log.warning("[SecondaryPasswordAuth]"+_activeClient.getLogin()+" forced changePassword");
			_activeClient.closeNow(false);
			return false;
		}
		
		if (!checkPassword(oldPassword, true))
			return false;
		
		if (!validatePassword(newPassword))
		{
			_activeClient.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.WRONG_PATTERN));
			return false;
		}
		
		newPassword = cryptPassword(newPassword);

        LoginServerCommunication.getInstance().sendPacket(new Player2ndAuthSetPassword(_activeClient.getLogin(), newPassword));

		_password = newPassword;
		_authed = false;
		return true;
	}
	
	public boolean checkPassword(String password, boolean skipAuth)
	{
		password = cryptPassword(password);
		
		if (!password.equals(_password))
		{
			_wrongAttempts++;
			if (_wrongAttempts < Config.SECOND_AUTH_MAX_ATTEMPTS)
			{
				_activeClient.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_WRONG, Config.SECOND_AUTH_MAX_ATTEMPTS - _wrongAttempts));
				insertWrongAttempt(_wrongAttempts);
			}
			else
			{
				insertBanTime(Config.SECOND_AUTH_BAN_TIME);
                _log.warning(_activeClient.getLogin()+" - ("+
						_activeClient.getIpAddr()+") has inputted the wrong password "+_wrongAttempts+" times in row.");
				insertWrongAttempt(0);
                _activeClient.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_BAN, Config.SECOND_AUTH_MAX_ATTEMPTS));
			}
			return false;
		}
		if (!skipAuth)
		{
			_authed = true;
			_activeClient.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_OK, _wrongAttempts));
		}
		insertWrongAttempt(0);
		return true;
	}
	
	public boolean passwordExist()
	{
		return (_password != null && !_password.isEmpty());
	}

    public void insertBanTime(int banTime)
    {
        LoginServerCommunication.getInstance().sendPacket(new Player2ndAuthSetBanTime(_activeClient.getLogin(), banTime));
        _unBanTime = System.currentTimeMillis()+(banTime*60000);
    }

	public void openDialog()
	{
        if (System.currentTimeMillis() <= _unBanTime)
            _activeClient.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_PROMPT, (int)(_unBanTime - System.currentTimeMillis())/1000));
        else if (passwordExist())
			_activeClient.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_PROMPT, 0));
		else
			_activeClient.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_NEW, 0));
	}
	
	public boolean isAuthed()
	{
		return _authed;
	}
	
	private String cryptPassword(String password)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes("UTF-8");
			byte[] hash = md.digest(raw);
			return Base64.encode(hash);
		}
		catch (NoSuchAlgorithmException e)
		{
			_log.severe("[SecondaryPasswordAuth]Unsupported Algorythm");
		}
		catch (UnsupportedEncodingException e)
		{
			_log.severe("[SecondaryPasswordAuth]Unsupported Encoding");
		}
		return null;
	}
	
	private boolean validatePassword(String password)
	{
        // целое число
		if (!Util.isNumber(password))
			return false;
		// длина пароля от 6 до 8 символов
		if (password.length() < 6 || password.length() > 8)
			return false;

        // Пароль не может содержать:
        // - Следующие подряд символы (123456, 654321)
        // - Одинаковые символы       (111111)
		for (int i = 0; i < password.length()-1; i++)
		{
			char curCh = password.charAt(i);
			char nxtCh = password.charAt(i+1);
			
			if (curCh+1 == nxtCh)
				return false;
			else if (curCh-1 == nxtCh)
				return false;
			else if (curCh == nxtCh)
				return false;
		}

        // пароль не должен содержать повторяющихся конструкций (23692308)
        // а так же обратных повторяющихся конструкций (2369320)
		for (int i = 0; i < password.length()-2; i++)
		{
			String toChk = password.substring(i+1);
			StringBuffer chkEr = new StringBuffer(password.substring(i, i+2));
			
			if (toChk.contains(chkEr))
				return false;
			else if (toChk.contains(chkEr.reverse()))
				return false;
		}
		_wrongAttempts = 0;
		return true;
	}
}