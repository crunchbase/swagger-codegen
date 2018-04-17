package io.swagger.codegen.languages;

import java.io.File;
import java.util.Map;

import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.Model;
import io.swagger.models.properties.*;

public class TypeScriptNodeClientCodegen extends AbstractTypeScriptClientCodegen {

    @Override
    public String getName() {
        return "typescript-node";
    }

    @Override
    public String getHelp() {
        return "Generates a TypeScript nodejs client library.";
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + "/apis/" + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + "/models/" + modelPackage().replace('.', File.separatorChar);
    }

    @Override
    public String toModelFilename(String name) {
        return toConventionalFilename(name);
    }

    @Override
    public String toApiFilename(String name) {
        return toConventionalFilename(name + "_api");
    }

    private String toConventionalFilename(String name) {
        return name.toLowerCase().replaceAll("[_]", "-");
    }
    
    @Override
    public CodegenModel fromModel(String name, Model model, Map<String, Model> allDefinitions) {
        CodegenModel m = super.fromModel(name, model, allDefinitions);
        m.classFileName = toConventionalFilename(m.classFileName);
        return m;
    }

    public TypeScriptNodeClientCodegen() {
        super();
        outputFolder = "generated-code/typescript-node";
        embeddedTemplateDir = templateDir = "TypeScript-node";
        apiTemplateFiles.put("api.mustache", ".ts");
        modelTemplateFiles.put("model.mustache", ".ts");
        supportingFiles.add(new SupportingFile("apis.mustache", null, "apis.ts"));
        supportingFiles.add(new SupportingFile("models.mustache", null, "models.ts"));
    }
}
