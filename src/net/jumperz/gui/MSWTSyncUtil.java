package net.jumperz.gui;

import javax.swing.*;
import java.awt.*;

public abstract class MSWTSyncUtil
implements Runnable
{
//--------------------------------------------------------------------------------------------
public void run()
{
updateSWT();
}
//--------------------------------------------------------------------------------------------
public abstract void updateSWT();
//--------------------------------------------------------------------------------------------
}
