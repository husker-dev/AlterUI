float4x4 mat;

float4 main(float3 pos : POSITION) : POSITION{
	return mul(mat, float4(pos.xyz, 1.0));
}