package com.wellness.vet.app.calls

import android.text.TextUtils
import androidx.multidex.MultiDexApplication
import com.sendbird.calls.DirectCall
import com.sendbird.calls.RoomInvitation
import com.sendbird.calls.SendBirdCall.Options.addDirectCallSound
import com.sendbird.calls.SendBirdCall.SoundType
import com.sendbird.calls.SendBirdCall.addListener
import com.sendbird.calls.SendBirdCall.init
import com.sendbird.calls.SendBirdCall.ongoingCallCount
import com.sendbird.calls.SendBirdCall.removeAllListeners
import com.sendbird.calls.handler.DirectCallListener
import com.sendbird.calls.handler.SendBirdCallListener
import com.wellness.vet.app.R
import com.wellness.vet.app.calls.activites.CallService
import com.wellness.vet.app.calls.utils.BroadcastUtils
import com.wellness.vet.app.calls.utils.PrefUtils
import java.util.UUID

class BaseApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        initSendBirdCall(PrefUtils.getAppId(applicationContext))
    }

    fun initSendBirdCall(appId: String?): Boolean {
        var appId = appId
        val context = applicationContext
        if (TextUtils.isEmpty(appId)) {
            appId = APP_ID
        }
        if (init(context, appId!!)) {
            removeAllListeners()
            addListener(UUID.randomUUID().toString(), object : SendBirdCallListener() {
                override fun onInvitationReceived(roomInvitation: RoomInvitation) {}
                override fun onRinging(call: DirectCall) {
                    val ongoingCallCount = ongoingCallCount
                    if (ongoingCallCount >= 2) {
                        call.end()
                        return
                    }
                    call.setListener(object : DirectCallListener() {
                        override fun onConnected(call: DirectCall) {}
                        override fun onEnded(call: DirectCall) {
                            val ongoingCallCount = ongoingCallCount
                            BroadcastUtils.sendCallLogBroadcast(context, call.callLog)
                            if (ongoingCallCount == 0) {
                                CallService.stopService(context)
                            }
                        }
                    })
                    CallService.onRinging(context, call)
                }
            })
            addDirectCallSound(SoundType.DIALING, R.raw.dialing)
            addDirectCallSound(SoundType.RINGING, R.raw.ringing)
            addDirectCallSound(SoundType.RECONNECTING, R.raw.reconnecting)
            addDirectCallSound(SoundType.RECONNECTED, R.raw.reconnected)
            return true
        }
        return false
    }

    companion object {
        const val TAG = "SendBirdCalls"
        const val APP_ID = "2B0621D3-6764-4B3D-838F-518E97C87BE1"
    }
}
