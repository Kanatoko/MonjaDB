package net.jumperz.gui;

import javax.swing.*;
import java.awt.*;

public class MSplitPaneFrame extends MFrame
{
private static final String default_dividerlocation = "241";
private JSplitPane splitter;
private JPanel west_panel;
private JPanel east_panel;
//---------------------------------------------------------------------------
public MSplitPaneFrame( String appName )
{
super( appName );

try
	{
	JPanel main_panel = new JPanel();
	//main_panel.setBorder( BorderFactory.createEtchedBorder() );
	
		//locate a Panel on the whole MFrame
	getContentPane().add( main_panel, BorderLayout.CENTER );
	
		//set BorderLayout. ( FlowLayout is the default )
	main_panel.setLayout( new BorderLayout() );
	
		//right panel
	west_panel = new JPanel();
	//west_panel.setBorder( BorderFactory.createLineBorder( Color.red ) );
	//main_panel.add( west_panel, BorderLayout.WEST );
	
		//left panel
	east_panel = new JPanel();
	//east_panel.setBorder( BorderFactory.createLineBorder( Color.blue ) );
	//main_panel.add( east_panel, BorderLayout.CENTER );
	
		//splitter
	splitter = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, west_panel, east_panel );
	splitter.setOneTouchExpandable( true );
	splitter.setDividerLocation( Integer.parseInt( prop.getProperty( "dividerlocation", default_dividerlocation ) ) );
	main_panel.add( splitter );
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
}
//---------------------------------------------------------------------------
public JPanel getWestPanel()
{
return west_panel;
}
//---------------------------------------------------------------------------
public JPanel getEastPanel()
{
return east_panel;
}
//---------------------------------------------------------------------------
public void windowClosing()
{
SaveSize();
SaveLocation();
SaveDividerLocation();
SaveAndExit();
}
//---------------------------------------------------------------------------
protected final void SaveDividerLocation()
{
prop.setProperty( "dividerlocation", String.valueOf( splitter.getDividerLocation() ) );
}
//---------------------------------------------------------------------------
}
