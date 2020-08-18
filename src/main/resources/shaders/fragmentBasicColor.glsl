#version 330

in vec4 out_vertexColor;
in vec2 out_textureCoordinate;
in vec3 out_ambient;
in vec3 out_normal;
in vec3 out_fragmentPosition;
in vec3 out_lightPosition;

out vec4 fragmentColor;

uniform sampler2D textureImage;
uniform vec3 lightColor;

void main() {
    vec4 objectColor = out_vertexColor
    * texture(textureImage, out_textureCoordinate);

    if (out_ambient.x > .99) { // Object that do not yet support diffuse light
        fragmentColor = objectColor;
        return;
    }

    vec3 lightDir = normalize(out_fragmentPosition - out_lightPosition);
    vec3 diffuse = max(dot(out_normal, lightDir), 0) * lightColor;

    vec4 ambientDiffuse = vec4(out_ambient + diffuse, 1);

    fragmentColor = ambientDiffuse * objectColor;
}
