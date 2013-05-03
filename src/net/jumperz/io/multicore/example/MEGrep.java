package net.jumperz.io.multicore.example;

import java.util.regex.Pattern;
import net.jumperz.io.multicore.MParser;

public class MEGrep
implements MParser
{
private Pattern pattern;
//--------------------------------------------------------------------------------
public MEGrep()
{
pattern = Pattern.compile( System.getProperty( "egrep" ) );
}
//--------------------------------------------------------------------------------
public String parse( String s )
{
if( pattern.matcher( s ).find() )
	{
	return s;
	}
else
	{
	return null;
	}
}
//--------------------------------------------------------------------------------
}