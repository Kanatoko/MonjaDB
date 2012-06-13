package net.jumperz.app.MMonjaDBCore;

import net.jumperz.util.MLogServer;

public abstract class MAbstractLogAgent
implements MConstants
{
public static boolean enabled = false;

protected String className = "";
protected String prefix = "";
// --------------------------------------------------------------------------------
public void log( int logLevel, Object message )
{
if( enabled )
	{
	MLogServer.getInstance().log( className, logLevel, prefix, message );
	}
}
// --------------------------------------------------------------------------------
public void info( Object message )
{
log( MLogServer.log_info, message );
}
// --------------------------------------------------------------------------------
public void warn( Object message )
{
log( MLogServer.log_warn, message );
}
// --------------------------------------------------------------------------------
public void debug( Object message )
{
log( MLogServer.log_debug, message );
}
// --------------------------------------------------------------------------------
}