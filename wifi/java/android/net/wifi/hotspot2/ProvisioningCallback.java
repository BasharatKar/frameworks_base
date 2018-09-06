/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.net.wifi.hotspot2;

import android.net.wifi.WifiManager;
import android.os.Handler;

/**
 * Base class for provisioning callbacks. Should be extended by applications and set when calling
 * {@link WifiManager#startSubscriptionProvisioning(OsuProvider, ProvisioningCallback, Handler)}.
 *
 * @hide
 */
public abstract class ProvisioningCallback {

    /**
     * The reason code for Provisioning Failure due to connection failure to OSU AP.
     */
    public static final int OSU_FAILURE_AP_CONNECTION = 1;

    /**
     * The reason code for invalid server URL address.
     */
    public static final int OSU_FAILURE_SERVER_URL_INVALID = 2;

    /**
     * The reason code for provisioning failure due to connection failure to the server.
     */
    public static final int OSU_FAILURE_SERVER_CONNECTION = 3;

    /**
     * The reason code for provisioning failure due to invalid server certificate.
     */
    public static final int OSU_FAILURE_SERVER_VALIDATION = 4;

    /**
     * The reason code for provisioning failure due to invalid service provider.
     */
    public static final int OSU_FAILURE_SERVICE_PROVIDER_VERIFICATION = 5;

    /**
     * The reason code for provisioning failure when a provisioning flow is aborted.
     */
    public static final int OSU_FAILURE_PROVISIONING_ABORTED = 6;

    /**
     * The reason code for provisioning failure when a provisioning flow is not possible.
     */
    public static final int OSU_FAILURE_PROVISIONING_NOT_AVAILABLE = 7;

    /**
     * The reason code for provisioning failure due to invalid server url.
     */
    public static final int OSU_FAILURE_INVALID_SERVER_URL = 8;

    /**
     * The reason code for provisioning failure when a command received is not the expected command
     * type.
     */
    public static final int OSU_FAILURE_UNEXPECTED_COMMAND_TYPE = 9;

    /**
     * The reason code for provisioning failure when a SOAP message is not the expected message
     * type.
     */
    public static final int OSU_FAILURE_UNEXPECTED_SOAP_MESSAGE_TYPE = 10;

    /**
     * The reason code for provisioning failure when a SOAP message exchange fails.
     */
    public static final int OSU_FAILURE_SOAP_MESSAGE_EXCHANGE = 11;

    /**
     * The reason code for provisioning failure when a redirect listener fails to start.
     */
    public static final int OSU_FAILURE_START_REDIRECT_LISTENER = 12;

    /**
     * The reason code for provisioning failure when a redirect listener timed out to receive a HTTP
     * redirect response.
     */
    public static final int OSU_FAILURE_TIMED_OUT_REDIRECT_LISTENER = 13;

    /**
     * The reason code for provisioning failure when there is no OSU activity to listen to
     * {@link WifiManager#ACTION_PASSPOINT_LAUNCH_OSU_VIEW} intent.
     */
    public static final int OSU_FAILURE_NO_OSU_ACTIVITY_FOUND = 14;

    /**
     * The status code for provisioning flow to indicate connecting to OSU AP
     */
    public static final int OSU_STATUS_AP_CONNECTING = 1;

    /**
     * The status code for provisioning flow to indicate the OSU AP is connected.
     */
    public static final int OSU_STATUS_AP_CONNECTED = 2;

    /**
     * The status code for provisioning flow to indicate the server connection is completed.
     */
    public static final int OSU_STATUS_SERVER_CONNECTED = 3;

    /**
     * The status code for provisioning flow to indicate the server certificate is validated.
     */
    public static final int OSU_STATUS_SERVER_VALIDATED = 4;

    /**
     * The status code for provisioning flow to indicate the service provider is verified.
     */
    public static final int OSU_STATUS_SERVICE_PROVIDER_VERIFIED = 5;

    /**
     * The status code for provisioning flow to indicate starting the SOAP exchange.
     */
    public static final int OSU_STATUS_INIT_SOAP_EXCHANGE = 6;

    /**
     * The status code for provisioning flow to indicate waiting for a HTTP redirect response.
     */
    public static final int OSU_STATUS_WAITING_FOR_REDIRECT_RESPONSE = 7;

    /**
     * The status code for provisioning flow to indicate a HTTP redirect response is received.
     */
    public static final int OSU_STATUS_REDIRECT_RESPONSE_RECEIVED = 8;

    /**
     * Provisioning status for OSU failure
     *
     * @param status indicates error condition
     */
    public abstract void onProvisioningFailure(int status);

    /**
     * Provisioning status when OSU is in progress
     *
     * @param status indicates status of OSU flow
     */
    public abstract void onProvisioningStatus(int status);
}

