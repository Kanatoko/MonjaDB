package net.jumperz.app.MMonjaDBCore;

import net.jumperz.app.MMonjaDBCore.action.*;
import net.jumperz.app.MMonjaDBCore.event.*;
import net.jumperz.mongo.MMongoUtil;
import net.jumperz.util.*;
import java.io.*;
import java.util.*;
import com.mongodb.*;

public class MStdoutView
extends MAbstractLogAgent
implements MOutputView, MCommand
{
private MThreadPool threadPool = MDataManager.getInstance().getThreadPool();
//--------------------------------------------------------------------------------
public void execute()
{
try
	{
	execute2();
	}
catch( Exception e )
	{
	warn( e );
	}
}
//--------------------------------------------------------------------------------
public void update( final Object e, final Object source )
{
//threadPool.addCommand( new MCommand() {	public void execute(){ //-----------------

final MEvent event = ( MEvent )e;
if( event.getEventName().indexOf( event_error ) == 0 )
	{
	final Object error = event.getData().get( "error" );
	debug( error );
	}
else if( event.getEventName().indexOf( event_connect + "_end" ) == 0 )
	{
	MConnectAction action = ( MConnectAction )source;
	debug( "connected:" + action.getMongo() );
	}
else if( event.getEventName().indexOf( event_showcollections + "_end" ) == 0 )
	{
	MShowCollectionAction action = ( MShowCollectionAction )source;
	debug( action.getCollSet() );
	}
else if( event.getEventName().indexOf( event_showdbs + "_end" ) == 0 )
	{
	MShowDBAction action = ( MShowDBAction )source;
	debug( action.getDBList() );
	}
else if( event.getEventName().indexOf( event_use + "_end" ) == 0 )
	{
	MUseAction action = ( MUseAction )source;
	debug( "switched to db " + action.getDBName() );
	}
else if( event.getEventName().indexOf( event_find + "_end" ) == 0 )
	{
	if( MMonjaDB.invoked )
		{
		List l = MDataManager.getInstance().getDocumentDataList();
		for( int i = 0; i < l.size(); ++i )
			{
			debug( l.get( i ) );
			}
		}
	}
}
//--------------------------------------------------------------------------------
private void execute2()
throws Exception
{
}
//--------------------------------------------------------------------------------
public void breakCommand()
{
}
//--------------------------------------------------------------------------------
}