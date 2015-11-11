/**
 * Copyright (c) 2015, Joyent, Inc. All rights reserved.
 */
package com.joyent.manta.client;

import com.joyent.manta.client.config.TestConfigContext;
import com.joyent.manta.config.ConfigContext;
import com.joyent.manta.exception.MantaClientHttpResponseException;
import com.joyent.manta.exception.MantaCryptoException;
import com.joyent.manta.exception.MantaObjectException;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.*;
import java.util.Collection;
import java.util.UUID;


/**
 * @author Yunong Xiao
 */
public class MantaClientTest {

    private static final String TEST_DATA = "EPISODEII_IS_BEST_EPISODE";
    private static final String TEST_FILENAME = "Master-Yoda.jpg";

    private MantaClient mantaClient;

    private String testPathPrefix;


    @BeforeClass
    @Parameters({"manta.url", "manta.user", "manta.key_path", "manta.key_id", "manta.timeout"})
    public void beforeClass(@Optional String mantaUrl,
                            @Optional String mantaUser,
                            @Optional String mantaKeyPath,
                            @Optional String mantaKeyId,
                            @Optional Integer mantaTimeout)
            throws IOException, MantaClientHttpResponseException, MantaCryptoException {

        // Let TestNG configuration take precedence over environment variables
        ConfigContext config = new TestConfigContext(
                mantaUrl, mantaUser, mantaKeyPath, mantaKeyId, mantaTimeout);

        mantaClient = MantaClient.newInstance(config);
        testPathPrefix = String.format("/%s/stor/%s/", config.getMantaUser(), UUID.randomUUID());
        mantaClient.putDirectory(testPathPrefix, null);
    }


    @AfterClass
    public void afterClass() throws IOException, MantaClientHttpResponseException, MantaCryptoException {
        if (mantaClient != null) {
            mantaClient.deleteRecursive(testPathPrefix);
        }
    }


