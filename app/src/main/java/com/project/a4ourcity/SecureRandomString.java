package com.project.a4ourcity;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by luca on 21/1/2016.
 */
public final class SecureRandomString {
    private SecureRandom random = new SecureRandom();

    public String nextString() {
        return new BigInteger(20, random).toString(8);
    }

    public String nextSmallString() { return new BigInteger(30,random).toString(16); }

}
