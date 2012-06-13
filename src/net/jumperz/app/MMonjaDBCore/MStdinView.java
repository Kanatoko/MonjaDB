package net.jumperz.app.MMonjaDBCore;

import net.jumperz.app.MMonjaDBCore.action.MActionManager;
import net.jumperz.util.*;
import java.io.*;

public class MStdinView
extends MAbstractLogAgent
implements MInputView, MCommand
{
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
private void execute2()
throws Exception
{
BufferedReader reader = new BufferedReader( new InputStreamReader( System.in, "UTF-8" ) );
String line = null;
while( true )
	{
	line = reader.readLine();
	if( line == null )
		{
		continue;
		}
	else if( line.equals( "\\q" ) || line.equals( "exit" ) || line.equals( "quit" ) )
		{
		break;
		}
	else if( line.length() > 2 )
		{
		MActionManager.getInstance().executeAction( line );
		}
	}

MDataManager.getInstance().stopThreadPools();
}
//--------------------------------------------------------------------------------
public void breakCommand()
{
}
//--------------------------------------------------------------------------------
}