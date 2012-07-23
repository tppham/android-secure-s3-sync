package com.isecpartners.samplesync;

public interface Constants {
    /* our account types */
    public static final String ACCOUNT_TYPE_PREFIX =  "com.isecpartners.sync.";
    public static final String ACCOUNT_TYPE_SD =  "com.isecpartners.sync.sd";
    public static final String ACCOUNT_TYPE_S3 =  "com.isecpartners.sync.s3";

    /* other account types we care about, see model.ContactSetDB */
    /* note: we may want to add more... */
    public static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    public static final String ACCOUNT_TYPE_EXCHANGE = "com.android.exchange";
    public static final String ACCOUNT_TYPE_DEV = "DeviceOnly"; // seen on HTC phone
}
