package net.jumperz.app.MMonjaDB.eclipse.handler;

import org.eclipse.core.commands.*;
import org.eclipse.ui.*;

public class MActivationHandler
extends AbstractHandler
{
private boolean enabled = true;
//--------------------------------------------------------------------------------
public Object execute( ExecutionEvent event )
throws ExecutionException
{
IWorkbench wb = PlatformUI.getWorkbench();
IWorkbenchWindow[] windowArray = wb.getWorkbenchWindows();
if( windowArray.length > 0 )
	{
	IWorkbenchWindow window = windowArray[ 0 ];
	try
		{
		wb.showPerspective( "net.jumperz.app.MMonjaDB.eclipse.MPerspectiveFactory1", window ); 	
		}
	catch( Exception e )
		{
		e.printStackTrace();
		}
	}
return null;
}
//--------------------------------------------------------------------------------
public boolean isEnabled()
{
return enabled;
}
//--------------------------------------------------------------------------------
}
