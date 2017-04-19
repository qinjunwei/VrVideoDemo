package com.qinjunwei.vrvideodemo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.widget.TextView;

import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private VrVideoView vr;
    private AppCompatSeekBar sk;
    private TextView tv;
    private MyAsyncTask mTask;
    boolean boo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vr = (VrVideoView) findViewById(R.id.vr);
        sk = (AppCompatSeekBar) findViewById(R.id.sk);
        tv = (TextView) findViewById(R.id.tv);

        //隐藏左下角信息按钮
        vr.setInfoButtonEnabled(false);
        //切换VR模式   设备模式
        vr.setDisplayMode(VrVideoView.DisplayMode.FULLSCREEN_STEREO);

        //对VR设置监听
        MyLisner lisner = new MyLisner();
        vr.setEventListener(lisner);

        //开启异步任务
        mTask = new MyAsyncTask();
        mTask.execute("congo.mp4");
    }


    class MyAsyncTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... params) {
            //设置参数对象    Options，用于解码Bitmap时的各种参数控制
            VrVideoView.Options ops = new VrVideoView.Options();
            try {
                //设置立体模式
                ops.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER;
                //处理加载格式，默认模式 SD  assets     HLS
                ops.inputFormat = VrVideoView.Options.FORMAT_DEFAULT;

//                从本地资源加载到内存中
                vr.loadVideoFromAsset(params[0],ops);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class MyLisner extends VrVideoEventListener{
        //成功回调
        @Override
        public void onLoadSuccess() {
            super.onLoadSuccess();

            //获取视频最大长度
            int duration = (int) vr.getDuration();
            //设置seekbar最大长度
            sk.setMax(duration);
        }

        @Override
        public void onLoadError(String errorMessage) {
            super.onLoadError(errorMessage);
        }


        //视频每获得一帧时回调
        @Override
        public void onNewFrame() {
            super.onNewFrame();
            //获取视频当前的进度
            int progress = (int) vr.getCurrentPosition();
            //设置SeekBar位置
            sk.setProgress(progress);

            tv.setText("当前进度："+String.format("%.2f",progress/1000.f));
        }

        //当视频结束后回调
        @Override
        public void onCompletion() {
            super.onCompletion();
            //让视频回到原点
            vr.seekTo(0);
            //停止视频
            vr.pauseVideo();
            //seekBar回到原点
            sk.setProgress(0);

            //视频播放完成重新设置为true
            boo = true;
        }

        @Override
        public void onClick() {
            super.onClick();

            if(boo){
                vr.playVideo();
            }else {
                vr.pauseVideo();
            }

            boo = !boo;
        }
    }

    /**
     * 内存优化
     */
    @Override
    protected void onPause() {
        vr.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        vr.resumeRendering();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭渲染
        vr.shutdown();
        //判断
        if(mTask!=null){
            if(!mTask.isCancelled()){
                mTask.cancel(true);
            }
        }
    }
}
