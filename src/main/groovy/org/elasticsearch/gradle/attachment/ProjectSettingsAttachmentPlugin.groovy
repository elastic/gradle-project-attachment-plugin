/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.gradle.attachment

import org.codehaus.groovy.control.CompilerConfiguration
import org.gradle.StartParameter
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.UnknownProjectException
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ObjectConfigurationAction
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.plugins.PluginManager

/**
 * A plugin to include attached projects and their subprojects in settings.gradle.
 */
class ProjectSettingsAttachmentPlugin implements Plugin<Settings> {

    @Override
    void apply(Settings settings) {
        settings.settingsDir.eachFile { file ->
            if (file.getName().startsWith(ProjectAttachmentPlugin.ATTACHMENT_PREFIX) == false) return

            String locationStr = file.getText('UTF-8').trim()
            if (locationStr.isEmpty()) {
                throw new IllegalStateException("Attachment configuration file '${file}' is empty")
            }
            String name = file.name.substring(ProjectAttachmentPlugin.ATTACHMENT_PREFIX.length())
            File location = new File(locationStr)
            if (location.exists() == false || location.isDirectory() == false) {
                throw new IllegalStateException("Attachment location '${locationStr}' for '${name}' is not valid")
            }

            settings.include(name)
            settings.project(":${name}").projectDir = location

            File settingsFile = new File(location, 'settings.gradle')
            if (settingsFile.exists()) {
                PrefixDelegatingSettings delegatingSettings = new PrefixDelegatingSettings(delegate: settings, prefix: "${name}:")
                CompilerConfiguration configuration = new CompilerConfiguration()
                configuration.setScriptBaseClass(DelegatingScript.getName())
                configuration.setSourceEncoding('UTF-8')
                GroovyShell shell = new GroovyShell(configuration)
                DelegatingScript script = shell.parse(settingsFile)
                script.setDelegate(delegatingSettings)
                script.run()
            }
        }
    }

    /**
     * A delegate settings, which prefixes all project includes with the given prefix.
     */
    static class PrefixDelegatingSettings implements Settings {
        Settings delegate
        String prefix

        @Override
        void include(String[] includes) {
            for (int i = 0; i < includes.length; ++i) {
                includes[i] = "${prefix}${includes[i]}"
            }
            delegate.include(includes)
        }

        @Override
        void includeFlat(String[] includes) {
            for (int i = 0; i < includes.length; ++i) {
                includes[i] = "${prefix}${includes[i]}"
            }
            delegate.includeFlat(includes)
        }

        @Override
        Settings getSettings() {
            return this
        }

        @Override
        File getSettingsDir() {
            return delegate.settingsDir
        }

        @Override
        File getRootDir() {
            return delegate.rootDir
        }

        @Override
        ProjectDescriptor getRootProject() {
            return delegate.rootProject
        }

        @Override
        ProjectDescriptor project(String s) throws UnknownProjectException {
            return delegate.project(s)
        }

        @Override
        ProjectDescriptor findProject(String s) {
            return delegate.findProject(s)
        }

        @Override
        ProjectDescriptor project(File file) throws UnknownProjectException {
            return delegate.project(file)
        }

        @Override
        ProjectDescriptor findProject(File file) {
            return delegate.findProject(file)
        }

        @Override
        StartParameter getStartParameter() {
            return delegate.getStartParameter()
        }

        @Override
        Gradle getGradle() {
            return delegate.gradle
        }

        @Override
        PluginContainer getPlugins() {
            return delegate.plugins
        }

        @Override
        void apply(Closure closure) {
            delegate.apply(closure)
        }

        @Override
        void apply(Action<? super ObjectConfigurationAction> action) {
            delegate.apply(action)
        }

        @Override
        void apply(Map<String, ?> map) {
            delegate.apply(map)
        }

        @Override
        PluginManager getPluginManager() {
            return delegate.pluginManager
        }
    }
}
