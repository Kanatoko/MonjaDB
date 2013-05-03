package net.jumperz.app.MMonjaDB.eclipse;

import com.mongodb.DB;

import net.jumperz.app.MMonjaDBCore.MAbstractLogAgent;
import net.jumperz.app.MMonjaDBCore.MDataManager;
import net.jumperz.app.MMonjaDBCore.action.MAction;
import net.jumperz.util.*;

public class MAuthCommand
extends MAbstractLogAgent
implements MCommand
{
private String username;
private String passwd;
private MAction action;
private boolean isSuperuser;
//--------------------------------------------------------------------------------
public MAuthCommand( String _username, String _passwd, MAction _action, boolean _isSuperuser )
{
username = _username;
passwd = _passwd;
action = _action;
isSuperuser = _isSuperuser;
}
//--------------------------------------------------------------------------------
public void execute()
{
try
	{
	DB db;
	if( isSuperuser )
		{
		db = MDataManager.getInstance().getMongo().getDB( "admin" );
		}
	else
		{
		db = MDataManager.getInstance().getDB();
		}
	
		//exec auth
	if( db.authenticate( username, passwd.toCharArray() ) )
		{
		MDataManager.getInstance().getActionThreadPool().addCommand( action );
		}
	else
		{
		info( "auth failed." );
		}
	}
catch( Exception e )
	{
	info( e );
	}
}
//--------------------------------------------------------------------------------
public void breakCommand()
{
}
//--------------------------------------------------------------------------------
}