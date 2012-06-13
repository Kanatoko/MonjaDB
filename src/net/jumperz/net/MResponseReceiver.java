package net.jumperz.net;

import java.net.*;
import java.io.*;
import net.jumperz.net.*;
import net.jumperz.util.*;

public class MResponseReceiver
implements MCommand, MSubject1
{
private MSubject1 subject = new MSubject1Impl();
private InputStream is;
private int state;
private MHttpResponse response;
private boolean headResponse;

public static final int SUCCESS = 0;
public static final int ERROR = 1;
// --------------------------------------------------------------------------------
public MResponseReceiver( InputStream is, boolean headResponse )
{
this.is = is;
this.headResponse = headResponse;
}
// --------------------------------------------------------------------------------
public void execute()
{
try
	{
	BufferedInputStream bis = new BufferedInputStream( is );
	response = new MHttpResponse( bis, headResponse );
	if( response.getStatusCode() == 100 )
		{
		response = new MHttpResponse( bis, headResponse );
		}
	state = SUCCESS;
	}
catch( IOException e )
	{
	state = ERROR;
	}
notify1();
}
// --------------------------------------------------------------------------------
public MHttpResponse getResponse()
{
return response;
}
// --------------------------------------------------------------------------------
public void breakCommand()
{
try
	{
	is.close();
	}
catch( IOException e )
	{
	//ignore
	}
}
// --------------------------------------------------------------------------------
public int getState()
{
return state;
}
// --------------------------------------------------------------------------------
public void notify1()
{
subject.notify1();
}
// --------------------------------------------------------------------------------
public void register1( MObserver1 observer )
{
subject.register1( observer );
}
// --------------------------------------------------------------------------------
public void removeObservers1()
{
subject.removeObservers1();
}
// --------------------------------------------------------------------------------
public void removeObserver1( MObserver1 observer )
{
subject.removeObserver1( observer );
}
// --------------------------------------------------------------------------------
}