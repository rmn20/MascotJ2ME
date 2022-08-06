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

class Utils {

	static final float TO_FLOAT = 2.4414062E-04f;
	static final float TO_RADIANS = (float) (Math.PI / 2048.0);
	static final int[] IDENTITY_AFFINE = {
			// 0     1     2     3
			// 0     4     8    12
			4096,    0, 4096,    0,
			// 4     5     6     7
			// 1     5     9    13
			   0, 4096,    0,    0,
			// 8     9    10    11
			// 2     6    10    14
			   0,    0, 4096,    0
	};

	static void parallelScale(float[] pm, int x, int y, FigureLayout layout, float vw, float vh) {
		float w = vw * (4096.0f / layout.scaleX);
		float h = vh * (4096.0f / layout.scaleY);

		float sx = 2.0f / w;
		float sy = 2.0f / h;
		float sz = 1.0f / 65536.0f;
		float tx = 2.0f * (layout.centerX + x) / vw - 1.0f;
		float ty = 2.0f * (layout.centerY + y) / vh - 1.0f;
		float tz = 0.0f;

		pm[ 0] =   sx; pm[ 4] = 0.0f; pm[ 8] = 0.0f; pm[12] =   tx;
		pm[ 1] = 0.0f; pm[ 5] =   sy; pm[ 9] = 0.0f; pm[13] =   ty;
		pm[ 2] = 0.0f; pm[ 6] = 0.0f; pm[10] =   sz; pm[14] =   tz;
		pm[ 3] = 0.0f; pm[ 7] = 0.0f; pm[11] = 0.0f; pm[15] = 1.0f;
	}

	static void parallelWH(float[] pm, int x, int y, FigureLayout layout, float vw, float vh) {
		float w = layout.parallelWidth == 0 ? 400.0f * 4.0f : layout.parallelWidth;
		float h = layout.parallelHeight == 0 ? w * (vh / vw) : layout.parallelHeight;

		float sx = 2.0f / w;
		float sy = 2.0f / h;
		float sz = 1.0f / 65536.0f;
		float tx = 2.0f * (layout.centerX + x) / vw - 1.0f;
		float ty = 2.0f * (layout.centerY + y) / vh - 1.0f;
		float tz = 0.0f;

		pm[0] =   sx; pm[4] = 0.0f; pm[ 8] = 0.0f; pm[12] =   tx;
		pm[1] = 0.0f; pm[5] =   sy; pm[ 9] = 0.0f; pm[13] =   ty;
		pm[2] = 0.0f; pm[6] = 0.0f; pm[10] =   sz; pm[14] =   tz;
		pm[3] = 0.0f; pm[7] = 0.0f; pm[11] = 0.0f; pm[15] = 1.0f;
	}

	static void perspectiveFov(float[] pm, int x, int y, FigureLayout layout, float vw, float vh) {
		float near = layout.near;
		float far = layout.far;
		float rd = 1.0f / (near - far);
		float sx = 1.0f / (float) Math.tan(layout.angle * TO_FLOAT * Math.PI);
		float sy = sx * (vw / vh);
		float sz = -(far + near) * rd;
		float tx = 2.0f * (layout.centerX + x) / vw - 1.0f;
		float ty = 2.0f * (layout.centerY + y) / vh - 1.0f;
		float tz = 2.0f * far * near * rd;

		pm[0] =   sx; pm[4] = 0.0f; pm[ 8] =   tx; pm[12] = 0.0f;
		pm[1] = 0.0f; pm[5] =   sy; pm[ 9] =   ty; pm[13] = 0.0f;
		pm[2] = 0.0f; pm[6] = 0.0f; pm[10] =   sz; pm[14] =   tz;
		pm[3] = 0.0f; pm[7] = 0.0f; pm[11] = 1.0f; pm[15] = 0.0f;
	}

