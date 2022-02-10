
#include <jni.h>
#include <windows.h>
#include <dwmapi.h>
#include <iostream>
#include <map>
#include <d3d9.h>
#include <d3dx9.h>

LRESULT CALLBACK WndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam);

jlong nCreateWindow();
jlong nGetDevice(jlong hwnd);

void nSetViewport(jlong device, jint width, jint height);
void nBeginScene(jlong device);
void nEndScene(jlong device);
void nClear(jlong device);
void nDrawArrays(jlong device, jfloat* array, jint count);
jlong nCreatePixelShader(jlong device, char* content, jint length);
jlong nCreateVertexShader(jlong device, char* content, jint length);
void nSetPixelShader(jlong device, jlong shader);
void nSetVertexShader(jlong device, jlong shader);
void nSetShaderValue3f(jlong device, jlong shader, char* name, jfloat v1, jfloat v2, jfloat v3);
void nSetShaderValue4f(jlong device, jlong shader, char* name, jfloat v1, jfloat v2, jfloat v3, jfloat v4);
void nSetShaderMatrix(jlong device, jlong shader, char* name, jfloat* matrix);

extern "C" {

	/* ===============
		Platform
	   ===============
	*/
	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateWindow(JNIEnv*, jobject) {
		return nCreateWindow();
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nGetDevice(JNIEnv*, jobject, jlong hwnd) {
		return nGetDevice(hwnd);
	}

	/* ===============
		Paint
	   ===============
	*/
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetViewport(JNIEnv*, jobject, jlong device, jint width, jint height) {
		nSetViewport(device, width, height);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nBeginScene(JNIEnv*, jobject, jlong device) {
		nBeginScene(device);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nEndScene(JNIEnv*, jobject, jlong device) {
		nEndScene(device);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nClear(JNIEnv*, jobject, jlong device) {
		nClear(device);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nDrawArrays(JNIEnv* env, jobject, jlong device, jobject _array, jint count) {
		jfloat* array = (jfloat*)env->GetDirectBufferAddress(_array);
		nDrawArrays(device, array, count);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreatePixelShader(JNIEnv* env, jobject, jlong device, jobject _content, jint length) {
		char* content = (char*)env->GetDirectBufferAddress(_content);
		return nCreatePixelShader(device, content, length);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateVertexShader(JNIEnv* env, jobject, jlong device, jobject _content, jint length) {
		char* content = (char*)env->GetDirectBufferAddress(_content);
		return nCreateVertexShader(device, content, length);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetPixelShader(JNIEnv* env, jobject, jlong device, jlong shader) {
		nSetPixelShader(device, shader);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetVertexShader(JNIEnv* env, jobject, jlong device, jlong shader) {
		nSetVertexShader(device, shader);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetShaderValue3f(JNIEnv* env, jobject, jlong device, jlong shader, jobject _name, jfloat v1, jfloat v2, jfloat v3) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		nSetShaderValue3f(device, shader, name, v1, v2, v3);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetShaderValue4f(JNIEnv* env, jobject, jlong device, jlong shader, jobject _name, jfloat v1, jfloat v2, jfloat v3, jfloat v4) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		nSetShaderValue4f(device, shader, name, v1, v2, v3, v4);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetShaderMatrix(JNIEnv* env, jobject, jlong device, jlong shader, jobject _name, jobject _matrix) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		jfloat* matrix = (jfloat*)env->GetDirectBufferAddress(_matrix);
		nSetShaderMatrix(device, shader, name, matrix);
	}

}