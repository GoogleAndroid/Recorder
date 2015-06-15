package com.jichao.monitorapp.activitys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jichao.monitorapp.R;
import com.jichao.monitorapp.R.id;
import com.jichao.monitorapp.R.layout;
import com.jichao.monitorapp.bean.AppInfo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityOptions;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AppActivity extends ListActivity implements OnScrollListener,
		OnClickListener, OnItemClickListener {

	private ListView lvapp;
	private PackageManager pkm;
	private List<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
	private LinearLayout mLoadLayout;
	private List<AppInfo> app;
	private DisplayMetrics dm = new DisplayMetrics();
	private static Toast toast;
	private ImageView iv1;
	private TextView tv1;
	private String appName; 
	ViewGroup root;
	Myadaptor madaptor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		initViews();
		initListeners();
		if (madaptor.count == 0) {
			tv1.setText("没有找到应用程序！！");
			tv1.setTextColor(android.graphics.Color.RED);
			tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		} else {
			lvapp.addFooterView(mLoadLayout);
			lvapp.setAdapter(madaptor);
		}
	}

	private void initListeners() {

		lvapp.setOnScrollListener(this);
		lvapp.setOnItemClickListener(this);
	}

	private void initViews() {

		lvapp = getListView();
		iv1 = (ImageView) findViewById(R.id.iv1);
		tv1 = (TextView) findViewById(R.id.tv1);
		root = (ViewGroup) findViewById(android.R.id.content);
		app = listApp();
		madaptor = new Myadaptor(app);
		mLoadLayout = (LinearLayout) getLayoutInflater().inflate(
				R.layout.footer, null);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (view.getLastVisiblePosition() == view.getCount() - 1) {
			madaptor.count = madaptor.count + 10 > app.size() ? app.size()
					: madaptor.count + 10;
			madaptor.notifyDataSetChanged();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		if (madaptor.count == app.size()) {
			lvapp.removeFooterView(mLoadLayout);
		}
		if (view.getLastVisiblePosition() == app.size() - 1) {
			showToast("没有更多应用！");
		}
	}

	private List<AppInfo> listApp() {
		// Filter out system app.
		List<AppInfo> infolist = new ArrayList<AppInfo>();
		pkm = getPackageManager();
		applist = pkm.getInstalledApplications(0);
		for (int i = 0; i < applist.size(); i++) {
			if (!((applist.get(i).flags & ApplicationInfo.FLAG_SYSTEM) > 0)) {
				AppInfo ap = new AppInfo();
				ap.setIcon(applist.get(i).loadIcon(pkm));
				ap.setAppName(applist.get(i).loadLabel(pkm).toString());
				ap.setPackageName(applist.get(i).packageName);
				infolist.add(ap);
			}

		}
		return infolist;
	}

	class Myadaptor extends BaseAdapter {

		private List<AppInfo> list;
		public int count;

		public Myadaptor(List<AppInfo> list) {
			this.list = list;
			float myDevice = Float.parseFloat(dm.heightPixels + "")
					/ dm.densityDpi;
			float std = 50 / 160f;
			this.count = Math.round(myDevice / std);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return this.count;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHold viewHold;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.activity_show_app, null);
				viewHold = new ViewHold();
				viewHold.iv = (ImageView) convertView.findViewById(R.id.iv1);
				viewHold.tv = (TextView) convertView.findViewById(R.id.tv1);
				viewHold.tv.setTextColor(android.graphics.Color.BLUE);
				convertView.setTag(viewHold);
			} else {
				viewHold = (ViewHold) convertView.getTag();
			}
			viewHold.iv.setImageDrawable(list.get(position).getIcon());
			viewHold.tv.setText(list.get(position).getAppName());
			viewHold.packageName = list.get(position).getPackageName();
			return convertView;
		}
	}

	void showToast(String msg) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_SHORT);
		}
		toast.setText(msg);
		toast.show();
	}

	class ViewHold {
		ImageView iv;
		TextView tv;
		String packageName;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		SharedPreferences sp = getApplication().getSharedPreferences(
				"Record_Status", Context.MODE_PRIVATE);
		if(sp.getBoolean("Recording", false)){
			showToast("正在录制! 请先停止当前录制！");
			return;
		}
		ViewHold vh = (ViewHold) view.getTag();
		String packageName = vh.packageName;
		appName=vh.tv.getText().toString();
		// Intent intent1 = new Intent(Intent.ACTION_MAIN);
		// intent1.addCategory(Intent.CATEGORY_LAUNCHER);
		// intent1.setPackage(vh.packageName);
		// List<ResolveInfo> list = pkm.queryIntentActivities(intent1, 0);
		// ResolveInfo ri = list.iterator().next();
		// if(ri!=null){
		// Intent intent = new Intent(Intent.ACTION_MAIN);
		// intent.addCategory(Intent.CATEGORY_LAUNCHER);
		// intent.setComponent(new
		// ComponentName(ri.activityInfo.packageName,ri.activityInfo.name));
		// startActivity(intent);
		// Intent intent2 = new Intent();
		// intent2.setClass(this, RecordingActivity.class);
		// startActivity(intent2);
		// }
		// else {
		// showToast("对不起！此应用无法打开！");
		// }
		Intent intent2 = new Intent();
		intent2.setClass(this, RecordingActivity.class);
		intent2.putExtra("packagename", packageName);
		startActivity(intent2);
	}			
}
