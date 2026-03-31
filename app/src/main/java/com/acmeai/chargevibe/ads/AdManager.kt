package com.acmeai.chargevibe.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // Test ad unit IDs — replace with real ones before release
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
    private const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null

    private var isInitialized = false

    // Smart interstitial timing
    private var appStartTimeMs = 0L
    private var lastInterstitialTimeMs = 0L
    private var eligibleCallCount = 0
    private const val GRACE_PERIOD_MS = 5 * 60 * 1000L
    private const val MIN_INTERSTITIAL_GAP_MS = 2 * 60 * 1000L
    private const val INTERSTITIAL_FREQUENCY = 3

    // App Open timing
    private var lastAppOpenTimeMs = 0L
    private const val APP_OPEN_COOLDOWN_MS = 30 * 1000L

    fun initialize(context: Context) {
        if (isInitialized) return
        appStartTimeMs = System.currentTimeMillis()
        MobileAds.initialize(context) {
            Log.d(TAG, "AdMob SDK initialized")
        }
        isInitialized = true
    }

    // --- App Open Ad ---

    fun loadAppOpenAd(context: Context) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(context, APP_OPEN_AD_UNIT_ID, request, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                Log.d(TAG, "App open ad loaded")
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "App open ad failed: ${error.message}")
                appOpenAd = null
            }
        })
    }

    fun showAppOpenAd(activity: Activity): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastAppOpenTimeMs < APP_OPEN_COOLDOWN_MS) return false
        val ad = appOpenAd ?: return false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                loadAppOpenAd(activity)
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                loadAppOpenAd(activity)
            }
        }
        ad.show(activity)
        lastAppOpenTimeMs = now
        return true
    }

    // --- Interstitial ---

    fun loadInterstitial(activity: Activity) {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(activity, INTERSTITIAL_AD_UNIT_ID, request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            loadInterstitial(activity)
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            interstitialAd = null
                            loadInterstitial(activity)
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    fun showInterstitialIfEligible(activity: Activity): Boolean {
        val now = System.currentTimeMillis()
        if (now - appStartTimeMs < GRACE_PERIOD_MS) return false
        if (lastInterstitialTimeMs > 0 && now - lastInterstitialTimeMs < MIN_INTERSTITIAL_GAP_MS) return false
        eligibleCallCount++
        if (eligibleCallCount % INTERSTITIAL_FREQUENCY != 0) return false
        val ad = interstitialAd ?: return false
        ad.show(activity)
        lastInterstitialTimeMs = now
        return true
    }

    // --- Rewarded Video ---

    fun loadRewarded(activity: Activity) {
        val request = AdRequest.Builder().build()
        RewardedAd.load(activity, REWARDED_AD_UNIT_ID, request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            loadRewarded(activity)
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            rewardedAd = null
                            loadRewarded(activity)
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            })
    }

    fun showRewarded(activity: Activity, onRewardEarned: () -> Unit): Boolean {
        val ad = rewardedAd ?: return false
        ad.show(activity) { rewardItem ->
            Log.d(TAG, "Reward earned: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned()
        }
        return true
    }

    fun isRewardedReady(): Boolean = rewardedAd != null

    // --- Rewarded Interstitial ---

    fun loadRewardedInterstitial(activity: Activity) {
        val request = AdRequest.Builder().build()
        RewardedInterstitialAd.load(activity, REWARDED_INTERSTITIAL_AD_UNIT_ID, request,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedInterstitialAd = null
                            loadRewardedInterstitial(activity)
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            rewardedInterstitialAd = null
                            loadRewardedInterstitial(activity)
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedInterstitialAd = null
                }
            })
    }

    fun showRewardedInterstitial(activity: Activity, onRewardEarned: () -> Unit): Boolean {
        val ad = rewardedInterstitialAd ?: return false
        ad.show(activity) { rewardItem ->
            Log.d(TAG, "Rewarded interstitial earned: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned()
        }
        return true
    }

    // --- Native Ad ---

    fun loadNativeAd(context: Context, onAdLoaded: (NativeAd) -> Unit) {
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
            .forNativeAd { ad -> onAdLoaded(ad) }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Native ad failed: ${error.message}")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun destroy() {
        interstitialAd = null
        rewardedAd = null
        rewardedInterstitialAd = null
        appOpenAd = null
        isInitialized = false
    }
}
