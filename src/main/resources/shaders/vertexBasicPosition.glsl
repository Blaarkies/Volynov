#version 330

layout (location = 0) in vec3 in_position;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec4 in_color;
layout (location = 3) in vec2 in_textureCoordinate;

out vec4 out_vertexColor;
out vec2 out_textureCoordinate;
out vec3 out_ambient;
out vec3 out_normal;
out vec3 out_fragmentPosition;
out vec3 out_lightPosition;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform int ambientStrength;
uniform vec3 lightPosition;

void main() {
    mat4 mvp = projection * view * model;

    out_normal = in_normal;
    out_vertexColor = in_color;
    out_textureCoordinate = in_textureCoordinate;
    out_lightPosition = (projection * view * vec4(lightPosition, 1)).xyz;

    vec3 ambientColor = vec3(1, 1, 1);
    out_ambient = ambientStrength * .01 * ambientColor;

    gl_Position = mvp * vec4(in_position, 1);
    out_fragmentPosition = gl_Position.xyz;
}
