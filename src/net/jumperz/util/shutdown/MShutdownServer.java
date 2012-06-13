package net.jumperz.util.shutdown;

import net.jumperz.app.*;
import net.jumperz.util.*;
import net.jumperz.net.*;

public class MShutdownServer
extends MAbstractShutdown
{
private MApplication application;
private MMultiAcceptor acceptor;
//--------------------------------------------------------------------------------
public MShutdownServer( MApplication in_application )
{
application = in_application;
}
// --------------------------------------------------------------------------------
public MMultiAcceptor getAcceptor()
{
return acceptor;
}
//--------------------------------------------------------------------------------
public void start( MThreadPool threadPool )
{
acceptor = new MMultiAcceptor( serverHost, serverPort );
MShutdownerObserver shutdownerObserver = new MShutdownerObserver();
shutdownerObserver.setAcceptor( acceptor );
shutdownerObserver.setApplication( application );
acceptor.register1( shutdownerObserver );
threadPool.addCommand( acceptor );
}
//--------------------------------------------------------------------------------
}