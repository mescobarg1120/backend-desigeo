package com.backend_desigeo.desigeo_report_service.util;

public class GeohashUtil {

    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int DEFAULT_PRECISION = 7;

    public static String encode(double lat, double lon) {
        return encode(lat, lon, DEFAULT_PRECISION);
    }

    public static String encode(double lat, double lon, int precision) {
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};
        StringBuilder hash = new StringBuilder();
        boolean isLon = true;
        int bitsTotal = 0;
        int hashValue = 0;

        while (hash.length() < precision) {
            double mid;
            if (isLon) {
                mid = (lonRange[0] + lonRange[1]) / 2;
                if (lon >= mid) { hashValue = (hashValue << 1) | 1; lonRange[0] = mid; }
                else            { hashValue = hashValue << 1;        lonRange[1] = mid; }
            } else {
                mid = (latRange[0] + latRange[1]) / 2;
                if (lat >= mid) { hashValue = (hashValue << 1) | 1; latRange[0] = mid; }
                else            { hashValue = hashValue << 1;        latRange[1] = mid; }
            }
            isLon = !isLon;
            if (++bitsTotal % 5 == 0) {
                hash.append(BASE32.charAt(hashValue));
                hashValue = 0;
            }
        }
        return hash.toString();
    }
}
