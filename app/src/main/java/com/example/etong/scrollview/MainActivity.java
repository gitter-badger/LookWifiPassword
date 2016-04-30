package com.example.etong.scrollview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<WifiI> wifiIs;
    private List<WifiI> currentList = new ArrayList<>();
    private SwipeMenuListView listView;
    private wifiListAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (SwipeMenuListView) findViewById(R.id.listView);
        SwipeMenuCreator creator = menu -> {
            SwipeMenuItem shareItem = new SwipeMenuItem(getApplicationContext());
            shareItem.setWidth(dip2px(this,50));
            shareItem.setIcon(R.mipmap.ic_share_white_48dp);
//            shareItem.setBackground(R.color.shareBg);
            shareItem.setTitleSize(18);
            menu.addMenuItem(shareItem);
        };
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener((position, menu, index) -> {
            WifiI item = currentList.get(position);
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,"WIFI名:"+item.ssid+"\n密码:"+item.Password);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        });
        WifiManage wifiManage = new WifiManage();
        try {
            wifiIs  =  wifiManage.Read();
            Collections.sort(wifiIs, new Comparator<WifiI>() {
                @Override
                public int compare(WifiI lhs, WifiI rhs) {
                    return lhs.ssid.compareTo(rhs.ssid);
                }
            });
            currentList.addAll(wifiIs);
            mAdapter = new wifiListAdapter(this,R.layout.wifi_info_item,currentList);
            listView.setAdapter(mAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
        final SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentList.clear();
                for (WifiI item:wifiIs){
                    for (int i=0;i < item.ssid.length();i++){
                        char c = item.ssid.charAt(i);
                        if (Pinyin.isChinese(c)){
                            String py =  Pinyin.toPinyin(c).toLowerCase();
                            if (py.length()<newText.length() && newText.contains(py)){
                                currentList.add(item);
                                break;
                            }else if(newText.length()>0){
                                if (py.startsWith(newText)){
                                    currentList.add(item);
                                    break;
                                }
                            }
                        }
                    }
                    if (item.ssid.contains(newText) || item.ssid.toLowerCase().contains(newText)){
                        if (!currentList.contains(item)){
                            currentList.add(item);
                        }

                    }
                }
                mAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }
    class wifiListAdapter extends ArrayAdapter{
        private Context mContext;
        private int mResource;
        private List<WifiI> mList;

        public wifiListAdapter(Context context, int resource, List<WifiI> objects) {
            super(context, resource, objects);
            mContext = context;
            mResource = resource;
            mList = objects;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WifiI item = mList.get(position);
            ViewHolder viewHolder = null;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(mResource,null);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.wifinameTextView);
                viewHolder.pwdTextView = (TextView) convertView.findViewById(R.id.pwdTextVeiw);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.nameTextView.setText(item.ssid);
            viewHolder.pwdTextView.setText(item.Password);
            return convertView;
        }
        @Override
        public boolean isEnabled(int position){
            return false;
        }
    }
    class ViewHolder{
        TextView nameTextView;
        TextView pwdTextView;
    }

    private int dip2px(Context context,float dipValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }
}
