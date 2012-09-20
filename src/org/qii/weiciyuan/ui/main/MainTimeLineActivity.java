package org.qii.weiciyuan.ui.main;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.discover.DiscoverFragment;
import org.qii.weiciyuan.ui.login.AccountActivity;
import org.qii.weiciyuan.ui.maintimeline.CommentsTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.FriendsTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsTimeLineFragment;
import org.qii.weiciyuan.ui.preference.SettingActivity;
import org.qii.weiciyuan.ui.userinfo.MyInfoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 */
public class MainTimeLineActivity extends AbstractAppActivity implements IUserInfo,
        IToken,
        IAccountInfo {

    private ViewPager mViewPager = null;
    private String token = "";
    private AccountBean accountBean = null;


    public String getToken() {
        return token;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("account", accountBean);
        outState.putString("token", token);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            accountBean = (AccountBean) intent.getSerializableExtra("account");
            if (accountBean != null) {
                token = accountBean.getAccess_token();
            } else {
                //because app crash
                AppLogger.e("MainTneActivity dont have account");
                finish();
            }

        } else {
            accountBean = (AccountBean) savedInstanceState.getSerializable("account");
            token = savedInstanceState.getString("token");
            if (accountBean == null) {
                AppLogger.e("MainTneActivity dont have account");
                finish();
            }
        }
        GlobalContext.getInstance().setSpecialToken(token);
        GlobalContext.getInstance().setCurrentAccountId(accountBean.getUid());
        GlobalContext.getInstance().setCurrentAccountName(accountBean.getUsernick());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("id", accountBean.getUid());
        editor.commit();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintimelineactivity_viewpager_layout);

        buildPhoneInterface();


    }


    private void buildPhoneInterface() {
        buildViewPager();
        buildActionBarAndViewPagerTitles();
        buildTabTitle(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        AccountBean newAccountBean = (AccountBean) intent.getSerializableExtra("account");
        if (newAccountBean != null && !newAccountBean.getUid().equals(accountBean.getUid())) {
            overridePendingTransition(0, 0);
            finish();

            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);

        } else if (newAccountBean != null) {

            accountBean = newAccountBean;
            token = newAccountBean.getAccess_token();
            GlobalContext.getInstance().setSpecialToken(token);
            buildTabTitle(intent);
        }
    }


    private void buildTabTitle(Intent intent) {


        CommentListBean comment = (CommentListBean) intent.getSerializableExtra("comment");
        MessageListBean repost = (MessageListBean) intent.getSerializableExtra("repost");

        if (repost != null && repost.getSize() > 0) {
            invlidateTabText(1, repost.getSize());
            getActionBar().setSelectedNavigationItem(1);
        }
        if (comment != null && comment.getSize() > 0) {
            invlidateTabText(2, comment.getSize());
            getActionBar().setSelectedNavigationItem(2);
        }
    }

    private void invlidateTabText(int index, int number) {

        ActionBar.Tab tab = getActionBar().getTabAt(index);
        String name = tab.getText().toString();
        String num = "(" + number + ")";
        if (!name.endsWith(")")) {
            tab.setText(name + num);
        } else {
            int i = name.indexOf("(");
            String newName = name.substring(0, i);
            tab.setText(newName + num);
        }

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.maintimelineactivity_menu, menu);
//
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GlobalContext.getInstance().startedApp = false;
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_my_info:
                intent = new Intent(this, MyInfoActivity.class);
                intent.putExtra("token", getToken());
                intent.putExtra("user", getUser());
                intent.putExtra("account", getAccount());
                startActivity(intent);
                return true;

            case R.id.menu_account:
                intent = new Intent(this, AccountActivity.class);
                intent.putExtra("launcher", false);
                startActivity(intent);
                return true;

            case R.id.menu_setting:
                startActivity(new Intent(this, SettingActivity.class));
                return true;

//            case R.id.menu_search:
//                startActivity(new Intent(this, SearchMainActivity.class));
//                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void buildViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        TimeLinePagerAdapter adapter = new TimeLinePagerAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);


    }


    private AbstractTimeLineFragment getHomeFragment() {
        return ((AbstractTimeLineFragment) getSupportFragmentManager().findFragmentByTag(
                FriendsTimeLineFragment.class.getName()));
    }

    private AbstractTimeLineFragment getMentionFragment() {
        return ((AbstractTimeLineFragment) getSupportFragmentManager().findFragmentByTag(
                MentionsTimeLineFragment.class.getName()));
    }

    private AbstractTimeLineFragment getCommentFragment() {
        return ((AbstractTimeLineFragment) getSupportFragmentManager().findFragmentByTag(
                CommentsTimeLineFragment.class.getName()));
    }

    private Fragment getDiscoverFragment() {
        return ((Fragment) getSupportFragmentManager().findFragmentByTag(
                DiscoverFragment.class.getName()));
    }

    private void buildActionBarAndViewPagerTitles() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        if (getResources().getBoolean(R.bool.is_phone)) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.home))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.mentions))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.comments))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.discover))
                .setTabListener(tabListener));


    }


    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            getActionBar().setSelectedNavigationItem(position);
        }
    };

    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        boolean home = false;
        boolean mentions = false;
        boolean comments = false;

        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {

            /**
             * workaround for fragment option menu bug
             *
             * http://stackoverflow.com/questions/9338122/action-items-from-viewpager-initial-fragment-not-being-displayed
             *
             */
            if (mViewPager.getCurrentItem() != tab.getPosition())
                mViewPager.setCurrentItem(tab.getPosition());


            if (getHomeFragment() != null) {
                getHomeFragment().clearActionMode();
            }

            if (getMentionFragment() != null) {
                getMentionFragment().clearActionMode();
            }

            if (getCommentFragment() != null) {
                getCommentFragment().clearActionMode();
            }


            switch (tab.getPosition()) {
                case 0:
                    home = true;
                    break;
                case 1:
                    mentions = true;
                    break;
                case 2:
                    comments = true;
                    break;
                case 3:
                    break;
            }

        }

        public void onTabUnselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {
                case 0:
                    home = false;
                    break;
                case 1:
                    mentions = false;
                    break;
                case 2:
                    comments = false;
                    break;
                case 3:
                    break;
            }
        }

        public void onTabReselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {
                case 0:
                    if (home)
                        getHomeFragment().getListView().setSelection(0);
                    break;
                case 1:
                    if (mentions)
                        getMentionFragment().getListView().setSelection(0);
                    break;
                case 2:
                    if (comments)
                        getCommentFragment().getListView().setSelection(0);
                    break;
                case 3:
                    break;
            }
        }
    };

    @Override
    public UserBean getUser() {
        return accountBean.getInfo();

    }


    @Override
    public AccountBean getAccount() {
        return accountBean;
    }


    class TimeLinePagerAdapter extends AppFragmentPagerAdapter {


        List<Fragment> list = new ArrayList<Fragment>();


        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);
            list.add(new FriendsTimeLineFragment());
            list.add(new MentionsTimeLineFragment());
            list.add(new CommentsTimeLineFragment());
            list.add(new DiscoverFragment());
        }


        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        protected String getTag(int position) {
            List<String> tagList = new ArrayList<String>();
            tagList.add(FriendsTimeLineFragment.class.getName());
            tagList.add(MentionsTimeLineFragment.class.getName());
            tagList.add(CommentsTimeLineFragment.class.getName());
            tagList.add(DiscoverFragment.class.getName());
            return tagList.get(position);
        }


        @Override
        public int getCount() {
            return list.size();
        }


    }
}