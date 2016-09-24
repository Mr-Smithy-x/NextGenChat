package co.yoprice.nextgenchat.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import co.yoprice.nextgenchat.data.jsinterface.NGUCallBack;
import co.yoprice.nextgenchat.data.models.Color;
import co.yoprice.nextgenchat.data.models.Message;
import co.yoprice.nextgenchat.data.models.NGUSmilies;

/**
 * Created by cj on 4/26/16.
 */

public class NGUClient {

    //region Fields
    private static NGUClient ngu = new NGUClient();
    private WebView webView;
    private List<NGUCallBackListener> listeners = new ArrayList<NGUCallBackListener>();
    private String username;
    private String userId;
    private static String USER_ICON_STUB = "http://www.nextgenupdate.com/forums/image.php?u=%s";
    public final static String mobile_chat_link = "http://www.nextgenupdate.com/forums/mobile_chat.php";
    public final static String mobile_chat_text = "mobile_chat.php";
    private static boolean simple = true;
    private boolean loggedIn = false;
    private ArrayList<NGUSmilies> nguSmilies;
    //endregion

    //region JavaScript Invoke
    private static String jsUserStub = "javascript:window.HTMLOUT.processLoginUsername(document.getElementById('vb_login_username').value = '%s');";
    private static String jsPassStub = "javascript:window.HTMLOUT.processLoginPassword(document.getElementById('vb_login_password').value = '%s');";
    private static String jsLogin = "javascript:window.HTMLOUT.processLogin(document.getElementsByTagName('button')[0].click());";
    private static String jsGetPageContent = "javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');";
    private static String jsShoutStub = "javascript:window.HTMLOUT.processHTML(document.getElementById('vbshout_pro_shoutbox_editor').value='%s');";
    private static String jsShoutSubmit = "javascript:window.HTMLOUT.processHTML(InfernoShoutboxControl.shout());";
    private static String jsGetShoutBoxFrame = "javascript:window.HTMLOUT.processShout(document.getElementById('shoutbox_frame').outerHTML)";
    private static String jsUnidle = "javascript:window.HTMLOUT.processUnidle(InfernoShoutboxControl.unidle())";
    //endregion

    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public ArrayList<NGUSmilies> getSmilies() {
        return nguSmilies;
    }

    public interface NGUCallBackListener {
        void OnShouted(String text);

        void OnLoggedIn();

        void OnNotLoggedIn();

        void OnMessageRetrieved(ArrayList<Message> messages);

        void OnNewLogin();
    }

    public void Login(String username, String password) {
        Log.e("URL", getWebView().getUrl());
        if (webView.getUrl().equalsIgnoreCase(mobile_chat_link)) {
            webView.loadUrl(String.format(jsUserStub, username));
            webView.loadUrl(String.format(jsPassStub, password));
            webView.loadUrl(String.format(jsLogin));
        }
    }

    public void addListener(NGUCallBackListener listener) {
        listeners.add(listener);
    }

    public static void initialize(WebView webView) {
        ngu.setWebView(webView);
    }

    public static NGUClient getInstance() {
        return ngu;
    }

