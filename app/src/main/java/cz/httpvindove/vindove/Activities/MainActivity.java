package cz.httpvindove.vindove.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.internal.NavigationMenuView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityManagerCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;
import java.net.URLDecoder;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import cz.httpvindove.vindove.Borrowed.HandleXML;
import cz.httpvindove.vindove.Borrowed.Utils;
import cz.httpvindove.vindove.Functions.Equinox;
import cz.httpvindove.vindove.Functions.MoonSun;
import cz.httpvindove.vindove.Functions.NetworkHelper;
import cz.httpvindove.vindove.R;

// https://developer.android.com/studio/publish/app-signing.html#secure-key
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Show web view.
     */
    private boolean showWebView;
    private String lastURL;
    private boolean clearHistory;

    static final int INTERNET_PERMISSION = 7;

    /**
     * Is Internet permission?
     * @return boolean
     */
    public  boolean isInternetPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED) {
                // Log.v(TAG,"Permission is granted");
                return true;
            } else {
                // Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            // Log.v(TAG,"Permission is granted");
            return true;
        }
    }
    public  boolean isStatePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                // Log.v(TAG,"Permission is granted");
                return true;
            } else {
                // Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, INTERNET_PERMISSION);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            // Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            if (requestCode == INTERNET_PERMISSION) {
                showInternet();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // default
        showWebView = false;
        lastURL = "";
        clearHistory = false;

        // register
        final WebView webView = (WebView)findViewById(R.id.mainWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        // other
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }
        // important!
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                if (clearHistory) {
                    //clearHistory = false;
                    webView.clearHistory();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (clearHistory) {
                    clearHistory = false;
                    webView.clearHistory();
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });

        // moon
        TextView moonText = (TextView)findViewById(R.id.moonText);
        TextView moonEvent = (TextView)findViewById(R.id.moonEvent);
        TextView moonName = (TextView)findViewById(R.id.moonName);
        TextView moonNumber = (TextView)findViewById(R.id.moonNumber);
        double moonAge = MoonSun.moonAge();
        int numberOfMoon = MoonSun.numberOfMoon();
        // info
        byte showInfo = 0;

        // till
        // 29.53059
        int tillFull = (int)((29.53059 / 2.0) - moonAge);
        int tillNew = (int)(29.53059 - moonAge);
        String celeb = getMoonTextCelebration(numberOfMoon);
        if (tillFull == 0) {
            // full moon
            if (celeb.equals(""))
                moonEvent.setVisibility(View.GONE);
            else
                moonEvent.setText(celeb);
            // full moon
            moonText.setTypeface(moonText.getTypeface(), Typeface.BOLD);
            moonText.setText(getResources().getString(R.string.moon_full));
        } else if (tillNew == 0) {
            moonEvent.setVisibility(View.GONE);
            // new moon
            moonText.setTypeface(moonText.getTypeface(), Typeface.BOLD);
            moonText.setText(getResources().getString(R.string.moon_new));
        } else if (tillFull > 0) {
            if (tillFull == 1) {
                moonEvent.setTypeface(moonText.getTypeface(), Typeface.BOLD);
                moonEvent.setText(getResources().getString(R.string.tomorrow_full));
                // growing
                if (celeb.equals(""))
                    moonText.setText(getResources().getString(R.string.moon_growing));
                else
                    moonText.setText(celeb);
            } else {
                showInfo = 1;
                moonEvent.setText(getResources().getString(R.string.till_full) +
                    " " + String.valueOf(tillFull));
                // growing
                moonText.setText(getResources().getString(R.string.moon_growing));
            }
        } else {
            if (tillNew == 1) {
                moonEvent.setTypeface(moonText.getTypeface(), Typeface.BOLD);
                moonEvent.setText(getResources().getString(R.string.tomorrow_new));
            } else {
                showInfo = 2;
                moonEvent.setText(getResources().getString(R.string.till_new) +
                        " " + String.valueOf(tillNew));
            }
            // fading
            moonText.setText(getResources().getString(R.string.moon_fading));
        }
        // show info
        if (showInfo > 0) {
            final byte showInfoFinal = showInfo;
            ImageView infoMoon = (ImageView)findViewById(R.id.infoMoon);
            infoMoon.setVisibility(View.VISIBLE);
            infoMoon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickMoonInfo(showInfoFinal);
                }
            });
            findViewById(R.id.moonEvent).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickMoonInfo(showInfoFinal);
                }
            });
        }

        // number
        String moon = "";
        switch (numberOfMoon) {
            case 1:
                moon = getResources().getString(R.string.moon_1);
                break;
            case 2:
                moon = getResources().getString(R.string.moon_2);
                break;
            case 3:
                moon = getResources().getString(R.string.moon_3);
                break;
            case 4:
                moon = getResources().getString(R.string.moon_4);
                break;
            case 5:
                moon = getResources().getString(R.string.moon_5);
                break;
            case 6:
                moon = getResources().getString(R.string.moon_6);
                break;
            case 7:
                moon = getResources().getString(R.string.moon_7);
                break;
            case 8:
                moon = getResources().getString(R.string.moon_8);
                break;
            case 9:
                moon = getResources().getString(R.string.moon_9);
                break;
            case 10:
                moon = getResources().getString(R.string.moon_10);
                break;
            case 11:
                moon = getResources().getString(R.string.moon_11);
                break;
            case 12:
            case 0:
            default:
                moon = getResources().getString(R.string.moon_12);
                break;
        }
        moonName.setText(moon);
        if (numberOfMoon != 0) {
            moonNumber.setText(String.valueOf(numberOfMoon) + ". " +
                getResources().getString(R.string.moon_after_solstice));
        }

        // sun
        TextView solWText = (TextView)findViewById(R.id.solW);
        TextView solSText = (TextView)findViewById(R.id.solS);
        TextView equSText = (TextView)findViewById(R.id.equS);
        TextView equAText = (TextView)findViewById(R.id.equA);
        TextView today = (TextView)findViewById(R.id.today);

        // today
        Calendar cal = Calendar.getInstance();
        today.setText( getResources().getString(R.string.sun_today) + " " +
                String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + ". " +
                String.valueOf(cal.get(Calendar.MONTH) + 1) + ". " +
                String.valueOf(cal.get(Calendar.YEAR)));

        // sol/equ
        Equinox equinox = MoonSun.equinoxSolstice();
        if (equinox.getDecemberSolstice().getHours() >= 16) {
            solWText.setText(formatFromDateTime(equinox.getDecemberSolstice())
                    + "\n" + getResources().getString(R.string.sunset_after));
        } else {
            solWText.setText(formatFromDateTime(equinox.getDecemberSolstice())
                    + "\n" + getResources().getString(R.string.sunset_before));
        }
        if (equinox.getJuneSolstice().getHours() >= 20) {
            solSText.setText(formatFromDateTime(equinox.getJuneSolstice())
                    + "\n" + getResources().getString(R.string.sunset_after));
        } else {
            solSText.setText(formatFromDateTime(equinox.getJuneSolstice())
                    + "\n" + getResources().getString(R.string.sunset_before));
        }
        if (equinox.getMarchEquinox().getHours() >= 18) {
            equSText.setText(formatFromDateTime(equinox.getMarchEquinox())
                    + "\n" + getResources().getString(R.string.sunset_after));
        } else {
            equSText.setText(formatFromDateTime(equinox.getMarchEquinox())
                    + "\n" + getResources().getString(R.string.sunset_before));
        }
        if (equinox.getSeptemberEquinox().getHours() >= 18) {
            equAText.setText(formatFromDateTime(equinox.getSeptemberEquinox())
                    + "\n" + getResources().getString(R.string.sunset_after));
        } else {
            equAText.setText(formatFromDateTime(equinox.getSeptemberEquinox())
                    + "\n" + getResources().getString(R.string.sunset_before));
        }

        // bold
        int solW = getDifferenceInTime(equinox.getDecemberSolstice());
        int solS = getDifferenceInTime(equinox.getJuneSolstice());
        int equS = getDifferenceInTime(equinox.getMarchEquinox());
        int equA = getDifferenceInTime(equinox.getSeptemberEquinox());
        int min = Math.min(Math.min(Math.min(solW, solS), equS), equA);
        if (min == solW) {
            solWText.setTypeface(moonText.getTypeface(), Typeface.BOLD);
        } else if (min == solS) {
            solSText.setTypeface(moonText.getTypeface(), Typeface.BOLD);
        } else if (min == equS) {
            equSText.setTypeface(moonText.getTypeface(), Typeface.BOLD);
        } else if (min == equA) {
            equAText.setTypeface(moonText.getTypeface(), Typeface.BOLD);
        }

        // click info about months
        findViewById(R.id.moonNameLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPage("file:///android_asset/pocta/mesice.html");
            }
        });

        // show Internet-related things
        if (isInternetPermissionGranted()) {
            showInternet();
        }
    }

    /**
     * Show dialog.
     * @param showInfo byte
     */
    public void onClickMoonInfo(byte showInfo) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
        dlgAlert.setTitle(R.string.moon_info);
        if (showInfo == 1) {
            dlgAlert.setMessage(R.string.full_moon_info);
        } else {
            dlgAlert.setMessage(R.string.new_moon_info);
        }
        dlgAlert.setPositiveButton("Ok", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    /**
     * Get the proper name of the celebration.
     * @param numberOfMoon int
     * @return String
     */
    public String getMoonTextCelebration(int numberOfMoon) {
        String celeb = "";
        if (numberOfMoon == 2) {
            celeb = getResources().getString(R.string.first_moon_celebration);
        } else if (numberOfMoon == 5) {
            celeb = getResources().getString(R.string.second_moon_celebration);
        } else if (numberOfMoon == 8) {
            celeb = getResources().getString(R.string.third_moon_celebration);
        } else if (numberOfMoon == 11) {
            celeb = getResources().getString(R.string.fourth_moon_celebration);
        }
        return celeb;
    }

    /**
     * Get the right date form.
     * @param dateTime Equinox.DateTime
     * @return String
     */
    public String formatFromDateTime(Equinox.DateTime dateTime) {
        return String.valueOf(dateTime.getDay()) + ". " +
                String.valueOf(dateTime.getMonth()) + ". " +
                String.valueOf(dateTime.getYear());
    }

    /**
     * Get difference in time.
     * @param dateTime Equinox.DateTime
     * @return int
     */
    public int getDifferenceInTime(Equinox.DateTime dateTime) {
        Calendar cal = Calendar.getInstance();
        long nowT = cal.getTimeInMillis() / 1000;
        cal.set(dateTime.getYear(), dateTime.getMonth() - 1, dateTime.getDay());
        long timeT = cal.getTimeInMillis() / 1000;
        return (int)Math.abs(timeT - nowT);
    }

    /**
     * Show a random post (story) possibility.
     */
    public void showInternet() {
        if (isStatePermissionGranted()) {
            // show actualities from the online site
            Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (!NetworkHelper.isNetworkAvailable((ConnectivityManager)
                                    getSystemService(Context.CONNECTIVITY_SERVICE))) {
                                sleep(1000); // check every second
                            }
                            // show actualities
                            final HandleXML handleXML = new HandleXML("https://www.vindove.cz/feed");
                            handleXML.fetchXML();
                            while (handleXML.parsingComplete);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        findViewById(R.id.firstArticleLayout).setVisibility(View.VISIBLE);
                                        findViewById(R.id.secondArticleLayout).setVisibility(View.VISIBLE);
                                        findViewById(R.id.firstLabelText).setVisibility(View.VISIBLE);
                                        findViewById(R.id.episodesLayout).setVisibility(View.VISIBLE);
                                        // findViewById(R.id.secondLabelText).setVisibility(View.VISIBLE);

                                        TextView titleEpisode = (TextView)findViewById(R.id.titleFirstArticle);
                                        TextView textEpisode = (TextView)findViewById(R.id.textFirstArticle);
                                        titleEpisode.setText(handleXML.getTitle().get(0));
                                        textEpisode.setText(Utils.ellipsize(handleXML.getDescription().get(0), 75));
                                        final String link1 = handleXML.getLink().get(0);
                                        findViewById(R.id.firstArticleLayout).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showPage(link1);
                                            }
                                        });
                                        titleEpisode = (TextView)findViewById(R.id.titleSecondArticle);
                                        textEpisode = (TextView)findViewById(R.id.textSecondArticle);
                                        titleEpisode.setText(handleXML.getTitle().get(1));
                                        textEpisode.setText(Utils.ellipsize(handleXML.getDescription().get(1), 75));
                                        final String link2 = handleXML.getLink().get(1);
                                        findViewById(R.id.secondArticleLayout).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showPage(link2);
                                            }
                                        });
                                        titleEpisode = (TextView)findViewById(R.id.titleEpisode);
                                        textEpisode = (TextView)findViewById(R.id.textEpisode);
                                        titleEpisode.setText(handleXML.getTitle().get(2));
                                        textEpisode.setText(Utils.ellipsize(handleXML.getDescription().get(2), 75));
                                        final String linkE = handleXML.getLink().get(2);
                                        findViewById(R.id.episodesLayout).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showPage(linkE);
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            /*
                            // episodes
                            final HandleXML handleXML1 = new HandleXML("https://www.vindove.cz/feed?cat=3");
                            handleXML1.fetchXML();
                            while (handleXML1.parsingComplete);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        findViewById(R.id.episodesLayout).setVisibility(View.VISIBLE);
                                        findViewById(R.id.secondLabelText).setVisibility(View.VISIBLE);

                                        TextView titleEpisode = (TextView)findViewById(R.id.titleEpisode);
                                        TextView textEpisode = (TextView)findViewById(R.id.textEpisode);
                                        titleEpisode.setText(handleXML1.getTitle().get(0));
                                        textEpisode.setText(Utils.ellipsize(handleXML1.getDescription().get(0), 75));
                                        final String linkE = handleXML1.getLink().get(0);
                                        findViewById(R.id.episodesLayout).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showPage(linkE);
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            */
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            }
    }

    public void showMain() {
        // design
        showWebView = false;
        findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.webLayout).setVisibility(View.GONE);

        // scroll
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.mainLayout).scrollTo(0, 0);
            }
        }, 200);
    }

    // file:///android_asset/your.html
    public void showPage(String url) {
        // url
        lastURL = url;

        // design
        showWebView = true;
        findViewById(R.id.mainLayout).setVisibility(View.GONE);
        findViewById(R.id.webLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);


        // page
        final WebView webView = (WebView)findViewById(R.id.mainWebView);
        //webView.loadUrl("about:blank");
        if (url.contains("android_asset")) { // offline
            findViewById(R.id.buttonRefresh).setVisibility(View.GONE);
        } else {
            findViewById(R.id.buttonRefresh).setVisibility(View.VISIBLE);
        }
        clearHistory = true;
        webView.clearView();
        webView.loadUrl(url);

        // scroll
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                webView.scrollTo(0, 0);
            }
        }, 200);
    }

    /**
     * Refresh webview with the right URL.
     * @param view View
     */
    public void refreshWeb(View view) {
        WebView browser = (WebView)findViewById(R.id.mainWebView);
        browser.reload();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (showWebView) {
                WebView webView = (WebView)findViewById(R.id.mainWebView);
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    final NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
                    navigationView.setCheckedItem(R.id.nav_overview);
                    // scroll
                    ((NavigationMenuView)navigationView.getChildAt(0)).smoothScrollToPosition(0);
                    // show main
                    showMain();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_overview) {
            showMain();
        } else if (id == R.id.nav_info) {
            if (!NetworkHelper.isNetworkAvailable((ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE))) {
                showPage("file:///android_asset/uvod/index.html");
            } else {
                showPage("file:///android_asset/uvod/index.html");
            }
        } else if (id == R.id.nav_for) {
            showPage("https://www.vindove.cz/lesni-krajina/");
        } else if (id == R.id.nav_phil) {
            showPage("https://www.vindove.cz/filozofie/");
        } else if (id == R.id.nav_quote) {
            showPage("https://www.vindove.cz/citaty-z-knih-a-kronik/");
        } else if (id == R.id.nav_stories) {
            showPage("file:///android_asset/vybrane/index.html");
        } else if (id == R.id.nav_story_first) {
            showPage("file:///android_asset/pribehy/index.html");
        } else if (id == R.id.nav_story_second) {
            showPage("file:///android_asset/kmen/index.html");
        } else if (id == R.id.nav_story_third) {
            showPage("file:///android_asset/lado/index.html");
        } else if (id == R.id.nav_story_fifth) {
            showPage("file:///android_asset/beovinidis/index.html");
        } else if (id == R.id.nav_story_sixth) {
            showPage("file:///android_asset/premysl/index.html");
        } else if (id == R.id.nav_story_seventh) {
            showPage("file:///android_asset/promluvy/index.html");
        } else if (id == R.id.nav_episodes) {
            showPage("https://www.vindove.cz/category/prihody/");
        } else if (id == R.id.nav_poems) {
            showPage("https://www.vindove.cz/category/basne/");
        } else if (id == R.id.nav_golden_heart) {
            showPage("https://www.vindove.cz/zlate-srdce/");
        } else if (id == R.id.nav_diary) {
            showPage("https://www.vindove.cz/category/denik-vinda/");
        } else if (id == R.id.nav_nature) {
            showPage("https://www.vindove.cz/category/prirodni-koutek/");
        } else if (id == R.id.nav_other) {
            showPage("https://www.vindove.cz/category/myslenky/");
        } else if (id == R.id.nav_fb) {
            // Vindove - zvlastni stranka s vysekem Facebooku
            showPage("https://www.vindove.cz/facebook-mob-app/");
        } else if (id == R.id.nav_about) {
            showPage("file:///android_asset/info/index.html");
        }

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // just for sure
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("showWebView", showWebView);
        outState.putString("lastURL", lastURL);
        outState.putBoolean("clearHistory", clearHistory);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        showWebView = savedInstanceState.getBoolean("showWebView");
        lastURL = savedInstanceState.getString("lastURL");
        clearHistory = savedInstanceState.getBoolean("clearHistory");
    }
}
