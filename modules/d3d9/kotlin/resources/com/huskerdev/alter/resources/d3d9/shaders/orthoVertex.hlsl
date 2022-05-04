
float u_ViewportWidth;
float u_ViewportHeight;

float4 main(float3 pos : POSITION) : POSITION {
    float4x4 ortho = float4x4(
        2/(u_ViewportWidth - 0),   0,                   0,  -1,
        0,                   2/(u_ViewportHeight - 0),  0,  -1,
        0,                   0,                   1,  0,
        0,                   0,                   0,  1
    );

	return mul(ortho, float4(pos.x - 0.5, u_ViewportHeight - (pos.y - 0.5), pos.z, 1.0));
}