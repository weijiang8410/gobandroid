package org.ligi.gobandroid_hd.ui.application;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import java.lang.reflect.Field;
import javax.inject.Inject;
import org.ligi.gobandroid_hd.App;
import org.ligi.gobandroid_hd.InteractionScope;
import org.ligi.gobandroid_hd.R;
import org.ligi.gobandroid_hd.logic.GoGame;
import org.ligi.gobandroid_hd.model.GameProvider;
import org.ligi.gobandroid_hd.ui.application.navigation.NavigationDrawerHandler;

public class GobandroidFragmentActivity extends AppCompatActivity {

    @Inject
    protected GoAndroidEnvironment env;

    @Inject
    public InteractionScope interactionScope;

    @Inject
    protected GameProvider gameProvider;

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.component().inject(this);

        if (getSupportActionBar() != null) {// yes this happens - e.g.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // a little hack because I strongly disagree with the style guide here
        // ;-)
        // not having the Actionbar overfow menu also with devices with hardware
        // key really helps discoverability
        // http://stackoverflow.com/questions/9286822/how-to-force-use-of-overflow-menu-on-devices-with-menu-button
        try {
            final ViewConfiguration config = ViewConfiguration.get(this);
            final Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore - but at least we tried ;-)
        }

        // we do not want focus on custom views ( mainly for GTV )
        /*
         * wd if ((this.getSupportActionBar()!=null) &&
		 * (this.getSupportActionBar().getCustomView()!=null))
		 * this.getSupportActionBar().getCustomView().setFocusable(false);
		 *
		 */


    }

    public void closeDrawers() {
        drawerLayout.closeDrawers();
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(R.layout.navigation_drawer_container);
        final View v = getLayoutInflater().inflate(layoutResId, (ViewGroup) findViewById(R.id.drawer_layout), false);
        final ViewGroup vg = (ViewGroup) findViewById(R.id.content_frame);
        vg.addView(v);
        new NavigationDrawerHandler(this).handle();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerToggle = new ActionBarDrawerToggle(this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

        };

        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (drawerToggle.onOptionsItemSelected(item)) ;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        if (drawerToggle != null) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }


    public boolean doFullScreen() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //NaDra mMenuDrawer.refresh();

        if (doFullScreen()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public App getApp() {
        return (App) getApplicationContext();
    }

    public GoGame getGame() {
        return gameProvider.get();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_WINDOW) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
        private void workingPostToGPlus() {
            // Create an interactive post with the "VIEW_ITEM" label. This will
            // create an enhanced share dialog when the post is shared on Google+.
            // When the user clicks on the deep link, ParseDeepLinkActivity will
            // immediately parse the deep link, and route to the appropriate resource.
            Uri callToActionUrl = Uri.parse("https://cloud-goban.appspot.com/game/ag1zfmNsb3VkLWdvYmFucgwLEgRHYW1lGPK_JAw");
            String callToActionDeepLinkId = "/foo/bar";


            // Create an interactive post builder.
            PlusShare.Builder builder = new PlusShare.Builder(this, mPlusClient);

            // Set call-to-action metadata.
            builder.addCallToAction("CREATE_ITEM", callToActionUrl, callToActionDeepLinkId);

            // Set the target url (for desktop use).
            builder.setContentUrl(Uri.parse("https://cloud-goban.appspot.com/game/ag1zfmNsb3VkLWdvYmFucgwLEgRHYW1lGPK_JAw"));

            // Set the target deep-link ID (for mobile use).
            builder.setContentDeepLinkId("/pages/",
                    null, null, null);

            // Set the pre-filled message.
            builder.setText("foo bar");

            startActivityForResult(builder.getIntent(), 0);
        }
    */
    //@Override
    public void onConnected(Bundle bundle) {

        /*
        ItemScope target = new ItemScope.Builder()
                //.setUrl("https://cloud-goban.appspot.com/game/"+key)
                .setUrl("https://developers.google.com/+/web/snippet/examples/thing")
                //.setUrl("http://cloud-goban.appspot.com/game/ag1zfmNsb3VkLWdvYmFucgwLEgRHYW1lGKGnJww")
                //.setType("http://schema.org/CreativeWork")
                .build();

        Moment moment = new Moment.Builder()
                .setType("http://schemas.google.com/AddActivity")
                .setTarget(target)
                .build();

        getPlusClient().writeMoment(moment);
         */

        /*
        final int errorCode = GooglePlusUtil.checkGooglePlusApp(this);
        if (errorCode == GooglePlusUtil.SUCCESS) {
            PlusShare.Builder builder = new PlusShare.Builder(this, mPlusClient);

            // Set call-to-action metadata.
            builder.addCallToAction(
                    "CREATE_ITEM",
                    Uri.parse("http://plus.google.com/pages/create"),
                    "/pages/create");
            // Set the content url (for desktop use).
            builder.setContentUrl(Uri.parse("https://plus.google.com/pages/"));

            // Set the target deep-link ID (for mobile use).
            builder.setContentDeepLinkId("/pages/", null, null, null);

            // Set the share text.
            builder.setText("Create your Google+ Page too!");
            startActivityForResult(builder.getIntent(), 0);
            */

            /*
               Intent shareIntent = new PlusShare.Builder(this)
          .setType("text/plain")
          .setText("Welcome to the Google+ platform.")
          .setContentUrl(Uri.parse("https://developers.google.com/+/"))
          .getIntent();

             startActivityForResult(shareIntent, 0);
               */

            /*
            Intent shareIntent = new PlusShare.Builder(this)
                    .setText("Lemon Cheesecake recipe")
                    .setType("text/plain")
                    .setContentDeepLinkId("/cheesecake/lemon",
                            "Lemon Cheesecake recipe",
                            "A tasty recipe for making lemon cheesecake.",
                            Uri.parse("http://example.com/static/lemon_cheesecake.png"))
                    .getIntent();

            startActivityForResult(shareIntent, 0);

            //workingPostToGPlus();


        } else {
            // Prompt the user to install the Google+ app.
            GooglePlusUtil.getErrorDialog(errorCode, this, 0).show();
        }
    */

    }

}
