float4x4 u_Matrix;

float4 main(float3 pos : POSITION) : POSITION{
	return mul(u_Matrix, float4(pos.xyz, 1.0));
}