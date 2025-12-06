package cd.software.flowchat.admob

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.rewarded.RewardItem
import kotlinx.coroutines.flow.StateFlow

interface AdManager {
    fun initialize(context: Context)
    fun showInterstitial(activity: Activity, onAdClosed: () -> Unit = {})
    fun showRewardedAd(
        activity: Activity,
        onRewarded: (RewardItem) -> Unit,
        onAdClosed: () -> Unit = {}
    )
    fun trackInteractionForAds()
    val isRewardedAdReady: StateFlow<Boolean>
    val isInterstitialAdReady: StateFlow<Boolean>
}