#version 330

in vec4 out_vertexColor;
in vec2 out_textureCoordinate;
in vec3 out_ambient;
in vec3 out_normal;
in vec3 out_fragmentPosition;
in vec3 out_lightPosition;

out vec4 fragmentColor;

uniform sampler3D textureImage;
uniform vec3 lightColor;
uniform vec3 viewPos;

void main() {
    vec4 objectColor = out_vertexColor
    + texture(textureImage, vec3(out_textureCoordinate, out_normal.z))
    + vec4(0, 0, 0, 1);

    if (out_ambient.x > .99) { // Object that do not yet support diffuse/specular light
        fragmentColor = objectColor;
        return;
    }

    vec3 lightDirection = normalize(out_lightPosition - out_fragmentPosition);
    vec3 diffuse = max(dot(out_normal, lightDirection), 0) * lightColor;

    float specularStrength = .6;
    vec3 viewDirection = normalize(viewPos - out_fragmentPosition);
    vec3 reflectDirection = reflect(-lightDirection, out_normal);
    float specularAmount = pow(max(dot(viewDirection, reflectDirection), 0), 32);
    vec3 specularColor = specularStrength * specularAmount * lightColor;

    vec4 ambientDiffuseSpecular = vec4(out_ambient + diffuse + specularColor, 1);

    fragmentColor = ambientDiffuseSpecular * objectColor;
}
