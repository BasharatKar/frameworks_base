/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.os;

import static android.app.ActivityManager.PROCESS_STATE_BOUND_FOREGROUND_SERVICE;
import static android.app.ActivityManager.PROCESS_STATE_BOUND_TOP;
import static android.app.ActivityManager.PROCESS_STATE_FOREGROUND_SERVICE;
import static android.app.ActivityManager.PROCESS_STATE_PERSISTENT;
import static android.app.ActivityManager.PROCESS_STATE_PERSISTENT_UI;
import static android.app.ActivityManager.PROCESS_STATE_TOP;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.annotation.SystemService;
import android.content.Context;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

/**
 * Interface to access and modify the permanent and temporary power save whitelist. The two lists
 * are kept separately. Apps placed on the permanent whitelist are only removed via an explicit
 * removeFromWhitelist call. Apps whitelisted by default by the system cannot be removed. Apps
 * placed on the temporary whitelist are removed from that whitelist after a predetermined amount of
 * time.
 *
 * @hide
 */
@SystemApi
@SystemService(Context.POWER_WHITELIST_MANAGER)
public class PowerWhitelistManager {
    private final Context mContext;
    // Proxy to DeviceIdleController for now
    // TODO: migrate to PowerWhitelistController
    private final IDeviceIdleController mService;

    /**
     * Indicates that an unforeseen event has occurred and the app should be whitelisted to handle
     * it.
     */
    public static final int EVENT_UNSPECIFIED = 0;

    /**
     * Indicates that an SMS event has occurred and the app should be whitelisted to handle it.
     */
    public static final int EVENT_SMS = 1;

    /**
     * Indicates that an MMS event has occurred and the app should be whitelisted to handle it.
     */
    public static final int EVENT_MMS = 2;

    /**
     * @hide
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = {"EVENT_"}, value = {
            EVENT_UNSPECIFIED,
            EVENT_SMS,
            EVENT_MMS,
    })
    public @interface WhitelistEvent {
    }

    /**
     * Allow the temp allowlist behavior, plus allow foreground service start from background.
     */
    public static final int TEMPORARY_ALLOWLIST_TYPE_FOREGROUND_SERVICE_ALLOWED = 0;
    /**
     * Only allow the temp allowlist behavior, not allow foreground service start from
     * background.
     */
    public static final int TEMPORARY_ALLOWLIST_TYPE_FOREGROUND_SERVICE_NOT_ALLOWED = 1;

