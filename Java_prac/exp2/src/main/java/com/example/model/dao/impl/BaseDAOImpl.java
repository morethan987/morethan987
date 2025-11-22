package com.example.model.dao.impl;

import java.io.File;

public abstract class BaseDAOImpl {

    protected static final String DB_URL = "jdbc:sqlite:data/app.db";

    protected BaseDAOImpl() {
        ensureDatabaseDirectoryExists();
    }

    private void ensureDatabaseDirectoryExists() {
        String path = DB_URL.replace("jdbc:sqlite:", "");
        File dbFile = new File(path);
        File parentDir = dbFile.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                throw new RuntimeException(
                    "数据库目录创建失败: " + parentDir.getAbsolutePath()
                );
            }
        }
    }
}
