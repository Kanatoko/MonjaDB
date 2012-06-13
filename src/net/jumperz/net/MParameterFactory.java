package net.jumperz.net;

import java.io.*;
import java.net.*;

public class MParameterFactory
{
// --------------------------------------------------------------------------------
public static MAbstractParameter getParameter( String name, String value, int type, String charset )
throws IOException
{
if( type == MAbstractParameter.URI || type == MAbstractParameter.BODY )
	{
		//文字コードに合わせたURLエンコードを行う
	return new MParameter( URLEncoder.encode( name, charset ), value, type );
	}
else if( type == MAbstractParameter.MULTIPART )
	{
		//name, valueにはJava方式のStringオブジェクトが入ってくるのでlatin1形式に戻す
		//文字コード自体には触れない
	name  = new String( name. getBytes( charset ), "ISO-8859-1" );
	value = new String( value.getBytes( charset ), "ISO-8859-1" );
	return new MMultipartParameter( name, value, type );
	}
else
	{
	throw new IOException( "Invalid type" );
	}
}
// --------------------------------------------------------------------------------
}