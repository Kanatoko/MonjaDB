package net.jumperz.net;

import java.io.*;
import java.net.*;
import net.jumperz.security.*;
import javax.net.ssl.*;

public class MSslConnector
extends MConnector
{
private KeyManager[] kmArray;
// --------------------------------------------------------------------------------
public MSslConnector( String host, int port )
{
super( host, port );
}
// --------------------------------------------------------------------------------
public MSslConnector( String host, int port, KeyManager[] kmArray )
{
super( host, port );
this.kmArray = kmArray;
}
// --------------------------------------------------------------------------------
protected Socket connect()
throws IOException
{
return MSecurityUtil.getBogusSslSocket( Host, port, kmArray );
}
// --------------------------------------------------------------------------------
}