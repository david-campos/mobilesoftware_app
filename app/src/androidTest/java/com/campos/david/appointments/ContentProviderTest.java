package com.campos.david.appointments;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import com.campos.david.appointments.model.ContentProvider;
import com.campos.david.appointments.model.DBContract;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

/**
 * Basic test for the ContentProvider. Not very exhaustive.
 */
@RunWith(AndroidJUnit4.class)
public class ContentProviderTest extends ProviderTestCase2<ContentProvider> {
    public ContentProviderTest() {
        super(ContentProvider.class, DBContract.CONTENT_AUTHORITY);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }

    @Test
    public void testUsers() {
        String[] usersProjection = {
                DBContract.UsersEntry.COLUMN_NAME,
                DBContract.UsersEntry.COLUMN_PHONE,
                DBContract.UsersEntry.COLUMN_PICTURE,
                DBContract.UsersEntry.COLUMN_BLOCKED};

        ContentValues values = new ContentValues();

        values.put(DBContract.UsersEntry._ID, 2);
        values.put(DBContract.UsersEntry.COLUMN_NAME, "Paco");
        values.put(DBContract.UsersEntry.COLUMN_PHONE, "666123456");
        values.put(DBContract.UsersEntry.COLUMN_PICTURE, "5");
        values.put(DBContract.UsersEntry.COLUMN_BLOCKED, false);

        Uri uri = getMockContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values);
        assertNotNull("insert returned null", uri);
        assertEquals(uri.getLastPathSegment(), "2");

        // Inserting another user
        ContentValues values2 = new ContentValues();

        values2.put(DBContract.UsersEntry._ID, 1);
        values2.put(DBContract.UsersEntry.COLUMN_NAME, "Manolo");
        values2.put(DBContract.UsersEntry.COLUMN_PHONE, "687207650");
        values2.put(DBContract.UsersEntry.COLUMN_PICTURE, "5");
        values2.put(DBContract.UsersEntry.COLUMN_BLOCKED, true);

