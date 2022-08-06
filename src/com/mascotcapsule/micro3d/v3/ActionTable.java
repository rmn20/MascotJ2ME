/*
 * Copyright 2020 Yury Kharchenko
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

package com.mascotcapsule.micro3d.v3;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ActionTable {
	Action[] actions;

	public ActionTable(byte[] b) {
		if (b == null) {
			throw new NullPointerException();
		}
		try {
			actions = Loader.loadMtraData(b);
		} catch (IOException e) {
			System.out.println(Util3D.TAG + " Error loading data");
                        e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public ActionTable(String name) throws IOException {
		if (name == null) {
			throw new NullPointerException();
		}
		
        DataInputStream io = new DataInputStream((new Object()).getClass().getResourceAsStream(name));
		byte[] bytes = new byte[io.available()];
        io.readFully(bytes);
                
		if (bytes == null) {
			throw new IOException();
		}
		try {
			actions = Loader.loadMtraData(bytes);
		} catch (IOException e) {
			System.out.println(Util3D.TAG + " Error loading data from [" + name + "]");
                        e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public final void dispose() {
		actions = null;
	}

	public final int getNumAction() {
		return getNumActions();
	}

	public final int getNumActions() {
		checkDisposed();
		return actions.length;
	}

	public final int getNumFrame(int idx) {
		return getNumFrames(idx);
	}

	public final int getNumFrames(int idx) {
		checkDisposed();
		if (idx < 0 || idx >= actions.length) {
			throw new IllegalArgumentException();
		}
		return actions[idx].keyframes << 16;
	}

	void checkDisposed() {
		if (actions == null) throw new IllegalStateException("ActionTable disposed!");
	}

}
