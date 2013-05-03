package net.jumperz.net;

public class MParameter
implements MAbstractParameter, Comparable
{
private String name;
private String value;
private int type;
// --------------------------------------------------------------------------------
public MParameter( String n, String v )
{
name = n;
value = v;
type = URI;
}
// --------------------------------------------------------------------------------
public MParameter( String n, String v, int t )
{
name = n;
value = v;
type = t;
}
//--------------------------------------------------------------------------------
public void setName( String s )
{
name = s;
}
// --------------------------------------------------------------------------------
public void setValue( String s )
{
value = s;
}
// --------------------------------------------------------------------------------
public int getType()
{
return type;
}
// --------------------------------------------------------------------------------
public String getValue()
{
return value;
}
// --------------------------------------------------------------------------------
public String getName()
{
return name;
}
// --------------------------------------------------------------------------------
public String toString()
{
return name + "=" + value;
}
//--------------------------------------------------------------------------------
public boolean equals( Object o )
{
if( o instanceof MParameter )
	{
	MParameter in = ( MParameter )o;
	return
		(  in.type == this.type
		&& in.name.equals( this.name )
		&& in.value.equals( this.value )
		);
	}
else
	{
	return false;
	}
}
//--------------------------------------------------------------------------------
public int hashCode()
{
return name.hashCode() + value.hashCode() + type;
}
//--------------------------------------------------------------------------------
public int compareTo( Object o )
{
if( o instanceof MParameter )
	{
	if( this.equals( o ) )
		{
		return 0;
		}
	else
		{
		MParameter in = ( MParameter )o;
		if( in.name.equals( this.name ) )
			{
			if( in.value.equals( this.value ) )
				{
					//type must be defferent here
				if( this.type > in.type )
					{
					return 1;
					}
				else
					{
					return -1;
					}
				}
			else
				{
				return this.value.compareTo( in.value );
				}
			}
		else
			{
			return this.name.compareTo( in.name );
			}
		}
	}
else
	{
	return this.toString().compareTo( o.toString() );
	}
}
// --------------------------------------------------------------------------------
}