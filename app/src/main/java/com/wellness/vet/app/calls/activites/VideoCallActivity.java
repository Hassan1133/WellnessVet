package com.wellness.vet.app.calls.activites;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sendbird.calls.AcceptParams;
import com.sendbird.calls.AudioDevice;
import com.sendbird.calls.CallOptions;
import com.sendbird.calls.DialParams;
import com.sendbird.calls.DirectCall;
import com.sendbird.calls.DirectCallUserRole;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.SendBirdVideoView;
import com.wellness.vet.app.R;
import com.wellness.vet.app.calls.BaseApplication;
import com.wellness.vet.app.calls.utils.ToastUtils;

import org.webrtc.RendererCommon;

import java.util.Set;

public class VideoCallActivity extends CallActivity {

    private boolean mIsVideoEnabled;

    //+ Views
    private SendBirdVideoView mVideoViewFullScreen;
    private View mViewConnectingVideoViewFullScreenFg;
    private RelativeLayout mRelativeLayoutVideoViewSmall;
    private SendBirdVideoView mVideoViewSmall;
    private ImageView mImageViewCameraSwitch;
    private ImageView mImageViewVideoOff;
    //- Views

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_video_call;
    }

    @Override
    protected void initViews() {
        super.initViews();

        mVideoViewFullScreen = findViewById(R.id.video_view_fullscreen);
        mViewConnectingVideoViewFullScreenFg = findViewById(R.id.view_connecting_video_view_fullscreen_fg);
        mRelativeLayoutVideoViewSmall = findViewById(R.id.relative_layout_video_view_small);
        mVideoViewSmall = findViewById(R.id.video_view_small);
        mImageViewCameraSwitch = findViewById(R.id.image_view_camera_switch);
        mImageViewVideoOff = findViewById(R.id.image_view_video_off);
    }

    @Override
    protected void setViews() {
        super.setViews();

        mVideoViewFullScreen.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mVideoViewFullScreen.setZOrderMediaOverlay(false);
        mVideoViewFullScreen.setEnableHardwareScaler(true);

        mVideoViewSmall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mVideoViewSmall.setZOrderMediaOverlay(true);
        mVideoViewSmall.setEnableHardwareScaler(true);

        if (mDirectCall != null) {
            if (mDirectCall.getMyRole() == DirectCallUserRole.CALLER && mState == STATE.STATE_OUTGOING) {
                mDirectCall.setLocalVideoView(mVideoViewFullScreen);
                mDirectCall.setRemoteVideoView(mVideoViewSmall);
            } else {
                mDirectCall.setLocalVideoView(mVideoViewSmall);
                mDirectCall.setRemoteVideoView(mVideoViewFullScreen);
            }
        }

        mImageViewCameraSwitch.setOnClickListener(view -> {
            if (mDirectCall != null) {
                mDirectCall.switchCamera(e -> {
                    if (e != null) {
                        Log.i(BaseApplication.TAG, "[VideoCallActivity] switchCamera(e: " + e.getMessage() + ")");
                    }
                });
            }
        });

        if (mDirectCall != null && !mDoLocalVideoStart) {
            mIsVideoEnabled = mDirectCall.isLocalVideoEnabled();
        } else {
            mIsVideoEnabled = true;
        }
        mImageViewVideoOff.setSelected(!mIsVideoEnabled);
        mImageViewVideoOff.setOnClickListener(view -> {
            if (mDirectCall != null) {
                if (mIsVideoEnabled) {
                    mDirectCall.stopVideo();
                    mIsVideoEnabled = false;
                    mImageViewVideoOff.setSelected(true);
                } else {
                    mDirectCall.startVideo();
                    mIsVideoEnabled = true;
                    mImageViewVideoOff.setSelected(false);
                }
            }
        });
    }

    protected void setLocalVideoSettings(DirectCall call) {
        mIsVideoEnabled = call.isLocalVideoEnabled();
        mImageViewVideoOff.setSelected(!mIsVideoEnabled);
    }

    @Override
    protected void setAudioDevice(AudioDevice currentAudioDevice, Set<AudioDevice> availableAudioDevices) {

    }

    @Override
    protected void startCall(boolean amICallee) {
        CallOptions callOptions = new CallOptions();
        callOptions.setVideoEnabled(mIsVideoEnabled).setAudioEnabled(mIsAudioEnabled);

        if (amICallee) {
            callOptions.setLocalVideoView(mVideoViewSmall).setRemoteVideoView(mVideoViewFullScreen);
        } else {
            callOptions.setLocalVideoView(mVideoViewFullScreen).setRemoteVideoView(mVideoViewSmall);
        }

        if (amICallee) {
            if (mDirectCall != null) {
                mDirectCall.accept(new AcceptParams().setCallOptions(callOptions));
            }
        } else {
            mDirectCall = SendBirdCall.dial(new DialParams(mCalleeIdToDial).setVideoCall(mIsVideoCall).setCallOptions(callOptions), (call, e) -> {
                if (e != null) {
                    if (e.getMessage() != null) {
                        ToastUtils.showToast(mContext, e.getMessage());
                    }
                    finishWithEnding(e.getMessage());
                    return;
                }
                updateCallService();
            });
            setListener(mDirectCall);
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected boolean setState(STATE state, DirectCall call) {
        if (!super.setState(state,call)) {
            return false;
        }

        switch (state) {
            case STATE_ACCEPTING: {
                mVideoViewFullScreen.setVisibility(View.GONE);
                mViewConnectingVideoViewFullScreenFg.setVisibility(View.GONE);
                mRelativeLayoutVideoViewSmall.setVisibility(View.GONE);
                mImageViewCameraSwitch.setVisibility(View.GONE);
                break;
            }

            case STATE_OUTGOING: {
                mVideoViewFullScreen.setVisibility(View.VISIBLE);
                mViewConnectingVideoViewFullScreenFg.setVisibility(View.VISIBLE);
                mRelativeLayoutVideoViewSmall.setVisibility(View.GONE);
                mImageViewCameraSwitch.setVisibility(View.VISIBLE);
                mImageViewVideoOff.setVisibility(View.VISIBLE);
                break;
            }

            case STATE_CONNECTED: {
                mVideoViewFullScreen.setVisibility(View.VISIBLE);
                mViewConnectingVideoViewFullScreenFg.setVisibility(View.GONE);
                mRelativeLayoutVideoViewSmall.setVisibility(View.VISIBLE);
                mImageViewCameraSwitch.setVisibility(View.VISIBLE);
                mImageViewVideoOff.setVisibility(View.VISIBLE);
                mLinearLayoutInfo.setVisibility(View.GONE);

                if (call != null && call.getMyRole() == DirectCallUserRole.CALLER) {
                    call.setLocalVideoView(mVideoViewSmall);
                    call.setRemoteVideoView(mVideoViewFullScreen);
                }
                break;
            }

            case STATE_ENDING:
            case STATE_ENDED: {
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mVideoViewFullScreen.setVisibility(View.GONE);
                mViewConnectingVideoViewFullScreenFg.setVisibility(View.GONE);
                mRelativeLayoutVideoViewSmall.setVisibility(View.GONE);
                mImageViewCameraSwitch.setVisibility(View.GONE);
            }
            break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mDirectCall != null && mDoLocalVideoStart) {
            mDoLocalVideoStart = false;
            updateCallService();
            mDirectCall.startVideo();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDirectCall != null && mDirectCall.isLocalVideoEnabled()) {
            mDirectCall.stopVideo();
            mDoLocalVideoStart = true;
            updateCallService();
        }
    }
}
