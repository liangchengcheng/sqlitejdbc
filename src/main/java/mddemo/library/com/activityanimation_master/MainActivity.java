package mddemo.library.com.activityanimation_master;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static String dbpath = "";
    private ListView listView;
    private int max = 20;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbpath = this.getFilesDir().getAbsolutePath() + "/test.db";
        checkDatabase();
        listView = (ListView) findViewById(R.id.listView);
    }

    /**
     * 检验数据库,确保能运行以后的程序
     */
    private void checkDatabase() {
        try {
            Class.forName("org.sqldroid.SqldroidDriver");
            if (new File(dbpath).exists()) {
                // updateDatabase();版本迭代时更新数据库
            } else {
                new File(dbpath).createNewFile();
                String sql = SqliteUtils.readFile(getResources().getAssets().open("backup.sql"), HTTP.UTF_8, 1, false);
                SqliteUtils.manage(sql, null);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
    }

    /**
     * 点击事件
     * @param view 按钮
     */
    public void run(View view) {
        for (int i = max - 20; i < max; i++) {
            new Insert(i).start();
        }
        max += 20;
    }

    public class Insert extends Thread {

        private int index;

        public Insert(int index) {
            this.index = index;
        }

        public void run() {
            try {
                String sql = "insert into backup (url, param, memberId, data, edit_date) values (?, ?, ?, ?, ?)";
                JSONObject object = new JSONObject();
                object.put("index", index);
                Object[] args = new Object[] { "http://www.baidu.com/" + index, "param=" + index, index,
                        object.toString(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) };
                SqliteUtils.manage(sql, args);
            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
        }
    }

    /**
     * 查询插入的数据
     * @param view
     */
    public void look(View view) {
        try {
            final String[] keys = new String[] { "id", "url", "param", "memberId", "data", "edit_date" };
            String sql = "select * from backup order by id desc";
            final List<Map<String, Object>> listItem = SqliteUtils.getMaps(sql, null, keys);
            ListAdapter adapter = new SimpleAdapter(this, listItem, R.layout.item, new String[] { "url" },
                    new int[] { R.id.name });
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, Object> map = listItem.get(position);
                    String message = "";
                    for (int i = 0; i < keys.length; i++) {
                        message += keys[i] + "：" + map.get(keys[i]) + "\n";
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage(message);
                    builder.setPositiveButton("确定", null);
                    builder.create().show();
                }
            });
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
    }

}
