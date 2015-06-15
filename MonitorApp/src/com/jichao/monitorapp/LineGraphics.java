package com.jichao.monitorapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;

import com.jichao.monitorapp.bean.Recorder;

import android.R.color;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class LineGraphics {

	final static int[] COLORS = { Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA,
			Color.YELLOW, Color.CYAN };

	public Intent showCPU(Context context, Map<String, Recorder> map) {
		/* 准备数据 */
		List<XYSeries> series = new ArrayList<>();
		for (String packagename : map.keySet()) {
			XYSeries xys = new XYSeries(packagename);
			Recorder recorder = map.get(packagename);
			List<Long> list = recorder.cpu_usage;
			for (int i = 0; i < list.size(); i++) {
				xys.add(5 + i * 5, list.get(i));
			}
			series.add(xys);
		}
		// XYSeries xvs1 = new XYSeries("line1");
		// XYSeries xvs2 = new XYSeries("line2");
		// for (int i = 0; i < 10; i++) {
		// xvs1.add(i, Math.random() * 100);
		// xvs2.add(i, Math.random() * 100);
		// }
		// series.add(xvs1);
		// series.add(xvs2);
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addAllSeries(series);
		/* 准备renderer */
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		for (int i = 0; i < series.size(); i++) {
			XYSeriesRenderer spr = new XYSeriesRenderer();
			spr.setColor(COLORS[i]);
			FillOutsideLine fill = new FillOutsideLine(
					FillOutsideLine.Type.BOUNDS_BELOW);
			fill.setColor(COLORS[i]);
			spr.addFillOutsideLine(fill);
			spr.setLineWidth(10);
			spr.setDisplayBoundingPoints(true);
			spr.setDisplayChartValues(true);
			spr.setChartValuesTextSize(20);
			renderer.addSeriesRenderer(spr);
		}
//		XYSeriesRenderer spr1 = new XYSeriesRenderer();
//		spr1.setColor(Color.RED);
//		FillOutsideLine fill = new FillOutsideLine(FillOutsideLine.Type.BELOW);
//		fill.setColor(Color.RED);
//		spr1.addFillOutsideLine(fill);
//		spr1.setLineWidth(10);
//		spr1.setDisplayBoundingPoints(true);
//		spr1.setDisplayChartValues(true);
//		spr1.setChartValuesTextSize(20);
//		spr1.setPointStyle(PointStyle.CIRCLE);
//		XYSeriesRenderer spr2 = new XYSeriesRenderer();
//		spr2.setColor(Color.BLUE);
//		spr2.setLineWidth(5);
//		renderer.addSeriesRenderer(spr1);
//		renderer.addSeriesRenderer(spr2);
		renderer.setXTitle("时间(单位s)");
		renderer.setXLabelsAngle(45);
		renderer.setYTitle("CPU占用率%");
		renderer.setAxisTitleTextSize(30);
		renderer.setBackgroundColor(Color.GRAY);
		renderer.setApplyBackgroundColor(true);
		renderer.setLabelsTextSize(30);
		renderer.setLegendTextSize(30);
		renderer.setLegendHeight(100);
		renderer.setChartTitle("CPU占用率");
		renderer.setAxesColor(Color.YELLOW);
		renderer.setChartTitleTextSize(60);
		renderer.setDisplayValues(true);
		renderer.setMargins(new int[] { 20, 100, 100, 30 });
		renderer.setShowGrid(true);
		return ChartFactory.getLineChartIntent(context, dataset, renderer);
	}

	public Intent showMEM(Context context, Map<String, Recorder> map) {

		return null;
	}
}
