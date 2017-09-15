package com.zd.wilddogdoctordemo.service;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.wilddog.video.CallStatus;
import com.wilddog.video.Conversation;
import com.wilddog.video.LocalStream;
import com.wilddog.video.LocalStreamOptions;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoError;
import com.zd.wilddogdoctordemo.R;
import com.zd.wilddogdoctordemo.beans.Result;
import com.zd.wilddogdoctordemo.beans.User;
import com.zd.wilddogdoctordemo.cons.ConversationCons;
import com.zd.wilddogdoctordemo.net.Net;
import com.zd.wilddogdoctordemo.storage.ObjectPreference;
import com.zd.wilddogdoctordemo.storage.memory.ObjectProvider;
import com.zd.wilddogdoctordemo.ui.doctor.VideoReceiverActivity;
import com.zd.wilddogdoctordemo.utils.Util;

import java.util.HashMap;

import io.reactivex.annotations.NonNull;


/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class VideoReceiverService extends Service implements Conversation.Listener {

    private static final String TAG = "VideoReceiverService";
    private Conversation mVideoConversation;
    private WilddogVideo mWilddogVideo;
    private LocalStream mLocalStream;
    private RemoteStream mRemoteStream;

    private Messenger mServerMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
//                服务绑定连接上
                case ConversationCons.CONNECTED:
                    mClientMessenger = message.replyTo;
                    break;
//                挂断
                case ConversationCons.HANG_UP:
                    closeConversation();
                    break;
//                接受
                case ConversationCons.ACCEPT:
                    if (mRingtone != null && mRingtone.isPlaying()) {
                        mRingtone.stop();
                        mRingtone = null;
                    }
                    if (mLocalStream == null) {
                        createLocalStream();
                    }
                    mVideoConversation.accept(mLocalStream);
                    break;
//                拒绝
                case ConversationCons.REJECT:
                    if (mRingtone != null && mRingtone.isPlaying()) {
                        mRingtone.stop();
                        mRingtone = null;
                    }
                    mVideoConversation.reject();
                    mVideoConversation = null;
                    break;
                default:
                    break;
            }
            return true;
        }
    }));
    private Messenger mClientMessenger;
    private boolean onCall = false;
    private User mUser;
    private Ringtone mRingtone;

    private long mStartTime;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mWilddogVideo.stop();
        Net.instance().removeRequest(VideoReceiverService.class.getSimpleName());
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mUser = Util.getUser(getApplicationContext());
            initWilddogVideo(mUser.getWilddogVideoToken());
        }
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mUser = null;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        mUser = Util.getUser(getApplicationContext());
        super.onRebind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mUser = Util.getUser(getApplicationContext());
        return mServerMessenger.getBinder();
    }


    private void initWilddogVideo(String token) {
        String videoAppID = getResources().getString(R.string.video_app_id);
        WilddogVideo.initialize(getApplicationContext(), videoAppID, token);
        mWilddogVideo = WilddogVideo.getInstance();
        mWilddogVideo.setListener(new WilddogVideo.Listener() {
            @Override
            public void onCalled(Conversation conversation, String s) {
                if (mVideoConversation != null) {
                    conversation.reject();
                    return;
                }
                HashMap<String, String> data = new Gson().fromJson(s, HashMap.class);
                User user = new User();
                user.setHead_img_url(data.get("faceurl"));
                user.setNick_name(data.get("nickname"));
                mVideoConversation = conversation;
                mVideoConversation.setConversationListener(VideoReceiverService.this);
                Intent intent = new Intent(VideoReceiverService.this, VideoReceiverActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("user", user);
                intent.putExtra("doctor", mUser);
                startActivity(intent);
                playRing();
            }

            @Override
            public void onTokenError(WilddogVideoError wilddogVideoError) {
                String message = wilddogVideoError.getMessage();
                Log.d(TAG, "onTokenError: " + message);
            }
        });
        mWilddogVideo.start();
    }

    private void playRing() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mRingtone = RingtoneManager.getRingtone(this, uri);
        mRingtone.play();
    }

    private void createLocalStream() {
        LocalStreamOptions.Dimension videoResolution = ObjectPreference.getObject(getApplicationContext(),
                "video_resolution", LocalStreamOptions.Dimension.class);
        LocalStreamOptions.Builder builder = new LocalStreamOptions.Builder();
        if (videoResolution != null) {
            builder.dimension(videoResolution);
        } else {
            builder.dimension(LocalStreamOptions.Dimension.DIMENSION_480P);
        }
        LocalStreamOptions options = builder.build();
        mLocalStream = mWilddogVideo.createLocalStream(options);
    }


    private void closeConversation() {
        if (onCall) {
            uploadVideoConversationRecord();
            mVideoConversation.close();
//            mLocalStream.detach();
//            mRemoteStream.detach();
            mVideoConversation = null;
            mLocalStream = null;
            mRemoteStream = null;
            onCall = false;
        }
    }

    private void uploadVideoConversationRecord() {
        long duration = System.currentTimeMillis() / 1000 - mStartTime;
        Net.instance().uploadConversationRecord(mUser.getToken(), mUser.getDoc_id(),
                mVideoConversation.getRemoteUid(), mStartTime, duration, new Net.OnNext<Result<Double>>() {
                    @Override
                    public void onNext(@NonNull Result<Double> result) {
                        if (result.getCode() == 100) {
                            User user = Util.getUser(getApplicationContext());
                            user.setAmount(result.getData());
                            Util.saveUser(getApplicationContext(), user);
                        }
                    }
                }, new Net.OnError() {
                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d(TAG, "onError: " + e);
                    }
                }, VideoReceiverService.class.getSimpleName());
    }


    @Override
    public void onCallResponse(CallStatus callStatus) {
    }

    @Override
    public void onStreamReceived(RemoteStream remoteStream) {
        mRemoteStream = remoteStream;
        Message msg = Message.obtain();
        msg.what = ConversationCons.STREAM_RECEIVED;
        msg.obj = new VideoStream(mLocalStream, mRemoteStream);
        try {
            mClientMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        onCall = true;
        mStartTime = System.currentTimeMillis() / 1000;
    }

    @Override
    public void onClosed() {
        closeConversation();
        Message msg = Message.obtain();
        msg.what = ConversationCons.HANG_UP;
        try {
            mClientMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WilddogVideoError wilddogVideoError) {
        String message = wilddogVideoError.getMessage();
        Log.d(TAG, "onError: " + message);
    }

    public class VideoStream {
        public LocalStream mLocalStream;
        public RemoteStream mRemoteStream;

        public VideoStream(LocalStream localStream, RemoteStream remoteStream) {
            mLocalStream = localStream;
            mRemoteStream = remoteStream;
        }
    }

}
