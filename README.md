# AppBoot

A Java application startup utility that prepares a boot class-loader with jar-files found in an application directory.

AppBoot does not work with fat-jars but instead searches for files and places them in a class-loader 
that is used when the main class of your application is started.
Advantages of this method include the option to load resources instead of files in your source code
and the option to override defaults with any files placed in the <tt>conf</tt> directory
(AppBoot places the conf- (or home) and lib-directory on the classpath before other jar-files). 

For more about class-loading (using fat-jars) at startup, see <http://www.jdotsoft.com/JarClassLoader.php>
and <https://docs.spring.io/spring-boot/docs/current/reference/html/executable-jar.html>

To install:

	mvn clean install
	
Full build for core:

	cd appboot
	mvn clean verify assembly:single

Usage (shown with <tt>java -jar appboot.jar</tt>):

	AppBoot version 1.0.0
	
	Creates a boot class-loader and runs the main class of the application.
	Resources (e.g. the appboot.jar) for the application must be located
	in the home-directory of the application and/or the 'home-dir/lib' directory.
	Parameters can be specified via a command-line argument, a Java system property (using the -D switch)
	or an environment variable (uppercased with underscores, e.g. APP_NAME).
	Double quotes surrounding parameter values are removed from the values.
	Parameters for AppBoot:
	app.main.class  : the main class of the application to run, or use
	app.name        : the name of the application to run.
	        If no name is set, AppBoot will use the name of the home-directory.
	        AppBoot will look for a jar-file starting with the application name.
	        The main-class specified in the manifest of the jar-file will be used to start the application.
	app.home.dir    : the home directory of the application (optional).
	app.conf.dir    : the configuration directory of the application (optional, default "conf").
	        The configuration directory is added to the boot-class loader
	        so that configuration files can be opened as a resource in the application.
	app.lib.dirname : the name of the lib-directory containing application dependencies (optional, default "lib").
	app.boot.debug  : a switch (no need to specifiy a value) to let AppBoot show debug-output.
	app.maven.test  : a switch to run AppBoot from a Maven target/test-classes directory.
	        If this switch is used, app.main.class must also be used.
	
	All AppBoot parameters are removed from the command line arguments before the application main class is started.

If AppBoot fails to start a runtime exception will be thrown (resulting in exit code 1).
In some cases (e.g. when app-name is only derived from the home-directory) the usage information shown above 
is printed on the console before the runtime exception is thrown.

AppBoot has some protection against starting itself.
If AppBoot starts itself an infinite loop occurs (eventually resulting in a stackoverflow exception).

Jar-files which are already on the class-path (and in the system class-loader) are not added to the boot class-loader.
This prevents loading the same class twice via different class-loaders.  

To run the demo:

	cd appboot-demo
	mvn clean package
	target/test-classes/rundemo	
	target/appboot-demo-<version>/run	
	
More usage information is in <tt>appboot-demo</tt> (especially the <tt>pom.xml</tt>) and in the Javadoc of
<tt>com.github.fwi.appboot.AppBoot.java</tt>

### AppBoot custom

The <tt>AppBoot.stop</tt> method is designed to receive events from Apache Commons Daemon (see the Javadoc on the method).
But what if an external service expects to see other methods on the main startup-class of your application?
That would require copying <tt>AppBoot</tt> and adding methods to it which is not ideal (code duplication instead of code re-use).
On top of that, you still cannot use dependencies that are added via the boot class-loader.

The <tt>appboot-custom</tt> module shows a method to work around that. The <tt>appboot-custom/pom.xml</tt>
shows how to create a custom <tt>"myappboot.jar"</tt> with classes copied from the original <tt>appboot.jar</tt>.
One additional class is created (<tt>MyAppBoot.java</tt>) that now serves as main class and can have any additional methods
to receive system/service events. The <tt>fireEvent</tt> method is used to send the events to a class that lives
in your application and has the option to use any dependencies (<tt>MyAppSysEvent.java</tt>).

To create the custom jar:

	cd appboot-custom
	mvn clean package

### Credits

This updated version of AppBoot was originally from <https://github.com/intercommit/basic-jsp-embed>
 