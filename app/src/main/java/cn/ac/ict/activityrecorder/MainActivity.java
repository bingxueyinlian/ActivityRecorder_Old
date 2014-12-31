package cn.ac.ict.activityrecorder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final String dirName = "ActivityRecorder";// 目录名
    private final String timeFileName = "ActivityTime.txt";// 活动时间文件名
    private final String typeFileName = "ActivityType.txt";// 活动类型文件名
    private Spinner spinnerActivity;
    private Button btnAddActivity;
    private Button btnStart;
    private TextView lblStatus;
    private static ArrayList<String> arrActivity = null;
    private FileUtils fileUtils;
    private String curActivity = null;// 当前活动

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        try {
            fileUtils = new FileUtils(dirName, timeFileName, typeFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        spinnerActivity = (Spinner) findViewById(R.id.spinnerActivity);
        btnAddActivity = (Button) findViewById(R.id.btnAddActivity);
        btnStart = (Button) findViewById(R.id.btnStart);
        lblStatus = (TextView) findViewById(R.id.lblStatus);
        lblStatus.setText("");
        fillSpinner();
        btnAddActivity.setOnClickListener(new AddActiviyButtonClick());
        btnStart.setOnClickListener(new StartActiviyButtonClick());

        // 初使化
        curActivity = fileUtils.GetCurrentActivityName();
        if (curActivity != null && !curActivity.equals("")) {
            int index = arrActivity.indexOf(curActivity);
            if (index != -1) {
                spinnerActivity.setSelection(index);// 设置默认值
                btnStart.setText(R.string.end);
                lblStatus.setText(curActivity + "中...");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void fillSpinner() {
        arrActivity = fileUtils.getAllActivityType();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arrActivity);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
        spinnerActivity.setAdapter(adapter);

    }

    class StartActiviyButtonClick implements OnClickListener {

        @Override
        public void onClick(View arg0) {
            if (arrActivity == null || arrActivity.size() == 0) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("没有活动类型，请先添加!")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", null).show();
                return;
            }
            if (curActivity == null || curActivity.equals("")) {// 当前没有活动在执行，则开始
                curActivity = spinnerActivity.getSelectedItem().toString();
                SaveData("start");
                btnStart.setText(R.string.end);
                lblStatus.setText(curActivity + "中...");

            } else {// 当前有活动在执行，则停止
                SaveData("stop");
                btnStart.setText(R.string.start);
                lblStatus.setText("");
                curActivity = null;
            }

        }

        // 保存数据
        private void SaveData(String status) {
            String time = new SimpleDateFormat("yyyyMMddHHmmss",
                    Locale.getDefault()).format(new Date());
            String msg = curActivity + "," + status + "," + time;
            try {
                fileUtils.appendLine(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class AddActiviyButtonClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            LayoutInflater factory = LayoutInflater.from(MainActivity.this);// 提示框
            final View view = factory.inflate(R.layout.activity_add, null);
            final EditText edit = (EditText) view
                    .findViewById(R.id.txtaddactivityname);// 获得输入框对象

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.inputactivityname) // 提示框标题
                    .setView(view)
                    .setPositiveButton(
                            R.string.ok,// 提示框的两个按钮
                            new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) { // 事件
                                    String newActivityName = edit.getText()
                                            .toString();
                                    newActivityName = newActivityName.trim();
                                    if (newActivityName.equals("")) {
                                        return;// 输入为空时不保存
                                    }
                                    boolean res = fileUtils.addActivityType(newActivityName);
                                    if (res) {
                                        fillSpinner();
                                    }
                                    int index = arrActivity.indexOf(newActivityName);
                                    if (index != -1) {
                                        spinnerActivity.setSelection(index);// 设置默认选择添加项
                                    }
                                }
                            }).setNegativeButton(R.string.cancel, null)
                    .create().show();
        }
    }

}
