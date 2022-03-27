#version 330 core
layout(location = 0, index = 0) out vec4 color;
layout(location = 0, index = 1) out vec4 colorMask;

uniform vec4 u_Bounds;
uniform vec4 u_Color;
uniform sampler2D u_Texture;
uniform float u_TextureColors;
uniform float u_Dpi;
uniform float u_IsLCD;

void main(){
    vec4 bounds = u_Bounds * vec4(u_Dpi, u_Dpi, u_Dpi, u_Dpi);
    bounds.y -= 0.1;    // 0.1 - for pixel-perfect on HiDPI (idk why)

    vec2 texCoord = vec2(
        (gl_FragCoord.x - bounds.x) / bounds.z,
        1 - (gl_FragCoord.y - bounds.y) / bounds.w
    );

    if(u_TextureColors == 3 && u_IsLCD == 1) {
        vec4 texColor = texture(u_Texture, texCoord);
        color = u_Color * texColor;
        colorMask = u_Color.a * texture(u_Texture, texCoord);
    }else if(u_TextureColors == 1){
        float a = texture(u_Texture, texCoord).r;
        color = vec4(1.0, 1.0, 1.0, a) * u_Color;
    }else {
        color = texture(u_Texture, texCoord) * u_Color;
    }
}