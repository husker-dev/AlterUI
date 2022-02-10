#include <jni.h>

#include <iostream>
#include <map>

#include <glad/glad.h>

jlong nCreateWindow(jlong shareWith);
void nMakeCurrent(jlong handle);
void nSwapBuffers(jlong handle);

void throwException(JNIEnv* env, const char* exceptionClass, const char* message) {
	env->ThrowNew(env->FindClass(exceptionClass), message);
}

extern "C" {

	/* ================
		 GL
	   ================
	*/

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_glClear(JNIEnv*, jobject, jint mask) {
		glClear(mask);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_glClearColor(JNIEnv*, jobject, jfloat red, jfloat green, jfloat blue, jfloat alpha) {
		glClearColor(red, green, blue, alpha);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_glViewport(JNIEnv*, jobject, jint x, jint y, jint width, jint height) {
		glViewport(x, y, width, height);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_glUseProgram(JNIEnv*, jobject, jint program) {
		glUseProgram(program);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_initContext(JNIEnv*, jobject) {
		unsigned int vao, vbo = 0;
		// VAO
		glGenVertexArrays(1, &vao);
		glBindVertexArray(vao);

		// VBO
		glGenBuffers(1, &vbo);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		
		// Attribute
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 0, (void*)0);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_drawArray(JNIEnv* env, jobject, jobject _array, jint count) {
		jfloat* array = (jfloat*)env->GetDirectBufferAddress(_array);

		glBufferData(GL_ARRAY_BUFFER, 12 * count, array, GL_STATIC_DRAW);
		glDrawArrays(GL_TRIANGLES, 0, count);
	}

	/* ================
		 GL-Shader
	   ================
	*/
	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_createShaderProgram(JNIEnv* env, jobject, jobject _vertex, jobject _fragment) {
		char* vertexSource = (char*)env->GetDirectBufferAddress(_vertex);
		char* fragmentSource = (char*)env->GetDirectBufferAddress(_fragment);

		// Vertex
		unsigned int vertex = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertex, 1, &vertexSource, NULL);
		glCompileShader(vertex);
		int success;
		glGetShaderiv(vertex, GL_COMPILE_STATUS, &success);
		if (!success) {
			GLint infoLen = 0;
			glGetShaderiv(vertex, GL_INFO_LOG_LENGTH, &infoLen);
			char* infoLog = new char[infoLen];
			glGetShaderInfoLog(vertex, infoLen, NULL, infoLog);
			
			throwException(env, "java/lang/RuntimeException", infoLog);
			delete[] infoLog;
			return -1;
		}

		// Fragment
		unsigned int fragment = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragment, 1, &fragmentSource, NULL);
		glCompileShader(fragment);
		glGetShaderiv(fragment, GL_COMPILE_STATUS, &success);
		if (!success) {
			GLint infoLen = 0;
			glGetShaderiv(vertex, GL_INFO_LOG_LENGTH, &infoLen);
			char* infoLog = new char[infoLen];
			glGetShaderInfoLog(vertex, infoLen, NULL, infoLog);

			throwException(env, "java/lang/RuntimeException", infoLog);
			delete[] infoLog;
			return -1;
		}
		
		// Program
		unsigned int program = glCreateProgram();
		glAttachShader(program, vertex);
		glAttachShader(program, fragment);
		glLinkProgram(program);
		glGetProgramiv(program, GL_LINK_STATUS, &success);
		if (!success) {
			GLint infoLen = 0;
			glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLen);
			char* infoLog = new char[infoLen];
			glGetProgramInfoLog(program, infoLen, NULL, infoLog);

			throwException(env, "java/lang/RuntimeException", infoLog);
			delete[] infoLog;
			return -1;
		}
		glDeleteShader(vertex);
		glDeleteShader(fragment);


		return program;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_setShaderVariable4f(JNIEnv* env, jobject, jint program, jobject _name, jfloat var1, jfloat var2, jfloat var3, jfloat var4) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		GLint location = glGetUniformLocation(program, name);
		glUniform4f(location, var1, var2,var3, var4);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_setShaderVariable3f(JNIEnv* env, jobject, jint program, jobject _name, jfloat var1, jfloat var2, jfloat var3) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		GLint location = glGetUniformLocation(program, name);
		glUniform3f(location, var1, var2, var3);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_setShaderMatrixVariable(JNIEnv* env, jobject, jint program, jobject _name, jobject _matrix) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		float* matrix = (float*)env->GetDirectBufferAddress(_matrix);

		GLint location = glGetUniformLocation(program, name);
		glUniformMatrix4fv(location, 1, GL_TRUE, &matrix[0]);
	}

	/* ================
		 Platform
	   ================
	*/
	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_nCreateWindow(JNIEnv*, jobject, jlong shareWith) {
		return nCreateWindow(shareWith);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_nMakeCurrent(JNIEnv*, jobject, jlong handle) {
		nMakeCurrent(handle);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_nSwapBuffers(JNIEnv*, jobject, jlong handle) {
		nSwapBuffers(handle);
	}

}