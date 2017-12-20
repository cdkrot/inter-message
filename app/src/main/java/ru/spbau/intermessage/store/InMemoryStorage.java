package ru.spbau.intermessage.store;

import java.util.*;

public class InMemoryStorage implements IStorage {
    private static class ObjectContainer {
        public ObjectContainer() {data = null; type = IStorage.ObjectType.NULL;}
        public ObjectContainer(Object dta, IStorage.ObjectType tp) {data = dta; type = tp;}
        
        public Object data;
        public IStorage.ObjectType type;
    };

    private static class ListContainer {
        public ListContainer() {}
        
        public ArrayList<ObjectContainer> data = new ArrayList<ObjectContainer>();
    };
    
    private static abstract class UnionBase implements Union {
        protected abstract ObjectContainer fetch();
        protected abstract ObjectContainer forceFetch();
        protected abstract void delete();
        
        @Override
        public IStorage.ObjectType getType() {
            ObjectContainer c = fetch();
            return (c == null ? IStorage.ObjectType.NULL : c.type);
        }
        
        @Override
        public String getString() {
            return (String)(fetch().data);
        }

        @Override
        public int getInt() {
            return (Integer)(fetch().data);
        }
        
        @Override
        public byte[] getData() {
            return (byte[])(fetch().data);
        }
        
        @Override
        public void setString(String s) {
            ObjectContainer c = forceFetch();
            c.type = IStorage.ObjectType.STRING;
            c.data = s;
        }
        
        @Override
        public void setInt(int i) {
            ObjectContainer c = forceFetch();
            c.type = IStorage.ObjectType.INTEGER;
            c.data = (Integer)i;
        }

        @Override
        public void setData(byte[] bt) {
            ObjectContainer c = forceFetch();
            c.type = IStorage.ObjectType.BYTE_ARRAY;
            c.data = bt;
        }
        
        @Override
        public void setNull() {
            delete();
        }
    };
    
    private TreeMap<String, ObjectContainer> kv = new TreeMap<String, ObjectContainer>();
    private TreeMap<String, ListContainer> lists = new TreeMap<String, ListContainer>();
    
    public InMemoryStorage() {}
    
    public Union get(final String key) {
        return new UnionBase() {
            protected ObjectContainer fetch() {
                return kv.get(key);
            }
            
            protected ObjectContainer forceFetch() {
                if (kv.get(key) == null)
                    kv.put(key, new ObjectContainer());
                return kv.get(key);
            }

            protected void delete() {
                kv.remove(key);
            }
        };
    }
    
    public List<String> getMatching(String group) {
        // proving that java is crap in yet another way.
        
        List<String> lst = new ArrayList<String>();

        NavigableMap<String, ObjectContainer> blad = kv.tailMap(group, true);
        Map.Entry<String, ObjectContainer> ent;
        while ((ent = blad.pollFirstEntry()) != null && ent.getKey().startsWith(group))
            lst.add(ent.getKey());
        
        return lst;
    }

    public IStorage.IList getList(final String key) {
        return new IList() {
            protected ListContainer fetch() {
                return lists.get(key);
            }

            protected ListContainer fetchXX() {
                return fetch();
            }
            
            protected ListContainer forceFetch() {
                if (lists.get(key) == null)
                    lists.put(key, new ListContainer());
                return lists.get(key);
            }

            public void delete() {
                lists.remove(key);
            }
            
            public int size() {
                ListContainer cont = fetch();
                return (cont == null ? 0 : cont.data.size());
            }

            public Union get(final int i) {
                return new UnionBase() {
                    protected ObjectContainer fetch() {
                        return fetchXX().data.get(i);
                    }
                    
                    protected ObjectContainer forceFetch() {
                        return fetch();
                    }
                    
                    protected void delete() {
                        fetch().data = null;
                        fetch().type = IStorage.ObjectType.NULL;
                    }  
                };
            }

            public Union[] getBatch(int i, int cnt) {
                throw new UnsupportedOperationException();
            }
            
            public void push() {
                forceFetch().data.add(new ObjectContainer());
            }
            
            public void push(int value) {
                push();
                get(fetch().data.size() - 1).setInt(value);
            }

            public void push(String value) {
                push();
                get(fetch().data.size() - 1).setString(value);
            }

            public void push(byte[] value) {
                push();
                get(fetch().data.size() - 1).setData(value);
            }
        };
    }
}
