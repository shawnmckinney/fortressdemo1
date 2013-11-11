/*
 * This is free and unencumbered software released into the public domain.
 */
package com.mycompany;

import org.apache.wicket.Component;
import us.jts.fortress.cfg.Config;
import us.jts.fortress.rbac.Permission;
import us.jts.fortress.rbac.Session;
import us.jts.fortress.util.attr.VUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * ...
 *
 * @author Shawn McKinney
 * @version $Rev$
 */
public class GlobalUtils
{
    public static final String WINDOW_LOCATION_REPLACE_DEMO_HOME_HTML = "window.location.replace(\"/fortressdemo1/home.html\");";
    private static final String PERMS_CACHED = "perms.cached";
    public static final boolean IS_PERM_CACHED = ( ( Config.getProperty( PERMS_CACHED ) != null ) && ( Config
        .getProperty( PERMS_CACHED ).equalsIgnoreCase( "true" ) ) );

    public static final String BUTTON1 = "button1";
    public static final String BUTTON2 = "button2";
    public static final String BUTTON3 = "button3";

    public static final String USER_ID = "userId";
    public static final String PSWD_FIELD = "pswdField";
    public static final String LOGIN = "login";
    public static final String PAGE_1 = "PAGE1";
    public static final String USER_1 = "user1";
    public static final String BTN_PAGE_1 = "page1";

    public static final String PAGE_2 = "PAGE2";
    public static final String USER_2 = "user2";
    public static final String BTN_PAGE_2 = "page2";

    public static final String PAGE_3 = "PAGE3";
    public static final String USER_3 = "user3";
    public static final String BTN_PAGE_3 = "page3";
    public static final String ROLE_SUPER = "ROLE_TEST_SUPER";
    public static final String ROLE_TEST1 = "ROLE_TEST1";
    public static final String ROLE_TEST2 = "ROLE_TEST2";
    public static final String ROLE_TEST3 = "ROLE_TEST3";
    public static final String LOGOUT = "logout";
    public static final String INACTIVE_ROLES = "inactiveRoles";
    public static final String ACTIVE_ROLES = "activeRoles";
    public static final String ROLES_DEACTIVATE = "roles.deactivate";
    public static final String ROLES_ACTIVATE = "roles.activate";
    public static final String FOOTER = "This is free and unencumbered software released into the public domain.";

    public static Session getRbacSession( Component component )
    {
        return ( ( RbacSession ) component.getSession() ).getRbacSession();
    }

    public static List<Permission> getRbacPermissions( Component component )
    {
        return ( ( RbacSession ) component.getSession() ).getPermissions();
    }

    public static boolean isAuthorized( String roleName, HttpServletRequest servletReq )
    {
        boolean isAuthorized = false;
        if ( servletReq.isUserInRole( roleName ) )
        {
            isAuthorized = true;
        }
        return isAuthorized;
    }

    public static boolean isFound( Permission permission, Component component )
    {
        List<Permission> permissions = GlobalUtils.getRbacPermissions( component );
        return VUtil.isNotNullOrEmpty( permissions ) && permissions.contains( permission );
    }
}