	static void perspectiveWH(float[] pm, int x, int y, FigureLayout layout, float vw, float vh) {
		float zFar = layout.far;
		float zNear = layout.near;
		float width = layout.perspectiveWidth == 0 ? vw : layout.perspectiveWidth * TO_FLOAT;
		float height = layout.perspectiveHeight == 0 ? vh : layout.perspectiveHeight * TO_FLOAT;

		float rd = 1.0f / (zNear - zFar);
		float sx = 2.0f * zNear / width;
		float sy = 2.0f * zNear / height;
		float sz = -(zNear + zFar) * rd;
		float tx = 2.0f * (layout.centerX + x) / vw - 1.0f;
		float ty = 2.0f * (layout.centerY + y) / vh - 1.0f;
		float tz = 2.0f * zFar * zNear * rd;

		pm[0] =   sx; pm[4] = 0.0f; pm[ 8] =   tx; pm[12] = 0.0f;
		pm[1] = 0.0f; pm[5] =   sy; pm[ 9] =   ty; pm[13] = 0.0f;
		pm[2] = 0.0f; pm[6] = 0.0f; pm[10] =   sz; pm[14] =   tz;
		pm[3] = 0.0f; pm[7] = 0.0f; pm[11] = 1.0f; pm[15] = 0.0f;
	}

	static void getSpriteVertex(float[] quad, float[] center, int angle, float halfW, float halfH) {
		float r = angle * TO_RADIANS;
		float sin = (float) Math.sin(r);
		float cos = (float) Math.cos(r);
		float x = center[0];
		float y = center[1];
		float z = center[2];
		float w = center[3];
		quad[0] = -halfW * cos + halfH * -sin + x;
		quad[1] = -halfW * sin + halfH * cos + y;
		quad[2] = z;
		quad[3] = w;
		float bx = -halfW * cos + -halfH * -sin + x;
		float by = -halfW * sin + -halfH * cos + y;
		quad[4] = bx;
		quad[5] = by;
		quad[6] = z;
		quad[7] = w;
		float cx = halfW * cos + halfH * -sin + x;
		float cy = halfW * sin + halfH * cos + y;
		quad[8] = cx;
		quad[9] = cy;
		quad[10] = z;
		quad[11] = w;
		quad[12] = cx;
		quad[13] = cy;
		quad[14] = z;
		quad[15] = w;
		quad[16] = bx;
		quad[17] = by;
		quad[18] = z;
		quad[19] = w;
		quad[20] = halfW * cos + -halfH * -sin + x;
		quad[21] = halfW * sin + -halfH * cos + y;
		quad[22] = z;
		quad[23] = w;
	}

	static native void fillBuffer(short[] buffer, short[] vertices, int[] indices);
	static native void fillBuffer(byte[] buffer, byte[] vertices, int[] indices);

	static native void transform(short[] srcVertices, short[] dstVertices,
								 byte[] srcNormals, byte[] dstNormals,
								 int[] boneMatrices, int[] actionMatrices);

