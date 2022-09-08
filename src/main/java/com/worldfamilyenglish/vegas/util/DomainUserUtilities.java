package com.worldfamilyenglish.vegas.util;

public class DomainUserUtilities {
    private DomainUserUtilities() {
        // util class - no construction
    }


    public static String[] parseName(final String fullName) {
        int secondCapitalPosition = 0;
        for (int i = 1; i < fullName.length(); i++) {
            char c = fullName.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                secondCapitalPosition = i;
                break;
            }
        }

        String givenName = fullName.substring(0, secondCapitalPosition);
        String familyName = fullName.substring(secondCapitalPosition);

        return new String[] {givenName, familyName};
    }
}
