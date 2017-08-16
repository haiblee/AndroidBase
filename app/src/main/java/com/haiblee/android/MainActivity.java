package com.haiblee.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseArray;

import com.haiblee.base.database.AbstractDatabase;
import com.haiblee.base.database.ITable;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public static class TestDatabase extends AbstractDatabase {
        private static TestDatabase INSTANCE = null;

        public static TestDatabase open(){
            if(INSTANCE == null){
                synchronized (TestDatabase.class){
                    if(INSTANCE == null){
                        INSTANCE = new TestDatabase();
                    }
                }
            }
            return INSTANCE;
        }

        private TestDatabase() {
            super(App.getContext(), "test_db.db");
        }

        @Override
        public SparseArray<ITable[]> staticTables() {
            return null;
        }

        @Override
        public int databaseVersion() {
            return 0;
        }
    }
}
