package net.jumperz.app.MMonjaDBCore.action;

import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;
//qimport net.jumperz.app.MMonjaDBCore.exception.MConnectedToWrongHostException;

public abstract class MAbstractAction
extends MAbstractLogAgent
implements MAction
{
protected MEventManager eventManager = MEventManager.getInstance();
protected MDataManager dataManager = MDataManager.getInstance();

public abstract void executeFunction() throws Exception;
public abstract int getActionCondition();
public abstract String getEventName();
//protected Object context;
private MInputView originView;
private boolean canceled = false;
/*
//--------------------------------------------------------------------------------
public final void setContext( Object context )
{
if( context != null )
	{
	this.context =context;
	setContextImpl( context );
	}
}
//--------------------------------------------------------------------------------
public final Object getContext()
{
return context;
}
*/
//--------------------------------------------------------------------------------
public final void setContextImpl( Object context )
{
}
//--------------------------------------------------------------------------------
public void breakCommand()
{
}
//--------------------------------------------------------------------------------
public final void execute()
{
try
	{
	if( checkCondition() )
		{
		eventManager.fireEvent( new MEvent( getEventName() + "_start" ), this );
		executeFunction();
		eventManager.fireEvent( new MEvent( getEventName() + "_end" ), this );	
		}
	}
catch( Exception e )
	{
	eventManager.fireErrorEvent( e, this );
	}
}
//--------------------------------------------------------------------------------
public MInputView getOriginView()
{
return originView;
}
//--------------------------------------------------------------------------------
public void setOriginView( MInputView v )
{
originView = v;
}
//--------------------------------------------------------------------------------
private boolean checkCondition()
throws Exception
{
if( getActionCondition() == action_cond_none )
	{
	
	}
else if( getActionCondition() == action_cond_not_connected_or_connected_to_different_host )
	{
	if( dataManager.isConnected() )
		{
		if( dataManager.connectedToSameHost( this ) )
			{
			return false;
			}
		}
	}
else if( getActionCondition() == action_cond_connected )
	{
	if( !dataManager.isConnected() )
		{
		throw new Exception( "Not connected to MongoDB." );
		}
	}
else if( getActionCondition() == action_cond_db )
	{
	if( dataManager.getDB() == null )
		{
		throw new Exception( "Database is not choosed." );
		}
	}
else if( getActionCondition() == action_cond_collection )
	{
	if( dataManager.getDocumentDataList() == null )
		{
		throw new Exception( "collection is not choosed." );
		}
	}
return true;
}
//--------------------------------------------------------------------------------
}