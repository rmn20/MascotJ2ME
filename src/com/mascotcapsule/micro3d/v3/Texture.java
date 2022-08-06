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

public class Texture {
	private static final int BMP_FILE_HEADER_SIZE = 14;
	private static final int BMP_VERSION_3 = 40;
	private static final int BMP_VERSION_CORE = 12;

	boolean isSphere;

	private int[] palette;
	private byte[] index;
	int width, height;

	public Texture(byte[] b, boolean isForModel) {
		if (b == null) {
			throw new NullPointerException();
		}
		isSphere = !isForModel;
		prepare(b);
	}

	public Texture(String name, boolean isForModel) throws IOException {
		this(getData(name), isForModel);
	}

	private static byte[] getData(String name) throws IOException {
		if (name == null) {
			throw new NullPointerException();
		}
		
        DataInputStream io = new DataInputStream((new Object()).getClass().getResourceAsStream(name));
		byte[] b = new byte[io.available()];
        io.readFully(b);
		
		if (b == null) throw new IOException();
		return b;
	}

	private void prepare(byte[] bytes) {
		if (bytes[0] != 'B' || bytes[1] != 'M') {
			throw new RuntimeException("Not a BMP!");
		}
		
		int bInfoOffset = BMP_FILE_HEADER_SIZE;
		int bInfoSize = bytes[bInfoOffset++] & 0xFF | (bytes[bInfoOffset++] & 0xFF) << 8
				| (bytes[bInfoOffset++] & 0xFF) << 16 | (bytes[bInfoOffset] & 0xFF) << 24;

		if (bInfoSize < BMP_VERSION_CORE || bInfoSize > BMP_VERSION_3) {
			throw new RuntimeException("Unsupported BMP version = " + bInfoSize);
		}
		
		int bpp;
		int paletteSize;
		if (bInfoSize == BMP_VERSION_CORE) {
			width = (bytes[18] & 0xff) | (bytes[19] & 0xff) << 8;
			height = (bytes[20] & 0xff) | (bytes[21] & 0xff) << 8;
			
			bpp = bytes[24] | bytes[25] << 8;
			paletteSize = 256;
		} else {
			width = (bytes[18] & 0xff) | ((bytes[19] & 0xff) << 8) | ((bytes[20] & 0xff) << 16) | ((bytes[21] & 0xff) << 24);
			height = (bytes[22] & 0xff) | ((bytes[23] & 0xff) << 8) | ((bytes[24] & 0xff) << 16) | ((bytes[25] & 0xff) << 24);
			
			bpp = bytes[28] | bytes[29] << 8;
			paletteSize = bytes[0x2e] & 0xFF | (bytes[0x2f] & 0xFF) << 8
					| (bytes[0x30] & 0xFF) << 16 | (bytes[0x31] & 0xFF) << 24;
			if (paletteSize == 0) {
				paletteSize = 256;
			}
			int usedPaletteSize = bytes[0x32] & 0xFF | (bytes[0x33] & 0xFF) << 8
					| (bytes[0x34] & 0xFF) << 16 | (bytes[0x35] & 0xFF) << 24;
			if (usedPaletteSize > 0 && usedPaletteSize < paletteSize) {
				paletteSize = usedPaletteSize;
			}
		}
		if (bpp != 8) { // supports only 8-bit per pixel format
			throw new RuntimeException("Unsupported BMP format: bpp = " + bpp);
		}
		int paletteOffset = bInfoSize + BMP_FILE_HEADER_SIZE;
		// get first color in palette

		palette = new int[paletteSize];
		for (int i = 0; i < palette.length; i++) {
			palette[i] = bytes[paletteOffset++] & 0xFF | (bytes[paletteOffset++] & 0xFF) << 8
					| (bytes[paletteOffset++] & 0xFF) << 16;
			paletteOffset++;
		}
		
		paletteOffset = bInfoSize + BMP_FILE_HEADER_SIZE;
		// get first color in palette
		
		index = new byte[width * height];
		
		for(int i=0; i<index.length; i++) {
			int x = i % width;
			int y = height - (i / width) - 1;
			index[x + y * width] = bytes[paletteOffset++];
		}
	}
}
