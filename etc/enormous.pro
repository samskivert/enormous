#
# $Id$
#
# Proguard configuration file for That's Enormous!

-basedirectory ../

-injars dist/enormous.jar
-injars lib/samskivert.jar(com/samskivert/Log.class,**/swing/**,**/util/**)
-injars lib/nenya-media.jar(!META-INF/*,!**/tools/**)
-injars lib/google-collect.jar(!META-INF/*)

-libraryjars lib/javalayer.jar
-libraryjars <java.home>/lib/rt.jar

-dontobfuscate
-dontskipnonpubliclibraryclasses
-outjars dist/enormous-pro.jar
-printseeds dist/proguard.seeds
-printmapping dist/proguard.map

-keep public class com.samskivert.enormous.EnormousApp {
    public static void main (java.lang.String[]);
}
