package net.jumperz.app.MMonjaDBCore;

import java.util.UUID;

import org.bson.types.ObjectId;

public class MCoreUtil
{
//--------------------------------------------------------------------------------
public static String getCollPrefix( String collName )
{
if( collName.matches( "^[\\.a-zA-Z0-9]+$" ) )
	{
	return "db." + collName;
	}
else
	{
	return "db[ '" + collName + "' ]";
	}
}
//--------------------------------------------------------------------------------
public static Object getObjectIdFromString( String _idStr )
{
Object _idObj;
if( _idStr.length() > 0 )
	{
	try
		{
		_idObj = new ObjectId( _idStr );
		}
	catch( Exception e )
		{
		if( _idStr.matches( "^[0-9]+\\.[0-9]+$" ) )
			{
				//assume the type of OID is double
			_idObj = new Double( _idStr );
			}
		else if( _idStr.matches( "^[0-9]+$" ) )
			{
				//assume the type of OID is integer
			_idObj = new Integer( _idStr );
			}
		else if( isUuid( _idStr ) )
			{
			_idObj = UUID.fromString( _idStr );
			}
		else
			{
				// String
			_idObj = _idStr;		
			}
		}
	return _idObj;
	}
else
	{
	return null;
	}
}
//--------------------------------------------------------------------------------
public static boolean isUuid( String s )
{
try
	{
	UUID.fromString( s );
	return true;
	}
catch( Exception e )
	{
	return false;
	}
}
//--------------------------------------------------------------------------------
}