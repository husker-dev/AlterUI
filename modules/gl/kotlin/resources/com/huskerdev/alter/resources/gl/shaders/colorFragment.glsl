#version 330 core
out vec4 color;


uniform float u_RenderType;
uniform float u_ColorChannels;
uniform float u_ViewportHeight;
uniform float u_InverseY;

uniform vec4 u_Color;

uniform sampler2D u_Texture;
uniform vec4 u_TextureBounds;
uniform float u_TextureColors;

vec2 getTextureCoord(vec4 bounds){

    if(u_InverseY == 0)
        bounds.y = u_ViewportHeight - (bounds.y + bounds.w);
    //bounds *= vec4(dpi, dpi, dpi, dpi);

    //bounds.y -= 0.1;    // 0.1 - for pixel-perfect on HiDPI (idk why)


    vec2 texCoord = vec2(
        (gl_FragCoord.x - bounds.x) / bounds.z,
        (gl_FragCoord.y - bounds.y) / bounds.w
    );
    if(u_InverseY == 0)
    texCoord.y = 1 - texCoord.y;
    return texCoord;
}

void main(){
    if(u_RenderType == 1)
        color = u_Color;
    if(u_RenderType == 3)
        color = texture(u_Texture, getTextureCoord(u_TextureBounds)) * u_Color;

    if(u_RenderType == 4){
        if(u_ColorChannels == 1)
            color = vec4(texture(u_Texture, getTextureCoord(u_TextureBounds)).r, 1, 1, 1);
        else
            color = vec4(1, 1, 1, texture(u_Texture, getTextureCoord(u_TextureBounds)).r);
    }
}