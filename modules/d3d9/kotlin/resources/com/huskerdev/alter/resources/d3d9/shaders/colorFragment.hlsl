

float u_RenderType;
float u_ColorChannels;
float u_ViewportHeight;

float4 u_Color: COLOR;

sampler u_Texture;
float4 u_TextureBounds;
float u_TextureColors;

float2 getTextureCoord(float2 Pos, float4 bounds){
    bounds.x -= 0.6;
    bounds.y -= 0.5;
    return float2(
        (Pos.x - bounds.x) / bounds.z,
        (Pos.y - bounds.y) / bounds.w
    );
}

float4 main(float2 Pos : SV_POSITION) : COLOR {
    if(u_RenderType == 1)
        return u_Color;
    if(u_RenderType == 3)
        return tex2D(u_Texture, getTextureCoord(Pos, u_TextureBounds)) * u_Color;

	return u_Color;
}