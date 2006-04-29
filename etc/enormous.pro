#
# $Id$
#
# Proguard configuration file for That's Enormous!

-basedirectory ../

-injars dist/enormous.jar
-injars lib/samskivert.jar(!META-INF/*,!**/velocity/**,!**/xml/**)
-injars lib/narya-base.jar(!META-INF/*,!**/tools/**)
-injars lib/narya-media.jar(!META-INF/*,!**/tools/**)

-libraryjars lib/javalayer.jar
-libraryjars <java.home>/lib/rt.jar

-outjars dist/enormous-pro.jar
-printseeds dist/proguard.seeds
-printmapping dist/proguard.map

-keep public class com.samskivert.enormous.EnormousApp {
    public static void main (java.lang.String[]);
}
