package co.yoprice.nextgenchat.data;


public class Constants {
    /*
      Logging flag
     */
    public static final boolean LOGGING = false;
    /*
      Your imgur client id. You need this to upload to imgur.
      More here: https://api.imgur.com/
     */
    public static final String MY_IMGUR_CLIENT_ID = "41f2083d3bb72e6";
    /*
      Redirect URL for android.
     */
    public static final String MY_IMGUR_REDIRECT_URL = "http://github.com/Mr-Smithy-x";
    /*
      Client Auth
     */
    public static String getClientAuth() {
        return "Client-ID " + MY_IMGUR_CLIENT_ID;
    }

}