	/*
	JNIEXPORT void JNICALL Java_com_mascotcapsule_micro3d_v3_Utils_fillBuffer
        (JNIEnv *env, jclass ,
         jobject buffer, jobject vertices, jintArray indices) {
    auto bufPtr = static_cast<Vec3f *>(env->GetDirectBufferAddress(buffer));
    jsize len = env->GetArrayLength(indices);
    auto indexPtr = env->GetIntArrayElements(indices, nullptr);
    auto vertPtr = static_cast<Vec3f *>(env->GetDirectBufferAddress(vertices));
    for (int i = 0; i < len; ++i) {
        auto src = &vertPtr[indexPtr[i]];
        auto dst = bufPtr++;
        dst->x = src->x;
        dst->y = src->y;
        dst->z = src->z;
    }
    env->ReleaseIntArrayElements(indices, indexPtr, 0);
}

JNIEXPORT void JNICALL
Java_com_mascotcapsule_micro3d_v3_Utils_transform(JNIEnv *env, jclass ,
                                                  jobject src_vertices,
                                                  jobject dst_vertices,
                                                  jobject src_normals,
                                                  jobject dst_normals,
                                                  jobject aBones,
                                                  jfloatArray action_matrices) {
    auto srcVert = static_cast<Vec3f *>(env->GetDirectBufferAddress(src_vertices));
    auto dstVert = static_cast<Vec3f *>(env->GetDirectBufferAddress(dst_vertices));
    Vec3f *srcNorm;
    Vec3f *dstNorm;
    if (src_normals == nullptr) {
        srcNorm = nullptr;
        dstNorm = nullptr;
    } else {
        srcNorm = static_cast<Vec3f *>(env->GetDirectBufferAddress(src_normals));
        dstNorm = static_cast<Vec3f *>(env->GetDirectBufferAddress(dst_normals));
    }
    auto bones = static_cast<Bone *>(env->GetDirectBufferAddress(aBones));
    jlong bonesLen = env->GetDirectBufferCapacity(aBones) / sizeof(Bone);
    jsize actionsLen = 0;
    float *actionsPtr = nullptr;
    Matrix *actions = nullptr;
    if (action_matrices != nullptr) {
        actionsPtr = env->GetFloatArrayElements(action_matrices, nullptr);
        actionsLen = env->GetArrayLength(action_matrices) / 12;
        actions = reinterpret_cast<Matrix *>(actionsPtr);
    }
    auto tmp = new Matrix[bonesLen];
    for (int i = 0; i < bonesLen; ++i) {
        Bone *bone = &bones[i];
        int parent = bone->parent;
        Matrix *matrix = &tmp[i];
        if (parent == -1) {
            memcpy(matrix, &bone->matrix, sizeof(Matrix));
        } else {
            multiplyMM(matrix, &tmp[parent], &bone->matrix);
        }
        if (i < actionsLen) {
            multiplyMM(matrix, matrix, actions++);
        }
        auto boneLen = bone->length;
        for (int j = 0; j < boneLen; ++j) {
            multiplyMV(dstVert++, srcVert++, matrix);

            if (srcNorm != nullptr) {
                multiplyMN(dstNorm++, srcNorm++, matrix);
            }
        }
    }
    delete[] tmp;
    if (action_matrices != nullptr) {
        env->ReleaseFloatArrayElements(action_matrices, actionsPtr, 0);
    }
}

static void multiplyMM(Matrix *m, Matrix *lm, Matrix *rm) {
    float l00 = lm->m00;
    float l01 = lm->m01;
    float l02 = lm->m02;
    float l10 = lm->m10;
    float l11 = lm->m11;
    float l12 = lm->m12;
    float l20 = lm->m20;
    float l21 = lm->m21;
    float l22 = lm->m22;
    float r00 = rm->m00;
    float r01 = rm->m01;
    float r02 = rm->m02;
    float r03 = rm->m03;
    float r10 = rm->m10;
    float r11 = rm->m11;
    float r12 = rm->m12;
    float r13 = rm->m13;
    float r20 = rm->m20;
    float r21 = rm->m21;
    float r22 = rm->m22;
    float r23 = rm->m23;

    m->m00 = l00 * r00 + l01 * r10 + l02 * r20;
    m->m01 = l00 * r01 + l01 * r11 + l02 * r21;
    m->m02 = l00 * r02 + l01 * r12 + l02 * r22;
    m->m03 = l00 * r03 + l01 * r13 + l02 * r23 + lm->m03;
    m->m10 = l10 * r00 + l11 * r10 + l12 * r20;
    m->m11 = l10 * r01 + l11 * r11 + l12 * r21;
    m->m12 = l10 * r02 + l11 * r12 + l12 * r22;
    m->m13 = l10 * r03 + l11 * r13 + l12 * r23 + lm->m13;
    m->m20 = l20 * r00 + l21 * r10 + l22 * r20;
    m->m21 = l20 * r01 + l21 * r11 + l22 * r21;
    m->m22 = l20 * r02 + l21 * r12 + l22 * r22;
    m->m23 = l20 * r03 + l21 * r13 + l22 * r23 + lm->m23;
}

static void multiplyMV(Vec3f *dst, Vec3f *src, Matrix *matrix) {
    float x = src->x;
    float y = src->y;
    float z = src->z;
    dst->x = x * matrix->m00 + y * matrix->m01 + z * matrix->m02 + matrix->m03;
    dst->y = x * matrix->m10 + y * matrix->m11 + z * matrix->m12 + matrix->m13;
    dst->z = x * matrix->m20 + y * matrix->m21 + z * matrix->m22 + matrix->m23;
}

static void multiplyMN(Vec3f *dst, Vec3f *src, Matrix *matrix) {
    float x = src->x;
    float y = src->y;
    float z = src->z;
    dst->x = x * matrix->m00 + y * matrix->m01 + z * matrix->m02;
    dst->y = x * matrix->m10 + y * matrix->m11 + z * matrix->m12;
    dst->z = x * matrix->m20 + y * matrix->m21 + z * matrix->m22;
}

#ifdef __cplusplus
}
#endif
	 */
}