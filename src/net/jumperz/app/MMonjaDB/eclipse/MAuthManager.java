package net.jumperz.app.MMonjaDB.eclipse;

import java.util.*;

import org.eclipse.swt.widgets.Shell;

import com.mongodb.DB;

import net.jumperz.app.MMonjaDB.eclipse.dialog.MPasswordDialog;
import net.jumperz.app.MMonjaDB.eclipse.dialog.MPromptDialog;
import net.jumperz.app.MMonjaDBCore.MAbstractLogAgent;
import net.jumperz.app.MMonjaDBCore.MConstants;
import net.jumperz.app.MMonjaDBCore.MDataManager;
import net.jumperz.app.MMonjaDBCore.action.MAbstractAction;
import net.jumperz.app.MMonjaDBCore.action.MAction;
import net.jumperz.app.MMonjaDBCore.event.MEvent;
import net.jumperz.util.*;

public class MAuthManager
extends MAbstractLogAgent
implements MObserver2, MConstants
{
//--------------------------------------------------------------------------------
public void update( Object e, Object source )
{
MEvent event = ( MEvent )e;

debug( event.getEventName() );

if( event.getEventName().equals( event_error ) )
	{
	Exception error = ( Exception )( event.getData().get( "error" ) );
	debug( event.getData().get( "error" ) );
	if( error.toString().indexOf( "unauthorized" ) > -1 
	 || error.toString().indexOf( "not authorized" ) > -1
	 || error.toString().indexOf( "need to login" ) > -1
	  )
		{
		MAbstractAction action = ( MAbstractAction )( event.getData().get( "source" ) );
		if( action.getEventName().equals( event_showdbs ) )
			{
			auth( action, true );
			}
		else
			{
			auth( action, false );
			}
		}
	}
}
//--------------------------------------------------------------------------------
private void auth( final MAbstractAction action, final boolean isSuperuser )
{
final Shell shell = Activator.getDefault().getShell();
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//-----

String username, passwd;
String title = null;

{
Set dataSet = new HashSet();
if( isSuperuser )
	{
	title = "Authorization Required for Admin";
	}
else
	{
	title = "Authorization Required for DB: " + MDataManager.getInstance().getDB().getName();
	}
MPromptDialog dialog = new MPromptDialog( shell, dataSet, title, "username :" );
dialog.open();

if( dataSet.size() > 0 )
	{
	username = dataSet.iterator().next() + "";
	}
else
	{
	return;
	}
}

{
Set dataSet = new HashSet();
MPasswordDialog dialog = new MPasswordDialog( shell, title, dataSet );
dialog.open();
if( dataSet.size() > 0 )
	{
	passwd = dataSet.iterator().next() + "";
	}
else
	{
	return;
	}

}

MDataManager.getInstance().getActionThreadPool().addCommand( new MAuthCommand( username, passwd, action, isSuperuser ) );

}});//-----

}
//--------------------------------------------------------------------------------
}