package com.jichao.monitorapp.activitys;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jichao.monitorapp.R;
import com.jichao.monitorapp.StatusActivity;
import com.jichao.monitorapp.bean.AppInfo;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener,
		OnItemClickListener, OnItemSelectedListener, Runnable {
	final static String TAG = "monitor";
	// 定义变量
	Spinner sp1;
	MyAdapter madapter;
	List<AppInfo> list;
	Button bt1;
	Button recordstatus;
	EditText filename;
	Toast toast;
	String packagename;
	String appname;
	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();
		initListeners();
	}

	private void initListeners() {
		Log.i(TAG, "进入initListeners！");
		sp1.setOnItemSelectedListener(this);
		bt1.setOnClickListener(this);
		recordstatus.setOnClickListener(this);
	}

	private void initViews() {
		Log.i(TAG, "进入initViews！");
		filename = (EditText) findViewById(R.id.filename);
		bt1 = (Button) findViewById(R.id.startRecording);
		recordstatus = (Button) findViewById(R.id.recordingStatus);
		sp1 = (Spinner) findViewById(R.id.spinner1);
		madapter = new MyAdapter(listApp());
		// Specify the layout to use when the list of choices appears
		sp1.setAdapter(madapter);
		TextWatcher textWatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

				if (!s.toString().matches("[0-9_a-zA-z]+")) {
					showToast("文件名不能包含特殊字符");
					s.clear();
				}
			}
		};
		filename.addTextChangedListener(textWatcher);
		handler = new Handler();
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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		Log.i(TAG, "进入click！");
		switch (v.getId()) {
		case R.id.startRecording:

			// 开始录制
			if (filename.getText().toString().trim().equals("")) {
				showToast("文件名不能为空");
				return;
			}
			String _filename = filename.getText().toString();
			try {
				startRecording(_filename);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.recordingStatus:
			Intent intent = new Intent();
			intent.setClassName(this, StatusActivity.class.getName());
			startActivity(intent);
			break;
		default:
			break;
		}

	}

	private List<AppInfo> listApp() {
		PackageManager pkm;
		List<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

	}

	class MyAdapter extends BaseAdapter {
		List<AppInfo> list;

		public MyAdapter(List<AppInfo> list) {

			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
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
			viewHold.appname = list.get(position).getAppName();
			return convertView;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return super.getDropDownView(position, convertView, parent);
		}
	}

	class ViewHold {
		ImageView iv;
		TextView tv;
		String packageName;
		String appname;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

		packagename = ((ViewHold) view.getTag()).packageName;
		appname = ((ViewHold) view.getTag()).appname;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	void showToast(String msg) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_SHORT);
		}
		toast.setText(msg);
		toast.show();
	}

	private void startRecording(String _filename) throws IOException,
			InterruptedException {
		System.out.println("************进入startRecording");
		if (getApplication().getSharedPreferences("Recordings",
				Context.MODE_PRIVATE).getBoolean("hasRecording", false)) {
			showToast("当前还有录制没有完成！请先停止再开始新的任务！");
			return;
		}
		File recordingfile;
		String storeFilename = "jichao" + "__" + appname + "__" + packagename+"__"+_filename
				+ "__" + new Date().getTime();
		storeFilename = storeFilename.replace(" ","");
		recordingfile = new File(getFilesDir(), storeFilename);
		File topStart = new File(getFilesDir(), "start.sh");
		System.out.println("***************storeFilename" + storeFilename);
		System.out.println("top  -d 5 |grep " + packagename + "> "
				+ recordingfile.getAbsolutePath());
		topStart.delete();
		FileWriter fw;
		fw = new FileWriter(topStart);
		fw.write("#!/system/bin/sh");
		fw.write("\n");
		fw.write("top  -d 5 -n 2|grep " + packagename + "> "
				+ recordingfile.getAbsolutePath());
		fw.close();
		topStart.setExecutable(true);
		Runtime.getRuntime().exec(topStart.getAbsolutePath());

		Editor et = getApplication().getSharedPreferences("Recordings",
				Context.MODE_PRIVATE).edit();
		et.putBoolean("hasRecording", true);
		et.putString("ongoing", packagename);
		et.apply();
	}

	@Override
	public void run() {

	}

}
