/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.spacelift.tool.basic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;

import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.execution.InvalidSetupException;
import org.arquillian.spacelift.tool.Tool;

/**
 * File downloader
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class DownloadTool extends Tool<Object, File> {

    private URL url;

    private File output;

    @Override
    public Collection<String> aliases() {
        return Arrays.asList("download");
    }

    public DownloadTool from(String url) throws IllegalArgumentException {
        try {
            return from(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public DownloadTool from(URL url) {
        this.url = url;
        return this;
    }

    public DownloadTool to(String filePath) {
        return to(new File(filePath));
    }

    public DownloadTool to(File file) {
        this.output = file;
        return this;
    }

    @Override
    protected File process(Object input) throws Exception {

        InputStream is = null;
        FileOutputStream fos = null;

        try {
            try {
                URLConnection urlConn = url.openConnection();// connect

                is = urlConn.getInputStream(); // get connection inputstream
                fos = new FileOutputStream(output); // open outputstream to local file

                byte[] buffer = new byte[4096]; // declare 4KB buffer
                int len;

                // while we have availble data, continue downloading and storing to local file
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }
            }
        } catch (IOException e) {
            throw new ExecutionException(e, "Unable to download from {0} to {1}", url, output);
        }

        return output;
    }

    @Override
    protected void validate() throws InvalidSetupException {
        if (url == null) {
            throw new InvalidSetupException("Source URL was not set");
        }
        if (output == null) {
            throw new InvalidSetupException("Destination file was not set");
        }
    }

}
