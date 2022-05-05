#version 330 core
out vec4 color;

const float PI = 3.14159265f;

uniform sampler2D u_Texture;
uniform vec4 u_size;
uniform float u_radius;
uniform float u_type;

float getKernel(float radius, float index) {
    float q = radius/3;

    return 1/(sqrt(2 * PI) * q) * exp(-(index * index)/(2 * q * q));
}

void main(){
    color = texture(u_Texture, vec2(gl_FragCoord.x / u_size.b, gl_FragCoord.y / u_size.a)) * getKernel(u_radius, 0);

    for(int i = 1; i < u_radius; i++){
        float kernel = getKernel(u_radius, i);

        if(u_type == 0){
            color +=
                texture(u_Texture, vec2((gl_FragCoord.x - i) / u_size.b, gl_FragCoord.y / u_size.a)) * kernel +
                texture(u_Texture, vec2((gl_FragCoord.x + i) / u_size.b, gl_FragCoord.y / u_size.a)) * kernel;
        }else {
            color +=
                texture(u_Texture, vec2(gl_FragCoord.x / u_size.b, (gl_FragCoord.y - i) / u_size.a)) * kernel +
                texture(u_Texture, vec2(gl_FragCoord.x / u_size.b, (gl_FragCoord.y + i) / u_size.a)) * kernel;
        }

    }
}

