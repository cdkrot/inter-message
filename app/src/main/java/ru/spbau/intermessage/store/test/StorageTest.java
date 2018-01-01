package ru.spbau.intermessage.store.test;

import org.junit.Assert;

import java.util.List;

import ru.spbau.intermessage.store.IStorage;
import ru.spbau.intermessage.store.IStorage.Union;
import ru.spbau.intermessage.store.IStorage.IList;
import ru.spbau.intermessage.store.Storage;

import static org.junit.Assert.*;

public class StorageTest {

    private static Storage store;

    public static void test() {
        testCreate();
        testGet();
        testAdd();
        testDel();
        testGetMatching();
        testGetList();
        testListPush();
        testListSize();
    }

    private static void testCreate() {
        store = new Storage();
    }

    private static void testGet() {
        Union u = store.get("ks");
        Assert.assertEquals(IStorage.ObjectType.NULL, u.getType());
    }

    private static void testAdd() {
        Union u = store.get("ks");
        u.setInt(67);
        u = store.get("ks");
        assertEquals(IStorage.ObjectType.INTEGER, u.getType());
        assertEquals(67, u.getInt());

        u.setString("abacaba");
        assertEquals(IStorage.ObjectType.STRING, u.getType());
        assertEquals("abacaba", u.getString());
    }

    private static void testDel() {
        Union u = store.get("ks");
        u.setInt(67);
        u = store.get("ks");
        u.setNull();

        u = store.get("ks");
        assertEquals(IStorage.ObjectType.NULL, u.getType());
    }

    private static void testGetMatching() {
        Union u1 = store.get("ks");
        Union u2 = store.get("ks.sdfs");
        Union u3 = store.get("dsks");
        u1.setInt(46);
        byte[] data = {0, 1, 2};
        u2.setData(data);
        u3.setString("fd");

        List<String> list = store.getMatching("ks");
        assertEquals(2, list.size());
        assertEquals("ks", list.get(0));
        assertEquals("ks.sdfs", list.get(1));

        u1.setNull();
        u2.setNull();
        u3.setNull();;
    }

    private static void testGetList() {
        IList list = store.getList("abc");
    }

    private static void testListPush() {
        IList list = store.getList("abc");
        byte[] data = {0, 1, 2};
        list.push(data);
        list.push(34);
        list.push("dfgd");

        Union u;
        u = list.get(0);
        assertEquals(IStorage.ObjectType.BYTE_ARRAY, u.getType());
        assertArrayEquals(data, u.getData());

        u = list.get(1);
        assertEquals(IStorage.ObjectType.INTEGER, u.getType());
        assertEquals(34, u.getInt());

        u = list.get(2);
        assertEquals(IStorage.ObjectType.STRING, u.getType());
        assertEquals("dfgd", u.getString());

        list.delete();
    }

    private static void testListSize() {
        IList list = store.getList("abc");
        byte[] data = {0, 1, 2};
        assertEquals(0, list.size());

        list.push(data);
        assertEquals(1, list.size());

        list.push(34);
        assertEquals(2, list.size());

        list.push("dfgd");
        assertEquals(3, list.size());

        list.delete();
    }
}
