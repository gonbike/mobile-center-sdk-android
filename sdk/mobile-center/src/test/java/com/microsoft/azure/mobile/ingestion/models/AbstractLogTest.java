package com.microsoft.azure.mobile.ingestion.models;

import com.microsoft.azure.mobile.test.TestUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.junit.Test;

import java.util.UUID;

import static com.microsoft.azure.mobile.test.TestUtils.checkEquals;
import static com.microsoft.azure.mobile.test.TestUtils.checkNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
public class AbstractLogTest {

    @Test
    public void utilsCoverage() {
        new CommonProperties();
    }

    @Test
    public void compareDifferentType() {
        TestUtils.compareSelfNullClass(new MockLog());
        MockLogWithProperties mockLogWithProperties = new MockLogWithProperties();
        TestUtils.compareSelfNullClass(mockLogWithProperties);
        mockLogWithProperties.setToffset(1L);
        checkNotEquals(mockLogWithProperties, new MockLogWithProperties());
    }

    @Test
    public void compare() {

        /* Empty objects. */
        AbstractLog a = new MockLog();
        AbstractLog b = new MockLog();
        checkEquals(a, b);

        /* Toffset. */
        a.setToffset(1);
        checkNotEquals(a, b);
        b.setToffset(1);
        checkEquals(a, b);

        /* Sid. */
        UUID sid1 = UUID.randomUUID();
        UUID sid2 = UUID.randomUUID();
        a.setSid(sid1);
        checkNotEquals(a, b);
        b.setSid(sid2);
        checkNotEquals(a, b);
        b.setSid(sid1);
        checkEquals(a, b);

        /* Device. */
        Device d1 = new Device();
        d1.setLocale("a");
        Device d2 = new Device();
        d2.setSdkVersion("a");
        a.setDevice(d1);
        checkNotEquals(a, b);
        b.setDevice(d2);
        checkNotEquals(a, b);
        b.setDevice(d1);
        checkEquals(a, b);
    }

    @Test(expected = JSONException.class)
    public void readDifferentTypeTest() throws JSONException {
        JSONObject mockJsonObject = mock(JSONObject.class);
        when(mockJsonObject.getString(CommonProperties.TYPE)).thenReturn("type");
        AbstractLog mockLog = new MockLog();
        mockLog.read(mockJsonObject);
    }

    @Test
    public void readWithoutOptionalPropertiesTest() throws JSONException {
        AbstractLog mockLog = new MockLogWithType();
        UUID uuid = UUID.randomUUID();

        JSONObject mockJsonObject = mock(JSONObject.class);
        when(mockJsonObject.getString(CommonProperties.TYPE)).thenReturn(mockLog.getType());
        when(mockJsonObject.has(AbstractLog.SID)).thenReturn(false).thenReturn(true);
        when(mockJsonObject.has(AbstractLog.DEVICE)).thenReturn(false).thenReturn(true);
        when(mockJsonObject.getString(AbstractLog.SID)).thenReturn(uuid.toString());
        when(mockJsonObject.getJSONObject(AbstractLog.DEVICE)).thenReturn(mock(JSONObject.class));

        mockLog.read(mockJsonObject);
        assertNull(mockLog.getSid());
        assertNull(mockLog.getDevice());

        mockLog.read(mockJsonObject);
        assertEquals(uuid, mockLog.getSid());
        assertNotNull(mockLog.getDevice());
    }

    @Test
    public void writeNullDeviceTest() throws JSONException {
        JSONStringer mockJsonStringer = mock(JSONStringer.class);
        when(mockJsonStringer.key(anyString())).thenReturn(mockJsonStringer);
        when(mockJsonStringer.value(anyString())).thenReturn(mockJsonStringer);

        AbstractLog mockLog = new MockLog();
        mockLog.write(mockJsonStringer);

        verify(mockJsonStringer, never()).key(AbstractLog.DEVICE);
    }

    private static class MockLog extends AbstractLog {

        @Override
        public String getType() {
            return null;
        }
    }

    private static class MockLogWithProperties extends LogWithProperties {

        @Override
        public String getType() {
            return null;
        }
    }

    private static class MockLogWithType extends MockLog {

        @Override
        public String getType() {
            return "mockType";
        }
    }
}
