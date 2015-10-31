/**
 * Copyright (c) 2015, Joyent, Inc. All rights reserved.
 */
package com.joyent.manta.client.crypto;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.joyent.manta.exception.MantaCryptoException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;

import java.io.*;
import java.net.URL;
import java.security.*;


/**
 * @author Yunong Xiao
 */
public class HttpSignerTest {

    private static final HttpRequestFactory REQUEST_FACTORY = new NetHttpTransport().createRequestFactory();

    private HttpSigner httpSigner;

    private HttpSigner httpSignerInitializedWithInMemoryKeyData;


    @BeforeClass
    @Parameters({"manta.test.key.private.filename", "manta.test.key.fingerprint", "manta.accountName"})
    public void beforeClass(String privateKeyFilename, String keyFingerPrint, @Optional String accountName)
            throws IOException, NoSuchAlgorithmException {
        URL privateKeyUrl = Thread.currentThread().getContextClassLoader().getResource(privateKeyFilename);
        Assert.assertNotNull(privateKeyUrl);

        httpSigner = HttpSigner.newInstance(privateKeyUrl.getFile(), keyFingerPrint, accountName);
        String privateKeyContent = readFile(privateKeyUrl.getFile());
        httpSignerInitializedWithInMemoryKeyData = HttpSigner.newInstance(privateKeyContent, keyFingerPrint, null, accountName);
    }


    @Test
    public final void testSignDataWithInMemSigner() throws IOException, MantaCryptoException {
        final HttpRequest httpRequest = REQUEST_FACTORY.buildGetRequest(new GenericUrl());
        httpSignerInitializedWithInMemoryKeyData.signRequest(httpRequest);
        final boolean verified = httpSignerInitializedWithInMemoryKeyData.verifyRequest(httpRequest);
        Assert.assertTrue(verified, "unable to verify signed authorization header");
    }


    @Test
    public final void testSignData() throws IOException, MantaCryptoException {
        final HttpRequest httpRequest = REQUEST_FACTORY.buildGetRequest(new GenericUrl());
        httpSigner.signRequest(httpRequest);
        final boolean verified = httpSigner.verifyRequest(httpRequest);
        Assert.assertTrue(verified, "unable to verify signed authorization header");
    }



    private String readFile(final String path) throws IOException {
        BufferedReader br = null;
        final StringBuilder result = new StringBuilder();
        try {
            String line;
            br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
            return result.toString();
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }


}