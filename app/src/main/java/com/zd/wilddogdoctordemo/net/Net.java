package com.zd.wilddogdoctordemo.net;

import android.content.Context;
import android.util.Log;

import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogcore.WilddogApp;
import com.zd.wilddogdoctordemo.beans.Result;
import com.zd.wilddogdoctordemo.beans.User;
import com.zd.wilddogdoctordemo.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


/**
 * Created by dongjijin on 2017/9/8 0008.
 */

public class Net {
    private static final String TAG = Net.class.getSimpleName();
    private static Net instance;
    private NetService mNetService;
    private Map<String, List<Disposable>> mRequests;

    private Net(Context context) {
        mNetService = (NetService) NetServiceProvider.instance(context)
                .provider(NetService.class, NetServiceConfig.SERVER_BASE_URL);
        mRequests = new HashMap<>();
    }

    public static void init(Context context) {
        instance = new Net(context);
    }

    public static Net instance() {
        return instance;
    }

    public void addRequest(String id, Disposable d) {
        List<Disposable> disposableList = mRequests.get(id);
        if (disposableList == null) {
            disposableList = new ArrayList<>();
        }
        disposableList.add(d);
        mRequests.put(id, disposableList);
    }

    public void removeRequest(String id) {
        List<Disposable> disposableList = mRequests.get(id);
        if (disposableList != null) {
            for (Disposable d: disposableList) {
                if (!d.isDisposed()) {
                    d.dispose();
                }
            }
        }
        mRequests.remove(id);
    }

    public void register(String mobile, String password, final String ref, final OnNext<Result<User>> next,
                           final OnError error, final String id) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";
        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("mobile", mobile);
        params.put("password", password);
        params.put("ref", ref);
        String sign = Util.sign(params);
        mNetService.register(ts, apiKey, sign, mobile, password, ref)
                .take(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result<User>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addRequest(id, d);
                    }

                    @Override
                    public void onNext(@NonNull Result<User> userResult) {
                        next.onNext(userResult);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        error.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    public void login(String mobile, String password, final OnNext<User> next,
                      final OnError err, final String id) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";

        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("mobile", mobile);
        params.put("password", password);
        String sign = Util.sign(params);
        mNetService.login(ts, apiKey, sign, mobile, password)
                .flatMap(new Function<Result<User>, ObservableSource<User>>() {
                    @Override
                    public ObservableSource<User> apply(@NonNull final Result<User> userResult) throws Exception {
                        if (userResult.isSuccessful()) {
                            final User user = userResult.getData();
                            return new Observable<User>() {
                                @Override
                                protected void subscribeActual(final Observer<? super User> observer) {
                                    WilddogAuth.getInstance(WilddogApp.getInstance())
                                            .signInWithCustomToken(user.getWilddog_token())
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(Task<AuthResult> authResultTask) {
                                                    if (authResultTask.isSuccessful()) {
                                                        String token = authResultTask.getResult()
                                                                .getWilddogUser()
                                                                .getToken(false)
                                                                .getResult()
                                                                .getToken();
                                                        user.setWilddogVideoToken(token);
                                                        observer.onNext(user);
                                                        observer.onComplete();
                                                    } else {
                                                        observer.onError(new Throwable("登录失败", authResultTask.getException()));
                                                    }
                                                }
                                            });
                                }
                            };
                        }
                        throw new Exception(userResult.getMsg());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addRequest(id, d);
                    }

                    @Override
                    public void onNext(@NonNull User user) {
                        next.onNext(user);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    public void wilddogLogin(final User user, final OnNext<Boolean> next, final OnError err, final String id) {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Boolean> e) throws Exception {
                WilddogAuth.getInstance(WilddogApp.getInstance())
                        .signInWithCustomToken(user.getWilddog_token())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> authResultTask) {
                                if (authResultTask.isSuccessful()) {
                                    e.onNext(true);
                                    e.onComplete();
                                } else {
                                    e.onError(new Throwable("登录失败", authResultTask.getException()));
                                }
                            }
                        });
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addRequest(id, d);
                    }

                    @Override
                    public void onNext(@NonNull Boolean success) {
                        next.onNext(success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public void uploadDoctorAvatar(String token, String userId, String headUrl,
                                    final OnNext<Result<String>> next, final OnError err, final String id) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";
        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("token", token);
        params.put("userId", userId);
        String sign = Util.sign(params);
        File file = new File(headUrl);

        RequestBody tsBody = RequestBody.create(MediaType.parse("multipart/form-data"),ts);
        RequestBody apiKeyBody = RequestBody.create(MediaType.parse("multipart/form-data"),apiKey);
        RequestBody signBody = RequestBody.create(MediaType.parse("multipart/form-data"),sign);
        RequestBody userIdBody = RequestBody.create(MediaType.parse("multipart/form-data"),userId);
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"),file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("upfile", file.getName(), fileBody);
        mNetService.uploadAvatar(tsBody, apiKeyBody, signBody, userIdBody, part)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addRequest(id, d);
                    }

                    @Override
                    public void onNext(@NonNull Result<String> stringResult) {
                        next.onNext(stringResult);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void uploadDoctorAd(String token, String userId, String headUrl,
                                   final OnNext<Result<String>> next, final OnError err, final String id) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";
        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("token", token);
        params.put("userId", userId);
        String sign = Util.sign(params);
        File file = new File(headUrl);

        RequestBody tsBody = RequestBody.create(MediaType.parse("multipart/form-data"),ts);
        RequestBody apiKeyBody = RequestBody.create(MediaType.parse("multipart/form-data"),apiKey);
        RequestBody signBody = RequestBody.create(MediaType.parse("multipart/form-data"),sign);
        RequestBody userIdBody = RequestBody.create(MediaType.parse("multipart/form-data"),userId);
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"),file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("upfile", file.getName(), fileBody);
        mNetService.uploadDoctorAd(tsBody, apiKeyBody, signBody, userIdBody, part)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addRequest(id, d);
                    }

                    @Override
                    public void onNext(@NonNull Result<String> stringResult) {
                        next.onNext(stringResult);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void uploadConversationRecord(final String token, final String userId, String callId, long start, long duration,
                                              final OnNext<Result<Object>> next, final OnError err) {
        final String ts = String.valueOf(System.currentTimeMillis() / 1000);
        final String apiKey = "test";
        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("token", token);
        params.put("userId", userId);
        params.put("docId", callId);
        params.put("start", String.valueOf(start));
        params.put("duration", String.valueOf(duration));
        String sign = Util.sign(params);
        mNetService.uploadVideoConversationRecord(ts, apiKey, sign, userId, callId, start, duration)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<Result<Object>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Result<Object> objectResult) {
                        next.onNext(objectResult);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public interface OnNext<T> {
        void onNext(@NonNull T result);
    }

    public interface OnError {
        void onError(@NonNull Throwable e);
    }

}
