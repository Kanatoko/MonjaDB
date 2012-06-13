package net.jumperz.app.MMonjaDBCore;

import java.util.logging.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MMonjaDB
extends MAbstractLogAgent
{
private static  MMonjaDB instance;
public static boolean invoked;
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
invoked = true;
System.setProperty( "DB.TRACE", "true" );
( new MMonjaDB() ).start( args );
}
//--------------------------------------------------------------------------------
private void start( String[] args )
{
Logger TRACE_LOGGER = Logger.getLogger( "com.mongodb.TRACE" );
TRACE_LOGGER.setLevel( Level.FINEST );
debug( System.getProperty( "DB.TRACE" ) );

MEventManager.getInstance().register2( MDataManager.getInstance() );
MEventManager.getInstance().register2( new MStdoutView() );

MThreadPool threadPool = MDataManager.getInstance().getThreadPool();
threadPool.addCommand( new MStdinView() );
}
//--------------------------------------------------------------------------------
public static MMonjaDB getInstance()
{
return instance;
}
//--------------------------------------------------------------------------------
private MMonjaDB()
{
instance = this;
}
//--------------------------------------------------------------------------------
}