This is free and unencumbered software released into the public domain.
___________________________________________________________________________________
###################################################################################
README for Wicket, Java EE, Spring and Fortress Security Sample
Last updated: Sep 11, 2013
___________________________________________________________________________________
###################################################################################
# Prerequisites
###################################################################################
1. Internet access to retrieve dependencies from online Maven repo.
2. Java SDK Version 7 or beyond installed to target environment
3. Fortress, Sentry, Apache Ant, Maven, Tomcat and Sentry installed (instructions follow)
___________________________________________________________________________________
###################################################################################
# Section 0: Instructions for installing Fortress and configuring Tomcat to use it for container security
###################################################################################
Note: This demo uses Apache Tomcat, Maven, Ant, OpenLDAP and Fortress Realm to download, build and configure
target environment.  All of these dependencies (and more) will be installed to your machine after successful completion
of this section.

1. Download one of the Fortress Quickstart package for Linux from here: http://iamfortress.org/download
*** Note: For this demo you must choose one of the four linux packages as the windows quickstart does not include Tomcat server.

2. Follow these steps: http://www.jts.us/iamfortress/guides/README-QUICKSTART.html:
http://www.jts.us/iamfortress/guides/README-QUICKSTART.html

*** Note: This guide also in root of Fortress Quickstart download package.

Perform Steps I, II, III and IX (only)
I. Instructions to extract and configure Fortress Builder Package to Target System
II. Instructions to run the Fortress Ant Build
III. Instructions to run the Builder to Install Symas OpenLDAP, configure and load with seed data
IX. Instructions to install Commander Web UI Demo

***
Note: After completing Step IX above Fortress' Web administration UI will be deployed to Tomcat.
To use Admin UI:  http://localhost:8080/commander
at the login prompt enter: test/test
***

3. Run the fortress demo policy load.
This is necessary to load test users, roles and permissions to LDAP
Note: The fortress demo policy is here:
https://github.com/shawnmckinney/wicketsecurityfortress/blob/master/demo-fortressproject-users.xml

a. Copy the demo-fortressproject-users.xml file to FORTRESS/ldap/setup folder under Quickstart package.
b. from FORTRESS root run this command:
./b.sh admin -Dparam1=ldap/setup/demo-fortressproject-users.xml

after this steps runs you will see this:

admin:
     [echo] ###############  Run Fortress Ant script ldap/setup/demo-fortressproject-users.xml  ###############
 [startAnt] Buildfile: /home/smckinn/fortress/builder/ldap/setup/demo-fortressproject-users.xml
 [startAnt]
 [startAnt] all:
 [startAnt] [FortressAdmin] ConnectionPool (Wed Sep 11 10:32:19 PDT 2013) : adding a connection to pool...
 [startAnt]
 [startAnt] BUILD SUCCESSFUL
 [startAnt] Total time: 2 seconds

___________________________________________________________________________________
###################################################################################
# Section 1: Instructions for building and running this sample application
###################################################################################
1. Download package: https://github.com/shawnmckinney/wicketsecurity/archive/master.zip
2. Extract
3. Set JAVA_HOME
4. Set M2_Home
5. Run the build from the root package:
$ mvn tomcat:deploy
6. Go to this URL:
http://localhost:8080/myfortressproject
7. Use the credentials defined in demo-fortressproject-users.xml (from step installing Fortress step 3):
user1/password
user2/password
user3/password
___________________________________________________________________________________
###################################################################################
# Section 3: Sample Policy with Fortress
###################################################################################
user1 can view all pages but only buttons on Page1
user2 can only view Page2 and Page2 buttons
user3 can only view Page3 and Page3 buttons