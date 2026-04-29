package com.itsfrz.tictactoe.common.background

import android.graphics.Paint
import androidx.compose.ui.draw.drawWithCache
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.graphics.RuntimeShader
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.sin

internal fun Modifier.appBackgroundCompat(themeTint : Color,time : Float = 0F) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.gpuBackground(themeTint)
    } else {
        this.appBackground(time)
    }

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Modifier.gpuBackground(
    themeTint: Color = Color.White
) = this.drawWithCache {
    // 1️⃣ AGSL Shader
    val shader = RuntimeShader("""
        uniform float u_time;
        uniform float u_breath;
        uniform float2 u_resolution;
        uniform float3 u_tint;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453);
        }
        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            f = f * f * (3.0 - 2.0 * f);
            return mix(
                mix(hash(i), hash(i + float2(1.0, 0.0)), f.x),
                mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), f.x),
                f.y
            );
        }

        void main(float2 fragCoord, out float4 fragColor) {
            float2 uv = fragCoord / u_resolution;
            uv.y = 1.0 - uv.y; // Android Y-flip

            float t = u_time * 0.15;
            float breath = u_breath;

            // 1. Cosmic-to-Warm Sky Gradient
            float y = uv.y;
            float3 sky = mix(float3(0.025, 0.035, 0.14), float3(0.16, 0.11, 0.28), smoothstep(0.0, 0.35, y));
            sky = mix(sky, float3(0.65, 0.44, 0.26), smoothstep(0.35, 0.75, y));
            sky = mix(sky, float3(0.86, 0.76, 0.58), smoothstep(0.75, 1.0, y));
            sky *= u_tint;

            // 2. Rising Sun Glow
            float2 sunPos = float2(0.5, 1.10);
            float dist = distance(uv, sunPos);
            float glow = exp(-dist * 3.8) * (0.14 + 0.08 * breath);
            sky += float3(1.0, 0.70, 0.35) * glow * u_tint;

            // 3. Liquid Glass Shimmer
            float2 shift = float2(sin(t * 0.6) * 0.015, cos(t * 0.4) * 0.01);
            float n1 = noise(uv * 5.0 + shift + t * 0.08);
            float n2 = noise(uv * 10.0 - shift * 1.5 + t * 0.12);
            float glass = smoothstep(0.48, 0.58, n1 * 0.6 + n2 * 0.4);
            float wave = sin(uv.y * 14.0 + t * 2.2) * 0.5 + 0.5;
            float shimmer = glass * wave * (0.02 + 0.015 * breath);
            sky += float3(1.0, 0.96, 0.88) * shimmer;

            // 4. Micro Stars
            float starN = noise(uv * 35.0 + float2(t * 0.04, 0.0));
            float stars = smoothstep(0.965, 0.99, starN);
            float twinkle = sin(u_time * 3.5 + starN * 6.28) * 0.5 + 0.5;
            stars *= twinkle * smoothstep(0.55, 0.1, uv.y);
            sky += float3(1.0) * stars * 0.3;

            // 5. Soft Vignette
            float vig = 1.0 - smoothstep(0.4, 1.5, length((uv - 0.5) * float2(1.15, 0.95)));
            sky *= 0.85 + 0.15 * vig;

            fragColor = float4(sky, 1.0);
        }
    """.trimIndent())
    val paint = Paint()
    onDrawBehind {
        val timeSec = (System.nanoTime() * 1e-9f).toFloat()
        val breath = (sin(timeSec * 0.8f) * 0.5f + 0.5f)
        shader.setFloatUniform("u_time", timeSec)
        shader.setFloatUniform("u_breath", breath)
        shader.setFloatUniform("u_resolution", size.width, size.height)
        shader.setFloatUniform("u_tint", themeTint.red, themeTint.green, themeTint.blue)
        drawIntoCanvas { canvas ->
            paint.shader = shader
            canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
        }
    }
}