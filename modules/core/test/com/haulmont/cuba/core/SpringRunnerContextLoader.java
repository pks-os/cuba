/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.sys.AppComponents;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.AppContextLoader;
import com.haulmont.cuba.testsupport.TestContext;
import com.haulmont.cuba.testsupport.TestDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextLoader;

import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class SpringRunnerContextLoader extends AppContextLoader implements ContextLoader {

    protected Map<String, String> initAppProperties() {
        final Properties properties = new Properties();

        List<String> appPropertiesFiles = Arrays.asList(
                "com/haulmont/cuba/app.properties",
                "com/haulmont/cuba/testsupport/test-app.properties",
                "com/haulmont/cuba/test-app.properties");

        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        for (String location : appPropertiesFiles) {
            Resource resource = resourceLoader.getResource(location);
            if (resource.exists()) {
                InputStream stream = null;
                try {
                    stream = resource.getInputStream();
                    properties.load(stream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }
        }

        StrSubstitutor substitutor = new StrSubstitutor(new StrLookup() {
            @Override
            public String lookup(String key) {
                String subst = properties.getProperty(key);
                return subst != null ? subst : System.getProperty(key);
            }
        });


        Map<String, String> appProperties = new HashMap<>();

        for (Object key : properties.keySet()) {
            String value = substitutor.replace(properties.getProperty((String) key));
            appProperties.put((String) key, value);
        }

        File dir;
        dir = new File(appProperties.get("cuba.confDir"));
        dir.mkdirs();
        dir = new File(appProperties.get("cuba.logDir"));
        dir.mkdirs();
        dir = new File(appProperties.get("cuba.tempDir"));
        dir.mkdirs();
        dir = new File(appProperties.get("cuba.dataDir"));
        dir.mkdirs();

        return appProperties;
    }

    private void initCubaSpecific() {
        AppContext.Internals.setAppComponents(new AppComponents(Collections.emptyList(), "core"));

        Map<String, String> appProperties = initAppProperties();
        for (Map.Entry<String, String> entry : appProperties.entrySet()) {
            AppContext.setProperty(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String[] processLocations(Class<?> clazz, String... locations) {
        return locations;
    }

    @Override
    public ApplicationContext loadContext(String... locations) throws Exception {
        initCubaSpecific();
        initDataSources();

        return createApplicationContext(locations);
    }

    protected void initDataSources() {
        String dbDriver = "org.hsqldb.jdbc.JDBCDriver";
        String dbUrl = "jdbc:hsqldb:hsql://localhost:9111/cubadb";
        String dbUser = "sa";
        String dbPassword = "";

        try {
            Class.forName(dbDriver);
            TestDataSource ds = new TestDataSource(dbUrl, dbUser, dbPassword);
            TestContext.getInstance().bind(AppContext.getProperty("cuba.dataSourceJndiName"), ds);
        } catch (ClassNotFoundException | NamingException e) {
            throw new RuntimeException("Error initializing datasource", e);
        }
    }
}
