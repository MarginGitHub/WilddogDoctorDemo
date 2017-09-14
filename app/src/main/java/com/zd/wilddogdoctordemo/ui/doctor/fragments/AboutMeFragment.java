package com.zd.wilddogdoctordemo.ui.doctor.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.popup.QMUIListPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.video.LocalStreamOptions;
import com.wilddog.video.WilddogVideo;
import com.wilddog.wilddogauth.WilddogAuth;
import com.yalantis.ucrop.UCrop;
import com.zd.wilddogdoctordemo.R;
import com.zd.wilddogdoctordemo.beans.Login;
import com.zd.wilddogdoctordemo.beans.Result;
import com.zd.wilddogdoctordemo.net.Net;
import com.zd.wilddogdoctordemo.net.NetServiceConfig;
import com.zd.wilddogdoctordemo.storage.ObjectPreference;
import com.zd.wilddogdoctordemo.storage.memory.ObjectProvider;
import com.zd.wilddogdoctordemo.ui.LoginActivity;
import com.zd.wilddogdoctordemo.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.annotations.NonNull;

import static android.app.Activity.RESULT_OK;


/**
 * Created by dongjijin on 2017/9/6 0006.
 */

public class AboutMeFragment extends BaseFragment {

    private static final int CAPTURE_REQUEST_CODE = 1;
    private static final int ALBUM_REQUEST_CODE = 2;
    @BindView(R.id.head_iv)
    ImageView mHeadIv;
    @BindView(R.id.nick_name)
    TextView mNickName;
    @BindView(R.id.balance_account)
    TextView mBalanceAccount;
    Unbinder unbinder;
    @BindView(R.id.video_resolution)
    TextView mVideoResolution;
    @BindView(R.id.add_ad)
    TextView mAddAd;
    private boolean isAddAd;
    private QMUIListPopup mVideoResolutionListPopup;
    private QMUIListPopup mAdPopup;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_about_me, container, false);
        unbinder = ButterKnife.bind(this, view);
        initViews();
        return view;
    }

    @Override
    public void onDestroyView() {
        Net.instance().removeRequest(AboutMeFragment.class.getSimpleName());
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
    }


    protected void initViews() {
        mNickName.setText(mUser.getNick_name());
        //        设置用户头像
        String imgUrl = mUser.getHead_img_url();
        if (!TextUtils.isEmpty(imgUrl)) {
            Util.setImageView(this, mHeadIv, NetServiceConfig.HEAD_IMAGE_BASE_URL + imgUrl);
        } else {
            Util.setImageView(this, mHeadIv, null);
        }
        LocalStreamOptions.Dimension videoResolution = ObjectPreference.getObject(getContext(),
                "video_resolution", LocalStreamOptions.Dimension.class);
        String resolution = "视频通话分辨率:\t";
        if (videoResolution != null) {
            switch (videoResolution) {
                case DIMENSION_360P:
                    resolution += "360p";
                    break;
                case DIMENSION_480P:
                    resolution += "480p";
                    break;
                case DIMENSION_720P:
                    resolution += "720p";
                    break;
                case DIMENSION_1080P:
                    resolution += "1080p";
                    break;
            }
        } else {
            resolution += "480p";
        }
        mVideoResolution.setText(resolution);
        mBalanceAccount.setText(String.format("余额: %f元", mUser.getAmount()));
    }


    @OnClick({R.id.logout_button, R.id.video_resolution, R.id.head_iv, R.id.add_ad})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.head_iv:
                isAddAd = false;
                changeImageView();
                break;
            case R.id.add_ad:
                showAd();
                break;
            case R.id.video_resolution:
                selectVideoResolution();
                break;
            case R.id.logout_button:
                logout();
                break;
        }
    }

    private void selectVideoResolution() {
        if (mVideoResolutionListPopup == null) {
            final String[] items = new String[]{
                    "360p",
                    "480p",
                    "720p",
                    "1080p"
            };
            final LocalStreamOptions.Dimension[] resolutions = new LocalStreamOptions.Dimension[]{
                    LocalStreamOptions.Dimension.DIMENSION_360P,
                    LocalStreamOptions.Dimension.DIMENSION_480P,
                    LocalStreamOptions.Dimension.DIMENSION_720P,
                    LocalStreamOptions.Dimension.DIMENSION_1080P
            };
            ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.item_video_resolution_popu, items);
            mVideoResolutionListPopup = new QMUIListPopup(getContext(), QMUIPopup.DIRECTION_NONE, adapter);
            mVideoResolutionListPopup.create(QMUIDisplayHelper.dp2px(getContext(), 300), QMUIDisplayHelper.dp2px(getContext(), 300),
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            String item = items[i];
                            mVideoResolution.setText("视频通话分辨率:\t" + item);
                            ObjectPreference.saveObject(getContext(), "video_resolution", resolutions[i]);
                            mVideoResolutionListPopup.dismiss();
                        }
                    });
        }
        mVideoResolutionListPopup.setAnimStyle(QMUIPopup.ANIM_AUTO);
        mVideoResolutionListPopup.setPreferredDirection(QMUIPopup.DIRECTION_BOTTOM);
        mVideoResolutionListPopup.show(mVideoResolution);
    }

    private void showAd() {
        if (mAdPopup == null) {
            mAdPopup = new QMUIListPopup(getContext(), QMUIPopup.DIRECTION_NONE, new BaseAdapter() {

                @Override
                public int getCount() {
                    return 1;
                }

                @Override
                public Object getItem(int i) {
                    return null;
                }

                @Override
                public long getItemId(int i) {
                    return 0;
                }

                @Override
                public View getView(int i, View view, ViewGroup viewGroup) {
                    if (view == null) {
                        view = LayoutInflater.from(getContext()).inflate(R.layout.view_display_ad_img, null);
                    }
                    Util.setImageView(getContext(), (ImageView) view.findViewById(R.id.ad_iv),
                            NetServiceConfig.AD_BASE_URL + mUser.getAd_url(), R.drawable.banner, false);
                    view.findViewById(R.id.update_ad).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isAddAd = true;
                            changeImageView();
                            mAdPopup.dismiss();
                        }
                    });
                    return view;
                }
            });
            mAdPopup.create(QMUIDisplayHelper.dp2px(getContext(), 300), QMUIDisplayHelper.dp2px(getContext(), 300),
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        }
                    });
        }
        mAdPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);
        mAdPopup.setPreferredDirection(QMUIPopup.DIRECTION_BOTTOM);
        mAdPopup.show(mAddAd);
    }

    private void logout() {
//        野狗方面的退出
        WilddogVideo.getInstance().stop();
        WilddogAuth.getInstance().signOut();
        WilddogSync.goOffline();
//        修改login信息
        Login login = ObjectPreference.getObject(getContext(), Login.class);
        login.setAutoLogin(false);
        ObjectPreference.saveObject(getContext(), login);
//        跳转到登录界面
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }


    private void changeImageView() {
        final String[] items = new String[]{"相机", "相册", "文件夹"};
        new QMUIDialog.CheckableDialogBuilder(getActivity())
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                openCamera();
                                break;
                            case 1:
                                openAlbum();
                                break;
                            case 2:
                                Toast.makeText(getContext(), "暂未实现", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                })
                .show();
    }

    private void openCamera() {
        String externalStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_UNMOUNTED.equals(externalStorageState)) {
            Toast.makeText(getContext(), "手机无外部存储无法拍照", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri;
        if (!isAddAd) {
            uri = Uri.fromFile(Util.getHeadImgFile("doctor_avatar.jpg"));
        } else {
            uri = Uri.fromFile(Util.getHeadImgFile("doctor_banner.jpg"));
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CAPTURE_REQUEST_CODE);
    }

    private void openAlbum() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, ALBUM_REQUEST_CODE);
        } else {

        }
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAPTURE_REQUEST_CODE:
                if (resultCode != -1) {
                    return;
                }
                if (!isAddAd) {
                    Uri uri = Uri.fromFile(Util.getHeadImgFile("doctor_avatar.jpg"));
                    try {
                        MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                                uri.getPath(), "doctor_avatar", null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
//                最后通知图库更新
                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                    UCrop.of(uri, Uri.fromFile(Util.getHeadImgFile("doctor_avatar_circle.jpg")))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(400, 400)
                            .start(getContext(), this);
                } else {
                    Uri uri = Uri.fromFile(Util.getHeadImgFile("doctor_banner.jpg"));
                    try {
                        MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                                uri.getPath(), "doctor_banner", null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
//                最后通知图库更新
                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                    UCrop.of(uri, Uri.fromFile(Util.getHeadImgFile("doctor_banner_resize.jpg")))
                            .withAspectRatio(16, 9)
                            .withMaxResultSize(712, 400)
                            .start(getContext(), this);
                }

                break;
            case ALBUM_REQUEST_CODE:
                if (data == null) {
                    return;
                }
                Uri selectUri = data.getData();
                if (selectUri == null) {
                    return;
                }
                if (!isAddAd) {
                    File imgFile = Util.getHeadImgFile("doctor_avatar_circle.jpg");
                    UCrop.of(selectUri, Uri.fromFile(imgFile))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(400, 400)
                            .start(getContext(), this);
                } else {
                    File imgFile = Util.getHeadImgFile("doctor_banner_resize.jpg");
                    UCrop.of(selectUri, Uri.fromFile(imgFile))
                            .withAspectRatio(16, 9)
                            .withMaxResultSize(712, 400)
                            .start(getContext(), this);
                }

            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri output = UCrop.getOutput(data);
                    if (output != null && output.getPath() != null) {
                        if (!isAddAd) {
                            Net.instance().uploadDoctorAvatar(mUser.getToken(), mUser.getUser_id(), output.getPath(), new Net.OnNext<Result<String>>() {
                                        @Override
                                        public void onNext(@NonNull Result<String> result) {
                                            if (result.getCode() == 100) {
                                                Util.setAvatarView(getContext(), mHeadIv, output);
                                                String url = result.getData();
                                                mUser.setHead_img_url(url);
                                                Util.saveUser(getContext(), mUser);
                                                Login login = ObjectPreference.getObject(getContext(), Login.class);
                                                login.setAvatarUrl(url);
                                                ObjectPreference.saveObject(getContext(), login);
                                            }
                                        }
                                    },
                                    new Net.OnError() {
                                        @Override
                                        public void onError(@NonNull Throwable e) {
                                            Log.d("uploadHead", "onError: " + e.toString());
                                        }
                                    }, AboutMeFragment.class.getSimpleName());
                        } else {
                            Net.instance().uploadDoctorAd(mUser.getToken(), mUser.getUser_id(), output.getPath(), new Net.OnNext<Result<String>>() {
                                        @Override
                                        public void onNext(@NonNull Result<String> result) {
                                            if (result.getCode() == 100) {
                                                mUser.setAd_url(result.getData());
                                                ObjectProvider.sharedInstance().set(mUser);
                                                Map<String, Object> param = new HashMap<>();
                                                param.put(mUser.getUser_id(), true);
                                                SyncReference doctorSyncReference = WilddogSync.getInstance()
                                                        .getReference(getResources().getString(R.string.doctors_room))
                                                        .child(mUser.getUser_id());
                                                doctorSyncReference.setValue(false);
                                                doctorSyncReference.setValue(true);
                                            }
                                        }
                                    },
                                    new Net.OnError() {
                                        @Override
                                        public void onError(@NonNull Throwable e) {

                                        }
                                    }, AboutMeFragment.class.getSimpleName());
                        }
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {

                }
                break;

        }
    }


}
