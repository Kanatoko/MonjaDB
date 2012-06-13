package net.jumperz.util;

import javax.swing.*;
import java.awt.*;

public abstract class MSwingUtil
implements Runnable
{
//--------------------------------------------------------------------------------------------
public void execute()
{
if( EventQueue.isDispatchThread() )
	{
	updateSwing();
	}
else
	{
	SwingUtilities.invokeLater( this );
	}
}
//--------------------------------------------------------------------------------------------
public void run()
{
updateSwing();
}
//--------------------------------------------------------------------------------------------
public abstract void updateSwing();
//--------------------------------------------------------------------------------------------
}
