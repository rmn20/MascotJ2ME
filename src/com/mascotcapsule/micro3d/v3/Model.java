/*
 *  Copyright 2020 Yury Kharchenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.mascotcapsule.micro3d.v3;

class Model {

	final int numVertices;
	final int numPatterns;
	final int numTextures;
	final boolean hasPolyC;
	final boolean hasPolyT;

	/*FloatBuffer vertexArray;
	FloatBuffer normalsArray;
	FloatBuffer normals;
	final ByteBuffer texCoordArray;*/
	final short[] originalVertices;
	final short[] vertices; //used for animation
	
	byte[] originalNormals;
	byte[] normals; //used for animation
	
	final Polygon[] polygonsC;
	final Polygon[] polygonsT;
	int numVerticesPolyT;
	final int[] indices;
	final int[] bones;

	Model(int vertices, int numBones, int patterns, int numTextures,
		  int polyT3, int polyT4, int polyC3, int polyC4) {
		numPatterns = patterns;
		this.numTextures = numTextures;
		
		numVerticesPolyT = polyT3 * 3 + polyT4 * 6;
		numVertices = (polyT3 + polyC3) * 3 + (polyT4 + polyC4) * 6;
		
		indices = new int[numVertices];
		
		polygonsC = new Polygon[polyC3 + polyC4];
		polygonsT = new Polygon[polyT3 + polyT4];
		hasPolyT = polyT3 + polyT4 > 0;
		hasPolyC = polyC3 + polyC4 > 0;
		
		/*ByteOrder order = ByteOrder.nativeOrder();
		texCoordArray = ByteBuffer.allocateDirect(numVertices * 5).order(order);*/
		
		originalVertices = new short[vertices * 3];
		this.vertices = new short[vertices * 3];
		bones = new int[numBones * (12 + 2) * 2];
	}

	static final class Polygon {
		// polygon material flags
		static final int TRANSPARENT = 1;
		static final int BLEND_HALF = 2;
		static final int BLEND_ADD = 4;
		static final int BLEND_SUB = 6;
		private static final int DOUBLE_FACE = 16;
		static final int LIGHTING = 32;
		static final int SPECULAR = 64;
		
		final int[] indices;
		final int blendMode;
		final int doubleFace;
		byte[] texCoords;
		//r, g, b, light 0/1, specular 0/1
		//or
		//u, v, light 0/1, specular 0/1, trasparent 0/1
		int face = -1; //texture number?
		int pattern; //idk...

		Polygon(int material, byte[] texCoords, int[] indices) {
			this.indices = indices;
			this.texCoords = texCoords;
			doubleFace = (material & DOUBLE_FACE) >> 4;
			blendMode = (material & BLEND_SUB);
		}
	}
}
