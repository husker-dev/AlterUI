#version 330 core
out vec4 color;

uniform vec4 u_Bounds;
uniform vec4 u_Color;
uniform sampler2D u_Texture;
uniform float u_TextureColors;
uniform float u_Dpi;

void main(){
    vec4 bounds = u_Bounds * vec4(u_Dpi, u_Dpi, u_Dpi, u_Dpi);
    bounds.y -= 0.1;    // 0.1 - for pixel-perfect on HiDPI (idk why)

    vec2 texCoord = vec2(
        (gl_FragCoord.x - bounds.x) / bounds.z,
        1 - (gl_FragCoord.y - bounds.y) / bounds.w
    );

    if(u_TextureColors != 1)
        color = texture(u_Texture, texCoord) * u_Color;
    else {
        float a = texture(u_Texture, texCoord).r;
        color = vec4(1.0, 1.0, 1.0, a) * u_Color;
    }
}