        Uri uri2 = getMockContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values2);
        assertNotNull("insert2 returned null", uri2);
        assertEquals(uri2.getLastPathSegment(), "1");

        ContentValues values3 = new ContentValues();

        // Another one, repeated id (should fail and return null)
        values3.put(DBContract.UsersEntry._ID, 2);
        values3.put(DBContract.UsersEntry.COLUMN_NAME, "Arturo");
        values3.put(DBContract.UsersEntry.COLUMN_PHONE, "123456");
        values3.put(DBContract.UsersEntry.COLUMN_PICTURE, "2");
        values3.put(DBContract.UsersEntry.COLUMN_BLOCKED, false);
        Uri uri3 = getMockContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values3);
        assertNull("insert3 returned not null", uri3);

        // Another one, repeated phone (should fail and return null)
        values3.put(DBContract.UsersEntry._ID, 3);
        values3.put(DBContract.UsersEntry.COLUMN_NAME, "Arturo");
        values3.put(DBContract.UsersEntry.COLUMN_PHONE, "666123456");
        values3.put(DBContract.UsersEntry.COLUMN_PICTURE, "2");
        values3.put(DBContract.UsersEntry.COLUMN_BLOCKED, false);
        uri3 = getMockContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values3);
        assertNull("insert4 returned not null", uri3);

        // Another one, not blocked column, should work
        values3 = new ContentValues();
        values3.put(DBContract.UsersEntry._ID, 4);
        values3.put(DBContract.UsersEntry.COLUMN_NAME, "Pepe");
        values3.put(DBContract.UsersEntry.COLUMN_PHONE, "923456789");
        values3.put(DBContract.UsersEntry.COLUMN_PICTURE, "2");
        uri3 = getMockContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values3);
        assertNotNull("insert5 returned null", uri3);

        // Check /users/
        Cursor c = getMockContentResolver().query(DBContract.UsersEntry.CONTENT_URI, usersProjection, null, null, null);
        assertNotNull("Query /users/ returned null", c);
        assertEquals(c.getCount(), 3);
        c.close();

        // Check /users/ with select clause
        c = getMockContentResolver().query(
                DBContract.UsersEntry.CONTENT_URI,
                usersProjection, DBContract.UsersEntry.COLUMN_BLOCKED + " = ?",
                new String[]{"1"},
                null);
        assertNotNull("Query /users/ with select returned null", c);
        assertEquals(c.getCount(), 1); // Only Manolo!
        assertTrue("Couldn't move to first the cursor", c.moveToFirst());
        assertEquals(c.getString(0), values2.getAsString(DBContract.UsersEntry.COLUMN_NAME));
        assertEquals(c.getString(1), values2.getAsString(DBContract.UsersEntry.COLUMN_PHONE));
        assertEquals(c.getString(2), values2.getAsString(DBContract.UsersEntry.COLUMN_PICTURE));
        assertEquals(c.getInt(3)!=0, (boolean) values2.getAsBoolean(DBContract.UsersEntry.COLUMN_BLOCKED));
        c.close();

        // Check /users/#
        c = getMockContentResolver().query(uri, usersProjection, null, null, null);
        assertNotNull("Check '"+uri.toString()+"' returned null", c);

        assertTrue("Couldn't move to first the cursor", c.moveToFirst());
        assertEquals(c.getString(0), values.getAsString(DBContract.UsersEntry.COLUMN_NAME));
        assertEquals(c.getString(1), values.getAsString(DBContract.UsersEntry.COLUMN_PHONE));
        assertEquals(c.getString(2), values.getAsString(DBContract.UsersEntry.COLUMN_PICTURE));
        assertEquals(c.getInt(3)!=0, (boolean)values.getAsBoolean(DBContract.UsersEntry.COLUMN_BLOCKED));
        c.close();

        // Deleting our beloved (and blocked) Manolo
        assertEquals(getMockContentResolver().delete(uri2, null, null), 1);

        // Check /users/ returns now only two users
        c = getMockContentResolver().query(DBContract.UsersEntry.CONTENT_URI, usersProjection, null, null, null);
        assertNotNull("Query /users/ (2) returned null", c);
        assertEquals(c.getCount(), 2);
        c.close();

        // Update Paco's picture
        ContentValues values4 = new ContentValues();
        values4.put(DBContract.UsersEntry.COLUMN_PICTURE, "8");
        assertEquals(getMockContentResolver().update(uri, values4, null, null), 1);

        // Check Paco
        c = getMockContentResolver().query(uri, usersProjection, null, null, null);
        assertNotNull("Check /users/# returned null", c);
        assertTrue("Couldn't move to first the cursor", c.moveToFirst());
        assertEquals(c.getString(0), values.getAsString(DBContract.UsersEntry.COLUMN_NAME));
        assertEquals(c.getString(1), values.getAsString(DBContract.UsersEntry.COLUMN_PHONE));
        assertEquals(c.getString(2), values4.getAsString(DBContract.UsersEntry.COLUMN_PICTURE));
        assertEquals(c.getInt(3)!=0, (boolean)values.getAsBoolean(DBContract.UsersEntry.COLUMN_BLOCKED));
        c.close();

        // Check types
        String type = getMockContentResolver().getType(DBContract.UsersEntry.CONTENT_URI);
        String itemType = getMockContentResolver().getType(uri);

        assertEquals(type, DBContract.UsersEntry.CONTENT_TYPE);
        // Not equals to other content type
        assertNotSame(type, DBContract.AppointmentsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.PropositionsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.AppointmentTypesEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.ReasonsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.InvitationsEntry.CONTENT_TYPE);
        // Not any item type
        assertNotSame(type, DBContract.UsersEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.PropositionsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentTypesEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.ReasonsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.InvitationsEntry.CONTENT_ITEM_TYPE);

        assertEquals(itemType, DBContract.UsersEntry.CONTENT_ITEM_TYPE);
        // Not other item type
        assertNotSame(itemType, DBContract.AppointmentsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(itemType, DBContract.PropositionsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(itemType, DBContract.AppointmentTypesEntry.CONTENT_ITEM_TYPE);
        assertNotSame(itemType, DBContract.ReasonsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(itemType, DBContract.InvitationsEntry.CONTENT_ITEM_TYPE);
        // Not any content type
        assertNotSame(itemType, DBContract.UsersEntry.CONTENT_TYPE);
        assertNotSame(itemType, DBContract.AppointmentsEntry.CONTENT_TYPE);
        assertNotSame(itemType, DBContract.PropositionsEntry.CONTENT_TYPE);
        assertNotSame(itemType, DBContract.AppointmentTypesEntry.CONTENT_TYPE);
        assertNotSame(itemType, DBContract.ReasonsEntry.CONTENT_TYPE);
        assertNotSame(itemType, DBContract.InvitationsEntry.CONTENT_TYPE);

        // Delete all users
        int n = getMockContentResolver().delete(DBContract.UsersEntry.CONTENT_URI, null, null);
        assertEquals(n, 2);
    }

    @Test
    public void testReasons() {
        String[] projection = {
                DBContract.ReasonsEntry._ID,
                DBContract.ReasonsEntry.COLUMN_NAME,
                DBContract.ReasonsEntry.COLUMN_DESCRIPTION};
        ContentValues values = new ContentValues();
        values.put(DBContract.ReasonsEntry.COLUMN_NAME, "A");
        values.put(DBContract.ReasonsEntry.COLUMN_DESCRIPTION, "D");

        ContentValues values1 = new ContentValues();
        values1.put(DBContract.ReasonsEntry.COLUMN_NAME, "B");
        values1.put(DBContract.ReasonsEntry.COLUMN_DESCRIPTION, "D2");

        // Check insert
        Uri uri = getMockContentResolver().insert(DBContract.ReasonsEntry.CONTENT_URI, values);
        assertNotNull(uri);
        Uri uri1 = getMockContentResolver().insert(DBContract.ReasonsEntry.CONTENT_URI, values1);
        assertNotNull(uri1);


        // Check getting content
        Cursor c = getMockContentResolver().query(DBContract.ReasonsEntry.CONTENT_URI, projection, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals(c.getString(0), uri.getLastPathSegment());
        assertEquals(c.getString(1), values.getAsString(DBContract.ReasonsEntry.COLUMN_NAME));
        assertEquals(c.getString(2), values.getAsString(DBContract.ReasonsEntry.COLUMN_DESCRIPTION));
        c.close();

        // Check updating content
        values.remove(DBContract.ReasonsEntry.COLUMN_DESCRIPTION);
        values.put(DBContract.ReasonsEntry.COLUMN_DESCRIPTION, "New description");
        assertEquals(
                getMockContentResolver().update(
                        DBContract.ReasonsEntry.CONTENT_URI,
                    values,
                    DBContract.ReasonsEntry._ID + "=?",
                    new String[]{uri.getLastPathSegment()}),
                1);
        c = getMockContentResolver().query(DBContract.ReasonsEntry.CONTENT_URI, projection, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals(c.getString(0), uri.getLastPathSegment());
        assertEquals(c.getString(1), values.getAsString(DBContract.ReasonsEntry.COLUMN_NAME));
        assertEquals(c.getString(2), values.getAsString(DBContract.ReasonsEntry.COLUMN_DESCRIPTION));
        assertTrue(c.moveToNext());
        assertEquals(c.getString(0), uri1.getLastPathSegment());
        assertEquals(c.getString(1), values1.getAsString(DBContract.ReasonsEntry.COLUMN_NAME));
        assertEquals(c.getString(2), values1.getAsString(DBContract.ReasonsEntry.COLUMN_DESCRIPTION));
        c.close();

        // Check removing content
        assertEquals(
                getMockContentResolver().delete(DBContract.ReasonsEntry.CONTENT_URI,
                        DBContract.ReasonsEntry.COLUMN_NAME + "=?",
                        new String[]{values1.getAsString(DBContract.ReasonsEntry.COLUMN_NAME)}),
                1
        );
        assertEquals(
                getMockContentResolver().delete(DBContract.ReasonsEntry.CONTENT_URI, null, null),
                1
        );
        c = getMockContentResolver().query(DBContract.ReasonsEntry.CONTENT_URI, projection, null, null, null);
        assertNotNull(c);
        assertEquals(c.getCount(), 0);
        c.close();

        // Check types
        String type = getMockContentResolver().getType(DBContract.ReasonsEntry.CONTENT_URI);
        assertEquals(type, DBContract.ReasonsEntry.CONTENT_TYPE);
        // Not equals to other content type
        assertNotSame(type, DBContract.AppointmentsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.PropositionsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.AppointmentTypesEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.UsersEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.InvitationsEntry.CONTENT_TYPE);
        // Not any item type
        assertNotSame(type, DBContract.UsersEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.PropositionsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentTypesEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.ReasonsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.InvitationsEntry.CONTENT_ITEM_TYPE);
    }

    @Test
    public void testAppointmentTypes() {
        String[] projection = {
                DBContract.AppointmentTypesEntry._ID,
                DBContract.AppointmentTypesEntry.COLUMN_NAME,
                DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION,
                DBContract.AppointmentTypesEntry.COLUMN_ICON};

        ContentValues values = new ContentValues();
        values.put(DBContract.AppointmentTypesEntry.COLUMN_NAME, "A");
        values.put(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION, "D");
        values.put(DBContract.AppointmentTypesEntry.COLUMN_ICON, "5");

        ContentValues values1 = new ContentValues();
        values1.put(DBContract.AppointmentTypesEntry.COLUMN_NAME, "B");
        values1.put(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION, "D2");
        values1.put(DBContract.AppointmentTypesEntry.COLUMN_ICON, "2");

        // Check insert
        Uri uri = getMockContentResolver().insert(DBContract.AppointmentTypesEntry.CONTENT_URI, values);
        assertNotNull(uri);
        Uri uri1 = getMockContentResolver().insert(DBContract.AppointmentTypesEntry.CONTENT_URI, values1);
        assertNotNull(uri1);


        // Check getting content
        Cursor c = getMockContentResolver().query(DBContract.AppointmentTypesEntry.CONTENT_URI, projection, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals(c.getString(0), uri.getLastPathSegment());
        assertEquals(c.getString(1), values.getAsString(DBContract.AppointmentTypesEntry.COLUMN_NAME));
        assertEquals(c.getString(2), values.getAsString(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION));
        assertEquals(c.getString(3), values.getAsString(DBContract.AppointmentTypesEntry.COLUMN_ICON));
        c.close();

        // Check updating content
        values.remove(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION);
        values.put(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION, "New description");
        assertEquals(
                getMockContentResolver().update(
                        DBContract.AppointmentTypesEntry.CONTENT_URI,
                        values,
                        DBContract.AppointmentTypesEntry._ID + "=?",
                        new String[]{uri.getLastPathSegment()}),
                1);
        c = getMockContentResolver().query(DBContract.AppointmentTypesEntry.CONTENT_URI, projection, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals(c.getString(0), uri.getLastPathSegment());
        assertEquals(c.getString(1), values.getAsString(DBContract.AppointmentTypesEntry.COLUMN_NAME));
        assertEquals(c.getString(2), values.getAsString(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION));
        assertEquals(c.getString(3), values.getAsString(DBContract.AppointmentTypesEntry.COLUMN_ICON));
        assertTrue(c.moveToNext());
        assertEquals(c.getString(0), uri1.getLastPathSegment());
        assertEquals(c.getString(1), values1.getAsString(DBContract.AppointmentTypesEntry.COLUMN_NAME));
        assertEquals(c.getString(2), values1.getAsString(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION));
        assertEquals(c.getString(3), values1.getAsString(DBContract.AppointmentTypesEntry.COLUMN_ICON));
        c.close();

        // Check removing content
        assertEquals(
                getMockContentResolver().delete(DBContract.AppointmentTypesEntry.CONTENT_URI,
                        DBContract.AppointmentTypesEntry.COLUMN_NAME+"=?",
                        new String[]{values1.getAsString(DBContract.AppointmentTypesEntry.COLUMN_NAME)}),
                1
        );
        assertEquals(
                getMockContentResolver().delete(DBContract.AppointmentTypesEntry.CONTENT_URI, null, null),
                1
        );
        c = getMockContentResolver().query(DBContract.AppointmentTypesEntry.CONTENT_URI, projection, null, null, null);
        assertNotNull(c);
        assertEquals(c.getCount(), 0);
        c.close();

        // Check types
        String type = getMockContentResolver().getType(DBContract.AppointmentTypesEntry.CONTENT_URI);
        assertEquals(type, DBContract.AppointmentTypesEntry.CONTENT_TYPE);
        // Not equals to other content type
        assertNotSame(type, DBContract.AppointmentsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.PropositionsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.ReasonsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.UsersEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.InvitationsEntry.CONTENT_TYPE);
        // Not any item type
        assertNotSame(type, DBContract.UsersEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.PropositionsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentTypesEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.ReasonsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.InvitationsEntry.CONTENT_ITEM_TYPE);
    }

    @Test
    public void testAppointments() {
        // Insert a user and a type
        ContentValues values = new ContentValues();
        values.put(DBContract.UsersEntry._ID, 1);
        values.put(DBContract.UsersEntry.COLUMN_NAME, "A");
        values.put(DBContract.UsersEntry.COLUMN_PHONE, "1");
        values.put(DBContract.UsersEntry.COLUMN_PICTURE, "1");
        Uri uriUser0 = getMockContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values);
        assertNotNull(uriUser0);
        values.clear();
        values.put(DBContract.AppointmentTypesEntry.COLUMN_NAME, "A");
        values.put(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION, "D");
        values.put(DBContract.AppointmentTypesEntry.COLUMN_ICON, "1");
        Uri uriType0 = getMockContentResolver().insert(DBContract.AppointmentTypesEntry.CONTENT_URI, values);
        assertNotNull(uriType0);

        // Preparing stuff
        String[] projection = {
                DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry._ID,
                DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL,
                DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_TYPE,
                DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_CREATOR,
                DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_CLOSED,
                DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_DESCRIPTION,
                DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_NAME};

        // Checking insertion
        values.clear();
        values.put(DBContract.AppointmentsEntry._ID, "1");
        values.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, "1"); // There is no foreign key check
        values.put(DBContract.AppointmentsEntry.COLUMN_TYPE, uriType0.getLastPathSegment());
        values.put(DBContract.AppointmentsEntry.COLUMN_CREATOR, uriUser0.getLastPathSegment());
        values.put(DBContract.AppointmentsEntry.COLUMN_CLOSED, "0");
        values.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, "D");
        values.put(DBContract.AppointmentsEntry.COLUMN_NAME, "A");
        Uri uri = getMockContentResolver().insert(DBContract.AppointmentsEntry.CONTENT_URI, values);
        assertNotNull(uri);
        // Should fail (repeated id)
        ContentValues values1 = new ContentValues();
        values1.put(DBContract.AppointmentsEntry._ID, "1");
        values1.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, "1"); // There is no foreign key check
        values1.put(DBContract.AppointmentsEntry.COLUMN_TYPE, uriType0.getLastPathSegment());
        values1.put(DBContract.AppointmentsEntry.COLUMN_CREATOR, uriUser0.getLastPathSegment());
        values1.put(DBContract.AppointmentsEntry.COLUMN_CLOSED, "0");
        values1.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, "E");
        values1.put(DBContract.AppointmentsEntry.COLUMN_NAME, "B");
        Uri uri1 = getMockContentResolver().insert(DBContract.AppointmentsEntry.CONTENT_URI, values1);
        assertNull(uri1);
        values1.clear();
        //Should work (only id should be unrepeatable) creator is null
        values1.put(DBContract.AppointmentsEntry._ID, "2");
        values1.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, "1"); // There is no foreign key check
        values1.put(DBContract.AppointmentsEntry.COLUMN_TYPE, uriType0.getLastPathSegment());
        values1.put(DBContract.AppointmentsEntry.COLUMN_CLOSED, "0");
        values1.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, "D");
        // Creator is null
        values1.put(DBContract.AppointmentsEntry.COLUMN_NAME, "A");
        uri1 = getMockContentResolver().insert(DBContract.AppointmentsEntry.CONTENT_URI, values1);
        assertNotNull(uri1);

        // Checking query /appointments/
        Cursor c = getMockContentResolver().query(DBContract.AppointmentsEntry.CONTENT_URI, projection, null, null, null);
        assertNotNull(c);
        assertEquals(c.getCount(), 2);
        assertTrue(c.moveToFirst());
        assertEquals(c.getString(0), values.getAsString(DBContract.AppointmentsEntry._ID));
        assertEquals(c.getString(1), values.getAsString(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL));
        assertEquals(c.getString(2), values.getAsString(DBContract.AppointmentsEntry.COLUMN_TYPE));
        assertEquals(c.getString(3), values.getAsString(DBContract.AppointmentsEntry.COLUMN_CREATOR));
        assertEquals(c.getString(4), values.getAsString(DBContract.AppointmentsEntry.COLUMN_CLOSED));
        assertEquals(c.getString(5), values.getAsString(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION));
        assertEquals(c.getString(6), values.getAsString(DBContract.AppointmentsEntry.COLUMN_NAME));
        assertTrue(c.moveToNext());
        assertEquals(c.getString(0), values1.getAsString(DBContract.AppointmentsEntry._ID));
        assertEquals(c.getString(1), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL));
        assertEquals(c.getString(2), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_TYPE));
        assertEquals(c.getString(3), null);
        assertEquals(c.getString(4), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_CLOSED));
        assertEquals(c.getString(5), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION));
        assertEquals(c.getString(6), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_NAME));
        c.close();

        // Checking /appointments/#
        c = getMockContentResolver().query(uri, projection, null, null, null);
        assertNotNull(c);
        assertEquals(c.getCount(), 1);
        assertTrue(c.moveToFirst());
        assertEquals(c.getString(0), values.getAsString(DBContract.AppointmentsEntry._ID));
        assertEquals(c.getString(1), values.getAsString(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL));
        assertEquals(c.getString(2), values.getAsString(DBContract.AppointmentsEntry.COLUMN_TYPE));
        assertEquals(c.getString(3), values.getAsString(DBContract.AppointmentsEntry.COLUMN_CREATOR));
        assertEquals(c.getString(4), values.getAsString(DBContract.AppointmentsEntry.COLUMN_CLOSED));
        assertEquals(c.getString(5), values.getAsString(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION));
        assertEquals(c.getString(6), values.getAsString(DBContract.AppointmentsEntry.COLUMN_NAME));
        c.close();

        // Checking /appointments/accepted (creator)
        c = getMockContentResolver().query(
                DBContract.AppointmentsEntry.CONTENT_ACCEPTED_URI,
                projection, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals(c.getString(0), values1.getAsString(DBContract.AppointmentsEntry._ID));
        assertEquals(c.getString(1), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL));
        assertEquals(c.getString(2), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_TYPE));
        assertEquals(c.getString(3), null);
        assertEquals(c.getString(4), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_CLOSED));
        assertEquals(c.getString(5), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION));
        assertEquals(c.getString(6), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_NAME));
        c.close();

        // Checking update
        values.remove(DBContract.AppointmentsEntry.COLUMN_NAME);
        values.put(DBContract.AppointmentsEntry.COLUMN_NAME, "New name");
        ContentValues updateValues = new ContentValues();
        updateValues.put(DBContract.AppointmentsEntry.COLUMN_NAME,
                values.getAsString(DBContract.AppointmentsEntry.COLUMN_NAME));
        assertEquals(getMockContentResolver().update(uri, updateValues, null, null), 1);
        updateValues.clear();
        updateValues.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, "New description for all");
        assertEquals(getMockContentResolver().update(DBContract.AppointmentsEntry.CONTENT_URI,
                updateValues, null, null), 2);
        c = getMockContentResolver().query(DBContract.AppointmentsEntry.CONTENT_URI, projection, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals(c.getString(0), values.getAsString(DBContract.AppointmentsEntry._ID));
        assertEquals(c.getString(1), values.getAsString(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL));
        assertEquals(c.getString(2), values.getAsString(DBContract.AppointmentsEntry.COLUMN_TYPE));
        assertEquals(c.getString(3), values.getAsString(DBContract.AppointmentsEntry.COLUMN_CREATOR));
        assertEquals(c.getString(4), values.getAsString(DBContract.AppointmentsEntry.COLUMN_CLOSED));
        assertEquals(c.getString(5), updateValues.getAsString(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION));
        assertEquals(c.getString(6), values.getAsString(DBContract.AppointmentsEntry.COLUMN_NAME));
        assertTrue(c.moveToNext());
        assertEquals(c.getString(0), values1.getAsString(DBContract.AppointmentsEntry._ID));
        assertEquals(c.getString(1), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL));
        assertEquals(c.getString(2), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_TYPE));
        assertEquals(c.getString(3), null);
        assertEquals(c.getString(4), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_CLOSED));
        assertEquals(c.getString(5), updateValues.getAsString(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION));
        assertEquals(c.getString(6), values1.getAsString(DBContract.AppointmentsEntry.COLUMN_NAME));
        c.close();

        // Checking delete
        assertEquals(getMockContentResolver().delete(uri, null, null), 1);
        assertEquals(getMockContentResolver().delete(DBContract.AppointmentsEntry.CONTENT_URI, null, null), 1);
        c = getMockContentResolver().query(DBContract.AppointmentsEntry.CONTENT_URI, projection, null, null, null);
        assertNotNull(c);
        assertEquals(c.getCount(), 0);
        c.close();

        // Check types
        String type = getMockContentResolver().getType(DBContract.AppointmentsEntry.CONTENT_URI);
        String itemType = getMockContentResolver().getType(uri);

        assertEquals(type, DBContract.AppointmentsEntry.CONTENT_TYPE);
        // Not equals to other content type
        assertNotSame(type, DBContract.UsersEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.PropositionsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.AppointmentTypesEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.ReasonsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.InvitationsEntry.CONTENT_TYPE);
        // Not any item type
        assertNotSame(type, DBContract.UsersEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.PropositionsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentTypesEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.ReasonsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.InvitationsEntry.CONTENT_ITEM_TYPE);

        assertEquals(itemType, DBContract.AppointmentsEntry.CONTENT_ITEM_TYPE);
        // Not other item type
        assertNotSame(itemType, DBContract.UsersEntry.CONTENT_ITEM_TYPE);
        assertNotSame(itemType, DBContract.PropositionsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(itemType, DBContract.AppointmentTypesEntry.CONTENT_ITEM_TYPE);
        assertNotSame(itemType, DBContract.ReasonsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(itemType, DBContract.InvitationsEntry.CONTENT_ITEM_TYPE);
        // Not any content type
        assertNotSame(itemType, DBContract.UsersEntry.CONTENT_TYPE);
        assertNotSame(itemType, DBContract.AppointmentsEntry.CONTENT_TYPE);
        assertNotSame(itemType, DBContract.PropositionsEntry.CONTENT_TYPE);
        assertNotSame(itemType, DBContract.AppointmentTypesEntry.CONTENT_TYPE);
        assertNotSame(itemType, DBContract.ReasonsEntry.CONTENT_TYPE);
        assertNotSame(itemType, DBContract.InvitationsEntry.CONTENT_TYPE);
    }

    @Test
    public void testPropositions() {
        // Insert two users, an appointment type, an appointment and a reason
        ContentValues values = new ContentValues();
        values.put(DBContract.UsersEntry._ID, 1);
        values.put(DBContract.UsersEntry.COLUMN_NAME, "UA");
        values.put(DBContract.UsersEntry.COLUMN_PHONE, "1");
        values.put(DBContract.UsersEntry.COLUMN_PICTURE, "1");
        Uri uriUser0 = getMockContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values);
        assertNotNull(uriUser0);
        values.clear();
        values.put(DBContract.UsersEntry._ID, 2);
        values.put(DBContract.UsersEntry.COLUMN_NAME, "UB");
        values.put(DBContract.UsersEntry.COLUMN_PHONE, "2");
        values.put(DBContract.UsersEntry.COLUMN_PICTURE, "1");
        Uri uriUser1 = getMockContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values);
        assertNotNull("insert2 returned null", uriUser1);
        values.clear();
        values.put(DBContract.AppointmentTypesEntry.COLUMN_NAME, "TA");
        values.put(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION, "TD");
        values.put(DBContract.AppointmentTypesEntry.COLUMN_ICON, "1");
        Uri uriType0 = getMockContentResolver().insert(DBContract.AppointmentTypesEntry.CONTENT_URI, values);
        assertNotNull(uriType0);
        values.clear();
        values.put(DBContract.ReasonsEntry.COLUMN_NAME, "RA");
        values.put(DBContract.ReasonsEntry.COLUMN_DESCRIPTION, "DD");
        Uri uriReason0 = getMockContentResolver().insert(DBContract.ReasonsEntry.CONTENT_URI, values);
        assertNotNull(uriReason0);
        values.clear();
        values.put(DBContract.AppointmentsEntry._ID, 1);
        values.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, "1"); // There is no foreign key check
        values.put(DBContract.AppointmentsEntry.COLUMN_TYPE, uriType0.getLastPathSegment());
        values.put(DBContract.AppointmentsEntry.COLUMN_CREATOR, uriUser0.getLastPathSegment());
        values.put(DBContract.AppointmentsEntry.COLUMN_CLOSED, "0");
        values.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, "D");
        values.put(DBContract.AppointmentsEntry.COLUMN_NAME, "A");
        Uri uriAppointment0 = getMockContentResolver().insert(DBContract.AppointmentsEntry.CONTENT_URI, values);
        assertNotNull(uriAppointment0);

        // Preparing stuff
        String[] projection = {
                DBContract.PropositionsEntry._ID,
                DBContract.PropositionsEntry.COLUMN_APPOINTMENT,
                DBContract.PropositionsEntry.COLUMN_CREATOR,
                DBContract.PropositionsEntry.COLUMN_TIMESTAMP,
                DBContract.PropositionsEntry.COLUMN_REASON,
                DBContract.PropositionsEntry.COLUMN_PLACE_NAME,
                DBContract.PropositionsEntry.COLUMN_PLACE_LAT,
                DBContract.PropositionsEntry.COLUMN_PLACE_LON
        };
        // Checking insertion
        Long randomDate = (new Date()).getTime();
        ContentValues values0 = new ContentValues();
        values0.put(DBContract.PropositionsEntry.COLUMN_APPOINTMENT, 1);
        values0.put(DBContract.PropositionsEntry.COLUMN_CREATOR, uriUser0.getLastPathSegment());
        values0.put(DBContract.PropositionsEntry.COLUMN_TIMESTAMP, randomDate);
        values0.put(DBContract.PropositionsEntry.COLUMN_REASON, uriReason0.getLastPathSegment());
        values0.put(DBContract.PropositionsEntry.COLUMN_PLACE_NAME, "Pa");
        values0.put(DBContract.PropositionsEntry.COLUMN_PLACE_LAT, 0.0);
        values0.put(DBContract.PropositionsEntry.COLUMN_PLACE_LON, 0.1);
        Uri uri0 = getMockContentResolver().insert(DBContract.PropositionsEntry.CONTENT_URI, values0);
        assertNotNull(uri0);
        randomDate+=100000;
        ContentValues values1 = new ContentValues();
        values1.put(DBContract.PropositionsEntry.COLUMN_APPOINTMENT, 1);
        // Creator null
        values1.put(DBContract.PropositionsEntry.COLUMN_TIMESTAMP, randomDate);
        values1.put(DBContract.PropositionsEntry.COLUMN_REASON, uriReason0.getLastPathSegment());
        values1.put(DBContract.PropositionsEntry.COLUMN_PLACE_NAME, "Pb");
        values1.put(DBContract.PropositionsEntry.COLUMN_PLACE_LAT, 13.4105301);
        values1.put(DBContract.PropositionsEntry.COLUMN_PLACE_LON, 13.4105302);
        Uri uri1 = getMockContentResolver().insert(DBContract.PropositionsEntry.CONTENT_URI, values1);
        assertNotNull(uri1);
        // Should fail bc of the repeated combination appointment/time/place_name:
        Uri uri2 = getMockContentResolver().insert(DBContract.PropositionsEntry.CONTENT_URI, values0);
        assertNull(uri2);
        randomDate+=100000;
        ContentValues values2 = new ContentValues();
        values2.put(DBContract.PropositionsEntry.COLUMN_APPOINTMENT, 1);
        values2.put(DBContract.PropositionsEntry.COLUMN_CREATOR, uriUser1.getLastPathSegment());
        values2.put(DBContract.PropositionsEntry.COLUMN_TIMESTAMP, randomDate);
        values2.put(DBContract.PropositionsEntry.COLUMN_REASON, uriReason0.getLastPathSegment());
        values2.put(DBContract.PropositionsEntry.COLUMN_PLACE_NAME, "Pc");
        values2.put(DBContract.PropositionsEntry.COLUMN_PLACE_LAT, 52.5243700);
        values2.put(DBContract.PropositionsEntry.COLUMN_PLACE_LON, 13.4105300);
        uri2 = getMockContentResolver().insert(DBContract.PropositionsEntry.CONTENT_URI, values2);
        assertNotNull(uri2);

        // Check query
        Cursor c = getMockContentResolver().query(
                DBContract.PropositionsEntry.buildUriForAppointment(
                        Integer.parseInt(uriAppointment0.getLastPathSegment())),
                projection, null, null, null);
        assertNotNull(c);
        assertEquals(c.getCount(), 3);
        assertTrue(c.moveToFirst());
        ContentValues ptr;
        do {
            if(c.getString(0).equals(uri0.getLastPathSegment())) {
                ptr = values0;
            } else if(c.getString(0).equals(uri1.getLastPathSegment())) {
                ptr = values1;
            } else if(c.getString(0).equals(uri2.getLastPathSegment())) {
                ptr = values2;
            } else {
                Assert.fail("Unknown id '" + c.getString(0) + "'");
                return;
            }

            assertEquals(c.getString(1), ptr.getAsString(DBContract.PropositionsEntry.COLUMN_APPOINTMENT));
            assertEquals(c.getString(2), ptr.getAsString(DBContract.PropositionsEntry.COLUMN_CREATOR));
            assertEquals(c.getString(3), ptr.getAsString(DBContract.PropositionsEntry.COLUMN_TIMESTAMP));
            assertEquals(c.getString(4), ptr.getAsString(DBContract.PropositionsEntry.COLUMN_REASON));
            assertEquals(c.getString(5), ptr.getAsString(DBContract.PropositionsEntry.COLUMN_PLACE_NAME));
            assertEquals(c.getDouble(6), ptr.getAsDouble(DBContract.PropositionsEntry.COLUMN_PLACE_LAT));
            assertEquals(c.getDouble(7), ptr.getAsDouble(DBContract.PropositionsEntry.COLUMN_PLACE_LON));
        } while(c.moveToNext());
        c.close();

        // Check update
        ContentValues updateValues = new ContentValues();
        updateValues.put(DBContract.PropositionsEntry.COLUMN_PLACE_NAME, "Lodz");
        assertEquals(getMockContentResolver().update(
                DBContract.PropositionsEntry.buildUriForAppointment(
                        Integer.parseInt(uriAppointment0.getLastPathSegment())),
                updateValues, null, null), 3);
        ContentValues updateValues2 = new ContentValues();
        updateValues2.put(DBContract.PropositionsEntry.COLUMN_PLACE_NAME, "Warsaw");
        assertEquals(getMockContentResolver().update(
                DBContract.PropositionsEntry.buildUriForAppointment(
                        Integer.parseInt(uriAppointment0.getLastPathSegment())),
                updateValues2, DBContract.PropositionsEntry.COLUMN_CREATOR + " IS NULL", null), 1);
        c = getMockContentResolver().query(
                DBContract.PropositionsEntry.buildUriForAppointment(
                        Integer.parseInt(uriAppointment0.getLastPathSegment())),
                projection, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        do {
            if(c.getString(0).equals(uri0.getLastPathSegment())) {
                ptr = values0;
                assertEquals(c.getString(5), updateValues.getAsString(DBContract.PropositionsEntry.COLUMN_PLACE_NAME));
            } else if(c.getString(0).equals(uri1.getLastPathSegment())) {
                ptr = values1;
                assertEquals(c.getString(5), updateValues2.getAsString(DBContract.PropositionsEntry.COLUMN_PLACE_NAME));
            } else if(c.getString(0).equals(uri2.getLastPathSegment())) {
                ptr = values2;
                assertEquals(c.getString(5), updateValues.getAsString(DBContract.PropositionsEntry.COLUMN_PLACE_NAME));
            } else
                Assert.fail("Unknown id '" + c.getString(0) + "'");
            assertEquals(c.getString(1), ptr.getAsString(DBContract.PropositionsEntry.COLUMN_APPOINTMENT));
            assertEquals(c.getString(2), ptr.getAsString(DBContract.PropositionsEntry.COLUMN_CREATOR));
            assertEquals(c.getString(3), ptr.getAsString(DBContract.PropositionsEntry.COLUMN_TIMESTAMP));
            assertEquals(c.getString(4), ptr.getAsString(DBContract.PropositionsEntry.COLUMN_REASON));
            assertEquals(c.getDouble(6), ptr.getAsDouble(DBContract.PropositionsEntry.COLUMN_PLACE_LAT));
            assertEquals(c.getDouble(7), ptr.getAsDouble(DBContract.PropositionsEntry.COLUMN_PLACE_LON));
        } while(c.moveToNext());
        c.close();

        // Check delete
        assertEquals(
                getMockContentResolver().delete(
                        DBContract.PropositionsEntry.buildUriForAppointment(uriAppointment0.getLastPathSegment()),
                        DBContract.PropositionsEntry._ID + " = ?",
                        new String[]{uri1.getLastPathSegment()}), 1);
        assertEquals(
                getMockContentResolver().delete(
                        DBContract.PropositionsEntry.buildUriForAppointment(uriAppointment0.getLastPathSegment()),
                        null, null), 2);
        c = getMockContentResolver().query(
                DBContract.PropositionsEntry.buildUriForAppointment(uriAppointment0.getLastPathSegment()),
                projection, null, null, null);
        assertNotNull(c);
        assertEquals(c.getCount(), 0);
        c.close();

        // Check types
        String type = getMockContentResolver().getType(DBContract.PropositionsEntry.CONTENT_URI);
        assertEquals(type, DBContract.PropositionsEntry.CONTENT_TYPE);
        // Not equals to other content type
        assertNotSame(type, DBContract.AppointmentsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.AppointmentTypesEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.ReasonsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.UsersEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.InvitationsEntry.CONTENT_TYPE);
        // Not any item type
        assertNotSame(type, DBContract.UsersEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.PropositionsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentTypesEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.ReasonsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.InvitationsEntry.CONTENT_ITEM_TYPE);
    }

    @Test
    public void testInvitations() {
        // Insert three users, a type, a reason and three appointments
        ContentValues values = new ContentValues();
        values.put(DBContract.UsersEntry._ID, 1);
        values.put(DBContract.UsersEntry.COLUMN_NAME, "A");
        values.put(DBContract.UsersEntry.COLUMN_PHONE, "1");
        values.put(DBContract.UsersEntry.COLUMN_PICTURE, "1");
        Uri uriUser0 = getMockContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values);
        assertNotNull(uriUser0);
        values.clear();
        values.put(DBContract.UsersEntry._ID, 2);
        values.put(DBContract.UsersEntry.COLUMN_NAME, "B");
        values.put(DBContract.UsersEntry.COLUMN_PHONE, "2");
        values.put(DBContract.UsersEntry.COLUMN_PICTURE, "1");
        Uri uriUser1 = getMockContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values);
        assertNotNull("insert2 returned null", uriUser1);
        values.clear();
        values.put(DBContract.UsersEntry._ID, 3);
        values.put(DBContract.UsersEntry.COLUMN_NAME, "C");
        values.put(DBContract.UsersEntry.COLUMN_PHONE, "3");
        values.put(DBContract.UsersEntry.COLUMN_PICTURE, "1");
        Uri uriUser2 = getMockContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values);
        assertNotNull("insert2 returned null", uriUser2);
        values.clear();
        values.put(DBContract.AppointmentTypesEntry.COLUMN_NAME, "A");
        values.put(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION, "D");
        values.put(DBContract.AppointmentTypesEntry.COLUMN_ICON, "1");
        Uri uriType0 = getMockContentResolver().insert(DBContract.AppointmentTypesEntry.CONTENT_URI, values);
        assertNotNull(uriType0);
        values.clear();
        values.put(DBContract.ReasonsEntry.COLUMN_NAME, "A");
        values.put(DBContract.ReasonsEntry.COLUMN_DESCRIPTION, "D");
        Uri uriReason0 = getMockContentResolver().insert(DBContract.ReasonsEntry.CONTENT_URI, values);
        assertNotNull(uriReason0);
        values.clear();
        values.put(DBContract.AppointmentsEntry._ID, "1");
        values.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, "1"); // There is no foreign key check
        values.put(DBContract.AppointmentsEntry.COLUMN_TYPE, uriType0.getLastPathSegment());
        values.put(DBContract.AppointmentsEntry.COLUMN_CREATOR, uriUser0.getLastPathSegment()); // It's important the creator not to be null
        values.put(DBContract.AppointmentsEntry.COLUMN_CLOSED, "0");
        values.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, "D");
        values.put(DBContract.AppointmentsEntry.COLUMN_NAME, "A");
        Uri uriAppointment0 = getMockContentResolver().insert(DBContract.AppointmentsEntry.CONTENT_URI, values);
        assertNotNull(uriAppointment0);
        values.clear();
        values.put(DBContract.AppointmentsEntry._ID, "2");
        values.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, "1"); // There is no foreign key check
        values.put(DBContract.AppointmentsEntry.COLUMN_TYPE, uriType0.getLastPathSegment());
        values.put(DBContract.AppointmentsEntry.COLUMN_CREATOR, uriUser0.getLastPathSegment()); // It's important the creator not to be null
        values.put(DBContract.AppointmentsEntry.COLUMN_CLOSED, "0");
        values.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, "D");
        values.put(DBContract.AppointmentsEntry.COLUMN_NAME, "B");
        Uri uriAppointment1 = getMockContentResolver().insert(DBContract.AppointmentsEntry.CONTENT_URI, values);
        assertNotNull(uriAppointment1);
        values.clear();
        values.put(DBContract.AppointmentsEntry._ID, "3");
        values.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, "1"); // There is no foreign key check
        values.put(DBContract.AppointmentsEntry.COLUMN_TYPE, uriType0.getLastPathSegment());
        values.put(DBContract.AppointmentsEntry.COLUMN_CREATOR, uriUser0.getLastPathSegment()); // It's important the creator not to be null
        values.put(DBContract.AppointmentsEntry.COLUMN_CLOSED, "0");
        values.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, "D");
        values.put(DBContract.AppointmentsEntry.COLUMN_NAME, "C");
        Uri uriAppointment2 = getMockContentResolver().insert(DBContract.AppointmentsEntry.CONTENT_URI, values);
        assertNotNull(uriAppointment2);

        // Preparing stuff
        String[] usersProjection = {
                DBContract.UsersEntry.TABLE_NAME + "." + DBContract.UsersEntry._ID};
        String[] appointmentsProjection = {
                DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry._ID};
        String[] invitationsProjection = {
                DBContract.InvitationsEntry.TABLE_NAME  + "." + DBContract.InvitationsEntry._ID,
                DBContract.InvitationsEntry.TABLE_NAME  + "." + DBContract.InvitationsEntry.COLUMN_USER,
                DBContract.InvitationsEntry.TABLE_NAME  + "." + DBContract.InvitationsEntry.COLUMN_APPOINTMENT,
                DBContract.InvitationsEntry.TABLE_NAME  + "." + DBContract.InvitationsEntry.COLUMN_REASON,
                DBContract.InvitationsEntry.TABLE_NAME  + "." + DBContract.InvitationsEntry.COLUMN_STATE};

        // Checking insertion
        ContentValues values0 = new ContentValues();
        values0.put(DBContract.InvitationsEntry.COLUMN_USER, uriUser1.getLastPathSegment());
        values0.put(DBContract.InvitationsEntry.COLUMN_APPOINTMENT, uriAppointment0.getLastPathSegment());
        values0.put(DBContract.InvitationsEntry.COLUMN_REASON, uriReason0.getLastPathSegment());
        values0.put(DBContract.InvitationsEntry.COLUMN_STATE, "pending");
        Uri uri0 = getMockContentResolver().insert(DBContract.InvitationsEntry.CONTENT_URI, values0);
        assertNotNull(uri0);
        ContentValues values1 = new ContentValues();
        values1.put(DBContract.InvitationsEntry.COLUMN_USER, uriUser2.getLastPathSegment());
        values1.put(DBContract.InvitationsEntry.COLUMN_APPOINTMENT, uriAppointment0.getLastPathSegment());
        // No reason
        values1.put(DBContract.InvitationsEntry.COLUMN_STATE, "pending");
        Uri uri1 = getMockContentResolver().insert(DBContract.InvitationsEntry.CONTENT_URI, values1);
        assertNotNull(uri1);
        ContentValues values2 = new ContentValues();
        // No user (owner of the session)
        values2.put(DBContract.InvitationsEntry.COLUMN_APPOINTMENT, uriAppointment0.getLastPathSegment());
        values2.put(DBContract.InvitationsEntry.COLUMN_REASON, uriReason0.getLastPathSegment());
        values2.put(DBContract.InvitationsEntry.COLUMN_STATE, "pending");
        Uri uri2 = getMockContentResolver().insert(DBContract.InvitationsEntry.CONTENT_URI, values2);
        assertNotNull(uri2);
        ContentValues values3 = new ContentValues();
        // No user (owner of the session)
        values3.put(DBContract.InvitationsEntry.COLUMN_APPOINTMENT, uriAppointment1.getLastPathSegment());
        values3.put(DBContract.InvitationsEntry.COLUMN_REASON, uriReason0.getLastPathSegment());
        values3.put(DBContract.InvitationsEntry.COLUMN_STATE, "accepted");
        Uri uri3 = getMockContentResolver().insert(DBContract.InvitationsEntry.CONTENT_URI, values3);
        assertNotNull(uri3);
        ContentValues values4 = new ContentValues();
        // No user (owner of the session)
        values4.put(DBContract.InvitationsEntry.COLUMN_APPOINTMENT, uriAppointment2.getLastPathSegment());
        values4.put(DBContract.InvitationsEntry.COLUMN_REASON, uriReason0.getLastPathSegment());
        values4.put(DBContract.InvitationsEntry.COLUMN_STATE, "refused");
        Uri uri4 = getMockContentResolver().insert(DBContract.InvitationsEntry.CONTENT_URI, values4);
        assertNotNull(uri4);

        // Checking querying
        // Check /invitations/
        Cursor c = getMockContentResolver().query(DBContract.InvitationsEntry.CONTENT_URI,
                invitationsProjection, null, null, null);
        assertNotNull(c);
        assertEquals(c.getCount(), 5);
        assertTrue(c.moveToFirst());
        ContentValues ptr;
        do {
            if(c.getString(0).equals(uri0.getLastPathSegment()))
                ptr = values0;
            else if(c.getString(0).equals(uri1.getLastPathSegment()))
                ptr = values1;
            else if(c.getString(0).equals(uri2.getLastPathSegment()))
                ptr = values2;
            else if(c.getString(0).equals(uri3.getLastPathSegment()))
                ptr = values3;
            else if(c.getString(0).equals(uri4.getLastPathSegment()))
                ptr = values4;
            else {
                Assert.fail("Unknown id '" + c.getString(0) + "'");
                return;
            }

            assertEquals(c.getString(1), ptr.getAsString(DBContract.InvitationsEntry.COLUMN_USER));
            assertEquals(c.getString(2), ptr.getAsString(DBContract.InvitationsEntry.COLUMN_APPOINTMENT));
            assertEquals(c.getString(3), ptr.getAsString(DBContract.InvitationsEntry.COLUMN_REASON));
            assertEquals(c.getString(4), ptr.getAsString(DBContract.InvitationsEntry.COLUMN_STATE));
        } while (c.moveToNext());
        c.close();
        // Check /appointments/pending
        c = getMockContentResolver().query(
                DBContract.AppointmentsEntry.CONTENT_PENDING_URI,
                appointmentsProjection, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals(c.getString(0), uriAppointment0.getLastPathSegment());
        c.close();
        // Check /appointments/accepted
        c = getMockContentResolver().query(
                DBContract.AppointmentsEntry.CONTENT_ACCEPTED_URI,
                appointmentsProjection, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals(c.getString(0), uriAppointment1.getLastPathSegment());
        c.close();
        // Check /appointments/refused
        c = getMockContentResolver().query(
                DBContract.AppointmentsEntry.CONTENT_REFUSED_URI,
                appointmentsProjection, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals(c.getString(0), uriAppointment2.getLastPathSegment());
        c.close();
        // Check /users/invited_to/#
        c = getMockContentResolver().query(
                DBContract.UsersEntry.buildUriInvitedTo(uriAppointment0.getLastPathSegment()),
                usersProjection, null, null, null);
        assertNotNull(c);
        assertEquals(c.getCount(), 3);
        assertTrue(c.moveToFirst());
        do {
            // Remember that uriUser0 is the creator, the other two are invited
            assertTrue(c.getString(0).equals(uriUser0.getLastPathSegment()) ||
                    c.getString(0).equals(uriUser1.getLastPathSegment()) ||
                    c.getString(0).equals(uriUser2.getLastPathSegment()));
        } while(c.moveToNext());
        c.close();

        // Check update invitation
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(DBContract.InvitationsEntry.COLUMN_STATE, "refused");
        assertEquals(getMockContentResolver().update(
                DBContract.InvitationsEntry.CONTENT_URI, updatedValues, null, null), 5);
        c = getMockContentResolver().query(DBContract.InvitationsEntry.CONTENT_URI,
                invitationsProjection, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        do {
            assertEquals(c.getString(4), "refused");
        } while (c.moveToNext());
        c.close();

        // Check invitation delete
        assertEquals(getMockContentResolver().delete(
                DBContract.InvitationsEntry.CONTENT_URI,
                DBContract.InvitationsEntry._ID + "=?",
                new String[]{uri0.getLastPathSegment()}), 1);
        assertEquals(getMockContentResolver().delete(
                DBContract.InvitationsEntry.CONTENT_URI, null, null), 4);
        c = getMockContentResolver().query(DBContract.InvitationsEntry.CONTENT_URI,
                invitationsProjection, null, null, null);
        assertNotNull(c);
        assertEquals(c.getCount(), 0);
        c.close();

        // Check types
        String type = getMockContentResolver().getType(DBContract.InvitationsEntry.CONTENT_URI);
        assertEquals(type, DBContract.InvitationsEntry.CONTENT_TYPE);
        // Not equals to other content type
        assertNotSame(type, DBContract.AppointmentsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.AppointmentTypesEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.ReasonsEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.UsersEntry.CONTENT_TYPE);
        assertNotSame(type, DBContract.PropositionsEntry.CONTENT_TYPE);
        // Not any item type
        assertNotSame(type, DBContract.UsersEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.PropositionsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.AppointmentTypesEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.ReasonsEntry.CONTENT_ITEM_TYPE);
        assertNotSame(type, DBContract.InvitationsEntry.CONTENT_ITEM_TYPE);
    }
}
