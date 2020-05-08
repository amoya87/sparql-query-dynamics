package cl.uchile.dcc.dynamics.utils;

//make the class non-extendable by adding final
public final class Constants {
//Hide the constructor
private Constants(){}

//triple
public static String TRIPLE_REGEX = "^(\\S+)\\s+(<[^>]+>)\\s+(.+\\S+)\\s*.$";

//triple
public static int TICKS = 100000000;
}