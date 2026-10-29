#version 310 es
precision highp float;

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aColor;

uniform float uAngle;
uniform float uAspect;

out vec3 vColor;

void main() {
    float cosine = cos(uAngle);
    float sine = sin(uAngle);
    vec2 position = vec2(
        aPosition.x * cosine - aPosition.y * sine,
        aPosition.x * sine + aPosition.y * cosine
    );

    if (uAspect > 1.0) {
        position.x /= uAspect;
    } else {
        position.y *= uAspect;
    }

    gl_Position = vec4(position, aPosition.z, 1.0);
    vColor = aColor;
}