    /**
     * The list of temp allowlist types.
     * @hide
     */
    @IntDef(flag = true, prefix = { "TEMPORARY_ALLOWLIST_TYPE_" }, value = {
            TEMPORARY_ALLOWLIST_TYPE_FOREGROUND_SERVICE_ALLOWED,
            TEMPORARY_ALLOWLIST_TYPE_FOREGROUND_SERVICE_NOT_ALLOWED,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface TempAllowListType {}

    /* Reason code for BG-FGS-launch. */
    /**
     * BG-FGS-launch is denied.
     * @hide
     */
    public static final int REASON_DENIED = -1;
    /**
     * The default reason code if reason is unknown.
     */
    public static final int REASON_UNKNOWN = 0;
    /**
     * Use REASON_OTHER if there is no better choice.
     */
    public static final int REASON_OTHER = 1;
    /** @hide */
    public static final int REASON_PROC_STATE_PERSISTENT = 10;
    /** @hide */
    public static final int REASON_PROC_STATE_PERSISTENT_UI = 11;
    /** @hide */
    public static final int REASON_PROC_STATE_TOP = 12;
    /** @hide */
    public static final int REASON_PROC_STATE_BTOP = 13;
    /** @hide */
    public static final int REASON_PROC_STATE_FGS = 14;
    /** @hide */
    public static final int REASON_PROC_STATE_BFGS = 15;
    /** @hide */
    public static final int REASON_UID_VISIBLE = 50;
    /** @hide */
    public static final int REASON_SYSTEM_UID = 51;
    /** @hide */
    public static final int REASON_ACTIVITY_STARTER = 52;
    /** @hide */
    public static final int REASON_START_ACTIVITY_FLAG = 53;
    /** @hide */
    public static final int REASON_FGS_BINDING = 54;
    /** @hide */
    public static final int REASON_DEVICE_OWNER = 55;
    /** @hide */
    public static final int REASON_PROFILE_OWNER = 56;
    /** @hide */
    public static final int REASON_COMPANION_DEVICE_MANAGER = 57;
    /**
     * START_ACTIVITIES_FROM_BACKGROUND permission.
     * @hide
     */
    public static final int REASON_BACKGROUND_ACTIVITY_PERMISSION = 58;
    /**
     * START_FOREGROUND_SERVICES_FROM_BACKGROUND permission.
     * @hide
     */
    public static final int REASON_BACKGROUND_FGS_PERMISSION = 59;
    /** @hide */
    public static final int REASON_INSTR_BACKGROUND_ACTIVITY_PERMISSION = 60;
    /** @hide */
    public static final int REASON_INSTR_BACKGROUND_FGS_PERMISSION = 61;
    /** @hide */
    public static final int REASON_SYSTEM_ALERT_WINDOW_PERMISSION = 62;
    /** @hide */
    public static final int REASON_DEVICE_DEMO_MODE = 63;
    /** @hide */
    public static final int REASON_EXEMPTED_PACKAGE = 64;
    /** @hide */
    public static final int REASON_ALLOWLISTED_PACKAGE  = 65;
    /**
     * If it's because of a role,
     * @hide
     */
    public static final int REASON_APPOP = 66;

    /* BG-FGS-launch is allowed by temp-allowlist or system-allowlist.
    Reason code for temp and system allowlist starts here.
    */
    public static final int REASON_GEOFENCING = 100;
    public static final int REASON_PUSH_MESSAGING = 101;
    public static final int REASON_ACTIVITY_RECOGNITION = 102;

    /**
     * Broadcast ACTION_BOOT_COMPLETED.
     * @hide
     */
    public static final int REASON_BOOT_COMPLETED = 103;
    /**
     * Broadcast ACTION_PRE_BOOT_COMPLETED.
     * @hide
     */
    public static final int REASON_PRE_BOOT_COMPLETED = 104;

    /**
     * Broadcast ACTION_LOCKED_BOOT_COMPLETED.
     * @hide
     */
    public static final int REASON_LOCKED_BOOT_COMPLETED = 105;
    /**
     * Device idle system allowlist, including EXCEPT-IDLE
     * @hide
     */
    public static final int REASON_SYSTEM_ALLOW_LISTED  = 106;
    /** @hide */
    public static final int REASON_ALARM_MANAGER_ALARM_CLOCK = 107;
    /**
     * AlarmManagerService.
     * @hide
     */
    public static final int REASON_ALARM_MANAGER_WHILE_IDLE = 108;
    /**
     * ActiveServices.
     * @hide
     */
    public static final int REASON_SERVICE_LAUNCH = 109;
    /**
     * KeyChainSystemService.
     * @hide
     */
    public static final int REASON_KEY_CHAIN = 110;
    /**
     * PackageManagerService.
     * @hide
     */
    public static final int REASON_PACKAGE_VERIFIER = 111;
    /**
     * SyncManager.
     * @hide
     */
    public static final int REASON_SYNC_MANAGER = 112;
    /**
     * DomainVerificationProxyV1.
     * @hide
     */
    public static final int REASON_DOMAIN_VERIFICATION_V1 = 113;
    /**
     * DomainVerificationProxyV2.
     * @hide
     */
    public static final int REASON_DOMAIN_VERIFICATION_V2 = 114;
    /** @hide */
    public static final int REASON_VPN = 115;
    /**
     * NotificationManagerService.
     * @hide
     */
    public static final int REASON_NOTIFICATION_SERVICE = 116;
    /**
     * Broadcast ACTION_MY_PACKAGE_REPLACED.
     * @hide
     */
    public static final int REASON_PACKAGE_REPLACED = 117;
    /**
     * LocationProviderManager.
     * @hide
     */
    public static final int REASON_LOCATION_PROVIDER = 118;
    /**
     * MediaButtonReceiver.
     * @hide
     */
    public static final int REASON_MEDIA_BUTTON = 119;
    /**
     * InboundSmsHandler.
     * @hide
     */
    public static final int REASON_EVENT_SMS = 120;
    /**
     * InboundSmsHandler.
     * @hide
     */
    public static final int REASON_EVENT_MMS = 121;
    /**
     * Shell app.
     * @hide
     */
    public static final int REASON_SHELL = 122;

    /**
     * The list of BG-FGS-Launch and temp-allowlist reason code.
     * @hide
     */
    @IntDef(flag = true, prefix = { "REASON_" }, value = {
            // BG-FGS-Launch reasons.
            REASON_DENIED,
            REASON_UNKNOWN,
            REASON_OTHER,
            REASON_PROC_STATE_PERSISTENT,
            REASON_PROC_STATE_PERSISTENT_UI,
            REASON_PROC_STATE_TOP,
            REASON_PROC_STATE_BTOP,
            REASON_PROC_STATE_FGS,
            REASON_PROC_STATE_BFGS,
            REASON_UID_VISIBLE,
            REASON_SYSTEM_UID,
            REASON_ACTIVITY_STARTER,
            REASON_START_ACTIVITY_FLAG,
            REASON_FGS_BINDING,
            REASON_DEVICE_OWNER,
            REASON_PROFILE_OWNER,
            REASON_COMPANION_DEVICE_MANAGER,
            REASON_BACKGROUND_ACTIVITY_PERMISSION,
            REASON_BACKGROUND_FGS_PERMISSION,
            REASON_INSTR_BACKGROUND_ACTIVITY_PERMISSION,
            REASON_INSTR_BACKGROUND_FGS_PERMISSION,
            REASON_SYSTEM_ALERT_WINDOW_PERMISSION,
            REASON_DEVICE_DEMO_MODE,
            REASON_EXEMPTED_PACKAGE,
            REASON_ALLOWLISTED_PACKAGE,
            REASON_APPOP,
            // temp and system allowlist reasons.
            REASON_GEOFENCING,
            REASON_PUSH_MESSAGING,
            REASON_ACTIVITY_RECOGNITION,
            REASON_BOOT_COMPLETED,
            REASON_PRE_BOOT_COMPLETED,
            REASON_LOCKED_BOOT_COMPLETED,
            REASON_SYSTEM_ALLOW_LISTED,
            REASON_ALARM_MANAGER_ALARM_CLOCK,
            REASON_ALARM_MANAGER_WHILE_IDLE,
            REASON_SERVICE_LAUNCH,
            REASON_KEY_CHAIN,
            REASON_PACKAGE_VERIFIER,
            REASON_SYNC_MANAGER,
            REASON_DOMAIN_VERIFICATION_V1,
            REASON_DOMAIN_VERIFICATION_V2,
            REASON_VPN,
            REASON_NOTIFICATION_SERVICE,
            REASON_PACKAGE_REPLACED,
            REASON_LOCATION_PROVIDER,
            REASON_MEDIA_BUTTON,
            REASON_EVENT_SMS,
            REASON_EVENT_MMS,
            REASON_SHELL,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ReasonCode {}

    /**
     * @hide
     */
    public PowerWhitelistManager(@NonNull Context context) {
        mContext = context;
        mService = context.getSystemService(DeviceIdleManager.class).getService();
    }

    /**
     * Add the specified package to the permanent power save whitelist.
     */
    @RequiresPermission(android.Manifest.permission.DEVICE_POWER)
    public void addToWhitelist(@NonNull String packageName) {
        addToWhitelist(Collections.singletonList(packageName));
    }

    /**
     * Add the specified packages to the permanent power save whitelist.
     */
    @RequiresPermission(android.Manifest.permission.DEVICE_POWER)
    public void addToWhitelist(@NonNull List<String> packageNames) {
        try {
            mService.addPowerSaveWhitelistApps(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Get a list of app IDs of app that are whitelisted. This does not include temporarily
     * whitelisted apps.
     *
     * @param includingIdle Set to true if the app should be whitelisted from device idle as well
     *                      as other power save restrictions
     * @hide
     */
    @NonNull
    public int[] getWhitelistedAppIds(boolean includingIdle) {
        try {
            if (includingIdle) {
                return mService.getAppIdWhitelist();
            } else {
                return mService.getAppIdWhitelistExceptIdle();
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Returns true if the app is whitelisted from power save restrictions. This does not include
     * temporarily whitelisted apps.
     *
     * @param includingIdle Set to true if the app should be whitelisted from device
     *                      idle as well as other power save restrictions
     * @hide
     */
    public boolean isWhitelisted(@NonNull String packageName, boolean includingIdle) {
        try {
            if (includingIdle) {
                return mService.isPowerSaveWhitelistApp(packageName);
            } else {
                return mService.isPowerSaveWhitelistExceptIdleApp(packageName);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Remove an app from the permanent power save whitelist. Only apps that were added via
     * {@link #addToWhitelist(String)} or {@link #addToWhitelist(List)} will be removed. Apps
     * whitelisted by default by the system cannot be removed.
     *
     * @param packageName The app to remove from the whitelist
     */
    @RequiresPermission(android.Manifest.permission.DEVICE_POWER)
    public void removeFromWhitelist(@NonNull String packageName) {
        try {
            mService.removePowerSaveWhitelistApp(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Add an app to the temporary whitelist for a short amount of time.
     *
     * @param packageName The package to add to the temp whitelist
     * @param durationMs  How long to keep the app on the temp whitelist for (in milliseconds)
     * @param reasonCode one of {@link ReasonCode}, use {@link #REASON_UNKNOWN} if not sure.
     * @param reason a optional human readable reason string, could be null or empty string.
     */
    @RequiresPermission(android.Manifest.permission.CHANGE_DEVICE_IDLE_TEMP_WHITELIST)
    public void whitelistAppTemporarily(@NonNull String packageName, long durationMs,
            @ReasonCode int reasonCode, @Nullable String reason) {
        try {
            mService.addPowerSaveTempWhitelistApp(packageName, durationMs, mContext.getUserId(),
                    reasonCode, reason);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Add an app to the temporary whitelist for a short amount of time.
     *
     * @param packageName The package to add to the temp whitelist
     * @param durationMs  How long to keep the app on the temp whitelist for (in milliseconds)
     * @deprecated Use {@link #whitelistAppTemporarily(String, long, int, String)} instead
     */
    @Deprecated
    @RequiresPermission(android.Manifest.permission.CHANGE_DEVICE_IDLE_TEMP_WHITELIST)
    public void whitelistAppTemporarily(@NonNull String packageName, long durationMs) {
        whitelistAppTemporarily(packageName, durationMs, REASON_UNKNOWN, packageName);
    }

    /**
     * Add an app to the temporary whitelist for a short amount of time for a specific reason. The
     * temporary whitelist is kept separately from the permanent whitelist and apps are
     * automatically removed from the temporary whitelist after a predetermined amount of time.
     *
     * @param packageName The package to add to the temp whitelist
     * @param event       The reason to add the app to the temp whitelist
     * @param reason      A human-readable reason explaining why the app is temp whitelisted. Only
     *                    used for logging purposes. Could be null or empty string.
     * @return The duration (in milliseconds) that the app is whitelisted for
     * @deprecated Use {@link #whitelistAppTemporarilyForEvent(String, int, int, String)} instead
     */
    @Deprecated
    @RequiresPermission(android.Manifest.permission.CHANGE_DEVICE_IDLE_TEMP_WHITELIST)
    public long whitelistAppTemporarilyForEvent(@NonNull String packageName,
            @WhitelistEvent int event, @Nullable String reason) {
        return whitelistAppTemporarilyForEvent(packageName, event, REASON_UNKNOWN, reason);
    }

    /**
     * Add an app to the temporary whitelist for a short amount of time for a specific reason. The
     * temporary whitelist is kept separately from the permanent whitelist and apps are
     * automatically removed from the temporary whitelist after a predetermined amount of time.
     *
     * @param packageName The package to add to the temp whitelist
     * @param event       The reason to add the app to the temp whitelist
     * @param reasonCode  one of {@link ReasonCode}, use {@link #REASON_UNKNOWN} if not sure.
     * @param reason      A human-readable reason explaining why the app is temp whitelisted. Only
     *                    used for logging purposes. Could be null or empty string.
     * @return The duration (in milliseconds) that the app is whitelisted for
     */
    @RequiresPermission(android.Manifest.permission.CHANGE_DEVICE_IDLE_TEMP_WHITELIST)
    public long whitelistAppTemporarilyForEvent(@NonNull String packageName,
            @WhitelistEvent int event, @ReasonCode int reasonCode, @Nullable String reason) {
        try {
            switch (event) {
                case EVENT_MMS:
                    return mService.addPowerSaveTempWhitelistAppForMms(
                            packageName, mContext.getUserId(), reasonCode, reason);
                case EVENT_SMS:
                    return mService.addPowerSaveTempWhitelistAppForSms(
                            packageName, mContext.getUserId(), reasonCode, reason);
                case EVENT_UNSPECIFIED:
                default:
                    return mService.whitelistAppTemporarily(
                            packageName, mContext.getUserId(), reasonCode, reason);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * @hide
     */
    public static @ReasonCode int getReasonCodeFromProcState(int procState) {
        if (procState <= PROCESS_STATE_PERSISTENT) {
            return REASON_PROC_STATE_PERSISTENT;
        } else if (procState <= PROCESS_STATE_PERSISTENT_UI) {
            return REASON_PROC_STATE_PERSISTENT_UI;
        } else if (procState <= PROCESS_STATE_TOP) {
            return REASON_PROC_STATE_TOP;
        } else if (procState <= PROCESS_STATE_BOUND_TOP) {
            return REASON_PROC_STATE_BTOP;
        } else if (procState <= PROCESS_STATE_FOREGROUND_SERVICE) {
            return REASON_PROC_STATE_FGS;
        } else if (procState <= PROCESS_STATE_BOUND_FOREGROUND_SERVICE) {
            return REASON_PROC_STATE_BFGS;
        } else {
            return REASON_DENIED;
        }
    }

    /**
     * Return string name of the integer reason code.
     * @hide
     * @param reasonCode
     * @return string name of the reason code.
     */
    public static String reasonCodeToString(@ReasonCode int reasonCode) {
        switch (reasonCode) {
            case REASON_DENIED:
                return "DENIED";
            case REASON_UNKNOWN:
                return "UNKNOWN";
            case REASON_OTHER:
                return "OTHER";
            case REASON_PROC_STATE_PERSISTENT:
                return "PROC_STATE_PERSISTENT";
            case REASON_PROC_STATE_PERSISTENT_UI:
                return "PROC_STATE_PERSISTENT_UI";
            case REASON_PROC_STATE_TOP:
                return "PROC_STATE_TOP";
            case REASON_PROC_STATE_BTOP:
                return "PROC_STATE_BTOP";
            case REASON_PROC_STATE_FGS:
                return "PROC_STATE_FGS";
            case REASON_PROC_STATE_BFGS:
                return "PROC_STATE_BFGS";
            case REASON_UID_VISIBLE:
                return "UID_VISIBLE";
            case REASON_SYSTEM_UID:
                return "SYSTEM_UID";
            case REASON_ACTIVITY_STARTER:
                return "ACTIVITY_STARTER";
            case REASON_START_ACTIVITY_FLAG:
                return "START_ACTIVITY_FLAG";
            case REASON_FGS_BINDING:
                return "FGS_BINDING";
            case REASON_DEVICE_OWNER:
                return "DEVICE_OWNER";
            case REASON_PROFILE_OWNER:
                return "PROFILE_OWNER";
            case REASON_COMPANION_DEVICE_MANAGER:
                return "COMPANION_DEVICE_MANAGER";
            case REASON_BACKGROUND_ACTIVITY_PERMISSION:
                return "BACKGROUND_ACTIVITY_PERMISSION";
            case REASON_BACKGROUND_FGS_PERMISSION:
                return "BACKGROUND_FGS_PERMISSION";
            case REASON_INSTR_BACKGROUND_ACTIVITY_PERMISSION:
                return "INSTR_BACKGROUND_ACTIVITY_PERMISSION";
            case REASON_INSTR_BACKGROUND_FGS_PERMISSION:
                return "INSTR_BACKGROUND_FGS_PERMISSION";
            case REASON_SYSTEM_ALERT_WINDOW_PERMISSION:
                return "SYSTEM_ALERT_WINDOW_PERMISSION";
            case REASON_DEVICE_DEMO_MODE:
                return "DEVICE_DEMO_MODE";
            case REASON_EXEMPTED_PACKAGE:
                return "EXEMPTED_PACKAGE";
            case REASON_ALLOWLISTED_PACKAGE:
                return "ALLOWLISTED_PACKAGE";
            case REASON_APPOP:
                return "APPOP";
            case REASON_GEOFENCING:
                return "GEOFENCING";
            case REASON_PUSH_MESSAGING:
                return "PUSH_MESSAGING";
            case REASON_ACTIVITY_RECOGNITION:
                return "ACTIVITY_RECOGNITION";
            case REASON_BOOT_COMPLETED:
                return "BOOT_COMPLETED";
            case REASON_PRE_BOOT_COMPLETED:
                return "PRE_BOOT_COMPLETED";
            case REASON_LOCKED_BOOT_COMPLETED:
                return "LOCKED_BOOT_COMPLETED";
            case REASON_SYSTEM_ALLOW_LISTED:
                return "SYSTEM_ALLOW_LISTED";
            case REASON_ALARM_MANAGER_ALARM_CLOCK:
                return "ALARM_MANAGER_ALARM_CLOCK";
            case REASON_ALARM_MANAGER_WHILE_IDLE:
                return "ALARM_MANAGER_WHILE_IDLE";
            case REASON_SERVICE_LAUNCH:
                return "SERVICE_LAUNCH";
            case REASON_KEY_CHAIN:
                return "KEY_CHAIN";
            case REASON_PACKAGE_VERIFIER:
                return "PACKAGE_VERIFIER";
            case REASON_SYNC_MANAGER:
                return "SYNC_MANAGER";
            case REASON_DOMAIN_VERIFICATION_V1:
                return "DOMAIN_VERIFICATION_V1";
            case REASON_DOMAIN_VERIFICATION_V2:
                return "DOMAIN_VERIFICATION_V2";
            case REASON_VPN:
                return "VPN";
            case REASON_NOTIFICATION_SERVICE:
                return "NOTIFICATION_SERVICE";
            case REASON_PACKAGE_REPLACED:
                return "PACKAGE_REPLACED";
            case REASON_LOCATION_PROVIDER:
                return "LOCATION_PROVIDER";
            case REASON_MEDIA_BUTTON:
                return "MEDIA_BUTTON";
            case REASON_EVENT_SMS:
                return "EVENT_SMS";
            case REASON_EVENT_MMS:
                return "EVENT_MMS";
            case REASON_SHELL:
                return "SHELL";
            default:
                return  "(unknown:" + reasonCode + ")";
        }
    }
}
