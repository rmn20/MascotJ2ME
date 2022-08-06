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

public class Figure {
	Model data;
	Texture[] textures;
	int selectedTex = -1;
	int currentPattern;

	public Figure(byte[] b) {
		if (b == null) {
			throw new NullPointerException();
		}
		try {
			init(b);
		} catch (Exception e) {
			System.out.println(Util3D.TAG + " Error loading data");
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public Figure(String name) throws IOException {
		DataInputStream io = new DataInputStream((new Object()).getClass().getResourceAsStream(name));
		byte[] bytes = new byte[io.available()];
        io.readFully(bytes);
		
		if (bytes == null) {
			throw new IOException("Error reading resource: " + name);
		}
		try {
			init(bytes);
		} catch (Exception e) {
			System.out.println(Util3D.TAG + " Error loading data from [" + name + "]");
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	private synchronized void init(byte[] bytes) throws IOException {
		data = Loader.loadMbacData(bytes);
		Utils.transform(data.originalVertices, data.vertices,
				data.originalNormals, data.normals, data.bones, null);
		//fillTexCoordBuffer();
	}

	public final void dispose() {
		data = null;
	}

	public synchronized final void setPosture(ActionTable actionTable, int action, int frame) {
		if (actionTable == null) {
			throw new NullPointerException();
		} else if (action < 0 || action >= actionTable.getNumActions()) {
			throw new IllegalArgumentException();
		}
		Action act = actionTable.actions[action];
		final int[] dynamic = act.dynamic;
		
		if (dynamic != null) {
			int iFrame = frame < 0 ? 0 : frame >> 16;
			for (int i = dynamic.length / 2 - 1; i >= 0; i--) {
				if (dynamic[i * 2] <= iFrame) {
					currentPattern = dynamic[i * 2 + 1];
					applyPattern();
					break;
				}
			}
		}
		//noinspection ManualMinMaxCalculation
		applyBoneAction(act, frame < 0 ? 0 : frame);
	}

	private void applyPattern() {
		int[] indexArray = data.indices;
		int pos = 0;
		int invalid = data.numVertices / 3 - 1;
		
		for (int i=0; i<data.polygonsT.length; i++) {
			Model.Polygon p = data.polygonsT[i];
			int[] indices = p.indices;
			int length = indices.length;
			int pp = p.pattern;
			
			if ((pp & currentPattern) == pp) {
				for (int t = 0; t < length; t++) {
					indexArray[pos++] = indices[t];
				}
			} else {
				while (length > 0) {
					indexArray[pos++] = invalid;
					length--;
				}
			}
		}

		for (int i=0; i<data.polygonsC.length; i++) {
			Model.Polygon p = data.polygonsC[i];
			int[] indices = p.indices;
			int length = indices.length;
			int pp = p.pattern;
			
			if ((pp & currentPattern) == pp) {
				for (int t = 0; t < length; t++) {
					indexArray[pos++] = indices[t];
				}
			} else {
				while (length > 0) {
					indexArray[pos++] = invalid;
					length--;
				}
			}
		}
	}

	public final Texture getTexture() {
		if (selectedTex < 0) {
			return null;
		}
		return textures[selectedTex];
	}

	public final void setTexture(Texture tex) {
		if (tex == null)
			throw new NullPointerException();
		if (tex.isSphere)
			throw new IllegalArgumentException();

		textures = new Texture[]{tex};
		selectedTex = 0;
	}

	public final void setTexture(Texture[] t) {
		if (t == null) throw new NullPointerException();
		if (t.length == 0) throw new IllegalArgumentException();
		for (int i=0; i<t.length; i++) {
			Texture texture = t[i];
			if (texture == null) throw new NullPointerException();
			if (texture.isSphere) throw new IllegalArgumentException();
		}
		textures = t;
		selectedTex = -1;
	}

	public final int getNumTextures() {
		if (textures == null) {
			return 0;
		}
		return textures.length;
	}

	public final void selectTexture(int idx) {
		if (idx < 0 || idx >= getNumTextures()) {
			throw new IllegalArgumentException();
		}
		selectedTex = idx;
	}

	public final int getNumPattern() {
		return data.numPatterns;
	}

	public synchronized final void setPattern(int idx) {
		currentPattern = idx;
		applyPattern();
	}

	private void applyBoneAction(Action act, int frame) {
		Action.Bone[] actionBones = act.boneActions;
		if (actionBones.length == 0) return;
		synchronized (act.matrices) {
			for (int i=0; i<actionBones.length; i++) {
				Action.Bone actionBone = actionBones[i];
				actionBone.setFrame(frame);
			}
			//todo
			Utils.transform(data.originalVertices, data.vertices,
					data.originalNormals, data.normals, data.bones, act.matrices);
		}
	}
	/*private void fillTexCoordBuffer() {
		ByteBuffer buffer = data.texCoordArray;
		buffer.rewind();
		for (Model.Polygon poly : data.polygonsT) {
			buffer.put(poly.texCoords);
			poly.texCoords = null;
		}
		for (Model.Polygon poly : data.polygonsC) {
			buffer.put(poly.texCoords);
			poly.texCoords = null;
		}
		buffer.rewind();
	}

	synchronized FloatBuffer getVertexData() {
		if (data.vertexArray == null) {
			data.vertexArray = ByteBuffer.allocateDirect(data.vertexArrayCapacity).asFloatBuffer();
		}
		Utils.fillBuffer(data.vertexArray, data.vertices, data.indices);
		return data.vertexArray;
	}

	synchronized FloatBuffer getNormalsData() {
		if (data.originalNormals == null) {
			return null;
		}
		if (data.normalsArray == null) {
			data.normalsArray = ByteBuffer.allocateDirect(data.vertexArrayCapacity)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
		}
		Utils.fillBuffer(data.normalsArray, data.normals, data.indices);
		return data.normalsArray;
	}*/
}


