package net.jumperz.gui;

import javax.swing.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Frame that has a function to save position and size of itself
 * @author Kanatoko<anvil@jumperz.net>
 * @version 1.01
 */
public class MFrame extends JFrame
{
private JPanel mainPanel;
private File inifile;
protected Properties prop;
private static final String default_height = "320";
private static final String default_width  = "500";
private static final String default_top    = "100";
private static final String default_left   = "100";
//----------------------------------------------------------------------
public MFrame( String appName )
{
super( appName );

mainPanel = new JPanel();	
getContentPane().add( mainPanel, BorderLayout.CENTER );

try
	{
	inifile = new File( appName + ".INI" );
	prop = new Properties();

	//UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
	UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );

	addWindowListener( new MFrameWindowAdapter( this ) );
	
	if( inifile.exists() )
		{
		FileInputStream inifile_in = new FileInputStream( inifile );
		prop.load( inifile_in );
		}

	setSize
		(
		Integer.parseInt( prop.getProperty( "width" , default_width  ) ),
		Integer.parseInt( prop.getProperty( "height", default_height ) )
		);

	setLocation
		(
		Integer.parseInt( prop.getProperty( "top" , default_top  ) ),
		Integer.parseInt( prop.getProperty( "left", default_left ) )
		);

	}
catch( Exception e )
	{
	e.printStackTrace();
	}
}
//----------------------------------------------------------------------
public void windowClosing()
{
SaveSize();
SaveLocation();
SaveAndExit();
}
//----------------------------------------------------------------------
protected final void SaveSize()
{
prop.setProperty( "height", String.valueOf( getHeight() ) );
prop.setProperty( "width" , String.valueOf( getWidth()  ) );
}
//----------------------------------------------------------------------
protected final void SaveLocation()
{
prop.setProperty( "top"   , String.valueOf( getLocation().x ) );
prop.setProperty( "left"  , String.valueOf( getLocation().y ) );
}
//----------------------------------------------------------------------
protected final void SaveAndExit()
{
try
	{
	FileOutputStream inifile_out = new FileOutputStream( inifile );
	prop.store( inifile_out, "" );
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
System.exit( 0 );
}
//----------------------------------------------------------------------
public JPanel getMainPanel()
{
return mainPanel;
}
//----------------------------------------------------------------------
}

