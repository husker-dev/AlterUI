#include <jni.h>

#include <iostream>
#include <map>

#include <glad/glad.h>

jlong nCreateWindow(jlong shareWith, jint samples);
void nMakeCurrent(jlong handle);
void nSwapBuffers(jlong handle);

void throwJavaException(JNIEnv* env, const char* exceptionClass, const char* message) {
	env->ThrowNew(env->FindClass(exceptionClass), message);
}

void loadContextDefaults() {
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

	// Configuration
	glEnable(GL_TEXTURE_2D);
	glEnable(GL_BLEND);
	glEnable(GL_MULTISAMPLE);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	//  Read/Write 
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	glPixelStorei(GL_PACK_ALIGNMENT, 1);
}

extern "C" {

	/* ================
		 GL
	   ================
	*/

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglClear(JNIEnv*, jobject, jint mask) {
		glClear(mask);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglViewport(JNIEnv*, jobject, jint x, jint y, jint width, jint height) {
		glViewport(x, y, width, height);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglBindTexture(JNIEnv*, jobject, jint target, jint texture) {
		glBindTexture(target, texture);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglBindFramebuffer(JNIEnv*, jobject, jint n, jint buffer) {
		glBindFramebuffer(n, buffer);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglFlush(JNIEnv*, jobject) {
		glFlush();
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglFinish(JNIEnv*, jobject) {
		glFinish();
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nDrawArray(JNIEnv* env, jobject, jobject _array, jint count, jint type) {
		jfloat* array = (jfloat*)env->GetDirectBufferAddress(_array);

		if (type > 1)
			type++;

		glBufferData(GL_ARRAY_BUFFER, 12 * count, array, GL_STREAM_DRAW);
		glDrawArrays(type, 0, count);
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nCreateTexture(JNIEnv* env, jobject, jint width, jint height, jint channels) {
		int type = GL_RED;
		if (channels == 3)
			type = GL_RGB;
		if (channels == 4)
			type = GL_RGBA;

		GLuint texture;
		glGenTextures(1, &texture);
		glBindTexture(GL_TEXTURE_2D, texture);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, type, width, height, 0, type, GL_UNSIGNED_BYTE, 0);
		glFlush();
		return texture;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nCreateEmptyMSAATexture(JNIEnv* env, jobject, jint width, jint height, jint channels, jint samples) {
		int type = GL_RED;
		if (channels == 3)
			type = GL_RGB;
		if (channels == 4)
			type = GL_RGBA;

		GLuint texture;
		glGenTextures(1, &texture);

		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, texture);
		glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, GL_RGB, width, height, GL_TRUE);
		return texture;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nCreateMSAATexture(JNIEnv* env, jobject, jint width, jint height, jint channels, jint samples, jobject _data) {
		char* data = (char*)env->GetDirectBufferAddress(_data);

		int type = GL_RED;
		if (channels == 3)
			type = GL_RGB;
		if (channels == 4)
			type = GL_RGBA;

		// Creating textures
		GLuint sourceTexture;
		glGenTextures(1, &sourceTexture);
		glBindTexture(GL_TEXTURE_2D, sourceTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, type, width, height, 0, type, GL_UNSIGNED_BYTE, data);

		GLuint targetTexture;
		glGenTextures(1, &targetTexture);
		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, targetTexture);
		glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, type, width, height, GL_TRUE);
		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);

		// Creating framebuffers
		GLuint sourceFramebuffer;
		glGenFramebuffers(1, &sourceFramebuffer);
		glBindFramebuffer(GL_FRAMEBUFFER, sourceFramebuffer);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, sourceTexture, 0);

		GLuint targetFramebuffer;
		glGenFramebuffers(1, &targetFramebuffer);
		glBindFramebuffer(GL_FRAMEBUFFER, targetFramebuffer);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, targetTexture, 0);

		// Blitting
		glBindFramebuffer(GL_READ_FRAMEBUFFER, sourceFramebuffer);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, targetFramebuffer);
		glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
		glFlush();

		glDeleteFramebuffers(1, &targetFramebuffer);
		glDeleteFramebuffers(1, &sourceFramebuffer);

		return targetTexture;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nCreateTextureFramebuffer(JNIEnv* env, jobject, jint texture, jboolean isMSAA) {
		GLuint framebuffer;
		glGenFramebuffers(1, &framebuffer);
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, isMSAA ? GL_TEXTURE_2D_MULTISAMPLE : GL_TEXTURE_2D, texture, 0);
		return framebuffer;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglBlitFramebuffer(JNIEnv* env, jobject, jint source, jint target, jint width, jint height) {
		glBindFramebuffer(GL_READ_FRAMEBUFFER, source);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, target);

		glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
		glFlush();
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nSetLinearFiltering(JNIEnv* env, jobject, jint texture, jboolean linearFiltering) {
		glBindTexture(GL_TEXTURE_2D, texture);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, linearFiltering ? GL_LINEAR : GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, linearFiltering ? GL_LINEAR : GL_NEAREST);
		glFlush();
	}

	JNIEXPORT jobject JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nReadPixels(JNIEnv* env, jobject, jint framebuffer, jint channels, jint x, jint y, jint width, jint height) {
		char* pixels = new char[width * height * channels];
		int type = GL_RED;
		if (channels == 3)
			type = GL_RGB;
		if (channels == 4)
			type = GL_RGBA;

		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
		glViewport(x, y, width, height);
		glReadPixels(x, y, width, height, type, GL_UNSIGNED_BYTE, pixels);

		return env->NewDirectByteBuffer(pixels, width * height * channels);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglDeleteTextures(JNIEnv* env, jobject, jint texture) {
		glDeleteTextures(1, (GLuint*)&texture);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglDeleteFrameBuffer(JNIEnv* env, jobject, jint buf) {
		glDeleteFramebuffers(1, (GLuint*)&buf);
	}

	/* ================
		 GL-Shader
	   ================
	*/
	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nCreateShaderProgram(JNIEnv* env, jobject, jobject _vertex, jobject _fragment) {
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
			
			throwJavaException(env, "java/lang/RuntimeException", infoLog);
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
			glGetShaderiv(fragment, GL_INFO_LOG_LENGTH, &infoLen);
			char* infoLog = new char[infoLen];
			glGetShaderInfoLog(fragment, infoLen, NULL, infoLog);

			throwJavaException(env, "java/lang/RuntimeException", infoLog);
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

			throwJavaException(env, "java/lang/RuntimeException", infoLog);
			delete[] infoLog;
			return -1;
		}
		glDeleteShader(vertex);
		glDeleteShader(fragment);


		return program;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglGetUniformLocation(JNIEnv* env, jobject, jint program, jobject _name) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		return glGetUniformLocation(program, name);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglUniform4f(JNIEnv* env, jobject, jint program, jint location, jfloat var1, jfloat var2, jfloat var3, jfloat var4) {
		glUniform4f(location, var1, var2,var3, var4);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglUniform3f(JNIEnv* env, jobject, jint program, jint location, jfloat var1, jfloat var2, jfloat var3) {
		glUniform3f(location, var1, var2, var3);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglUniform1f(JNIEnv* env, jobject, jint program, jint location, jfloat var1) {
		glUniform1f(location, var1);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglUniform1i(JNIEnv* env, jobject, jint location, jint v0) {
		glUniform1i(location, v0);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLContext_nglUseProgram(JNIEnv*, jobject, jint program) {
		glUseProgram(program);
	}

	/* ================
		 Platform
	   ================
	*/
	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_nCreateWindow(JNIEnv*, jobject, jlong shareWith, jint samples) {
		return nCreateWindow(shareWith, samples);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_nMakeCurrent(JNIEnv*, jobject, jlong handle) {
		nMakeCurrent(handle);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_gl_GLPipeline_nSwapBuffers(JNIEnv*, jobject, jlong handle) {
		nSwapBuffers(handle);
	}

}