package com.jichao.monitorapp.activitys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ApplicationErrorReport.AnrInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.jichao.monitorapp.LineGraphics;
import com.jichao.monitorapp.R;
import com.jichao.monitorapp.R.id;
import com.jichao.monitorapp.R.layout;
import com.jichao.monitorapp.bean.Recorder;

@SuppressLint({ "InflateParams", "NewApi" })
public class StatusActivity extends Activity implements OnClickListener,
	 Callback {

	Toast toast;
	TableLayout tbl;
	String[] filenames;
	Button OperatorBtn;
	private HashMap<String, Recorder> map = new HashMap<String, Recorder>();
	private int nameindex;
	private int cpuindex;
	private int rssindex;
	ViewGroup rl;
	TextView loading;
	ProgressBar pb;
	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status);
		initView();
		filenames = getRecordingFiles();
		drawTable(filenames);
		initlst();
		handler = new Handler(this);
	}

	private void drawTable(String[] filename) {
		tbl.removeViews(1, tbl.getChildCount() - 1);
		for (int i = 0; i < filename.length; i++) {
			TableRow tr = (TableRow) getLayoutInflater().inflate(
					R.layout.tablerow, null);

			((TextView) tr.findViewById(R.id.appname)).setText(filename[i]
					.split("__")[1]);
			((TextView) tr.findViewById(R.id.filename)).setText(filename[i]
					.split("__")[3]);
			((TextView) tr.findViewById(R.id.filename)).getPaint().setFlags(
					Paint.UNDERLINE_TEXT_FLAG);
			tr.findViewById(R.id.filename).setOnClickListener(this);
			Date date = new Date(Long.parseLong(filename[i].split("__")[4]));
			String d = (date.getYear() + 1900) + "-" + date.getMonth() + "-"
					+ date.getDay() + " " + date.getHours() + ":"
					+ date.getMinutes() + ":" + date.getSeconds();
			((TextView) tr.findViewById(R.id.date)).setText(d);
			((TextView) tr.findViewById(R.id.date)).setTag(filename[i]
					.split("__")[4]);
			Long time = getSharedPreferences("Recordings",
					Context.MODE_PRIVATE).getLong("ongoing", 0);
			String ongoing = Long.toString(time);
			String status = ongoing.equals(filename[i].split("__")[4]) ? "正在录制"
					: "完成";
			((TextView) tr.findViewById(R.id.status)).setText(status);
			String operator = status.equals("正在录制") ? "停止" : "删除";
			((Button) tr.findViewById(R.id.operator)).setText(operator);
			tr.findViewById(R.id.operator).setOnClickListener(this);
			tbl.addView(tr);
		}
	}

	private void initlst() {
	}

	private void initView() {

		tbl = (TableLayout) findViewById(R.id.tablestatus);
		rl = (ViewGroup) findViewById(android.R.id.content);
//		pb = (ProgressBar) findViewById(R.id.pb);
//		loading = (TextView) findViewById(R.id.loading);
//		loading.setVisibility(View.INVISIBLE);
//		pb.setVisibility(View.INVISIBLE);
	}

	private String[] getRecordingFiles() {
		File filedir = getFilesDir();
		String[] filelist = filedir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				if (filename.startsWith("jichao")) {
					return true;
				}
				return false;
			}
		});
		return filelist;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.operator:
			if (((TextView) v).getText().equals("停止")) {
				try {
					stopRecording();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				((Button) v).setText("删除");
				((TextView) ((ViewGroup) v.getParent())
						.findViewById(R.id.status)).setText("完成");

			} else {
				ViewGroup vg = (ViewGroup) ((TextView) v).getParent();
				TextView tv = (TextView) vg.findViewById(R.id.date);
				String date = (String) tv.getTag();
				String recordfile = "";
				for (int i = 0; i < filenames.length; i++) {
					if (filenames[i].contains(date)) {
						recordfile = filenames[i];
						break;
					}
				}
				new File(getFilesDir(), recordfile).delete();
				((TableRow) v.getParent()).setVisibility(View.GONE);
			}
			break;

		case R.id.filename:
			((TextView) v).setTextColor(Color.BLUE);
			ViewGroup vg = (ViewGroup) ((TextView) v).getParent();
			TextView tv = (TextView) vg.findViewById(R.id.date);
			String date = (String) tv.getTag();
			String recordfile = "";
			for (int i = 0; i < filenames.length; i++) {
				if (filenames[i].contains(date)) {
					recordfile = filenames[i];
					break;
				}
			}
			Intent intent = new Intent();
			intent.setClassName(this, MonitorActivity.class.getName());
			intent.putExtra("recordfile", recordfile);
			startActivity(intent);
//			new Thread(new RecordingInfo(recordfile)).start();
//			loading.setVisibility(View.VISIBLE);
//			pb.setVisibility(View.VISIBLE);
//			pb.invalidate();
//			loading.invalidate();
			break;
		default:
			break;
		}

	}

	class RecordingInfo implements Runnable {
		String filename;

		public RecordingInfo(String filename) {
			this.filename = filename;
		}

		@Override
		public void run() {
			try {
				readRecordFile(filename);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			handler.sendEmptyMessage(0);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

	}

	private void readRecordFile(String fileName) throws FileNotFoundException,
			IOException, InterruptedException {
		File recordingfile = new File(getFilesDir(), fileName);
		System.out.println("---Start ready data");
		System.out.println("This file path is :"
				+ recordingfile.getAbsolutePath());
		BufferedReader br = new BufferedReader(new FileReader(recordingfile));
		String data;
		int count = 0;
		int nameindex = getNameColumn();
		int cpuindex = getCPUColumn();
		int rssindex = getRRSColumn();
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m;
		while ((data = br.readLine()) != null) {
			String s[] = data.trim().split(" +");
			//正在写入时候，读取可能存在数据不全，忽略掉，完成录制，不会出现这种情况
			if(s.length<10){
				System.out.println("-----------------------"+Arrays.toString(s));
				continue;
			}
			// 为每一个进程创建一个记录对象
			System.out.println("Process Name: " + s[nameindex]);
			Recorder recorder;
			if (map.containsKey(s[nameindex])) {
				recorder = map.get(s[nameindex]);
			} else {
				recorder = new Recorder();
				map.put(s[s.length - 1], recorder);
			}
			// 只取数字部分
			m = p.matcher(s[cpuindex]);
			m.find();
			recorder.cpu_usage.add(Long.parseLong(m.group()));
			m = p.matcher(s[rssindex]);
			m.find();
			recorder.mem_usage.add(Long.parseLong(m.group()));
			count++;
		}
		br.close();
		System.out.println("录制记录条数: " + count);
		System.out.println("创建记录对象个数： " + map.size());
	}

	private void stopRecording() throws IOException, InterruptedException {
		if (!getApplication().getSharedPreferences("Recordings",
				Context.MODE_PRIVATE).getBoolean("hasRecording", false)) {
			showToast("没有正在录制任务！");
			return;
		}
		ArrayList<Integer> pidlist = getTopPids();
		// 关闭所有top命令
		for (int i = 0; i < pidlist.size(); i++) {
			System.out.println("List data:" + pidlist.get(i));
			android.os.Process.killProcess(pidlist.get(i));
		}
		Editor et = getApplication().getSharedPreferences("Recordings",
				Context.MODE_PRIVATE).edit();
		et.putBoolean("hasRecording", false);
		et.putLong("ongoing", 0);
		et.apply();
		System.out.println("************修改preference的值为false");
	}

	void showToast(String msg) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_SHORT);
		}
		toast.setText(msg);
		toast.show();
	}

	@SuppressLint("NewApi")
	private ArrayList<Integer> getTopPids() throws IOException,
			InterruptedException {
		ArrayList<Integer> pidlist = new ArrayList<>();
		int pidindex = getPIDColumn();
		if (pidindex == -1) {
			System.out
					.println("Sorry, Cannot find PID in PS command! Could not get PIDs!");
			return null;
		}
		File ps2 = new File(getFilesDir(), "ps2.sh");
		if (ps2.createNewFile()) {
			FileWriter fw;
			fw = new FileWriter(ps2);
			fw.write("#!/system/bin/sh");
			fw.write("\n");
			fw.write("ps| grep top");
			fw.close();
			ps2.setExecutable(true);
		}
		Process ps = Runtime.getRuntime().exec(ps2.getAbsolutePath());
		ps.waitFor();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				ps.getInputStream()));
		String data;
		System.out.println("Colume is : " + pidindex);
		while ((data = br.readLine()) != null) {
			System.out.println("PIDs data is :"
					+ Arrays.toString(data.split(" +")));
			System.out
					.println("PID add to list: " + data.split(" +")[pidindex]);
			pidlist.add(Integer.parseInt(data.trim().split(" +")[pidindex]));
		}
		return pidlist;

	}

	private int getPIDColumn() throws IOException, InterruptedException {
		String columnname = "PID";
		String filename = "ps1.sh";
		String[] command = { "ps| grep PID" };
		return getIndex(columnname, filename, command);
	}

	@SuppressLint("NewApi")
	private int getIndex(String columnname, String filename, String[] command)
			throws IOException, InterruptedException {
		File shfile = new File(getFilesDir(), filename);
		if (shfile.createNewFile()) {
			FileWriter fw;
			fw = new FileWriter(shfile);
			fw.write("#!/system/bin/sh");
			fw.write("\n");
			// 命令写入shell文件
			for (int i = 0; i < command.length; i++) {
				fw.write(command[i]);
				fw.write("\n");
			}
			fw.close();
			shfile.setExecutable(true);
		}
		Process ps = Runtime.getRuntime().exec(shfile.getAbsolutePath());
		ps.waitFor();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				ps.getInputStream()));
		String[] data = br.readLine().trim().split(" +");
		System.out.println("Column date is :" + Arrays.toString(data));
		for (int i = 0; i < data.length; i++) {
			if (data[i].toLowerCase().equals(columnname.toLowerCase()))
				return i;
		}
		return -1;
	}

	private int getNameColumn() throws IOException, InterruptedException {
		if (nameindex == 0) {
			String columnname = "Name";
			String filename = "top1.sh";
			String[] command = { "top -n 1| grep PID" };
			nameindex = getIndex(columnname, filename, command);

		}
		return nameindex;
	}

	private int getCPUColumn() throws IOException, InterruptedException {
		if (cpuindex == 0) {
			String columnname = "CPU%";
			String filename = "top1.sh";
			String[] command = { "top -n 1| grep PID" };
			cpuindex = getIndex(columnname, filename, command);
		}
		return cpuindex;
	}

	private int getRRSColumn() throws IOException, InterruptedException {
		if (rssindex == 0) {
			String columnname = "RSS";
			String filename = "top1.sh";
			String[] command = { "top -n 1| grep PID" };
			rssindex = getIndex(columnname, filename, command);
		}
		return rssindex;
	}

	@Override
	public boolean handleMessage(Message msg) {
		LineGraphics lineg = new LineGraphics();
		switch (msg.what) {
		case 0:
			loading.setVisibility(View.INVISIBLE);
			pb.setVisibility(View.INVISIBLE);
//			startActivity(lineg.showCPU(this, map));
			break;

		default:
			break;
		}
		return false;
	}
}
