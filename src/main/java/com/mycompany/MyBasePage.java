/*
 * This is free and unencumbered software released into the public domain.
 */
package com.mycompany;

import com.googlecode.wicket.kendo.ui.form.combobox.ComboBox;
import com.googlecode.wicket.kendo.ui.renderer.ChoiceRenderer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openldap.fortress.AccessMgr;
import org.openldap.fortress.GlobalErrIds;
import org.openldap.fortress.ReviewMgr;
import org.openldap.fortress.rbac.Permission;
import org.openldap.fortress.rbac.Session;
import org.openldap.fortress.rbac.User;
import org.openldap.fortress.rbac.UserRole;
import org.openldap.fortress.rbac.Warning;
import org.openldap.fortress.util.attr.VUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.security.Principal;
import java.util.List;

/**
 * Base class for Fortressdemo1 Wicket sample project.  It contains security control functions to demonstrate ANSI
 * RBAC security concepts.
 *
 * @author Shawn McKinney
 * @version $Rev$
 */
public abstract class MyBasePage extends WebPage
{
    @SpringBean
    private AccessMgr accessMgr;
    @SpringBean
    private ReviewMgr reviewMgr;
    private static final Logger LOG = Logger.getLogger( MyBasePage.class.getName() );
    private Form myForm;
    private static final String LINKS_LABEL = "linksLabel";
    private String linksLabel = "Authorized Links";
    protected String infoField;
    private TextArea infoTA;

    public enum ChildPage
    {
        PAGE1,
        PAGE2,
        PAGE3,
    }

    private ChildPage childPage;

    public MyBasePage()
    {
        addSecureLinks();
        final Link actionLink = new Link( GlobalUtils.LOGOUT )
        {
            @Override
            public void onClick()
            {
                HttpServletRequest servletReq = ( HttpServletRequest ) getRequest().getContainerRequest();
                servletReq.getSession().invalidate();
                getSession().invalidate();
                setResponsePage( LoginPage.class );
            }
        };
        add( actionLink );
        infoTA = new TextArea<String>( "infoField", Model.of( infoField ) );
        add( infoTA );
        add( new Label( "footer", GlobalUtils.FOOTER ) );
        if ( !initializeRbacSession() )
        {
            actionLink.setVisible( false );
        }
        else
        {
            Session session = GlobalUtils.getRbacSession( this );
            linksLabel = "Authorized Links for " + session.getUserId();
        }
        myForm = new MyBasePageForm( "commonForm" );
        myForm.setOutputMarkupId( true );
        add( myForm );
    }

    private void addSecureLinks()
    {
        add( new Label( LINKS_LABEL, new PropertyModel<String>( this, LINKS_LABEL ) ) );
        SecureBookmarkablePageLink page1Link = new SecureBookmarkablePageLink( GlobalUtils.BTN_PAGE_1, Page1.class,
            GlobalUtils.ROLE_SUPER +
            "," + GlobalUtils.ROLE_TEST1 );
        add( page1Link );
        SecureBookmarkablePageLink page2Link = new SecureBookmarkablePageLink( GlobalUtils.BTN_PAGE_2, Page2.class,
            GlobalUtils.ROLE_SUPER +
            "," + GlobalUtils.ROLE_TEST2 );
        add( page2Link );
        SecureBookmarkablePageLink page3Link = new SecureBookmarkablePageLink( GlobalUtils.BTN_PAGE_3, Page3.class,
            GlobalUtils.ROLE_SUPER +
            "," + GlobalUtils.ROLE_TEST3 );
        add( page3Link );
    }

    public class MyBasePageForm extends Form
    {
        private ComboBox<UserRole> rolesCB;
        private String roleSelection;
        private List<UserRole> inactiveRoles;
        private ComboBox<UserRole> activeRolesCB;
        private String activeRoleSelection;
        private List<UserRole> activeRoles;

        public MyBasePageForm( String id )
        {
            super( id );
            loadActivatedRoleSets();
            addRoleActivationComboBoxesAndButtons();
        }

        private void addRoleActivationComboBoxesAndButtons()
        {
            rolesCB = new ComboBox<UserRole>( GlobalUtils.INACTIVE_ROLES, new PropertyModel<String>( this,
                "roleSelection" ), inactiveRoles, new ChoiceRenderer<UserRole>( "name" ) );
            rolesCB.setOutputMarkupId( true );
            add( rolesCB );
            add( new SecureIndicatingAjaxButton( GlobalUtils.ROLES_ACTIVATE, "ROLE_TEST_BASE" )
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit( AjaxRequestTarget target, Form<?> form )
                {
                    if ( VUtil.isNotNullOrEmpty( roleSelection ) )
                    {
                        if ( checkAccess( roleSelection, "addActiveRole" ) )
                        {
                            if ( addActiveRole( target, roleSelection ) )
                            {
                                setMyResponsePage();
                            }
                            target.add( form );
                        }
                        else
                        {
                            target.appendJavaScript( ";alert('Unauthorized');" );
                            roleSelection = "";
                        }
                    }
                }

                @Override
                protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
                {
                    super.updateAjaxAttributes( attributes );
                    AjaxCallListener ajaxCallListener = new AjaxCallListener()
                    {
                        @Override
                        public CharSequence getFailureHandler( Component component )
                        {
                            return GlobalUtils.WINDOW_LOCATION_REPLACE_DEMO_HOME_HTML;
                        }
                    };
                    attributes.getAjaxCallListeners().add( ajaxCallListener );
                }
            } );

