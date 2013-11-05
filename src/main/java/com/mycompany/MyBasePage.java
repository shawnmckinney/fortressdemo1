/*
 * This is free and unencumbered software released into the public domain.
 */
package com.mycompany;

import com.googlecode.wicket.jquery.ui.kendo.combobox.ComboBox;
import com.googlecode.wicket.jquery.ui.kendo.combobox.ComboBoxRenderer;
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
import us.jts.fortress.AccessMgr;
import us.jts.fortress.GlobalErrIds;
import us.jts.fortress.ReviewMgr;
import us.jts.fortress.rbac.Permission;
import us.jts.fortress.rbac.Session;
import us.jts.fortress.rbac.User;
import us.jts.fortress.rbac.UserRole;
import us.jts.fortress.util.attr.VUtil;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

/**
 * Base class for Fortressdemo1 Wicket sample project.  It contains security control functions to demonstrate ANSI RBAC security concepts.
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
        infoTA = new TextArea<String>("infoField", Model.of( infoField ) );
        add( infoTA );
        add( new Label( "footer", GlobalUtils.FOOTER ) );
        if(!initializeRbacSession())
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
            GlobalUtils.ROLE_TEST_1 );
        add( page1Link );
        SecureBookmarkablePageLink page2Link = new SecureBookmarkablePageLink( GlobalUtils.BTN_PAGE_2, Page2.class,
            GlobalUtils.ROLE_TEST_1 +
            "," + GlobalUtils.ROLE_TEST2 );
        add( page2Link );
        SecureBookmarkablePageLink page3Link = new SecureBookmarkablePageLink( GlobalUtils.BTN_PAGE_3, Page3.class,
            GlobalUtils.ROLE_TEST_1 +
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
                "roleSelection" ), inactiveRoles, new ComboBoxRenderer<UserRole>( "name" ) );
            rolesCB.setOutputMarkupId( true );
            add( rolesCB );
            add( new SecureIndicatingAjaxButton( this, GlobalUtils.ROLES_ACTIVATE,
                "us.jts.fortress.rbac.AccessMgrImpl", "addActiveRole" )
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit( AjaxRequestTarget target, Form<?> form )
                {
                    if ( checkAccess() )
                    {
                        if ( VUtil.isNotNullOrEmpty( roleSelection ) )
                        {
                            if ( addActiveRole( target, roleSelection ) )
                            {
                                setMyResponsePage();
                            }
                            target.add( form );
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
                "activeRoleSelection" ), activeRoles, new ComboBoxRenderer<UserRole>( "name" ) );
            activeRolesCB.setOutputMarkupId( true );
            add( activeRolesCB );
            add( new SecureIndicatingAjaxButton( this, GlobalUtils.ROLES_DEACTIVATE,
                "us.jts.fortress.rbac.AccessMgrImpl", "dropActiveRole" )
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit( AjaxRequestTarget target, Form<?> form )
                {
                    if ( checkAccess() )
                    {
                        if ( VUtil.isNotNullOrEmpty( activeRoleSelection ) )
                        {
                            if ( dropActiveRole( target, activeRoleSelection ) )
                            {
                                setMyResponsePage();
                            }
                            target.add( form );
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
         * This loads the set of user's activated roles into a local page variable.  It is used for deactivate combo box.
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

                    LOG.info( "user: " + session.getUserId() + " inactiveRoles for activate list: " + inactiveRoles );
                    activeRoles = session.getRoles();
                }
                catch ( us.jts.fortress.SecurityException se )
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
            accessMgr.addActiveRole( session.getRbacSession(), new UserRole( roleName ) );
            getPermissions();
            isSuccessful = true;
            String message = "Fortress addActiveRole roleName: " + roleName + " was successful";
            LOG.info( message );
        }
        catch ( us.jts.fortress.SecurityException se )
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
        catch ( us.jts.fortress.SecurityException se )
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
     * Call Fortress createSession and load into the Wicket session object
     *
     * @return
     */
    private boolean initializeRbacSession()
    {
        HttpServletRequest servletReq = ( HttpServletRequest ) getRequest().getContainerRequest();
        Principal principal = servletReq.getUserPrincipal();
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
                        String szRoleToActivate = outUser.getProperty( "fortressdemo1" );
                        inUser.setRole( new UserRole( szRoleToActivate ) );
                        Session session = accessMgr.createSession( inUser, true );
                        String message = "RBAC Session successfully created for userId: " + session.getUserId();
                        LOG.info( message );
                        ( ( RbacSession ) RbacSession.get() ).setSession( session );
                        getPermissions();
                    }
                    catch ( us.jts.fortress.SecurityException se )
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
        catch ( us.jts.fortress.SecurityException se )
        {
            String error = "getPermissions caught SecurityException=" + se;
            LOG.error( error );
            throw new RuntimeException( error );
        }
    }
}