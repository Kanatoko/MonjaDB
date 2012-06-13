package net.jumperz.net.exception;

import java.io.*;

public class MHttpException
extends IOException
{
private static final long	serialVersionUID	= 6273730311499917132L;
protected int errorCode;
protected String message;
//------------------------------------------------------------------------------------------
public MHttpException( int in_errorCode, String in_message )
{
super( Integer.toString( in_errorCode ) + ":" + in_message );
errorCode	= in_errorCode;
message		= in_message;
}
//------------------------------------------------------------------------------------------
}