    public WebView getWebView() {
        return webView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void startClient(final Handler handler) {
        nguSmilies = MediaParser.getSmilies(getWebView().getContext());
        getWebView().getSettings().setAllowContentAccess(true);
        getWebView().getSettings().setJavaScriptEnabled(true);
        getWebView().addJavascriptInterface(new NGUCallBack() {
            @JavascriptInterface
            @Override
            public void processHTML(String html) {
                if (html.contains("Username") && html.contains("Password") && html.contains("Sign in to use chat.")) {
                    aLog.i("Not Logged In", "Not Logged In");
                    for (final NGUCallBackListener callback : listeners) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.OnNotLoggedIn();
                            }
                        });
                    }
                } else if (html.contains("Thank you for logging in")) {
                    aLog.i("Logged In", "User Logged In");
                    loggedIn = true;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (NGUCallBackListener listener : listeners) {
                                listener.OnNewLogin();

                            }
                        }
                    }, 3000);
                } else if (html.contains("Active Users")) {
                    start();
                    dumpCookies();
                    aLog.i("Start Session", "Threading Started");
                    for (final NGUCallBackListener callback : listeners) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.OnLoggedIn();
                            }
                        });
                    }

                } else if (html.contains("You have entered an invalid username or password.")) {
                    aLog.e("Invalid Password", "User entered Invalid Password");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            getWebView().loadUrl(mobile_chat_link);
                        }
                    });
                }
            }

            @JavascriptInterface
            @Override
            public void processShout(final String html) throws IOException {
                onMessageRecieved(Parse(html));
            }

            @JavascriptInterface
            @Override
            public void processUnidle(String html) {
                // Log.e("Un-idled", html);
            }

            @JavascriptInterface
            @Override
            public void processLoginUsername(String username) {
                aLog.i("NGUCALLBACK-USER", username);
                NGUClient.getInstance().setUserName(username);
            }

            @JavascriptInterface
            @Override
            public void processLoginPassword(String pass) {
                aLog.i("NGUCALLBACK-PASS", pass);
            }

            @JavascriptInterface
            @Override
            public void processLogin(String html) {
                aLog.i("NGUCALLBACK-LOGIN", html);
            }


        }, "HTMLOUT");
        getWebView().setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl(jsGetPageContent);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                aLog.i("Overriding URL: ", url);
                return true;
            }

        });
        getWebView().loadUrl(mobile_chat_link);
    }

    public String generateUserIcon() {
        return String.format(USER_ICON_STUB, userId);
    }

    public String generateUserIcon(String userId) {
        return String.format(USER_ICON_STUB, userId);
    }

    public void stop() {
        if (thread.isAlive())
            thread.stop();
    }

    private void dumpCookies() {
        String cookies = CookieManager.getInstance().getCookie(mobile_chat_link);
        aLog.w("Cookies", cookies);
        int index = cookies.indexOf("bb_userid=") + "bb_userid=".length();
        if (index > -1) {
            String user_id = cookies.substring(index);
            user_id = user_id.substring(0, user_id.indexOf(";"));
            aLog.i("user_id", user_id);
            this.userId = user_id;
        }
    }

    public void shout(String text) {
        if (webView.getUrl().contains(mobile_chat_text)) {
            webView.loadUrl(String.format(jsShoutStub, text.replace("'", "\\'")));
            webView.loadUrl(jsShoutSubmit);
            for (NGUCallBackListener callback : listeners) {
                callback.OnShouted(text);
            }
            refresh();
        }
    }

    //region Parse Message Static Functions
    public static ArrayList<Message> Parse(Context context) throws IOException {
        String fileLoc = "/storage/emulated/0/AppProjects/NGUParser/file.txt";
        File file = new File(fileLoc);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String content = null;
        StringBuilder sb = new StringBuilder();
        while ((content = br.readLine()) != null) {
            sb.append(content);
        }
        br.close();
        return Parse(content);
    }

    public static ArrayList<Message> Parse(String text) throws IOException {
        return Parse(Jsoup.parse(text, "UTF-8"));
    }

    public static ArrayList<Message> Parse(Document doc) throws IOException {
        ArrayList<Message> messages = new ArrayList<>();
        Elements divs = doc.getElementsByTag("div");
        for (Element div : divs) {
            if (divs.indexOf(div) == 0) continue;
            else if (div.text().startsWith("Notice")) continue;
            else if (div.text().startsWith("*")) {
                messages.add(Message.Builder().toggleActionMessage(true).setMessage(div.text()).setMessageElement(div));
            } else {
                Message m = ParseMessage(div);
                if (m != null) {
                    messages.add(m);
                }
            }
        }
        return messages;
    }

    private static Message ParseMessage(Element div) {
        ArrayList<String> smilies = new ArrayList<>();
        Elements childs = div.children();
        Element time = childs.get(0), user = null, messageElement = null;
        String message = null, username = null;
        switch (childs.size()) {
            case 3:

                break;
            //region 4
            case 4:
                user = childs.get(2);
                username = user.text();
                messageElement = childs.get(3);
                for (TextNode tn : div.textNodes()) {
                    if (tn.text().startsWith(": ")) {
                        message = tn.text().substring(2);
                        break;
                    }
                }
                if (message == null) message = messageElement.text();
                for (Element img : div.getElementsByTag("img")) {
                    String imageUrl = img.attr("src");
                    smilies.add(imageUrl);
                }
                break;
            //endregion
            //region 5
            case 5:
                user = childs.get(3);
                messageElement = childs.get(4);
                message = messageElement.text();
                for (Element img : div.getElementsByTag("img")) {
                    String imageUrl = img.attr("src");
                    smilies.add(imageUrl);
                }
                username = user.text();
                if (message.isEmpty()) {
                    for (TextNode tn : div.textNodes()) {
                        if (tn.text().startsWith(": ")) {
                            message = tn.text();
                            break;
                        }
                    }
                }
                break;
            //endregion
            //region 6
            case 6:
                user = childs.get(4);
                username = user.text();
                messageElement = childs.get(5);
                message = messageElement.text();
                for (Element img : div.getElementsByTag("img")) {
                    String imageUrl = img.attr("src");
                    smilies.add(imageUrl);
                }
                break;
            //endregion
            //region 7
            case 7:
                user = childs.get(3);
                username = user.text();
                messageElement = childs.get(4).append(childs.get(5).html()).append(childs.get(6).html());
                message = messageElement.text();
                for (Element img : div.getElementsByTag("img")) {
                    String imageUrl = img.attr("src");
                    smilies.add(imageUrl);
                }
                break;
            //endregion
        }
        String link = null;
        if (messageElement != null && messageElement.getElementsByTag("a") != null && messageElement.getElementsByTag("a").size() > 0) {
            link = messageElement.getElementsByTag("a").attr("href");
        }
        String user_id = null;
        for (Element e : div.getElementsByTag("a")) {
            if (e.hasAttr("onclick") && e.attr("href").equals("#")) {
                String onclick = e.attr("onclick");
                if (onclick.startsWith("return")) {
                    onclick = onclick.replace("return InfernoShoutboxControl.open_pm_tab('pm_", "");
                    int index = onclick.indexOf("'");
                    user_id = onclick.substring(0, index).replace("'", "");
                    break;
                }
            }
        }

        return Message.Builder().setTime(time.text())
                .setMessage(message).setUser(username)
                .setMessageColor(Color.getColorFont(messageElement))
                .setUser_color(Color.getColor(user)).setSmilies(smilies)
                .setLink(link).setUserId(user_id).setMessageElement(messageElement);
        //   .setUserElement(user);
    }
    //endregion

    //region Private Functions
    private void onMessageRecieved(ArrayList<Message> messageList) {
        for (NGUCallBackListener callback : listeners) {
            callback.OnMessageRetrieved(messageList);
        }
    }

    private void setWebView(WebView webView) {
        this.webView = webView;
    }

    private void refresh() {
        webView.loadUrl(jsGetShoutBoxFrame);
        webView.loadUrl(jsUnidle);
    }

    private final Handler handler = new Handler();
    private boolean started = false;

    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (started) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        }
    });

    private void start() {
        started = true;
        if (!thread.isAlive()) {
            aLog.w("Thread Started", "Thread");
            thread.start();
        } else {
            aLog.w("Thread Not Started", "Thread Err");
        }
    }
    //endregion

}