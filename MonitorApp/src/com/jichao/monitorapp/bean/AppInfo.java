package com.jichao.monitorapp.bean;

import android.graphics.drawable.Drawable;

public class AppInfo {
private Drawable icon;
private String appName;
private String path;
private String packageName;
public Drawable getIcon() {
	return icon;
}
public void setIcon(Drawable icon) {
	this.icon = icon;
}
public String getAppName() {
	return appName;
}
public void setAppName(String name) {
	this.appName = name;
}
public String getPath() {
	return path;
}
public void setPath(String path) {
	this.path = path;
}
public String getPackageName() {
	return packageName;
}
public void setPackageName(String packageName) {
	this.packageName = packageName;
}

}
