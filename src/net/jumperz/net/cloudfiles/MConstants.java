package net.jumperz.net.cloudfiles;

public interface MConstants
{
public static int CFILES_ERROR_STATUS_TOO_MANY_ERRORS	= -1;
public static int CFILES_ERROR_STATUS_DEFAULT		= 0;
public static int CFILES_ERROR_STATUS_OK		= 1;

public static int CFILES_MAX_ALLOWED_ERROR = 5;

public static int CFILES_STATUS_NO_AUTH		= 0;
public static int CFILES_STATUS_AUTH_OK		= 1;

public static int CFILES_DEFAULT_TTL		= 60 * 60 * 24 * 3; //3 days
}