package ru.spbau.intermessage.store;

import java.util.List;

public interface IStorage {
    public enum ObjectType {
        NULL(0),
        STRING(1),
        BYTE_ARRAY(2),
        INTEGER(3);

        public final int id;
        private ObjectType(int theId) {
            id = theId;
        }
    };
    
    public interface IObject {
        public ObjectType getType();
        
        public String getString();
        public byte[] getData();
        public int getInt();

        public void setString(String str);
        public void setData(byte[] data);
        public void setInt(int nt);
        public void setNull();
    };

    public IObject get(String key);
    public List<String> getMatching(String group);
}
