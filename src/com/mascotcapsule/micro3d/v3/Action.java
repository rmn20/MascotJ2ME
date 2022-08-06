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

import com.mascotcapsule.micro3d.v3.Utils;

public class Action {
	final int keyframes;
	final Bone[] boneActions;
	final int[] matrices;
	int[] dynamic; //[frame, pattern]

	Action(int keyframes, int numBones) {
		this.keyframes = keyframes;
		this.boneActions = new Bone[numBones];
		this.matrices = new int[numBones * 12];
	}

	static final class Bone {
		private final int type;
		private final int mtxOffset;
		final int[] matrix;
		RollAnim roll;
		Animation rotate;
		Animation scale;
		Animation translate;
		private int frame = -1;

		Bone(int type, int mtxOffset, int[] matrix) {
			this.type = type;
			this.mtxOffset = mtxOffset;
			this.matrix = matrix;
		}

		void setFrame(int frame) {
			if (this.frame == frame) return;
			this.frame = frame;
			
			final int[] m = matrix;
			switch (type) {
				case 2: {
					System.arraycopy(Utils.IDENTITY_AFFINE, 0, m, mtxOffset, 12);
					int[] arr = new int[3];

					// translate
					translate.get(frame, arr);
					m[mtxOffset +  3] = arr[0];
					m[mtxOffset +  7] = arr[1];
					m[mtxOffset + 11] = arr[2];

					// rotate
					rotate.get(frame, arr);
					rotate(m, arr[0], arr[1], arr[2]);

					// roll
					final int r = roll.get(frame);
					roll(m, r);

					// scale
					scale.get(frame, arr);
					int x = arr[0];
					int y = arr[1];
					int z = arr[2];
					m[mtxOffset     ] = (m[mtxOffset     ] * x) >> 12;
					m[mtxOffset +  1] = (m[mtxOffset +  1] * y) >> 12;
					m[mtxOffset +  2] = (m[mtxOffset +  2] * z) >> 12;
					
					m[mtxOffset +  4] = (m[mtxOffset +  4] * x) >> 12;
					m[mtxOffset +  5] = (m[mtxOffset +  5] * y) >> 12;
					m[mtxOffset +  6] = (m[mtxOffset +  6] * z) >> 12;
					
					m[mtxOffset +  8] = (m[mtxOffset +  8] * x) >> 12;
					m[mtxOffset +  9] = (m[mtxOffset +  9] * y) >> 12;
					m[mtxOffset + 10] = (m[mtxOffset + 10] * z) >> 12;
					break;
				}
				case 3: {
					System.arraycopy(Utils.IDENTITY_AFFINE, 0, m, mtxOffset, 12);
					int[] arr = translate.cloneValues(0);

					// translate (for all frames)
					m[mtxOffset +  3] = arr[0];
					m[mtxOffset +  7] = arr[1];
					m[mtxOffset + 11] = arr[2];

					// rotate
					rotate.get(frame, arr);
					rotate(m, arr[0], arr[1], arr[2]);

					// roll (for all frames)
					final int r = roll.values[0];
					roll(m, r);
					break;
				}
				case 4: {
					System.arraycopy(Utils.IDENTITY_AFFINE, 0, m, mtxOffset, 12);
					int[] arr = new int[3];

					// rotate
					rotate.get(frame, arr);
					rotate(m, arr[0], arr[1], arr[2]);

					// roll
					final int r = roll.get(frame);
					roll(m, r);
					break;
				}
				case 5: {
					System.arraycopy(Utils.IDENTITY_AFFINE, 0, m, mtxOffset, 12);
					int[] arr = new int[3];

					// rotate
					rotate.get(frame, arr);
					rotate(m, arr[0], arr[1], arr[2]);
					break;
				}
				case 6: {
					System.arraycopy(Utils.IDENTITY_AFFINE, 0, m, mtxOffset, 12);
					int[] arr = new int[3];

					// translate
					translate.get(frame, arr);
					m[mtxOffset +  3] = arr[0];
					m[mtxOffset +  7] = arr[1];
					m[mtxOffset + 11] = arr[2];

					// rotate
					rotate.get(frame, arr);
					rotate(m, arr[0], arr[1], arr[2]);

					// roll
					final int r = roll.get(frame);
					roll(m, r);
					break;
				}
			}
		}

