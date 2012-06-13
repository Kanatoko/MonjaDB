package net.jumperz.gui;

import java.util.*;
import java.io.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

public class MSWTApplication
implements Listener
{
protected static final String appName = "MSWTApplication@JUMPERZ.NET";

private static final String defaultHeight = "320";
private static final String defaultWidth  = "500";
private static final String defaultTop    = "100";
private static final String defaultLeft   = "100";

private int height;
private int width;
private int top;
private int left;
private File configFile;
protected Properties prop;
protected Shell shell;

private static MSWTApplication instance;

//-----------------------------------------------------------------------------------
public static final MSWTApplication getInstance()
{
return instance;
}
//-----------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
if( args.length == 1 )
	{
	instance = new MSWTApplication( args[ 0 ] );
	instance.startShell();
	}
else
	{
	System.out.println( "java app [ config_file ]" );
	return;
	}
}
//-----------------------------------------------------------------------------------
public MSWTApplication( String configFileName )
throws IOException
{
configFile = new File( configFileName );
loadConfig();
}
//-----------------------------------------------------------------------------------
private void loadConfig()
throws IOException
{
prop = new Properties();
if( configFile.exists() )
	{
	FileInputStream inifile_in = new FileInputStream( configFile );
	prop.load( inifile_in );
	}
height	= Integer.parseInt( prop.getProperty( "height"	, defaultHeight ) );
width	= Integer.parseInt( prop.getProperty( "width"	, defaultWidth	) );
top	= Integer.parseInt( prop.getProperty( "top"	, defaultTop	) );
left	= Integer.parseInt( prop.getProperty( "left"	, defaultLeft	) );
}
//-----------------------------------------------------------------------------------
public void startShell()
throws Exception
{
Display display = new Display();
shell = new Shell( display );

startApplication();

shell.setText( appName );
shell.setSize( width, height );
shell.setLocation( left, top );

//shell.pack();
shell.open();
shell.addListener( SWT.Close	, this );
shell.addListener( SWT.Resize	, this );

while (!shell.isDisposed())
	{
	if ( !display.readAndDispatch() )
		{
		display.sleep();
		}
	}
display.dispose();
}
//-----------------------------------------------------------------------------------
private void saveConfig()
throws IOException
{
Point size = shell.getSize();
prop.setProperty( "width"	, String.valueOf( size.x ) );
prop.setProperty( "height"	, String.valueOf( size.y ) );

Point location = shell.getLocation();
prop.setProperty( "top"		, String.valueOf( location.y ) );
prop.setProperty( "left"	, String.valueOf( location.x ) );

FileOutputStream configFileOut = new FileOutputStream( configFile );
prop.store( configFileOut, "" ); 
}
//-----------------------------------------------------------------------------------
public void handleEvent( Event event )
{
System.err.println( event.widget );
try
	{
	switch( event.type )
		{
		case SWT.Close:
			this.onClose();
			break;	
		case SWT.Resize:
			this.onResize();
			break;
		}
	}
catch( Exception e )
	{
	System.out.println( event );
	e.printStackTrace();
	}
}
//-----------------------------------------------------------------------------------
protected void onResize()
throws Exception
{
//System.out.println( "resize" );
}
//-----------------------------------------------------------------------------------
protected void onClose()
throws Exception
{
saveConfig();
}
//-----------------------------------------------------------------------------------
protected void startApplication()
throws Exception
{
}
//-----------------------------------------------------------------------------------
}