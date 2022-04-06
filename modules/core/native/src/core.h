#include <jni.h>
#include <iostream>

#define __STDC_LIB_EXT1__
#define STB_IMAGE_IMPLEMENTATION
#define STB_IMAGE_RESIZE_IMPLEMENTATION
#define STB_IMAGE_WRITE_IMPLEMENTATION

#define STBI_NO_FAILURE_STRINGS
#define STBIW_WINDOWS_UTF8
#include "stb/stb_image.h"
#include "stb/stb_image_resize.h"
#include "stb/stb_image_write.h"

#include "earcut/earcut.hpp"

#include "ft2build.h"

#include FT_FREETYPE_H
#include FT_SFNT_NAMES_H
#include FT_LCD_FILTER_H

#include "hb.h"
#include "hb-ft.h"

extern "C" {

	/* =======================
		  Image
	   =======================
	*/
	JNIEXPORT jintArray JNICALL Java_com_huskerdev_alter_graphics_ImageInfo_nGetBitmapInfo(JNIEnv* env, jobject, jobject _data) {
		char* data = (char*)env->GetDirectBufferAddress(_data);
		jlong length = env->GetDirectBufferCapacity(_data);

		int width, height, components;
		stbi_info_from_memory((const stbi_uc*)data, length, &width, &height, &components);

		jint result[3]{ width, height, components };
		jintArray array = env->NewIntArray(3);
		env->SetIntArrayRegion(array, 0, 3, result);
		return array;
	}

	JNIEXPORT jobject JNICALL Java_com_huskerdev_alter_graphics_Image_nGetBitmap(JNIEnv* env, jobject, jobject _data) {
		char* data = (char*)env->GetDirectBufferAddress(_data);
		jlong length = env->GetDirectBufferCapacity(_data);

		int width, height, components;
		unsigned char* bitmap = stbi_load_from_memory((const stbi_uc*)data, length, &width, &height, &components, 0);
		return env->NewDirectByteBuffer(bitmap, width * height * components);
	}

	JNIEXPORT jintArray JNICALL Java_com_huskerdev_alter_graphics_ImageInfo_nGetBitmapInfoFromFile(JNIEnv* env, jobject, jobject _path) {
		char* path = (char*)env->GetDirectBufferAddress(_path);

		int width, height, components;
		stbi_info(path, &width, &height, &components);

		jint result[3]{ width, height, components };
		jintArray array = env->NewIntArray(3);
		env->SetIntArrayRegion(array, 0, 3, result);
		return array;
	}

	JNIEXPORT jobject JNICALL Java_com_huskerdev_alter_graphics_Image_nGetBitmapFromFile(JNIEnv* env, jobject, jobject _path) {
		char* path = (char*)env->GetDirectBufferAddress(_path);

		int width, height, components;
		unsigned char* bitmap = stbi_load(path, &width, &height, &components, 0);
		return env->NewDirectByteBuffer(bitmap, width * height * components);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_graphics_Image_nReleaseBitmap(JNIEnv* env, jobject, jobject _data) {
		char* data = (char*)env->GetDirectBufferAddress(_data);
		stbi_image_free(data);
	}

	JNIEXPORT jobject JNICALL Java_com_huskerdev_alter_graphics_Image_nResize(JNIEnv* env, jobject, jobject _data, jint oldWidth, jint oldHeight, jint components, jint newWidth, jint newHeight, jint type) {
		char* data = (char*)env->GetDirectBufferAddress(_data);
		char* result = new char[newWidth * newHeight * components];
		stbir_resize(
			data, oldWidth, oldHeight, 0,
			result, newWidth, newHeight, 0,
			STBIR_TYPE_UINT8, components, components == 3 ? STBIR_ALPHA_CHANNEL_NONE : 3, 0,
			STBIR_EDGE_REFLECT, STBIR_EDGE_REFLECT,
			(stbir_filter)type, (stbir_filter)type,
			STBIR_COLORSPACE_SRGB, NULL);
		return env->NewDirectByteBuffer(result, newWidth * newHeight * components);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_graphics_Image_nWriteToFile(JNIEnv* env, jobject, jint type, jobject _path, jobject _data, jint width, jint height, jint components, jint quality) {
		char* data = (char*)env->GetDirectBufferAddress(_data);
		char* path = (char*)env->GetDirectBufferAddress(_path);

		if (type == 0) {
			stbi_write_png_compression_level = quality > 0 ? quality : 8;
			stbi_write_png(path, width, height, components, data, width * components);
		}else if(type == 1)
			stbi_write_jpg(path, width, height, components, data, quality > 0 ? quality : 80);
		else if (type == 2)
			stbi_write_bmp(path, width, height, components, data);
		else if (type == 3)
			stbi_write_tga(path, width, height, components, data);
	}

	/* =======================
		  Font
	   =======================
	*/
	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_graphics_font_Font_nLoadFreeType(JNIEnv*, jobject) {
		FT_Library ft;
		FT_Init_FreeType(&ft);
		FT_Library_SetLcdFilter(ft, FT_LCD_FILTER_DEFAULT);
		return (jlong)ft;
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_graphics_font_Font_nCreateFace(JNIEnv* env, jobject, jlong _ft, jobject _data) {
		FT_Library ft = (FT_Library)_ft;
		FT_Long length = (FT_Long)env->GetDirectBufferCapacity(_data);
		FT_Byte* data = (FT_Byte*)env->GetDirectBufferAddress(_data);
		
		FT_Face face;
		FT_New_Memory_Face(ft, data, length, 0, &face);
		FT_Select_Charmap(face, FT_ENCODING_UNICODE);

		return (jlong)face;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_graphics_font_Font_nSetFaceSize(JNIEnv*, jobject, jlong _face, jint size) {
		FT_Set_Pixel_Sizes((FT_Face)_face, 0, (FT_UInt)size);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_graphics_font_Font_nLoadChar(JNIEnv*, jobject, jlong _face, jint charIndex) {
		FT_Face face = (FT_Face)_face;

		FT_Load_Glyph(face, charIndex, FT_LOAD_NO_BITMAP);
	}

	JNIEXPORT jobject JNICALL Java_com_huskerdev_alter_graphics_font_Font_nGetGlyphData(JNIEnv* env, jobject, jlong _face, jboolean useSubpixel) {
		FT_Face face = (FT_Face)_face;
		FT_Render_Glyph(face->glyph, useSubpixel ? FT_RENDER_MODE_LCD : FT_RENDER_MODE_LIGHT);
		auto bitmap = face->glyph->bitmap;

		//std::cout << face->glyph->bitmap.pitch << std::endl;

		if (useSubpixel) {
			char* newData = new char[bitmap.width * bitmap.rows];

			for (int row = 0; row < bitmap.rows; row++) {
				int indexSource = row * bitmap.pitch;
				int indexTarget = row * bitmap.width;

				std::copy(
					bitmap.buffer + indexSource, 
					bitmap.buffer + indexSource + bitmap.width, 
					newData + indexTarget
				);
			}

			return env->NewDirectByteBuffer(newData, (jlong)(bitmap.width * bitmap.rows));
		}

		return env->NewDirectByteBuffer(bitmap.buffer, (jlong)(bitmap.width * bitmap.rows));
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_graphics_font_Font_nGetGlyphWidth(JNIEnv*, jobject, jlong _face) {
		FT_Face face = (FT_Face)_face;

		if (face->glyph->bitmap.pixel_mode == FT_PIXEL_MODE_GRAY)
			return face->glyph->bitmap.width;
		else
			return face->glyph->bitmap.width / 3;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_graphics_font_Font_nGetGlyphHeight(JNIEnv*, jobject, jlong _face) {
		return ((FT_Face)_face)->glyph->bitmap.rows;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_graphics_font_FontFamily_nGetFacePropertiesCount(JNIEnv* env, jobject, jlong _face) {
		return (jint)FT_Get_Sfnt_Name_Count((FT_Face)_face);
	}

	JNIEXPORT jobjectArray JNICALL Java_com_huskerdev_alter_graphics_font_FontFamily_nGetFaceProperty(JNIEnv* env, jobject, jlong _face, jint property) {
		FT_SfntName name;
		FT_Get_Sfnt_Name((FT_Face)_face, property, &name);

		auto byteArray = env->NewByteArray((jsize)name.string_len);
		env->SetByteArrayRegion(byteArray, 0, (jsize)name.string_len, (jbyte*)name.string);

		auto encodingArray = env->NewIntArray(3);
		int encoding[] = { name.platform_id, name.encoding_id, name.language_id };
		env->SetIntArrayRegion(encodingArray, 0, 3, (jint*)encoding);

		jobjectArray out = env->NewObjectArray(2, env->FindClass("java/lang/Object"), nullptr);
		env->SetObjectArrayElement(out, 0, byteArray);
		env->SetObjectArrayElement(out, 1, encodingArray);

		return out;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_graphics_font_Font_nGetBearingX(JNIEnv* env, jobject, jlong _face) {
		return (jint)((FT_Face)_face)->glyph->bitmap_left;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_graphics_font_Font_nGetBearingY(JNIEnv* env, jobject, jlong _face) {
		return (jint)((FT_Face)_face)->glyph->bitmap_top;
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_graphics_font_Font_nHBCreateBuffer(JNIEnv* env, jobject) {
		hb_buffer_t* buf;
		buf = hb_buffer_create();

		hb_buffer_set_direction(buf, HB_DIRECTION_LTR);
		hb_buffer_set_script(buf, HB_SCRIPT_LATIN);
		hb_buffer_set_language(buf, hb_language_from_string("en", -1));
		return (jlong)buf;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_graphics_font_FontRasterMetrics_nHBSetBufferText(JNIEnv* env, jobject, jlong buffer, jobject _text) {
		auto buf = (hb_buffer_t*)buffer;
		char* text = (char*)env->GetDirectBufferAddress(_text);
		hb_buffer_reset(buf);
		hb_buffer_set_direction(buf, HB_DIRECTION_LTR);
		hb_buffer_set_script(buf, HB_SCRIPT_LATIN);
		hb_buffer_set_language(buf, hb_language_from_string("en", -1));
		hb_buffer_add_utf8((hb_buffer_t*)buffer, text, -1, 0, -1);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_graphics_font_FontRasterMetrics_nHBCreateFont(JNIEnv* env, jobject, jlong _face, jint size) {
		FT_Set_Pixel_Sizes((FT_Face)_face, 0, (FT_UInt)size);
		hb_font_t* font = hb_ft_font_create((FT_Face)_face, nullptr);
		hb_ft_font_set_funcs(font);
		return (jlong)font;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_graphics_font_FontRasterMetrics_nHBShape(JNIEnv* env, jobject, jlong font, jlong buffer) {
		hb_shape((hb_font_t*)font, (hb_buffer_t*)buffer, NULL, 0);
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_graphics_font_FontRasterMetrics_nHBGetGlyphCount(JNIEnv* env, jobject, jlong buffer) {
		unsigned int glyph_count;
		hb_buffer_get_glyph_infos((hb_buffer_t*)buffer, &glyph_count);
		return (jint)glyph_count;
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_graphics_font_FontRasterMetrics_nHBGetGlyphInfo(JNIEnv* env, jobject, jlong buffer) {
		return (jlong)hb_buffer_get_glyph_infos((hb_buffer_t*)buffer, nullptr);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_graphics_font_FontRasterMetrics_nHBGetGlyphPositions(JNIEnv* env, jobject, jlong buffer) {
		return (jlong)hb_buffer_get_glyph_positions((hb_buffer_t*)buffer, nullptr);
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_graphics_font_FontRasterMetrics_nHBGetGlyphId(JNIEnv* env, jobject, jlong info, jint index) {
		return ((hb_glyph_info_t*)info)[index].codepoint;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_graphics_font_FontRasterMetrics_nHBGetXOffset(JNIEnv* env, jobject, jlong positions, jint index) {
		return ((hb_glyph_position_t*)positions)[index].x_offset;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_graphics_font_FontRasterMetrics_nHBGetYOffset(JNIEnv* env, jobject, jlong positions, jint index) {
		return ((hb_glyph_position_t*)positions)[index].y_offset;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_graphics_font_FontRasterMetrics_nHBGetXAdvance(JNIEnv* env, jobject, jlong positions, jint index) {
		return ((hb_glyph_position_t*)positions)[index].x_advance;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_graphics_font_FontRasterMetrics_nHBGetYAdvance(JNIEnv* env, jobject, jlong positions, jint index) {
		return ((hb_glyph_position_t*)positions)[index].y_advance;
	}
}