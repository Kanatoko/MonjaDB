package net.jumperz.gui;

import java.awt.event.*;

class MFrameWindowAdapter extends WindowAdapter
{
private MFrame frame;
//----------------------------------------------------------------------
public MFrameWindowAdapter( MFrame IN_frame )
{
frame = IN_frame;
}
//----------------------------------------------------------------------
public void windowClosing( WindowEvent e )
{
frame.windowClosing();
}
//----------------------------------------------------------------------
}