    @Test
    public final void testCRUDObject() throws IOException, MantaClientHttpResponseException, MantaCryptoException {
        final String name = UUID.randomUUID().toString();
        final MantaObject mantaObject = new MantaObject(testPathPrefix + name);
        mantaObject.setDataInputString(TEST_DATA);
        mantaClient.put(mantaObject);

        final MantaObject gotObject = mantaClient.get(testPathPrefix + name);
        Assert.assertNotNull(gotObject);
        Assert.assertNotNull(gotObject.getContentType());
        Assert.assertNotNull(gotObject.getContentLength());
        Assert.assertNotNull(gotObject.getEtag());
        Assert.assertNotNull(gotObject.getMtime());
        Assert.assertNotNull(gotObject.getPath());

        final String data = MantaUtils.inputStreamToString(gotObject.getDataInputStream());
        Assert.assertEquals(mantaObject.getDataInputString(), data);
        mantaClient.delete(mantaObject.getPath());
        boolean thrown = false;
        try {
            mantaClient.get(testPathPrefix + name);
        } catch (final MantaClientHttpResponseException e) {
            Assert.assertEquals(404, e.getStatusCode());
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }


    @Test
    public final void testCRUDWithFileObject() throws IOException, MantaClientHttpResponseException, MantaCryptoException {
        final String name = UUID.randomUUID().toString();
        final MantaObject mantaObject = new MantaObject(testPathPrefix + name);
        mantaObject.setDataInputString(TEST_DATA);
        mantaClient.put(mantaObject);
        final MantaObject gotObject = mantaClient.get(testPathPrefix + name);
        final File file = new File("/tmp/" + name);
        MantaUtils.inputStreamToFile(gotObject.getDataInputStream(), file);
        final String data = MantaUtils.readFileToString(file);
        Assert.assertEquals(mantaObject.getDataInputString(), data);
        mantaClient.delete(mantaObject.getPath());
        boolean thrown = false;
        try {
            mantaClient.get(testPathPrefix + name);
        } catch (final MantaClientHttpResponseException e) {
            Assert.assertEquals(404, e.getStatusCode());
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }


    @Test
    public final void testCRUDObjectWithHeaders() throws IOException, MantaClientHttpResponseException, MantaCryptoException {
        final String name = UUID.randomUUID().toString();
        final MantaObject mantaObject = new MantaObject(testPathPrefix + name);
        mantaObject.setHeader("durability-level", 4);
        mantaObject.setDataInputString(TEST_DATA);
        mantaClient.put(mantaObject);
        final MantaObject gotObject = mantaClient.get(testPathPrefix + name);
        final String data = MantaUtils.inputStreamToString(gotObject.getDataInputStream());
        Assert.assertEquals(mantaObject.getDataInputString(), data);
        Assert.assertEquals(4, mantaObject.getHeader("durability-level"));
        mantaClient.delete(mantaObject.getPath());
        boolean thrown = false;
        try {
            mantaClient.get(testPathPrefix + name);
        } catch (final MantaClientHttpResponseException e) {
            Assert.assertEquals(404, e.getStatusCode());
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }


    @Test
    public final void testRecursiveDeleteObject() throws IOException, MantaClientHttpResponseException, MantaCryptoException {

        final MantaObject mantaObject = new MantaObject(testPathPrefix + UUID.randomUUID().toString());

        mantaClient.putDirectory(testPathPrefix + "1", null);
        final MantaObject mantaObject1 = new MantaObject(testPathPrefix + "1/"+ UUID.randomUUID().toString());
        mantaObject1.setDataInputString(TEST_DATA);
        mantaClient.put(mantaObject1);

        mantaClient.putDirectory(testPathPrefix + "1/2", null);
        final MantaObject mantaObject2 = new MantaObject(testPathPrefix + "1/2/" + UUID.randomUUID().toString());
        mantaObject2.setDataInputString(TEST_DATA);
        mantaClient.put(mantaObject2);

        mantaClient.putDirectory(testPathPrefix + "1/2/3", null);
        final MantaObject mantaObject3 = new MantaObject(testPathPrefix + "1/2/3/" + UUID.randomUUID().toString());
        mantaObject3.setDataInputString(TEST_DATA);
        mantaClient.put(mantaObject3);

        mantaClient.deleteRecursive(testPathPrefix + "1");

        boolean thrown = false;
        try {
            mantaClient.get(testPathPrefix + "1");
        } catch (final MantaClientHttpResponseException e) {
            Assert.assertEquals(404, e.getStatusCode());
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }


    @Test
    public final void testPutWithStream() throws IOException, MantaClientHttpResponseException, MantaCryptoException {
        final String name = UUID.randomUUID().toString();
        final MantaObject mantaObject = new MantaObject(testPathPrefix + name);
        final InputStream testDataInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEST_FILENAME);
        mantaObject.setDataInputStream(testDataInputStream);
        mantaClient.put(mantaObject);
    }


    @Test
    public final void testHead() throws IOException, MantaClientHttpResponseException, MantaCryptoException {
        final String name = UUID.randomUUID().toString();
        final MantaObject mantaObject = new MantaObject(testPathPrefix + name);
        mantaObject.setDataInputString(TEST_DATA);
        mantaClient.put(mantaObject);
        final MantaObject obj = mantaClient.head(mantaObject.getPath());
        Assert.assertNotNull(obj);
    }


    @Test
    public final void testPutLink() throws IOException, MantaClientHttpResponseException, MantaCryptoException {
        final String name = UUID.randomUUID().toString();
        final MantaObject original = new MantaObject(testPathPrefix + name);
        original.setDataInputString(TEST_DATA);
        mantaClient.put(original);

        final String link = UUID.randomUUID().toString();
        mantaClient.putSnapLink(testPathPrefix + link, testPathPrefix + name, null);
        final MantaObject linkObj = mantaClient.get(testPathPrefix + link);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(linkObj.getDataInputStream()));
        String data;
        while ((data = reader.readLine()) != null) {
            Assert.assertEquals(TEST_DATA, data);
        }
    }


    @Test
    public final void testList() throws IOException, MantaClientHttpResponseException, MantaCryptoException, MantaObjectException {
        final String pathPrefix = testPathPrefix + "/" + UUID.randomUUID().toString();
        mantaClient.putDirectory(pathPrefix, null);
        mantaClient.put(new MantaObject(pathPrefix + "/" + UUID.randomUUID().toString()));
        mantaClient.put(new MantaObject(pathPrefix + "/" + UUID.randomUUID().toString()));
        final String subDir = pathPrefix + "/" + UUID.randomUUID().toString();
        mantaClient.putDirectory(subDir, null);
        mantaClient.put(new MantaObject(subDir + "/" + UUID.randomUUID().toString()));
        final Collection<MantaObject> objs = mantaClient.listObjects(pathPrefix);
        for (final MantaObject mantaObject : objs) {
            Assert.assertTrue(mantaObject.getPath().startsWith(testPathPrefix));
        }
        Assert.assertEquals(3, objs.size());
    }


    @Test(expectedExceptions = MantaObjectException.class)
    public final void testListNotADir() throws IOException, MantaClientHttpResponseException,MantaCryptoException, MantaObjectException {
        final String name = UUID.randomUUID().toString();
        final MantaObject mantaObject = new MantaObject(testPathPrefix + name);
        mantaObject.setDataInputString(TEST_DATA);
        mantaClient.put(mantaObject);
        mantaClient.listObjects(mantaObject.getPath());
    }


    @Test
    public final void testRFC3986() throws IOException, MantaClientHttpResponseException, MantaCryptoException {
        final String name = "spaces in the name of the file";
        final MantaObject mantaObject = new MantaObject(testPathPrefix + name);
        mantaObject.setDataInputString(TEST_DATA);
        mantaClient.put(mantaObject);
        final MantaObject gotObject = mantaClient.get(testPathPrefix + name);
        final String data = MantaUtils.inputStreamToString(gotObject.getDataInputStream());
        Assert.assertEquals(mantaObject.getDataInputString(), data);
        mantaClient.delete(mantaObject.getPath());
        boolean thrown = false;
        try {
            mantaClient.get(testPathPrefix + name);
        } catch (final MantaClientHttpResponseException e) {
            Assert.assertEquals(404, e.getStatusCode());
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }


}
