#version 330 core
out vec4 color;

uniform float u_Dpi;
uniform float u_RenderType;

uniform vec4 u_Color;

uniform sampler2D u_Texture;
uniform vec4 u_TextureBounds;
uniform float u_TextureColors;

vec2 getTextureCoord(vec4 bounds, float dpi){
    bounds *= vec4(u_Dpi, u_Dpi, u_Dpi, u_Dpi);
    bounds.y -= 0.1;    // 0.1 - for pixel-perfect on HiDPI (idk why)

    return vec2(
        (gl_FragCoord.x - bounds.x) / bounds.z,
        1 - (gl_FragCoord.y - bounds.y) / bounds.w
    );
}

void main(){
    if(u_RenderType == 1)
        color = u_Color;
    if(u_RenderType == 3)
        color = texture(u_Texture, getTextureCoord(u_TextureBounds, u_Dpi)) * u_Color;

}



