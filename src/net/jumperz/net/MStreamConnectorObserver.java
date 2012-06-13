package net.jumperz.net;

import java.net.*;
import java.io.*;
import java.util.*;
import net.jumperz.util.*;

public class MStreamConnectorObserver
implements MObserver1
{
private MStreamConnector streamConnector;
private List socketList = new ArrayList();
//--------------------------------------------------------------------------------
public MStreamConnectorObserver( MStreamConnector streamConnector )
{
this.streamConnector = 	streamConnector;
}
//--------------------------------------------------------------------------------
public void update()
{
int state = streamConnector.getState();
if( state == MStreamConnector.CLOSED )
	{
	try
		{
		Thread.sleep( 2000 );
		}
	catch( Exception e )
		{
		}
	Iterator p = socketList.iterator();
	while( p.hasNext() )
		{
		Socket socket = ( Socket )p.next();
		MSystemUtil.closeSocket( socket );
		}
	}
}
//--------------------------------------------------------------------------------
public void addSocket( Socket socket )
{
socketList.add( socket );
}
//--------------------------------------------------------------------------------
}