/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2014 Jean-Noël Rouvignac - initial API and implementation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program under LICENSE-GNUGPL.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution under LICENSE-ECLIPSE, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.autorefactor.cfg.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestUtils {

    public static String readAll(File file) throws IOException {
        // FIXME Java 7 version of this method:
        // return new String(Files.readAllBytes(file.toPath()), "UTF8");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            final InputStreamReader reader = new InputStreamReader(fis);
            final StringBuilder sb = new StringBuilder();
            final char[] buf = new char[4096];
            int nbRead;
            while ((nbRead = reader.read(buf)) != -1) {
                sb.append(buf, 0, nbRead);
            }
            return sb.toString();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

}