            activeRolesCB = new ComboBox<UserRole>( GlobalUtils.ACTIVE_ROLES, new PropertyModel<String>( this,
                "activeRoleSelection" ), activeRoles, new ChoiceRenderer<UserRole>( "name" ) );
            activeRolesCB.setOutputMarkupId( true );
            add( activeRolesCB );
            add( new SecureIndicatingAjaxButton( GlobalUtils.ROLES_DEACTIVATE, "ROLE_TEST_BASE" )
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit( AjaxRequestTarget target, Form<?> form )
                {
                    if ( VUtil.isNotNullOrEmpty( activeRoleSelection ) )
                    {
                        if ( checkAccess( activeRoleSelection, "dropActiveRole" ) )
                        {
                            if ( dropActiveRole( target, activeRoleSelection ) )
                            {
                                setMyResponsePage();
                            }
                            target.add( form );
                        }
                        else
                        {
                            target.appendJavaScript( ";alert('Unauthorized');" );
                            activeRoleSelection = "";
                        }
                    }
                }

                @Override
                protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
                {
                    super.updateAjaxAttributes( attributes );
                    AjaxCallListener ajaxCallListener = new AjaxCallListener()
                    {
                        @Override
                        public CharSequence getFailureHandler( Component component )
                        {
                            return GlobalUtils.WINDOW_LOCATION_REPLACE_DEMO_HOME_HTML;
                        }
                    };
                    attributes.getAjaxCallListeners().add( ajaxCallListener );
                }
            } );
        }

        /**
         * Called during page form submission.
         */
        private void setMyResponsePage()
        {
            if ( childPage != null )
            {
                switch ( childPage )
                {
                    case PAGE1:
                        setResponsePage( new Page1() );
                        break;
                    case PAGE2:
                        setResponsePage( new Page2() );
                        break;
                    case PAGE3:
                        setResponsePage( new Page3() );
                        break;
                }
            }
            else
            {
                setResponsePage( new LaunchPage() );
            }
        }

        /**
         * This loads the set of user's activated roles into a local page variable.  It is used for deactivate combo
         * box.
         */
        private void loadActivatedRoleSets()
        {
            Session session = GlobalUtils.getRbacSession( this );
            if ( session != null )
            {
                LOG.info( "get assigned roles for user: " + session.getUserId() );
                try
                {
                    inactiveRoles = reviewMgr.assignedRoles( session.getUser() );
                    // remove inactiveRoles already activated:
                    for ( UserRole activatedRole : session.getRoles() )
                    {
                        inactiveRoles.remove( activatedRole );
                    }
                    inactiveRoles.remove( new UserRole( "ROLE_TEST_USER" ) );
                    LOG.info( "user: " + session.getUserId() + " inactiveRoles for activate list: " + inactiveRoles );
                    activeRoles = session.getRoles();
                    //activeRoles.remove( new UserRole ( "ROLE_TEST_USER" ) );
                }
                catch ( org.openldap.fortress.SecurityException se )
                {
                    String error = "SecurityException getting assigned inactiveRoles for user: " + session.getUserId();
                    LOG.error( error );
                }
            }
        }
    }

    /**
     * Called by the child of this class and used during page's submit operations to determine which page to reload.
     *
     * @param childPage
     */
    protected void setChildPage( ChildPage childPage )
    {
        this.childPage = childPage;
    }

    /**
     * Call RBAC addActiveRole to active role into session.
     *
     * @param target
     * @param roleName
     * @return
     */
    protected boolean addActiveRole( AjaxRequestTarget target, String roleName )
    {
        boolean isSuccessful = false;
        try
        {
            RbacSession session = ( RbacSession ) getSession();
            session.getRbacSession().setWarnings( null );
            accessMgr.addActiveRole( session.getRbacSession(), new UserRole( roleName ) );
            List<Warning> warnings = session.getRbacSession().getWarnings();
            if ( VUtil.isNotNullOrEmpty( warnings ) )
            {
                for ( Warning warning : warnings )
                {
                    //if(warning.getId() == GlobalErrIds.)
                    LOG.info( "Warning: " + warning.getMsg() + " errCode: " + warning.getId() + " name: " + warning
                        .getName() + " type: " + warning.getType().toString() );
                    if ( warning.getType() == Warning.Type.ROLE && warning.getName().equalsIgnoreCase( roleName ) )
                    {
                        String error = warning.getMsg() + " code: " + warning.getId();
                        LOG.error( error );
                        target.appendJavaScript( ";alert('" + error + "');" );
                        return false;
                    }
                }
            }

            getPermissions();
            isSuccessful = true;
            String message = "Activate role name: " + roleName + " successful";
            LOG.info( message );
        }
        catch ( org.openldap.fortress.SecurityException se )
        {
            String msg = "Role selection " + roleName + " activation failed because of ";
            if ( se.getErrorId() == GlobalErrIds.DSD_VALIDATION_FAILED )
            {
                msg += "Dynamic SoD rule violation";
            }
            else if ( se.getErrorId() == GlobalErrIds.URLE_ALREADY_ACTIVE )
            {
                msg += "Role already active in Session";
            }
            else
            {
                msg += "System error: " + se + ", " + "errId=" + se.getErrorId();
            }
            LOG.error( msg );
            target.appendJavaScript( ";alert('" + msg + "');" );
        }
        return isSuccessful;
    }

    /**
     * Call RBAC dropActiveRole to deactivate role from session.
     *
     * @param target
     * @param roleName
     * @return
     */
    protected boolean dropActiveRole( AjaxRequestTarget target, String roleName )
    {
        boolean isSuccessful = false;
        try
        {
            RbacSession session = ( RbacSession ) getSession();
            accessMgr.dropActiveRole( session.getRbacSession(), new UserRole( roleName ) );
            getPermissions();
            isSuccessful = true;
            LOG.info( "Fortress dropActiveRole roleName: " + roleName + " was successful" );
        }
        catch ( org.openldap.fortress.SecurityException se )
        {
            String msg = "Role selection " + roleName + " deactivation failed because of ";
            if ( se.getErrorId() == GlobalErrIds.URLE_NOT_ACTIVE )
            {
                msg += "Role not active in session";
            }
            else
            {
                msg += "System error: " + se + ", " + "errId=" + se.getErrorId();
            }
            LOG.error( msg );
            target.appendJavaScript( ";alert('" + msg + "');" );
        }
        return isSuccessful;
    }

    /**
     * Deserialize any object
     * @param str
     * @param cls
     * @return
     */
    public static <T> T deserialize(String str, Class<T> cls)
    {
        // deserialize the object
        try
        {
            // This encoding induces a bijection between byte[] and String (unlike UTF-8)
            byte b[] = str.getBytes("ISO-8859-1");
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            return cls.cast(si.readObject());
        } catch (Exception e) {
            // TODO: handle properly:
            e.printStackTrace();
        }
        return null;
    }

        /**
         * Call Fortress createSession and load into the Wicket session object
         *
         * @return
         */
    private boolean initializeRbacSession()
    {
        HttpServletRequest servletReq = ( HttpServletRequest ) getRequest().getContainerRequest();
        Principal principal = servletReq.getUserPrincipal();
        if(principal != null)
        {
            String szPrincipal = principal.toString();
            //LOG.info( "context: " + szPrincipal);
            Session realmSession = deserialize(szPrincipal, Session.class);
            if(realmSession != null)
                LOG.info( "realmSession user: " + realmSession.getUserId() );

        }

        boolean isLoggedIn = principal != null;
        if ( isLoggedIn )
        {
            // TODO: make sure this is necessary:
            synchronized ( ( RbacSession ) RbacSession.get() )
            {
                if ( GlobalUtils.getRbacSession( this ) == null )
                {
                    try
                    {
                        // Create an RBAC session and attach to Wicket session:
                        User inUser = new User( principal.getName() );
                        User outUser = reviewMgr.readUser( inUser );
                        String szRolesToActivate = outUser.getProperty( "fortressdemo1" );
                        String[] tokens = StringUtils.splitPreserveAllTokens( szRolesToActivate, "," );
                        inUser.setRole( new UserRole( tokens[0] ) );
                        inUser.setRole( new UserRole( tokens[1] ) );

                        // This role enables user to activate/inactivate other roles:
                        //inUser.setRole( new UserRole( "ROLE_TEST_USER" ) );
                        Session session = accessMgr.createSession( inUser, true );
                        String message = "RBAC Session successfully created for userId: " + session.getUserId();
                        LOG.info( message );
                        ( ( RbacSession ) RbacSession.get() ).setSession( session );
                        getPermissions();
                    }
                    catch ( org.openldap.fortress.SecurityException se )
                    {
                        String error = "caught SecurityException=" + se;
                        LOG.error( error );
                        throw new RuntimeException( error );
                    }
                }
            }
        }
        return isLoggedIn;
    }

    /**
     * Retrieve RBAC session permissions from Fortress and place in the Wicket session.
     */
    private void getPermissions()
    {
        try
        {
            if ( GlobalUtils.IS_PERM_CACHED )
            {
                RbacSession session = ( RbacSession ) getSession();
                List<Permission> permissions = accessMgr.sessionPermissions( session.getRbacSession() );
                ( ( RbacSession ) RbacSession.get() ).setPermissions( permissions );
            }
        }
        catch ( org.openldap.fortress.SecurityException se )
        {
            String error = "getPermissions caught SecurityException=" + se;
            LOG.error( error );
            throw new RuntimeException( error );
        }
    }
}