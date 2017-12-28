package com.youme.fragment;

import android.app.Activity;
import android.content.Context;
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
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.youme.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Thinkpad on 2017/2/9.
 */
public class SpeechFragment extends Fragment {
    private final int QUERY_INTERNET = 0;
    private SpeechUtil speech;
    private Activity activity;
    private EditText mEditText = null;
    private Button readButton, saveButton = null;
    private TextView showView = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.speech_layout, null);
        activity = getActivity();
        speech = new SpeechUtil(activity);
        mEditText = (EditText) view.findViewById(R.id.edittext);
        readButton = (Button) view.findViewById(R.id.rbutton);
        saveButton = (Button) view.findViewById(R.id.sbutton);
        showView = (TextView) view.findViewById(R.id.showText);

        return view;
    }

    @Override
    public void onDestroy() {
        if (speech != null) {
            speech.onDestroy();
        }
        super.onDestroy();
    }

    class SpeechUtil {
        private TextToSpeech mTextToSpeech;
        private Locale locale = Locale.CHINESE;//默认中文
        private ProgressListener pListener = new ProgressListener();

        public SpeechUtil(final Activity activity) {
            //朗读监听按钮
            readButton.setOnClickListener(clickListener);
            //保存按钮监听
            saveButton.setOnClickListener(clickListener);
            //EditText内容变化监听
            mEditText.addTextChangedListener(mTextWatcher);

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

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.edittext:
                        //朗读EditText里的内容
                        String text = mEditText.getText().toString();
                        if (text == null || "".equals(text)) {
                            return;
                        }
                        if (text.startsWith("http")) {
                            getContext(text);
                        } else {
                            speek(text);
                        }
                    case R.id.sbutton:
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
                        break;
                }
            }
        };

        final class InJavaScriptLocalObj {
            @JavascriptInterface
            public void showSource(String html) {
                showView.setText(html);
            }
        }


        public void getContext(String urls) {
            WebView webView = new WebView(activity);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    // 在结束加载网页时会回调
                    // 获取页面内容
                    view.loadUrl("javascript:window.java_obj.showSource(document.getElementsByTagName('html')[0].innerHTML);");
                    handler.sendEmptyMessage(QUERY_INTERNET);
                    super.onPageFinished(view, url);
                }

            });
            webView.loadUrl(urls);
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
                if (s.length() != 0) {
                    //获得EditText的所有内容
                    String t = s.toString();
                    mTextToSpeech.speak(t.substring(start, start + num), TextToSpeech.QUEUE_FLUSH, null, "1");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                this.start = start;
                this.num = count;
            }
        };

        public void speek(String text) {
            ArrayList<String> list = new ArrayList<>();
            int s = 0, e = 0, len = text.length(), max = mTextToSpeech.getMaxSpeechInputLength();
            pListener.setText(list);
            mTextToSpeech.setOnUtteranceProgressListener(pListener);
            Bundle bundle = new Bundle();
            bundle.putString(TextToSpeech.Engine.KEY_PARAM_STREAM, "uid");

            for (int i = 0; i < len; i += max) {
                e = i + max;
                e = e > len ? len : e;
                list.add(text.substring(i, e));
                mTextToSpeech.speak(text.substring(i, e), TextToSpeech.QUEUE_ADD, bundle, "uid");
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
            Bundle bundle = new Bundle();
            bundle.putString(TextToSpeech.Engine.KEY_PARAM_STREAM, "uid");
            if (null != text && text.size() > 0) {
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QUERY_INTERNET:
                    Toast.makeText(activity, "查询完成", Toast.LENGTH_LONG).show();
                    speech.speek(showView.getText().toString());
                default:
                    super.handleMessage(msg);
            }

        }
    };
}

