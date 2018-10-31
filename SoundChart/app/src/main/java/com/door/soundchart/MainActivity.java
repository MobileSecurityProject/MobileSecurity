package com.door.soundchart;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.scichart.charting.ClipMode;
import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.modifiers.AxisDragModifierBase;
import com.scichart.charting.modifiers.ModifierGroup;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.annotations.HorizontalAnchorPoint;
import com.scichart.charting.visuals.annotations.TextAnnotation;
import com.scichart.charting.visuals.annotations.VerticalAnchorPoint;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.pointmarkers.EllipsePointMarker;
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries;
import com.scichart.core.annotations.Orientation;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.core.model.DoubleValues;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    AudioProcess ap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Added in Tutorial #1
        // Create a SciChartSurface
        final SciChartSurface surface = new SciChartSurface(this);

        // Get a layout declared in "activity_main.xml" by id
        LinearLayout chartLayout = (LinearLayout) findViewById(R.id.chart_layout);

        // Add the SciChartSurface to the layout
        chartLayout.addView(surface);

        // Initialize the SciChartBuilder
        SciChartBuilder.init(this);

        // Obtain the SciChartBuilder instance
        final SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

        // Create a numeric X axis
        final IAxis xAxis = sciChartBuilder.newNumericAxis()
                .withAxisTitle("X Axis Title")
                .withVisibleRange(0, 512)
                .build();

        // Create a numeric Y axis
        final IAxis yAxis = sciChartBuilder.newNumericAxis()
                .withAxisTitle("Y Axis Title")
                .withVisibleRange(0, 3000000000.0)
                .withAutoRangeMode(AutoRange.Never).build();

        // Create a TextAnnotation and specify the inscription and position for it
        TextAnnotation textAnnotation = sciChartBuilder.newTextAnnotation()
                .withX1(0)
                .withY1(55.0)
                .withText("Hello World!")
                .withHorizontalAnchorPoint(HorizontalAnchorPoint.Center)
                .withVerticalAnchorPoint(VerticalAnchorPoint.Center)
                .withFontStyle(20, ColorUtil.White)
                .build();

        // Added in Tutorial #3
        // Add a bunch of interaction modifiers to a ModifierGroup
        ModifierGroup chartModifiers = sciChartBuilder.newModifierGroup()
                .withPinchZoomModifier().build()
                .withZoomPanModifier().withReceiveHandledEvents(true).build()
                .withZoomExtentsModifier().withReceiveHandledEvents(true).build()
                .withXAxisDragModifier().withReceiveHandledEvents(true).withDragMode(AxisDragModifierBase.AxisDragMode.Scale).withClipModex(ClipMode.None).build()
                .withYAxisDragModifier().withReceiveHandledEvents(true).withDragMode(AxisDragModifierBase.AxisDragMode.Pan).build()
                .build();

        // Add the Y axis to the YAxes collection of the surface
        Collections.addAll(surface.getYAxes(), yAxis);

        // Add the X axis to the XAxes collection of the surface
        Collections.addAll(surface.getXAxes(), xAxis);

        // Add the annotation to the Annotations collection of the surface
        Collections.addAll(surface.getAnnotations(), textAnnotation);

        // Add the interactions to the ChartModifiers collection of the surface
        Collections.addAll(surface.getChartModifiers(), chartModifiers);

        // Added in Tutorial #6 - FIFO (scrolling) series
        // Create a couple of DataSeries for numeric (Int, Double) data
        // Set FIFO capacity to 500 on DataSeries
        final int fifoCapacity = 513;

        final XyDataSeries lineData = sciChartBuilder.newXyDataSeries(Integer.class, Double.class)
                .withFifoCapacity(fifoCapacity)
                .build();

        ap = new AudioProcess(surface, lineData);
        ap.start();
        TimerTask updateDataTask = new TimerTask() {
            private int x = 0;

            @Override
            public void run() {
                UpdateSuspender.using(surface, new Runnable() {
                    @Override
                    public void run() {
//                        lineData.append(x, Math.sin(x * 0.1));

                        // Zoom series to fit the viewport
//                        surface.up();
//                        ++x;
                    }
                });
            }
        };

        Timer timer = new Timer();

        long delay = 0;
        long interval = 10;
        timer.schedule(updateDataTask, delay, interval);


        // Create and configure a line series
        final IRenderableSeries lineSeries = sciChartBuilder.newLineSeries()
                .withDataSeries(lineData)
                .withStrokeStyle(ColorUtil.LightBlue, 2f, true)
                .build();


        // Add a RenderableSeries onto the SciChartSurface
        surface.getRenderableSeries().add(lineSeries);
        surface.zoomExtents();


        // Create and configure a CursorModifier
        ModifierGroup cursorModifier = sciChartBuilder.newModifierGroup()
                .withCursorModifier().withShowTooltip(true).build()
                .build();

        // Add the CursorModifier to the SciChartSurface
        surface.getChartModifiers().add(cursorModifier);
    }
}