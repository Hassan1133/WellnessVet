package com.wellness.vet.app.calls.activites;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.sendbird.calls.AcceptParams;
import com.sendbird.calls.AudioDevice;
import com.sendbird.calls.CallOptions;
import com.sendbird.calls.DialParams;
import com.sendbird.calls.DirectCall;
import com.sendbird.calls.SendBirdCall;
import com.wellness.vet.app.R;
import com.wellness.vet.app.calls.BaseApplication;
import com.wellness.vet.app.calls.utils.PrefUtils;
import com.wellness.vet.app.calls.utils.TimeUtils;
import com.wellness.vet.app.calls.utils.ToastUtils;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class VoiceCallActivity extends CallActivity {

    private Timer mCallDurationTimer;


    private ImageView mImageViewSpeakerphone;

    boolean convertToVideo = false;


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_voice_call;
    }

    @Override
    protected void initViews() {
        super.initViews();
        mImageViewSpeakerphone = findViewById(R.id.image_view_speakerphone);
    }

    @Override
    protected void setViews() {
        super.setViews();

        mImageViewSpeakerphone.setOnClickListener(view -> {
            if (mDirectCall != null) {
                mImageViewSpeakerphone.setSelected(!mImageViewSpeakerphone.isSelected());
                if (mImageViewSpeakerphone.isSelected()) {
                    mDirectCall.selectAudioDevice(AudioDevice.SPEAKERPHONE, e -> {
                        if (e != null) {
                            mImageViewSpeakerphone.setSelected(false);
                        }
                    });
                } else {
                    mDirectCall.selectAudioDevice(AudioDevice.WIRED_HEADSET, e -> {
                        if (e != null) {
                            mDirectCall.selectAudioDevice(AudioDevice.EARPIECE, null);
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void setAudioDevice(AudioDevice currentAudioDevice, Set<AudioDevice> availableAudioDevices) {
        if (currentAudioDevice == AudioDevice.SPEAKERPHONE) {
            mImageViewSpeakerphone.setSelected(true);
        } else if (currentAudioDevice == AudioDevice.BLUETOOTH) {
            mImageViewSpeakerphone.setSelected(false);
        } else {
            mImageViewSpeakerphone.setSelected(false);
        }

        if (availableAudioDevices.contains(AudioDevice.SPEAKERPHONE)) {
            mImageViewSpeakerphone.setEnabled(true);
        } else if (!mImageViewSpeakerphone.isSelected()) {
            mImageViewSpeakerphone.setEnabled(false);
        }
    }

    @Override
    protected void startCall(boolean amICallee) {
        CallOptions callOptions = new CallOptions();
        callOptions.setAudioEnabled(mIsAudioEnabled);

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

        switch (mState) {
            case STATE_ACCEPTING:
                cancelCallDurationTimer();
                break;

            case STATE_CONNECTED: {
                setInfo(call, "");
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                setCallDurationTimer(call);
                break;
            }

            case STATE_ENDING:
            case STATE_ENDED: {
                cancelCallDurationTimer();
                break;
            }
        }
        return true;
    }

    private void setCallDurationTimer(final DirectCall call) {
        if (mCallDurationTimer == null) {
            mCallDurationTimer = new Timer();
            mCallDurationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        String callDuration = TimeUtils.getTimeString(call.getDuration());
                        mTextViewStatus.setText(callDuration);
                    });
                }
            }, 0, 1000);
        }
    }

    private void cancelCallDurationTimer() {
        if (mCallDurationTimer != null) {
            mCallDurationTimer.cancel();
            mCallDurationTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCallDurationTimer();
        if(convertToVideo) {
            overridePendingTransition(0,0);
            CallService.dial(VoiceCallActivity.this, mCalleeIdToDial, true);
            overridePendingTransition(0,0);
            PrefUtils.setCalleeId(VoiceCallActivity.this, mCalleeIdToDial);
        }
    }

    @Override
    public void convertVideoCall() {
        convertToVideo = true;
    }
}