		/**
		 * Rotate matrix to new z-axis
		 *
		 * @param m destination matrix
		 * @param x X coord of new z-axis
		 * @param y Y coord of new z-axis
		 * @param z Y coord of new z-axis
		 */
		private void rotate(int[] m, int x, int y, int z) {
			if (x == 0.0f && y == 0.0f) {
				if (z < 0.0f) {// reverse (rotate 180 degrees around x-axis)
					m[mtxOffset + 5] = -4096;
					m[mtxOffset + 10] = -4096;
				} // else identity (no rotate)
				return;
			}
			// normalize direction vector
			int rld = (int) (4096 / Math.sqrt(x * x + y * y + z * z));
			x = (x * rld) >> 12;
			y = (y * rld) >> 12;
			z = (z * rld) >> 12;

			// compute rotate axis R = Z x Z' (x means "cross product")
			int rx = -y; // 0*z - 1*y
			int ry = x;  // 1*x - 0*z
			// rz = 0.0f   // 0*y - 0*x (inlined)

			// and normalize R
			int rls = (int) (4096 / Math.sqrt(rx * rx + ry * ry));
			rx = (rx * rls) >> 12;
			ry = (ry * rls) >> 12;

			// cos = z (inlined)
			// compute sin from cos
			int sin = (int) (4096 * Math.sqrt(1 - (z * z / 4096f / 4096f)));
			if (4096 == rx && 0 == ry) {
				m[mtxOffset +  5] = z;
				m[mtxOffset +  6] = -sin;
				m[mtxOffset +  9] =  sin;
				m[mtxOffset + 10] = z;
			} else if (0.0f == rx && 1.0f == ry) {
				m[mtxOffset] = z;
				m[mtxOffset +  2] = sin;
				m[mtxOffset +  8] = -sin;
				m[mtxOffset + 10] = z;
			} else {
				int nc = 4096 - z;
				int xy = (rx * ry) >> 12;
				int xs = (rx * sin) >> 12;
				int ys = (ry * sin) >> 12;
				m[mtxOffset] = ((((rx * rx) >> 12) * nc) >> 12) + z;
				m[mtxOffset +  1] = (xy * nc) >> 12;
				m[mtxOffset +  2] = ys;
				m[mtxOffset +  4] = (xy * nc) >> 12;
				m[mtxOffset +  5] = ((((ry * ry) >> 12) * nc) >> 12) + z;
				m[mtxOffset +  6] = -xs;
				m[mtxOffset +  8] = -ys;
				m[mtxOffset +  9] = xs;
				m[mtxOffset + 10] = z;
			}
		}

		/**
		 * @param m     dest matrix
		 * @param angle rotate angle in 0-4096
		 */
		private void roll(int[] m, int angle) {
			if (angle == 0) return;
			
			int s = Util3D.sin(angle);
			int c = Util3D.cos(angle);
			
			int m00 = m[mtxOffset];
			int m10 = m[mtxOffset + 4];
			int m20 = m[mtxOffset + 8];
			int m01 = m[mtxOffset + 1];
			int m11 = m[mtxOffset + 5];
			int m21 = m[mtxOffset + 9];
			
			m[mtxOffset    ] = (m00 * c + m01 * s) >> 10;
			m[mtxOffset + 4] = (m10 * c + m11 * s) >> 10;
			m[mtxOffset + 8] = (m20 * c + m21 * s) >> 10;
			m[mtxOffset + 1] = (m01 * c - m00 * s) >> 10;
			m[mtxOffset + 5] = (m11 * c - m10 * s) >> 10;
			m[mtxOffset + 9] = (m21 * c - m20 * s) >> 10;
		}
	}

	static final class Animation {
		private final int[] keys;
		final int[][] values;

		Animation(int count) {
			keys = new int[count];
			values = new int[count][3];
		}

		void set(int idx, int kf, int x, int y, int z) {
			keys[idx] = kf;
			values[idx][0] = x;
			values[idx][1] = y;
			values[idx][2] = z;
		}

		void get(int kgf, int[] arr) {
			final int max = keys.length - 1;
			if (kgf >= keys[max]) {
				int[] value = values[max];
				arr[0] = value[0];
				arr[1] = value[1];
				arr[2] = value[2];
				return;
			}
			for (int i = max - 1; i >= 0; i--) {
				final int prevKey = keys[i];
				if (prevKey > kgf) {
					continue;
				}
				final int[] prevVal = values[i];
				int x = prevVal[0];
				int y = prevVal[1];
				int z = prevVal[2];
				if (prevKey == kgf) {
					arr[0] = x;
					arr[1] = y;
					arr[2] = z;
					return;
				}
				int nextKey = keys[i + 1];
				int[] nextValue = values[i + 1];
				int delta = (int) (4096f * (kgf - prevKey) / (nextKey - prevKey));
				arr[0] = x + (((nextValue[0] - x) * delta) >> 12);
				arr[1] = y + (((nextValue[1] - y) * delta) >> 12);
				arr[2] = z + (((nextValue[2] - z) * delta) >> 12);
				return;
			}
		}
		
		int[] cloneValues(int i) {
			int[] newValues = new int[values[i].length];
			System.arraycopy(values[i], 0, newValues, 0, values[i].length);
			return newValues;
		}
	}

	static final class RollAnim {
		private final int[] keys;
		final int[] values;

		RollAnim(int count) {
			keys = new int[count];
			values = new int[count];
		}

		void set(int idx, int kf, int v) {
			keys[idx] = kf;
			values[idx] = v;
		}

		int get(int kgf) {
			final int max = keys.length - 1;
			if (kgf >= keys[max]) {
				return values[max];
			}
			for (int i = max - 1; i >= 0; i--) {
				final int key = keys[i];
				if (key > kgf) {
					continue;
				}
				int value = values[i];
				if (key == kgf) {
					return value;
				}
				int nextKey = keys[i + 1];
				int nextValue = values[i + 1];
				
				return value + (nextValue - value) * (kgf - key) / (nextKey - key);
			}
			return 0;
		}
	}
}
