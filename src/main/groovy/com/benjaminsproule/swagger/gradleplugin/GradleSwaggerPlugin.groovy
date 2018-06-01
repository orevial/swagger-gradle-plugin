package com.benjaminsproule.swagger.gradleplugin

import com.benjaminsproule.swagger.gradleplugin.classpath.ClassFinder
import com.benjaminsproule.swagger.gradleplugin.generator.GeneratorFactory
import com.benjaminsproule.swagger.gradleplugin.model.SwaggerExtension
import com.benjaminsproule.swagger.gradleplugin.reader.ReaderFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.classloader.VisitableURLClassLoader

class GradleSwaggerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def createdClassFinder = new ClassFinder(project)
        SwaggerExtension swaggerExtension = project.extensions.create('swagger', SwaggerExtension, project, createdClassFinder)

        def generateSwaggerDocsTask = project.task(type: GenerateSwaggerDocsTask,
            dependsOn: 'classes',
            group: 'swagger',
            description: 'Generates swagger documentation',
            GenerateSwaggerDocsTask.TASK_NAME,
            {
                classFinder = createdClassFinder
                readerFactory = new ReaderFactory(createdClassFinder)
                generatorFactory = new GeneratorFactory(createdClassFinder)
            }) as GenerateSwaggerDocsTask

        if (project.hasProperty('swagger.skip')) {
            generateSwaggerDocsTask.enabled = false
        }

        project.afterEvaluate {
            generateSwaggerDocsTask.outputDirectories = swaggerExtension.apiSourceExtensions.collect {
                if (it.swaggerDirectory) {
                    return new File(it.swaggerDirectory)
                }
                return null as File
            }.findAll {
                it != null
            }
            generateSwaggerDocsTask.outputFile = swaggerExtension.apiSourceExtensions.collect {
                if (it.outputPath) {
                    return new File(it.outputPath)
                }
                return null as File
            }.findAll {
                it != null
            }
            generateSwaggerDocsTask.inputFiles = ((createdClassFinder.getClassLoader() as URLClassLoader).parent as VisitableURLClassLoader).URLs.collect {
                new File(it.toURI())
            }
        }
    }
}
