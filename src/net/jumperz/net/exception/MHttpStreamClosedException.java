package net.jumperz.net.exception;

import java.io.*;

public class MHttpStreamClosedException
extends MHttpIOException
{
private static final long	serialVersionUID	= -4316779902102872145L;
//------------------------------------------------------------------------------------------
public MHttpStreamClosedException( String message )
{
super( message );
}
//------------------------------------------------------------------------------------------
}