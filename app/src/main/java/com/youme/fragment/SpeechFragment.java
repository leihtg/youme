package com.youme.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.youme.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Thinkpad on 2017/2/9.
 */
public class SpeechFragment extends Fragment {
    private SpeechUtil speech;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.speech_layout, null);
        speech = new SpeechUtil(view, getActivity());
        return view;
    }

    @Override
    public void onDestroy() {
        speech.onDestroy();
        super.onDestroy();
    }

    class SpeechUtil {

        private EditText mEditText = null;
        private Button readButton = null;
        private Button saveButton = null;
        private CheckBox mCheckBox = null;
        private RadioGroup languageRadio = null;
        private RadioButton china = null;
        private TextToSpeech mTextToSpeech = null;
        private Locale locale = null;
        private TextView showView = null;
        private View view;
        private ProgressListener pListener = new ProgressListener();
        private final Activity activity;

        public SpeechUtil(View view, final Activity activity) {
            this.view = view;
            this.activity = activity;

            mEditText = (EditText) view.findViewById(R.id.edittext);
            readButton = (Button) view.findViewById(R.id.rbutton);
            saveButton = (Button) view.findViewById(R.id.sbutton);
            mCheckBox = (CheckBox) view.findViewById(R.id.rCheckbox);
            languageRadio = (RadioGroup) view.findViewById(R.id.languageSelect);
            china = (RadioButton) view.findViewById(R.id.china);
            showView = (TextView) view.findViewById(R.id.showText);

            languageRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.china:
                            locale = Locale.CHINESE;
                            break;
                        case R.id.english:
                            locale = Locale.ENGLISH;
                            break;
                        default:
                            return;
                    }

                    //实例并初始化TTS对象
                    mTextToSpeech = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status == TextToSpeech.SUCCESS) {
                                //设置朗读语言
                                int supported = mTextToSpeech.setLanguage(locale);
                                if ((supported != TextToSpeech.LANG_AVAILABLE) && (supported != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
                                    Toast.makeText(activity, "不支持当前语言！", Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                    });
                }
            });
            china.setChecked(true);
            //朗读监听按钮
            readButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    //朗读EditText里的内容
                    String text = mEditText.getText().toString();
                    if (text == null || "".equals(text)) {
                        return;
                    }
                    if (text.startsWith("http")) {
                        new Thread(new NetThread(text)).start();
                    } else {
                        speek(text);
                    }

                }
            });


            //保存按钮监听
            saveButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    //将EditText里的内容保存为语音文件
                    File sdCardDir = Environment.getExternalStorageDirectory();
                    File savePath = new File(sdCardDir, "speech");
                    if (!savePath.exists()) {
                        savePath.mkdirs();
                    }
                    String str = mEditText.getText().toString();
                    if (null == str || "".equals(str)) {
                        return;
                    }
                    int end = str.length() > 5 ? 5 : str.length();
                    File file = new File(savePath, str.substring(0, end) + ".wav");
                    int r = mTextToSpeech.synthesizeToFile(str, null, file, Math.random() * 1000 + "");
                    if (r == TextToSpeech.SUCCESS) {
                        Toast.makeText(activity, "保存成功！\n路径:" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            //EditText内容变化监听
            mEditText.addTextChangedListener(mTextWatcher);
        }

        public String getContext(String urls) {
            StringBuffer sb = null;
            String str = "";
            try {
                URL url = new URL(urls);
                URLConnection conn = url.openConnection();
                int len = conn.getContentLength();
                len = len < 0 ? 0 : len;
                String cset = null;
                String ct = conn.getContentType();
                if (null != ct && ct.indexOf("charset=") >= 0) {
                    cset = ct.substring(ct.indexOf("charset=") + 8);
                }

                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                Pattern pbody = Pattern.compile("<body.*?>(.*)</body>");
                Pattern cp = Pattern.compile("<meta\\s+charset=\"(.*)\"");
                sb = new StringBuffer(len);

                String line;
                while ((line = br.readLine()) != null) {
                    if (cset == null) {
                        Matcher m = cp.matcher(line);
                        if (m.find()) {
                            cset = m.group(1);
                        }
                    }
                    sb.append(line);
                }

                Matcher m = pbody.matcher(sb.toString());
                while (m.find()) {
                    str = m.group(1);
                }
                Pattern filter = Pattern.compile(
                        "(?i)<script.*?>.*?</script>|<head.*?>.*?</head>|<style.*?>.*?</style>|<!--.*?>?.*?(?:<!.*?>|-->)|&[#a-z0-9]*?;|<button.*?>.*?</button>|"
                                + "(<(h\\d|center|hr|div|ul|li|a|s(?:pan|trong|)|img|textarea|p|br|i|em|form|label|b).*?>)|</(h\\d|center|div|ul|li|a|span|img|textarea|p|strong|i|em|form|label|s|b)>");
                Matcher mf = filter.matcher(str);
                StringBuffer buf = new StringBuffer(sb.length());
                buf.append(mf.replaceAll(" "));
                Pattern space = Pattern.compile("(\\s|\\t)+");
                Matcher sm = space.matcher(buf.toString());
                if (sm.find()) {
                    str = sm.replaceAll(" ");
                }
                str = new String(str.getBytes(), cset == null ? "gbk" : cset);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }

        protected void onDestroy() {

            if (mTextToSpeech != null) {
                mTextToSpeech.shutdown();//关闭TTS
            }
        }

        private TextWatcher mTextWatcher = new TextWatcher() {
            private int start = 0, num = 0;

            @Override
            public void afterTextChanged(Editable s) {
                //如果是边写边读
                if (mCheckBox.isChecked() && (s.length() != 0)) {
                    //获得EditText的所有内容
                    String t = s.toString();
                    mTextToSpeech.speak(t.substring(start, start + num), TextToSpeech.QUEUE_FLUSH, null, "1");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int before,
                                          int count) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                this.start = start;
                this.num = count;
            }
        };

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String val = data.getString("value");
                // TODO
                // UI界面的更新等相关操作
                Toast.makeText(activity, "查询完成", Toast.LENGTH_LONG).show();
                showView.setText(val);
                showView.setMovementMethod(ScrollingMovementMethod.getInstance());
                speek(val);
            }
        };

        public void speek(String text) {
            ArrayList<String> list = new ArrayList<>();
            int s = 0, e = 0, len = text.length(), max = mTextToSpeech.getMaxSpeechInputLength();
            pListener.setText(list);
            mTextToSpeech.setOnUtteranceProgressListener(pListener);
            Bundle bundle=new Bundle();
            bundle.putString(TextToSpeech.Engine.KEY_PARAM_STREAM, "uid");

            for (int i = 0; i < len; i += max) {
                e = i + max;
                e = e > len ? len : e;
                list.add(text.substring(i, e));
                mTextToSpeech.speak(text.substring(i, e), TextToSpeech.QUEUE_ADD, bundle, "uid");
            }

        }

        class NetThread implements Runnable {
            private String url;

            private NetThread(String url) {
                this.url = url;
            }

            @Override
            public void run() {
                // TODO
                // 在这里进行 http request.网络请求相关操作
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("value", getContext(url));
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }
    }

    class ProgressListener extends UtteranceProgressListener {
        private ArrayList<String> text;
        private TextToSpeech tts;

        public void setTts(TextToSpeech tts) {
            this.tts = tts;
        }

        public void setText(ArrayList<String> txt) {
            this.text = txt;
        }

        @Override
        public void onDone(String utteranceId) {
            Bundle bundle=new Bundle();
            bundle.putString(TextToSpeech.Engine.KEY_PARAM_STREAM,"uid");
            if(null!=text&&text.size()>0){
                tts.speak(text.remove(0), TextToSpeech.QUEUE_ADD, bundle, "uid");
            }
        }

        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onError(String utteranceId) {

        }
    }
}
