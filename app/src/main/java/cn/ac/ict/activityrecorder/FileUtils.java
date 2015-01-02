package cn.ac.ict.activityrecorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

public class FileUtils {
    private String dirName = null;
    private String timeFileName = null;
    private String typeFileName = null;
    final private String TAG = "FileUtils";
    final private String Encoding = "UTF-8";

    public FileUtils(String dirName, String timeFileName, String typeFileName)
            throws IOException {
        // 获取SD卡目录
        String sdPath = Environment.getExternalStorageDirectory() + "/";
        this.dirName = sdPath + dirName + "/";
        this.timeFileName = this.dirName + timeFileName;
        this.typeFileName = this.dirName + typeFileName;
        createSDDir(this.dirName);
        createSDFile(this.timeFileName);
        createSDFile(this.typeFileName);
    }

    /**
     * 创建文件
     */
    private File createSDFile(String fName) throws IOException {
        File file = new File(fName);
        if (!file.exists()) {
            boolean res = file.createNewFile();
            if (!res) {
                Log.e(TAG, fName + ": Create Fail");
            }
        }
        return file;
    }

    /**
     * 创建目录
     */
    private File createSDDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            boolean res = file.mkdir();
            if (!res) {
                Log.e(TAG, dir + ": Create Fail");
            }
        }
        return file;
    }

    /**
     * 将msg追加到写入SD文件中
     *
     * @throws IOException
     */
    public void appendLine(String msg) throws IOException {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(
                    this.timeFileName, true), Encoding);
            writer.write(msg + "\r\n");
        } catch (Exception e) {
            Log.i(TAG, "appendLine write==>" + e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e2) {
                Log.i(TAG, "appendLine close==>" + e2);
            }
        }
    }

    /**
     * 添加活动类型，如果活动类型已经存在，则忽略
     */
    public boolean addActivityType(String msg) {
        if (isTypeExist(msg)) {
            return false;
        }
        boolean result = true;
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(
                    this.typeFileName, true), Encoding);
            writer.write(msg + "\r\n");
        } catch (Exception e) {
            Log.i(TAG, "appendLine write==>" + e);
            result = false;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e2) {
                Log.i(TAG, "appendLine close==>" + e2);
                result = false;
            }
        }
        return result;
    }

    /**
     * 判断活动类型是否已经存在
     */
    private boolean isTypeExist(String msg) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    this.typeFileName), Encoding));
            String line;
            while ((line = br.readLine()) != null) {
                if (msg.equals(line))
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    /**
     * 获取所有活动类型
     */
    public ArrayList<String> getAllActivityType() {
        BufferedReader br = null;
        ArrayList<String> result = new ArrayList<>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    this.typeFileName), Encoding));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals("") && !result.contains(line))
                    result.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    /**
     * 获取当前正在执行的活动类型，如果没有则返回null
     */
    public String GetCurrentActivityName() {
        try {
            File file = new File(this.timeFileName);
            String lastLine = readLastLine(file, Encoding);
            if (lastLine == null || lastLine.equals(""))
                return null;
            String[] data = lastLine.split(",");
            if (data.length >= 3) {// 数据格式为:睡觉,start,20140814181054
                String status = data[1];
                if (status.equals("start")) {
                    return data[0];
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // 读取一行数据
    public static String readLastLine(File file, String charset)
            throws IOException {
        if (!file.exists() || file.isDirectory() || !file.canRead()) {
            return null;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            long len = raf.length();
            if (len == 0L) {
                return "";
            } else {
                long pos = len - 1;
                while (pos > 0) {
                    pos--;
                    raf.seek(pos);
                    if (raf.readByte() == '\n') {
                        break;
                    }
                }
                if (pos == 0) {
                    raf.seek(0);
                }
                byte[] bytes = new byte[(int) (len - pos)];
                raf.read(bytes);
                if (charset == null) {
                    return new String(bytes);
                } else {
                    return new String(bytes, charset);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }
}
