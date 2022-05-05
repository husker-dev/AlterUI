
static const float PI = 3.14159265f;

sampler u_Texture;
float4 u_size;
float u_radius;
float u_type;

float getKernel(float radius, float index) {
    float q = radius/3;

    return 1/(sqrt(2 * PI) * q) * exp(-(index * index)/(2 * q * q));
}

float4 main(float2 Pos : SV_POSITION) : COLOR {
    float4 result = tex2D(u_Texture, float2(Pos.x / u_size.b, Pos.y / u_size.a)) * getKernel(u_radius, 0);

    for(int i = 1; i < u_radius; i++){
        float kernel = getKernel(u_radius, i);

        if(u_type == 0){
            result +=
                tex2Dlod(u_Texture, float4((Pos.x - i) / u_size.b, Pos.y / u_size.a, 0, 0)) * kernel +
                tex2Dlod(u_Texture, float4((Pos.x + i) / u_size.b, Pos.y / u_size.a, 0, 0)) * kernel;
        }else {
            result +=
                tex2Dlod(u_Texture, float4(Pos.x / u_size.b, (Pos.y - i) / u_size.a, 0, 0)) * kernel +
                tex2Dlod(u_Texture, float4(Pos.x / u_size.b, (Pos.y + i) / u_size.a, 0, 0)) * kernel;
        }
    }

	return result;
}

